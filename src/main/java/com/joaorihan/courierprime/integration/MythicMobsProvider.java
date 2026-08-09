package com.joaorihan.courierprime.integration;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.courier.CourierType;
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
        return Bukkit.getPluginManager() != null
                && Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME);
    }

    @Override
    public boolean hasEntity(String entityId) {
        if (!isAvailable() || !CourierType.isValidCustomIdentifier(entityId)) {
            return false;
        }

        try {
            if (MythicProvider.get() == null || MythicProvider.get().getMobManager() == null) {
                return false;
            }
            Optional<MythicMob> mythicMob = MythicProvider.get().getMobManager().getMythicMob(entityId);
            return mythicMob != null && mythicMob.isPresent();
        } catch (LinkageError e) {
            logger().log(Level.WARNING, "MythicMobs API could not validate courier mob '" + entityId + "'.", e);
            return false;
        } catch (RuntimeException e) {
            logger().log(Level.WARNING, "MythicMobs could not validate courier mob '" + entityId + "'.", e);
            return false;
        }
    }

    @Override
    public Entity spawnEntity(String entityId, Location location) {
        if (!isAvailable() || location == null || location.getWorld() == null
                || !CourierType.isValidCustomIdentifier(entityId)) {
            return null;
        }

        try {
            if (MythicProvider.get() == null || MythicProvider.get().getMobManager() == null) {
                return null;
            }

            Optional<MythicMob> mythicMob = MythicProvider.get().getMobManager().getMythicMob(entityId);
            
            if (mythicMob == null || mythicMob.isEmpty()) {
                CourierPrime.getPlugin().getLogger().warning(
                    "MythicMob '" + entityId + "' not found! Falling back to vanilla entity."
                );
                return null;
            }

            var activeMob = mythicMob.get().spawn(BukkitAdapter.adapt(location), 1);
            if (activeMob == null || activeMob.getEntity() == null) {
                return null;
            }
            return activeMob.getEntity().getBukkitEntity();
            
        } catch (LinkageError e) {
            logger().log(Level.WARNING, "MythicMobs API could not spawn courier mob '" + entityId + "'.", e);
            return null;
        } catch (Exception e) {
            CourierPrime.getPlugin().getLogger().log(Level.WARNING, 
                "Failed to spawn MythicMob '" + entityId + "': " + e.getMessage(), e);
            return null;
        }
    }

    private java.util.logging.Logger logger() {
        CourierPrime plugin = CourierPrime.getPlugin();
        return plugin == null ? java.util.logging.Logger.getLogger("CourierPrime") : plugin.getLogger();
    }
}
