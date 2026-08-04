package com.joaorihan.courierprime.integration;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.config.MainConfig;
import com.joaorihan.courierprime.courier.CourierType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Optional;
import java.util.logging.Level;

/**
 * Custom entity provider for ModelEngine integration.
 */
public class ModelEngineProvider implements CustomEntityProvider {

    private static final String PLUGIN_NAME = "ModelEngine";

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
            return ModelEngineAPI.getBlueprint(entityId) != null;
        } catch (LinkageError e) {
            logger().log(Level.WARNING, "ModelEngine API could not validate courier model '" + entityId + "'.", e);
            return false;
        } catch (RuntimeException e) {
            logger().log(Level.WARNING, "ModelEngine could not validate courier model '" + entityId + "'.", e);
            return false;
        }
    }

    @Override
    public Entity spawnEntity(String entityId, Location location) {
        if (!isAvailable() || location == null || location.getWorld() == null
                || !CourierType.isValidCustomIdentifier(entityId) || !hasEntity(entityId)) {
            return null;
        }

        Entity baseEntity = null;
        ModeledEntity modeledEntity = null;
        ActiveModel model = null;
        try {
            // Spawn the configured host first. Every later failure path below
            // removes it, including a provider API returning null/empty.
            baseEntity = location.getWorld().spawnEntity(location, MainConfig.getModelEngineBaseEntity());
            if (baseEntity == null || baseEntity.isDead()) {
                cleanup(baseEntity, modeledEntity, model);
                return null;
            }

            model = ModelEngineAPI.createActiveModel(entityId);
            if (model == null) {
                warn("ModelEngine model '" + entityId + "' could not be created.");
                cleanup(baseEntity, modeledEntity, model);
                return null;
            }

            modeledEntity = ModelEngineAPI.createModeledEntity(baseEntity);
            if (modeledEntity == null) {
                warn("ModelEngine could not create a modeled host for '" + entityId + "'.");
                cleanup(baseEntity, modeledEntity, model);
                return null;
            }

            Optional<ActiveModel> attachedModel = modeledEntity.addModel(model, true);
            boolean modelAttached = attachedModel != null && attachedModel.isPresent();
            if (!modelAttached && modeledEntity.getModels() != null) {
                // R4 providers have used the Optional return value both for
                // add success and for a replaced model across API revisions;
                // the model registry is the stable confirmation of an attach.
                modelAttached = modeledEntity.getModels().containsValue(model);
            }
            if (!modelAttached) {
                warn("ModelEngine could not attach model '" + entityId + "'.");
                cleanup(baseEntity, modeledEntity, model);
                return null;
            }

            return baseEntity;
        } catch (LinkageError e) {
            cleanup(baseEntity, modeledEntity, model);
            logger().log(Level.WARNING,
                    "ModelEngine's optional API could not attach courier model '" + entityId + "'.", e);
            return null;
        } catch (Exception e) {
            cleanup(baseEntity, modeledEntity, model);
            logger().log(Level.WARNING,
                    "Failed to spawn ModelEngine courier '" + entityId
                            + "'; the host entity was cleaned up.", e);
            return null;
        }
    }

    private void cleanup(Entity baseEntity, ModeledEntity modeledEntity, ActiveModel model) {
        if (model != null) {
            try {
                model.destroy();
            } catch (LinkageError | RuntimeException ignored) {
                // Continue cleanup of the modeled record and Bukkit host.
            }
        }

        if (modeledEntity != null) {
            try {
                modeledEntity.destroy();
            } catch (LinkageError | RuntimeException ignored) {
                // Continue cleanup of the Bukkit host.
            }
        }

        if (baseEntity != null) {
            try {
                ModelEngineAPI.removeModeledEntity(baseEntity);
            } catch (LinkageError | RuntimeException ignored) {
                // The host removal below is still safe and necessary.
            }
            try {
                if (!baseEntity.isDead()) {
                    baseEntity.remove();
                }
            } catch (RuntimeException ignored) {
                // A host that was already removed needs no further action.
            }
        }
    }

    private void warn(String message) {
        logger().warning(message + " Falling back to a vanilla courier.");
    }

    private java.util.logging.Logger logger() {
        CourierPrime plugin = CourierPrime.getPlugin();
        return plugin == null ? java.util.logging.Logger.getLogger("CourierPrime") : plugin.getLogger();
    }
}
