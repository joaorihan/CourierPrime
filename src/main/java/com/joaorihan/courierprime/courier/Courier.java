package com.joaorihan.courierprime.courier;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.config.MainConfig;
import com.joaorihan.courierprime.config.Message;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class Courier {

    // Getters for accessing private fields.
    private Entity courier;
    private final Player recipient;
    private boolean delivered;

    private static final CourierPrime plugin = CourierPrime.getPlugin();

    /**
     * Constructs a Courier for the given player and immediately spawns the courier entity.
     *
     * @param recipient The player who will receive the courier.
     */
    public Courier(Player recipient) {
        this.recipient = recipient;
        this.delivered = false;
        spawn();
    }

    /**
     * Spawns the courier entity and sets up its behavior.
     */
    private void spawn() {

        if (!CourierManager.canSpawn(recipient)) return;

        // Determine spawn location based on player's location and direction.
        Location loc = recipient.getLocation()
                .add(recipient.getLocation().getDirection().setY(0).multiply(MainConfig.getSpawnDistance()));

        EntityType playerActiveCourier = plugin.getCourierSelectManager().getActiveCourier(recipient.getUniqueId());

        // Tries to get the player's active courier type on the config. Spawns default courier type if null
        if (playerActiveCourier == null){
            courier = recipient.getWorld().spawnEntity(loc, MainConfig.getDefaultCourierEntityType());
        } else {
            courier = recipient.getWorld().spawnEntity(loc, playerActiveCourier);
        }

        // Register this courier in the manager.
        CourierManager.getActiveCouriers().put(courier, this);

        // Configure the courier entity.
        courier.setCustomName(plugin.getMessageManager().getMessage(Message.COURIER_NAME)
                .replace("$PLAYER$", recipient.getName()));
        courier.setCustomNameVisible(false);
        courier.setInvulnerable(true);
        recipient.sendMessage(plugin.getMessageManager().getMessage(Message.SUCCESS_COURIER_ARRIVED, true));
        courier.getWorld().playSound(courier.getLocation(), Sound.UI_TOAST_IN, 1, 1);

        // Runnable to adjust courier movement.
        new BukkitRunnable() {
            @Override
            public void run() {
                courier.setFallDistance(0);
                if (courier.isOnGround() && courier.getWorld() == recipient.getWorld()) {
                    courier.teleport(courier.getLocation().setDirection(
                            recipient.getLocation().subtract(courier.getLocation()).toVector()));
                    ((LivingEntity) courier).setAI(false);
                }
                if (courier.isDead()) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);

        // Runnable to remove the courier after a delay.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!courier.isDead()) {
                    remove();

                    if (recipient.isOnline() && !delivered) {
                        recipient.sendMessage(plugin.getMessageManager().getMessage(Message.SUCCESS_IGNORED, true));
                    }
                    courier.getWorld().playSound(courier.getLocation(), Sound.UI_TOAST_OUT, 1, 1);

                    // Schedule a new courier spawn after a resend delay.
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            new Courier(recipient);
                        }
                    }.runTaskLater(plugin, MainConfig.getResendDelay());
                }
            }
        }.runTaskLater(plugin, MainConfig.getRemoveDelay());
    }

    /**
     * Removes the courier entity from the world and unregisters it.
     */
    public void remove() {
        CourierManager.getActiveCouriers().remove(courier);
        courier.remove();
    }

    /**
     * Marks the courier as having delivered its mail.
     */
    public void setDelivered() {
        delivered = true;
        courier.setCustomName(plugin.getMessageManager().getMessage(Message.COURIER_NAME_RECEIVED));
    }

}
