package com.joaorihan.courierprime.courier;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.config.MainConfig;
import com.joaorihan.courierprime.config.Message;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CourierManager {

    // A replacement reservation prevents a second trigger from spawning a
    // courier while the scheduled retry is still waiting to run.
    private static final HashMap<UUID, Courier> replacementReservations = new HashMap<>();

    // Tracks active courier instances (keyed by the spawned entity).
    @Getter
    private static final HashMap<Entity, Courier> activeCouriers = new HashMap<>() {
        @Override
        public void clear() {
            super.clear();
            // Reload and shutdown already clear this existing public map;
            // make that lifecycle boundary clear delayed retry reservations too.
            replacementReservations.clear();
        }
    };

    /**
     * Checks whether a courier can be spawned for the given player. All calls
     * are expected on the Bukkit main thread because entity state is queried
     * and the active map is cleaned here.
     */
    public static boolean canSpawn(Player recipient) {
        return canSpawn(recipient, null);
    }

    /**
     * Checks whether a courier can spawn, optionally as the owner of a pending
     * replacement reservation. The owner exception is package-private so only
     * Courier's scheduled handoff can use it.
     */
    static boolean canSpawn(Player recipient, Courier replacementOwner) {
        CourierPrime plugin = CourierPrime.getPlugin();
        if (recipient == null) {
            return false;
        }
        if (plugin == null || !recipient.isOnline()) {
            return false;
        }

        cleanupStaleCouriers();

        if (plugin.getOutgoingManager() == null
                || !hasPendingMail(plugin, recipient.getUniqueId())) {
            clearReplacementReservation(recipient.getUniqueId(), null);
            return false;
        }

        Courier reservedBy = replacementReservations.get(recipient.getUniqueId());
        if (reservedBy != null && reservedBy != replacementOwner) {
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

    static boolean reserveReplacement(UUID recipient, Courier courier) {
        if (recipient == null || courier == null) {
            return false;
        }
        Courier existing = replacementReservations.putIfAbsent(recipient, courier);
        return existing == null || existing == courier;
    }

    static void clearReplacementReservation(UUID recipient, Courier courier) {
        if (recipient == null) {
            return;
        }
        if (courier == null) {
            replacementReservations.remove(recipient);
        } else {
            replacementReservations.remove(recipient, courier);
        }
    }

    /**
     * Returns whether an entity is still available for interaction. This
     * deliberately works for both living and non-living Bukkit entities.
     */
    static boolean isLiveEntity(Entity entity) {
        return entity != null && !entity.isDead() && entity.isValid();
    }

    private static void cleanupStaleCouriers() {
        List<Map.Entry<Entity, Courier>> staleEntries = new ArrayList<>();
        for (Map.Entry<Entity, Courier> entry : activeCouriers.entrySet()) {
            Entity entity = entry.getKey();
            Courier courier = entry.getValue();
            if (courier == null || !isLiveEntity(entity)) {
                staleEntries.add(entry);
            }
        }

        for (Map.Entry<Entity, Courier> entry : staleEntries) {
            Courier courier = entry.getValue();
            if (courier != null) {
                courier.retireAfterExternalRemoval();
            }
            activeCouriers.remove(entry.getKey());
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
