package com.joaorihan.courierprime.config;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.courier.CourierType;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * config options from the main config. all times are in ticks
 *
 * @author João Rihan
 */
@UtilityClass
public class MainConfig {

    private FileConfiguration config;

    /**
     * delay between sending a letter, joining the server, switching out of a blocked gamemode, or coming from a blocked
     * world, to the recipient receiving the letter
     */
    public int getReceiveDelay(){ return config.getInt("receive-delay"); }
    
    /**
     * delay between respawning the courier when mail is not taken
     */
    public int getResendDelay(){ return config.getInt("resend-delay"); }
    
    /**
     * delay before removing courier after spawning
     */
    public int getRemoveDelay(){ return config.getInt("remove-delay"); }
    
    /**
     * how far away to spawn the courier from the player, in blocks
     */
    public int getSpawnDistance(){ return config.getInt("spawn-distance"); }
    
    /**
     * entity type to use as the courier
     */
    public EntityType getDefaultCourierEntityType(){
        String configuredType = config.getString("default-courier-entity-type", "VILLAGER");
        if (configuredType == null) {
            configuredType = "VILLAGER";
        }
        try {
            EntityType entityType = EntityType.valueOf(configuredType.trim().toUpperCase(Locale.ROOT));
            if (entityType.isSpawnable()) {
                return entityType;
            }
        } catch (IllegalArgumentException ignored) {
            // Fall through to the safe default below.
        }

        CourierPrime.getPlugin().getLogger().warning(
                "Invalid default courier entity type '" + configuredType + "'; using VILLAGER."
        );
        return EntityType.VILLAGER;
    }
    
    /**
     * gamemodes that disallow receiving mail
     */
    public Set<GameMode> getBlockedGamemodes(){
        Set<GameMode> gameModes = new HashSet<>();
        for (String s : config.getStringList("blocked-gamemodes")) {
            try {
                gameModes.add(GameMode.valueOf(s.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                CourierPrime.getPlugin().getLogger().warning("Invalid blocked gamemode in config: " + s);
            }
        }
        return gameModes;
    }
    
    /**
     * worlds that disallow receiving mail
     */
    public Set<World> getBlockedWorlds(){
        Set<World> worlds = new HashSet<>();
        for (String s : config.getStringList("blocked-worlds")) {
            World world = Bukkit.getWorld(s);
            if (world != null) {
                worlds.add(world);
            }
        }
        return worlds;
    }

    public int getLetterCustomModelData() { return config.getInt("letter.letter-custom-model-data");  }

    public int getAnonLetterCustomModelData() { return config.getInt("letter.anon-letter-custom-model-data");  }


    public boolean isAnonymousLetters(){ return config.getBoolean("letter.anonymous-letters-enabled"); }

    public boolean isCustomModelData(){ return config.getBoolean("letter.use-custom-model-data"); }

    /**
     * Gets all enabled courier types (vanilla and custom)
     * @return List of CourierType objects
     */
    public List<CourierType> getEnabledCourierTypes() {
        List<CourierType> types = new ArrayList<>();
        for (String s : config.getStringList("enabled-courier-types")) {
            CourierType type = CourierType.parse(s);
            if (type != null) {
                types.add(type);
            } else {
                CourierPrime.getPlugin().getLogger().warning("Invalid courier type in config: " + s);
            }
        }
        return types;
    }

    /**
     * Gets a list of enabled courier type strings for tab completion
     * @return List of courier type strings
     */
    public List<String> getEnabledCourierTypeStrings() {
        LinkedHashSet<String> types = new LinkedHashSet<>();
        for (CourierType type : getEnabledCourierTypes()) {
            types.add(type.toString());
        }
        return new ArrayList<>(types);
    }

    /**
     * Checks the normal player-selection allow-list using parsed values, so
     * case and provider-prefix normalization are handled consistently.
     */
    public boolean isCourierTypeEnabled(CourierType courierType) {
        return courierType != null && getEnabledCourierTypes().contains(courierType);
    }

    /**
     * Gets every spawnable vanilla value and every valid custom value present
     * in the configuration. This is intended for administrator completion;
     * administrators may assign vanilla types that are not enabled for normal
     * player selection.
     */
    public List<String> getAllSpawnableCourierTypeStrings() {
        LinkedHashSet<String> types = new LinkedHashSet<>();
        for (EntityType entityType : EntityType.values()) {
            if (entityType.isSpawnable()) {
                types.add(entityType.name());
            }
        }
        for (CourierType type : getEnabledCourierTypes()) {
            types.add(type.toString());
        }
        return new ArrayList<>(types);
    }

    /**
     * Base entity type for ModelEngine couriers
     */
    public EntityType getModelEngineBaseEntity() {
        String configuredType = config.getString("modelengine-base-entity");
        if (configuredType == null) {
            // Keep the setting from the earlier custom-entities configuration layout.
            configuredType = config.getString("custom-entities.modelengine-base-entity", "ARMOR_STAND");
        }
        if (configuredType == null) {
            configuredType = "ARMOR_STAND";
        }

        try {
            EntityType entityType = EntityType.valueOf(configuredType.trim().toUpperCase(Locale.ROOT));
            if (entityType.isSpawnable()) {
                return entityType;
            }
        } catch (IllegalArgumentException ignored) {
            // Fall through to the safe default below.
        }

        CourierPrime.getPlugin().getLogger().warning(
                "Invalid ModelEngine base entity type '" + configuredType + "'; using ARMOR_STAND."
        );
        return EntityType.ARMOR_STAND;
    }

    /**
     * load config options from the config file
     */
    public static void load() {
        config = null;
        CourierPrime.getPlugin().reloadConfig();
        config = CourierPrime.getPlugin().getConfig();
    }
    
}
