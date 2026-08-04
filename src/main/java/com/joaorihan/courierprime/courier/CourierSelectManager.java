package com.joaorihan.courierprime.courier;

import com.joaorihan.courierprime.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CourierSelectManager {

    private final HashMap<UUID, CourierType> activeCourierTypes = new HashMap<>();

    private final ConfigManager configManager;

    public CourierSelectManager(ConfigManager configManager){
        this.configManager = configManager;
        loadActiveCouriers();
    }

    protected void loadActiveCouriers(){
        if (configManager == null || configManager.getCourierSelectConfig() == null) {
            return;
        }

        Map<String, Object> storedCouriers;
        try {
            storedCouriers = configManager.getCourierSelectConfig().getValues(false);
        } catch (RuntimeException e) {
            configManager.getPlugin().getLogger().warning(
                    "Unable to read couriers.yml entries; keeping the file unchanged and using defaults."
            );
            return;
        }

        HashMap<UUID, CourierType> loadedCouriers = new HashMap<>();
        storedCouriers.forEach((playerId, storedValue) -> {
            if (storedValue == null) {
                configManager.getPlugin().getLogger().warning(
                        "Ignoring empty courier type for player " + playerId + " in couriers.yml."
                );
                return;
            }

            CourierType type;
            try {
                type = CourierType.parse(storedValue.toString());
            } catch (RuntimeException e) {
                type = null;
            }
            if (type == null) {
                configManager.getPlugin().getLogger().warning(
                        "Invalid courier type for player " + playerId + ": " + storedValue
                );
                return;
            }

            try {
                loadedCouriers.put(UUID.fromString(playerId), type);
            } catch (IllegalArgumentException e) {
                configManager.getPlugin().getLogger().warning(
                        "Invalid player UUID in couriers.yml: " + playerId
                );
            }
        });

        activeCourierTypes.clear();
        activeCourierTypes.putAll(loadedCouriers);
    }

    public CourierType getActiveCourier(UUID player){
        return activeCourierTypes.get(player);
    }

    public void setActiveCourier(UUID player, CourierType courierType){
        if (player == null || courierType == null) {
            return;
        }
        configManager.getCourierSelectConfig().set(String.valueOf(player), courierType.toString());
        activeCourierTypes.put(player, courierType);
        configManager.saveCourierConfig();
    }

}
