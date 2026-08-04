package com.joaorihan.courierprime.courier;

import com.joaorihan.courierprime.config.ConfigManager;

import java.util.HashMap;
import java.util.UUID;

public class CourierSelectManager {

    private final HashMap<UUID, CourierType> activeCourierTypes = new HashMap<>();

    private final ConfigManager configManager;

    public CourierSelectManager(ConfigManager configManager){
        this.configManager = configManager;
        loadActiveCouriers();
    }

    protected void loadActiveCouriers(){
        activeCourierTypes.clear();
        configManager.getCourierSelectConfig().getValues(false).forEach((s, o) -> {
            CourierType type = CourierType.parse(o.toString());
            if (type == null) {
                configManager.getPlugin().getLogger().warning("Invalid courier type for player " + s + ": " + o);
                return;
            }

            try {
                activeCourierTypes.put(UUID.fromString(s), type);
            } catch (IllegalArgumentException e) {
                configManager.getPlugin().getLogger().warning("Invalid player UUID in couriers.yml: " + s);
            }
        });
    }

    public CourierType getActiveCourier(UUID player){
        return activeCourierTypes.get(player);
    }

    public void setActiveCourier(UUID player, CourierType courierType){
        configManager.getCourierSelectConfig().set(String.valueOf(player), courierType.toString());
        activeCourierTypes.put(player, courierType);
        configManager.saveCourierConfig();
    }

}
