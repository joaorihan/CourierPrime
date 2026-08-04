package com.joaorihan.courierprime.courier;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.config.MainConfig;
import com.joaorihan.courierprime.config.Message;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CourierManager {

    // Tracks active courier instances (keyed by the spawned entity).
    @Getter
    private static final HashMap<Entity, Courier> activeCouriers = new HashMap<>();

    /**
     * Checks whether a courier can be spawned for the given player. All calls
     * are expected on the Bukkit main thread because entity state is queried
     * and the active map is cleaned here.
     */
    public static boolean canSpawn(Player recipient) {
        CourierPrime plugin = CourierPrime.getPlugin();
        if (recipient == null || plugin == null || !recipient.isOnline()) {
            return false;
        }

        cleanupStaleCouriers();

        if (plugin.getOutgoingManager() == null
                || !hasPendingMail(plugin, recipient.getUniqueId())) {
            return false;
        }

        // The map is keyed by entities, so explicitly compare recipients to
        // prevent a second live courier from being created for the same UUID.
        for (Map.Entry<Entity, Courier> entry : activeCouriers.entrySet()) {
            Entity entity = entry.getKey();
            Courier courier = entry.getValue();
            if (courier != null && courier.getRecipient() != null
                    && courier.getRecipient().getUniqueId().equals(recipient.getUniqueId())
                    && isLiveEntity(entity)) {
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

        if (plugin.getLetterManager() != null && plugin.getLetterManager().isInBlockedMode(recipient)) {
            recipient.sendMessage(plugin.getMessageManager().getMessage(Message.ERROR_IN_BLOCKED_MODE, true));
            return false;
        }

        return true;
    }

    /**
     * Returns whether an entity is still available for interaction. This
     * deliberately works for both living and non-living Bukkit entities.
     */
    static boolean isLiveEntity(Entity entity) {
        return entity != null && !entity.isDead() && entity.isValid();
    }

    private static void cleanupStaleCouriers() {
        Iterator<Map.Entry<Entity, Courier>> iterator = activeCouriers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Entity, Courier> entry = iterator.next();
            Entity entity = entry.getKey();
            Courier courier = entry.getValue();
            if (courier == null || !isLiveEntity(entity)) {
                iterator.remove();
            }
        }
    }

    private static boolean hasPendingMail(CourierPrime plugin, java.util.UUID recipient) {
        if (plugin.getOutgoingManager() == null
                || plugin.getOutgoingManager().getOutgoing() == null) {
            return false;
        }
        var letters = plugin.getOutgoingManager().getOutgoing().get(recipient);
        return letters != null && !letters.isEmpty();
    }
}
