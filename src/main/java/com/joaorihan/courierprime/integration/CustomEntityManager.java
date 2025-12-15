package com.joaorihan.courierprime.integration;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.config.MainConfig;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages custom entity providers and handles spawning logic
 */
public class CustomEntityManager {

    private final List<CustomEntityProvider> providers = new ArrayList<>();
    private CustomEntityProvider activeProvider;

    public CustomEntityManager() {
        // Register providers (order matters for AUTO mode - MythicMobs takes priority)
        providers.add(new MythicMobsProvider());
        providers.add(new ModelEngineProvider());
    }

    /**
     * Initializes the manager and detects available providers
     */
    public void initialize() {
        if (!MainConfig.isCustomEntitiesEnabled()) {
            CourierPrime.getPlugin().getLogger().info("Custom entities are disabled in config.");
            return;
        }

        String preferred = MainConfig.getPreferredPlugin().toUpperCase();

        if (preferred.equals("AUTO")) {
            // Auto-detect: Use first available provider
            for (CustomEntityProvider provider : providers) {
                if (provider.isAvailable()) {
                    activeProvider = provider;
                    CourierPrime.getPlugin().getLogger().info(
                        "Custom entities enabled using " + provider.getName() + " (auto-detected)"
                    );
                    return;
                }
            }
            CourierPrime.getPlugin().getLogger().warning(
                "Custom entities enabled but no supported plugin found! Falling back to vanilla entities."
            );
        } else {
            // Use specific provider
            for (CustomEntityProvider provider : providers) {
                if (provider.getName().equalsIgnoreCase(preferred)) {
                    if (provider.isAvailable()) {
                        activeProvider = provider;
                        CourierPrime.getPlugin().getLogger().info(
                            "Custom entities enabled using " + provider.getName()
                        );
                    } else {
                        CourierPrime.getPlugin().getLogger().warning(
                            preferred + " is configured but not installed! Falling back to vanilla entities."
                        );
                    }
                    return;
                }
            }
            CourierPrime.getPlugin().getLogger().warning(
                "Unknown preferred plugin '" + preferred + "'! Valid options: AUTO, MYTHICMOBS, MODELENGINE"
            );
        }
    }

    /**
     * Checks if custom entity spawning is available
     * @return true if custom entities are enabled and a provider is active
     */
    public boolean isEnabled() {
        return MainConfig.isCustomEntitiesEnabled() && activeProvider != null;
    }

    /**
     * Gets the active provider name
     * @return The provider name, or "None" if not available
     */
    public String getActiveProviderName() {
        return activeProvider != null ? activeProvider.getName() : "None";
    }

    /**
     * Spawns a custom entity at the given location
     * @param location The spawn location
     * @return The spawned entity, or null if spawning failed or not enabled
     */
    public Entity spawnCustomEntity(Location location) {
        if (!isEnabled()) {
            return null;
        }

        return activeProvider.spawnEntity(, location);
    }
}

