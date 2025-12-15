package com.joaorihan.courierprime.integration;

import com.joaorihan.courierprime.CourierPrime;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Optional;
import java.util.logging.Level;

/**
 * Custom entity provider for MythicMobs integration
 */
public class MythicMobsProvider implements CustomEntityProvider {

    private static final String PLUGIN_NAME = "MythicMobs";

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME);
    }

    @Override
    public Entity spawnEntity(String entityId, Location location) {
        if (!isAvailable()) {
            return null;
        }

        try {
            Optional<MythicMob> mythicMob = MythicProvider.get().getMobManager().getMythicMob(entityId);
            
            if (mythicMob.isEmpty()) {
                CourierPrime.getPlugin().getLogger().warning(
                    "MythicMob '" + entityId + "' not found! Falling back to vanilla entity."
                );
                return null;
            }

            var activeMob = mythicMob.get().spawn(BukkitAdapter.adapt(location), 1);
            return activeMob.getEntity().getBukkitEntity();
            
        } catch (Exception e) {
            CourierPrime.getPlugin().getLogger().log(Level.WARNING, 
                "Failed to spawn MythicMob '" + entityId + "': " + e.getMessage(), e);
            return null;
        }
    }
}

