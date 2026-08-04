package com.joaorihan.courierprime.courier;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.config.MainConfig;
import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.integration.CustomEntityManager;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Level;

/**
 * Represents one courier delivery attempt for one player.
 */
@Getter
public class Courier {

    private Entity courier;
    private final Player recipient;
    private boolean delivered;
    private boolean deliveryInProgress;
    private boolean retired;
    private BukkitTask movementTask;
    private BukkitTask timeoutTask;
    private BukkitTask replacementTask;

    public Courier(Player recipient) {
        this.recipient = recipient;
        this.delivered = false;
        this.deliveryInProgress = false;
        spawn();
    }

    /**
     * Spawns the selected courier and sets up its behavior. Entity work is
     * performed synchronously on the Bukkit thread.
     */
    private void spawn() {
        CourierPrime plugin = CourierPrime.getPlugin();
        if (plugin == null || !CourierManager.canSpawn(recipient)) {
            return;
        }

        Location recipientLocation = recipient.getLocation();
        Location location = recipientLocation.clone().add(
                recipientLocation.getDirection().setY(0).multiply(MainConfig.getSpawnDistance()));

        CourierType selectedType = plugin.getCourierSelectManager() == null
                ? null
                : plugin.getCourierSelectManager().getActiveCourier(recipient.getUniqueId());

        if (selectedType != null && !selectedType.isVanilla()) {
            CustomEntityManager customEntityManager = plugin.getCustomEntityManager();
            // This availability check is intentionally before spawnEntity:
            // unavailable or unknown saved custom selections must fall back to
            // vanilla without reaching an optional provider spawn call.
            if (customEntityManager != null && customEntityManager.isEntityAvailable(selectedType)) {
                courier = customEntityManager.spawnEntity(selectedType, location);
            }
        }

        if (!CourierManager.isLiveEntity(courier)) {
            removeSpawnedEntity();
            EntityType vanillaType = selectedType != null && selectedType.isVanilla()
                    ? selectedType.getVanillaType()
                    : MainConfig.getDefaultCourierEntityType();
            courier = spawnVanilla(location, vanillaType);

            if (!CourierManager.isLiveEntity(courier)) {
                removeSpawnedEntity();
                EntityType defaultType = MainConfig.getDefaultCourierEntityType();
                if (vanillaType != defaultType) {
                    courier = spawnVanilla(location, defaultType);
                }
            }
        }

        if (!CourierManager.isLiveEntity(courier)) {
            removeSpawnedEntity();
            plugin.getLogger().warning("Unable to spawn a courier for " + recipient.getName());
            return;
        }

        CourierManager.getActiveCouriers().put(courier, this);

        courier.setCustomName(plugin.getMessageManager().getMessage(Message.COURIER_NAME)
                .replace("$PLAYER$", recipient.getName()));
        courier.setCustomNameVisible(false);
        courier.setInvulnerable(true);
        recipient.sendMessage(plugin.getMessageManager().getMessage(Message.SUCCESS_COURIER_ARRIVED, true));
        courier.getWorld().playSound(courier.getLocation(), Sound.UI_TOAST_IN, 1, 1);

        movementTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (retired) {
                    cancel();
                    return;
                }
                if (!CourierManager.isLiveEntity(courier)) {
                    retire(false, true);
                    cancel();
                    return;
                }

                courier.setFallDistance(0);
                if (courier.isOnGround()
                        && courier.getWorld() != null
                        && courier.getWorld().equals(recipient.getWorld())) {
                    courier.teleport(courier.getLocation().setDirection(
                            recipient.getLocation().subtract(courier.getLocation()).toVector()));
                    if (courier instanceof LivingEntity livingEntity) {
                        livingEntity.setAI(false);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (retired) {
                    cancel();
                    return;
                }
                if (!CourierManager.isLiveEntity(courier)) {
                    retire(false, true);
                    cancel();
                    return;
                }

                if (!delivered && hasPendingMail() && recipient.isOnline()) {
                    recipient.sendMessage(plugin.getMessageManager().getMessage(Message.SUCCESS_IGNORED, true));
                }
                courier.getWorld().playSound(courier.getLocation(), Sound.UI_TOAST_OUT, 1, 1);
                retire(true, !delivered);
                cancel();
            }
        }.runTaskLater(plugin, Math.max(0L, MainConfig.getRemoveDelay()));
    }

