package com.joaorihan.courierprime.integration;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.courier.CourierType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Manages custom entity providers and handles spawning logic
 */
public class CustomEntityManager {

    private final MythicMobsProvider mythicMobsProvider;
    private final ModelEngineProvider modelEngineProvider;

    public CustomEntityManager() {
        mythicMobsProvider = new MythicMobsProvider();
        modelEngineProvider = new ModelEngineProvider();
    }

    /**
     * Initializes the manager and logs available providers
     */
    public void initialize() {
        if (mythicMobsProvider.isAvailable()) {
            CourierPrime.getPlugin().getLogger().info("MythicMobs detected - custom mm: couriers enabled!");
        }
        if (modelEngineProvider.isAvailable()) {
            CourierPrime.getPlugin().getLogger().info("ModelEngine detected - custom meg: couriers enabled!");
        }
        if (!mythicMobsProvider.isAvailable() && !modelEngineProvider.isAvailable()) {
            CourierPrime.getPlugin().getLogger().info("No custom entity plugins detected. Only vanilla couriers available.");
        }
    }

    public boolean isMythicMobsAvailable() {
        return mythicMobsProvider.isAvailable();
    }

    public boolean isModelEngineAvailable() {
        return modelEngineProvider.isAvailable();
    }

    public boolean isEntityAvailable(CourierType courierType) {
        if (courierType == null || courierType.isVanilla()) {
            return courierType != null;
        }
        if (courierType.isMythicMobs()) {
            return mythicMobsProvider.hasEntity(courierType.getIdentifier());
        }
        if (courierType.isModelEngine()) {
            return modelEngineProvider.hasEntity(courierType.getIdentifier());
        }
        return false;
    }

    /**
     * Spawns a custom entity based on the CourierType
     * @param courierType The courier type to spawn
     * @param location The spawn location
     * @return The spawned entity, or null if spawning failed
     */
    public Entity spawnEntity(CourierType courierType, Location location) {
        if (courierType == null || courierType.isVanilla()) {
            return null;
        }

        if (courierType.isMythicMobs()) {
            if (!mythicMobsProvider.isAvailable()) {
                CourierPrime.getPlugin().getLogger().warning(
                    "Attempted to spawn MythicMobs courier but MythicMobs is not installed!"
                );
                return null;
            }
            return mythicMobsProvider.spawnEntity(courierType.getIdentifier(), location);
        }

        if (courierType.isModelEngine()) {
            if (!modelEngineProvider.isAvailable()) {
                CourierPrime.getPlugin().getLogger().warning(
                    "Attempted to spawn ModelEngine courier but ModelEngine is not installed!"
                );
                return null;
            }
            return modelEngineProvider.spawnEntity(courierType.getIdentifier(), location);
        }

        return null;
    }
}
