package com.joaorihan.courierprime.courier;

import com.joaorihan.courierprime.config.ConfigManager;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.UUID;

public class CourierSelectManager {

    private final HashMap<UUID, EntityType> activeCourierTypes = new HashMap<>();

    private final ConfigManager configManager;

    public CourierSelectManager(ConfigManager configManager){
        this.configManager = configManager;
        loadActiveCouriers();
    }

    protected void loadActiveCouriers(){
        activeCourierTypes.clear();
        configManager.getCourierSelectConfig().getValues(false).forEach((s, o) -> activeCourierTypes.put(UUID.fromString(s), EntityType.valueOf(o.toString())));
    }

    public EntityType getActiveCourier(UUID player){
        return activeCourierTypes.get(player);
    }

    public void setActiveCourier(UUID player, EntityType courierType){
        configManager.getCourierSelectConfig().set(String.valueOf(player), courierType.toString());
        activeCourierTypes.put(player, courierType);
        configManager.saveCourierConfig();
    }


}
