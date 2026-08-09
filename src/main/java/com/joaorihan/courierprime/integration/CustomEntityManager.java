package com.joaorihan.courierprime.integration;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.courier.CourierType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.logging.Level;

/**
 * Manages optional custom entity providers and handles their lifecycle.
 *
 * Provider classes are created lazily, after their corresponding Bukkit
 * plugin is enabled. This keeps an absent soft dependency from resolving its
 * compile-only API during normal vanilla startup.
 */
public class CustomEntityManager {

    private static final String MYTHICMOBS = "MythicMobs";
    private static final String MODELENGINE = "ModelEngine";

    private CustomEntityProvider mythicMobsProvider;
    private CustomEntityProvider modelEngineProvider;
    private boolean mythicMobsLinkageFailure;
    private boolean modelEngineLinkageFailure;

    /**
     * Initializes the manager and logs available providers. Calling this more
     * than once is safe; provider instances are reused while the manager is
     * alive.
     */
    public void initialize() {
        boolean mythicMobsAvailable = isMythicMobsAvailable();
        boolean modelEngineAvailable = isModelEngineAvailable();

        if (mythicMobsAvailable) {
            CourierPrime.getPlugin().getComponentLogger().info(
                    Component.text("Hooked into ", NamedTextColor.GRAY)
                            .append(Component.text("MythicMobs", NamedTextColor.GOLD))
                            .append(Component.text(" - custom ", NamedTextColor.GRAY))
                            .append(Component.text("mm:", NamedTextColor.GOLD))
                            .append(Component.text(" couriers enabled!", NamedTextColor.GREEN))
            );
        }
        if (modelEngineAvailable) {
            CourierPrime.getPlugin().getComponentLogger().info(
                    Component.text("Hooked into ", NamedTextColor.GRAY)
                            .append(Component.text("ModelEngine", NamedTextColor.AQUA))
                            .append(Component.text(" - custom ", NamedTextColor.GRAY))
                            .append(Component.text("meg:", NamedTextColor.AQUA))
                            .append(Component.text(" couriers enabled!", NamedTextColor.GREEN))
            );
        }
        if (!mythicMobsAvailable && !modelEngineAvailable) {
            CourierPrime.getPlugin().getComponentLogger().info(
                    Component.text("No custom courier plugins hooked; vanilla couriers only.", NamedTextColor.GRAY)
            );
        }
    }

    public boolean isMythicMobsAvailable() {
        if (!isPluginEnabled(MYTHICMOBS)) {
            return false;
        }
        return providerAvailable(getMythicMobsProvider(), true);
    }

    public boolean isModelEngineAvailable() {
        if (!isPluginEnabled(MODELENGINE)) {
            return false;
        }
        return providerAvailable(getModelEngineProvider(), false);
    }

    /**
     * Validates both the provider availability and the provider-side
     * identifier. A missing dependency or type is intentionally reported as
     * unavailable so saved selections can fall back to vanilla at spawn time.
     */
    public boolean isEntityAvailable(CourierType courierType) {
        if (courierType == null) {
            return false;
        }
        if (courierType.isVanilla()) {
            return courierType.getVanillaType() != null
                    && courierType.getVanillaType().isSpawnable();
        }
        if (!CourierType.isValidCustomIdentifier(courierType.getIdentifier())) {
            return false;
        }

        return switch (courierType.getProvider()) {
            case MYTHICMOBS -> hasEntity(getMythicMobsProvider(), true, courierType.getIdentifier());
            case MODELENGINE -> hasEntity(getModelEngineProvider(), false, courierType.getIdentifier());
            case VANILLA -> false;
        };
    }

