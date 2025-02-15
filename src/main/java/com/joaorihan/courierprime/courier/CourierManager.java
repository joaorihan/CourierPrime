package com.joaorihan.courierprime.courier;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.config.MainConfig;
import com.joaorihan.courierprime.config.Message;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.HashMap;

public class CourierManager {

    private static final CourierPrime plugin = CourierPrime.getPlugin();

    // Tracks active courier instances (keyed by the spawned entity)
    @Getter
    private static final HashMap<Entity, Courier> activeCouriers = new HashMap<>();

    /**
     * Checks whether a courier can be spawned for the given player.
     *
     * @param recipient The player to check.
     * @return true if allowed; false otherwise.
     */
    public static boolean canSpawn(Player recipient) {
        if (!recipient.isOnline()
                || !plugin.getOutgoingManager().getOutgoing().containsKey(recipient.getUniqueId())
                || plugin.getOutgoingManager().getOutgoing().get(recipient.getUniqueId()).isEmpty()) {
            return false;
        }

        double dist = MainConfig.getSpawnDistance() * 2;
        for (Entity entity : recipient.getNearbyEntities(dist, dist, dist)) {
            if (activeCouriers.containsKey(entity)
                    && activeCouriers.get(entity).getRecipient().equals(recipient)) {
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

        if (plugin.getLetterManager().isInBlockedMode(recipient)) {
            recipient.sendMessage(plugin.getMessageManager().getMessage(Message.ERROR_IN_BLOCKED_MODE, true));
            return false;
        }

        return true;
    }
}
