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
     * Spawns a custom entity at the given location
     * @param entityId The custom entity/mob ID
     * @param location The spawn location
     * @return The spawned entity, or null if spawning failed
     */
    Entity spawnEntity(String entityId, Location location);

}