    /**
     * Spawns a custom entity based on the CourierType.
     *
     * @param courierType the courier type to spawn
     * @param location the spawn location
     * @return the spawned entity, or null if spawning failed
     */
    public Entity spawnEntity(CourierType courierType, Location location) {
        if (courierType == null || courierType.isVanilla() || location == null
                || location.getWorld() == null
                || !CourierType.isValidCustomIdentifier(courierType.getIdentifier())) {
            return null;
        }

        boolean mythicMobs = courierType.isMythicMobs();
        CustomEntityProvider provider = mythicMobs
                ? getMythicMobsProvider()
                : getModelEngineProvider();
        if (!providerAvailable(provider, mythicMobs)) {
            return null;
        }

        String identifier = courierType.getIdentifier();
        try {
            // Revalidate immediately before spawning. This protects callers
            // that invoke spawnEntity directly and ensures unknown IDs never
            // reach the provider's spawn method.
            if (!provider.hasEntity(identifier)) {
                return null;
            }
            return provider.spawnEntity(identifier, location);
        } catch (LinkageError e) {
            markLinkageFailure(mythicMobs, e);
            return null;
        } catch (RuntimeException e) {
            logger().log(Level.WARNING,
                    "The " + provider.getName() + " provider failed while spawning '"
                            + identifier + "'. Falling back to a vanilla courier.", e);
            return null;
        }
    }

    private boolean hasEntity(CustomEntityProvider provider, boolean mythicMobs, String identifier) {
        if (!isPluginEnabled(mythicMobs ? MYTHICMOBS : MODELENGINE)
                || !CourierType.isValidCustomIdentifier(identifier)
                || !providerAvailable(provider, mythicMobs)) {
            return false;
        }

        try {
            return provider.hasEntity(identifier);
        } catch (LinkageError e) {
            markLinkageFailure(mythicMobs, e);
            return false;
        } catch (RuntimeException e) {
            logger().log(Level.WARNING,
                    "The " + provider.getName() + " provider could not validate '"
                            + identifier + "'.", e);
            return false;
        }
    }

    private boolean providerAvailable(CustomEntityProvider provider, boolean mythicMobs) {
        if (provider == null) {
            return false;
        }
        try {
            return provider.isAvailable();
        } catch (LinkageError e) {
            markLinkageFailure(mythicMobs, e);
            return false;
        } catch (RuntimeException e) {
            logger().log(Level.WARNING,
                    "The " + provider.getName() + " provider could not be queried.", e);
            return false;
        }
    }

    private CustomEntityProvider getMythicMobsProvider() {
        if (!isPluginEnabled(MYTHICMOBS) || mythicMobsLinkageFailure) {
            return null;
        }
        if (mythicMobsProvider == null) {
            try {
                mythicMobsProvider = new MythicMobsProvider();
            } catch (LinkageError e) {
                markLinkageFailure(true, e);
            }
        }
        return mythicMobsProvider;
    }

    private CustomEntityProvider getModelEngineProvider() {
        if (!isPluginEnabled(MODELENGINE) || modelEngineLinkageFailure) {
            return null;
        }
        if (modelEngineProvider == null) {
            try {
                modelEngineProvider = new ModelEngineProvider();
            } catch (LinkageError e) {
                markLinkageFailure(false, e);
            }
        }
        return modelEngineProvider;
    }

    private boolean isPluginEnabled(String pluginName) {
        return Bukkit.getPluginManager() != null
                && Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    private void markLinkageFailure(boolean mythicMobs, LinkageError error) {
        if (mythicMobs) {
            if (mythicMobsLinkageFailure) {
                return;
            }
            mythicMobsLinkageFailure = true;
        } else {
            if (modelEngineLinkageFailure) {
                return;
            }
            modelEngineLinkageFailure = true;
        }

        String provider = mythicMobs ? MYTHICMOBS : MODELENGINE;
        logger().log(Level.WARNING,
                provider + " is enabled but its optional API could not be linked. "
                        + "That courier provider will remain disabled until the plugin is reloaded.",
                error);
    }

    private java.util.logging.Logger logger() {
        CourierPrime plugin = CourierPrime.getPlugin();
        return plugin == null ? java.util.logging.Logger.getLogger("CourierPrime") : plugin.getLogger();
    }
}
