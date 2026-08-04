package com.joaorihan.courierprime.integration;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Interface for custom entity providers (MythicMobs, ModelEngine, etc.)
 */
public interface CustomEntityProvider {

    /**
     * Gets the name of this provider
     * @return The provider name
     */
    String getName();

    /**
     * Checks if the required plugin is available
     * @return true if the plugin is installed and enabled
     */
    boolean isAvailable();

    /**
     * Checks whether a provider-side identifier exists before a spawn is
     * attempted. Providers should override this with their native registry
     * lookup; the default keeps third-party implementations source-compatible
     * while still refusing an empty identifier.
     */
    default boolean hasEntity(String entityId) {
        return entityId != null && !entityId.trim().isEmpty();
    }

    /**
     * Spawns a custom entity at the given location
     * @param entityId The custom entity/mob ID
     * @param location The spawn location
     * @return The spawned entity, or null if spawning failed
     */
    Entity spawnEntity(String entityId, Location location);

}