    private Entity spawnVanilla(Location location, EntityType type) {
        if (type == null || !type.isSpawnable()) {
            return null;
        }
        try {
            return location.getWorld().spawnEntity(location, type);
        } catch (RuntimeException e) {
            CourierPrime plugin = CourierPrime.getPlugin();
            if (plugin != null) {
                plugin.getLogger().warning(
                        "Unable to spawn vanilla courier type " + type.name() + "; trying the default type."
                );
            }
            return null;
        }
    }

    private void removeSpawnedEntity() {
        if (courier != null && !courier.isDead()) {
            courier.remove();
        }
        courier = null;
    }

    private boolean hasPendingMail() {
        CourierPrime plugin = CourierPrime.getPlugin();
        return plugin != null && plugin.getOutgoingManager() != null
                && plugin.getOutgoingManager().getOutgoing() != null
                && plugin.getOutgoingManager().getOutgoing().get(recipient.getUniqueId()) != null
                && !plugin.getOutgoingManager().getOutgoing().get(recipient.getUniqueId()).isEmpty();
    }

    /**
     * Removes the courier entity without scheduling a replacement. Reload and
     * shutdown own their pending-mail rescheduling contract.
     */
    public void remove() {
        retire(true, false);
    }

    /**
     * Marks the delivery as completed. A completed courier is only cleaned up
     * by its timeout and can never emit the ignored message or schedule a
     * replacement.
     */
    public void setDelivered() {
        if (retired || delivered || hasPendingMail()) {
            return;
        }
        delivered = true;
        deliveryInProgress = false;
        if (replacementTask != null) {
            replacementTask.cancel();
            replacementTask = null;
        }
        if (courier != null) {
            courier.setCustomName(CourierPrime.getPlugin().getMessageManager()
                    .getMessage(Message.COURIER_NAME_RECEIVED));
        }
    }

    /**
     * Claims the courier for one click-delivery attempt. All entity events run
     * on the Bukkit thread, but the guard also handles synchronous re-entry
     * from inventory or plugin callbacks.
     *
     * @return true when this click may call LetterSender.receive
     */
    public boolean beginDelivery() {
        if (retired || delivered || deliveryInProgress) {
            return false;
        }
        deliveryInProgress = true;
        return true;
    }

    /**
     * Finishes a receive attempt after LetterSender has transferred as much
     * mail as it can. The courier is completed only when no pending mail
     * remains for its recipient.
     *
     * @return true when the courier is now fully delivered
     */
    public boolean completeDeliveryAttempt() {
        deliveryInProgress = false;
        if (retired || hasPendingMail()) {
            return false;
        }
        setDelivered();
        return delivered;
    }

    private void retire(boolean removeEntity, boolean retry) {
        if (retired) {
            return;
        }
        retired = true;
        deliveryInProgress = false;

        if (movementTask != null) {
            movementTask.cancel();
            movementTask = null;
        }
        if (timeoutTask != null) {
            timeoutTask.cancel();
            timeoutTask = null;
        }

        Entity oldCourier = courier;
        if (oldCourier != null) {
            CourierManager.getActiveCouriers().remove(oldCourier);
            if (removeEntity && !oldCourier.isDead()) {
                oldCourier.remove();
            }
        }
        courier = null;

        if (retry && !delivered) {
            scheduleReplacement();
        }
    }

    private void scheduleReplacement() {
        if (delivered || !hasPendingMail() || !recipient.isOnline()) {
            return;
        }

        CourierPrime plugin = CourierPrime.getPlugin();
        if (plugin == null) {
            return;
        }

        replacementTask = new BukkitRunnable() {
            @Override
            public void run() {
                replacementTask = null;
                if (!recipient.isOnline() || !hasPendingMail()) {
                    return;
                }
                new Courier(recipient);
            }
        }.runTaskLater(plugin, Math.max(0L, MainConfig.getResendDelay()));
    }
}
