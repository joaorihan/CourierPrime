package com.joaorihan.courierprime.integration;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.config.MainConfig;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.logging.Level;

/**
 * Custom entity provider for ModelEngine integration
 */
public class ModelEngineProvider implements CustomEntityProvider {

    private static final String PLUGIN_NAME = "ModelEngine";

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME);
    }

    public boolean hasEntity(String entityId) {
        return isAvailable() && ModelEngineAPI.getBlueprint(entityId) != null;
    }

    @Override
    public Entity spawnEntity(String entityId, Location location) {
        if (!isAvailable()) {
            return null;
        }

        Entity baseEntity = null;
        try {
            // Spawn the base entity first
            baseEntity = location.getWorld().spawnEntity(location, MainConfig.getModelEngineBaseEntity());

            // Create and attach the model
            ActiveModel model = ModelEngineAPI.createActiveModel(entityId);
            if (model == null) {
                CourierPrime.getPlugin().getLogger().warning(
                    "ModelEngine model '" + entityId + "' not found! Falling back to vanilla entity."
                );
                baseEntity.remove();
                return null;
            }

            ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(baseEntity);
            modeledEntity.addModel(model, true);

            return baseEntity;

        } catch (Exception e) {
            if (baseEntity != null && !baseEntity.isDead()) {
                baseEntity.remove();
            }
            CourierPrime.getPlugin().getLogger().log(Level.WARNING,
                "Failed to spawn ModelEngine entity '" + entityId + "': " + e.getMessage(), e);
            return null;
        }
    }
}
