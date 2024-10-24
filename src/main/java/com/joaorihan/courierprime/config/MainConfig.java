package com.joaorihan.courierprime.config;

import com.joaorihan.courierprime.CourierPrime;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.HashSet;
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
    public EntityType getCourierEntityType(){ return EntityType.valueOf(config.getString("courier-entity-type")); };
    
    /**
     * gamemodes that disallow receiving mail
     */
    public Set<GameMode> getBlockedGamemodes(){
        Set<GameMode> gameModes = new HashSet<>();
        for(String s : config.getStringList("blocked-gamemodes")) gameModes.add(GameMode.valueOf(s));
        return gameModes;
    }
    
    /**
     * worlds that disallow receiving mail
     */
    public Set<World> getBlockedWorlds(){
        Set<World> worlds = new HashSet<>();
        for(String s : config.getStringList("blocked-worlds")) worlds.add(Bukkit.getWorld(s));
        return worlds;
    }


    public boolean isAnonymousLetters(){ return config.getBoolean("letter.anonymous-letters-enabled"); }
    
    /**
     * load config options from the config file
     */
    public static void load() {
        config = CourierPrime.getPlugin().getConfig();
    }
    
}
