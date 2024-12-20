package com.joaorihan.courierprime.courier;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.config.MainConfig;
import com.joaorihan.courierprime.config.Message;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

/**
 * courier used to send mail
 *
 * @author Jeremy Noesen
 */
public class Courier {

    @Getter
    private static HashMap<Entity, Courier> couriers = new HashMap<>();

    @Getter
    private Entity courier;

    @Getter
    private Player recipient;

    @Getter
    private boolean delivered;

    // Change
    private static CourierPrime plugin = CourierPrime.getPlugin();

    /**
     * create a new courier entity to deliver mail for a player
     *
     * @param recipient player receiving mail
     */
    public Courier(Player recipient) {
        this.recipient = recipient;
        this.delivered = false;
    }

    /**
     * spawn the courier entity
     */
    private void spawn() {
        Location loc = recipient.getLocation().add(recipient.getLocation().getDirection().setY(0).multiply(MainConfig.getSpawnDistance()));
        courier = recipient.getWorld().spawnEntity(loc, MainConfig.getCourierEntityType());
        couriers.put(courier, this);

        courier.setCustomName(plugin.getMessageManager().getMessage(Message.COURIER_NAME).replace("$PLAYER$", recipient.getName()));
        courier.setCustomNameVisible(false);
        courier.setInvulnerable(true);
        recipient.sendMessage(plugin.getMessageManager().getMessage(Message.SUCCESS_COURIER_ARRIVED, true));
        courier.getWorld().playSound(courier.getLocation(), Sound.UI_TOAST_IN, 1, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                courier.setFallDistance(0);
                if (courier.isOnGround() && courier.getWorld() == recipient.getWorld()) {
                    courier.teleport(courier.getLocation().setDirection(recipient.getLocation()
                            .subtract(courier.getLocation()).toVector()));
                    ((LivingEntity) courier).setAI(false);
                }
                if (courier.isDead()) this.cancel();
            }
        }.runTaskTimer(CourierPrime.getPlugin(), 0, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!courier.isDead()) {
                    remove();

                    if (recipient.isOnline() && !delivered) recipient.sendMessage(plugin.getMessageManager().getMessage(Message.SUCCESS_IGNORED, true));
                    courier.getWorld().playSound(courier.getLocation(), Sound.UI_TOAST_OUT, 1, 1);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            spawn(recipient);
                        }
                    }.runTaskLater(CourierPrime.getPlugin(), MainConfig.getResendDelay());
                }
            }
        }.runTaskLater(CourierPrime.getPlugin(), MainConfig.getRemoveDelay());
    }

    /**
     * Create a new courier entity for a player if possible
     *
     * @param recipient player receiving letters
     */
    public static void spawn(Player recipient) {
        if (Courier.canSpawn(recipient)) {
            Courier courier = new Courier(recipient);
            courier.spawn();
        }
    }

    /**
     * remove the courier entity
     */
    public void remove() {
        couriers.remove(courier);
        courier.remove();
    }

    /**
     * set the status of the courier to delivered
     */
    public void setDelivered() {
        delivered = true;
        courier.setCustomName(plugin.getMessageManager().getMessage(Message.COURIER_NAME_RECEIVED));
    }

    /**
     * check if a courier is allowed to spawn for the specified player
     *
     * @param recipient player to spawn courier for
     * @return true if the courier is allowed to spawn
     */
    private static boolean canSpawn(Player recipient) {
        if (!recipient.isOnline() || !plugin.getOutgoingManager().getOutgoing().containsKey(recipient.getUniqueId())
                || plugin.getOutgoingManager().getOutgoing().get(recipient.getUniqueId()).size() == 0) {
            return false;
        }

        double dist = MainConfig.getSpawnDistance() * 2;
        for (Entity entity : recipient.getNearbyEntities(dist, dist, dist)) {
            if (couriers.containsKey(entity) &&
                    couriers.get(entity).getRecipient().equals(recipient)) {
                return false;
            }
        }

        for (MetadataValue meta : recipient.getMetadata("vanished")) {
            if (meta.asBoolean()) {
                recipient.sendMessage(plugin.getMessageManager().getMessage(Message.ERROR_VANISHED, true));
                return false;
            }
        }

        if (MainConfig.getBlockedGamemodes().contains(recipient.getGameMode())) {
            recipient.sendMessage(plugin.getMessageManager().getMessage(Message.ERROR_GAMEMODE, true));
            return false;
        }

        if (MainConfig.getBlockedWorlds().contains(recipient.getWorld())) {
            recipient.sendMessage(plugin.getMessageManager().getMessage(Message.ERROR_WORLD, true));
            return false;
        }


        if (plugin.getLetterManager().isInBlockedMode(recipient)){
            recipient.sendMessage(plugin.getMessageManager().getMessage(Message.ERROR_IN_BLOCKED_MODE, true));
            return false;
        }

        return true;
    }
}
