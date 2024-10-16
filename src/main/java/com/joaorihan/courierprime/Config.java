package com.joaorihan.courierprime;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * class used to manage all config files in plugin
 *
 * @author Jeremy Noesen
 */
public class Config {
    
    /**
     * main config instance
     */
    private static Config main = new Config("config.yml");
    
    /**
     * outgoing config instance
     */
    private static Config outgoing = new Config("outgoing.yml");
    
    /**
     * message config instance
     */
    private static Config message = new Config("messages.yml");
    
    /**
     * file used for the config
     */
    private File configFile;
    
    /**
     * file loaded as YAML config file
     */
    private YamlConfiguration YMLConfig;
    
    /**
     * config file name
     */
    private final String filename;
    
    /**
     * create a new config with the specified type
     *
     * @param filename config file name
     */
    public Config(String filename) {
        this.filename = filename;
        configFile = new File(CourierPrime.getInstance().getDataFolder(), filename);
    }
    
    /**
     * get the main config
     *
     * @return main config instance
     */
    public static Config getMainConfig() {
        return main;
    }
    
    /**
     * get the outgoing config
     *
     * @return outgoing config instance
     */
    public static Config getOutgoingConfig() {
        return outgoing;
    }
    
    /**
     * get the message config
     *
     * @return message config instance
     */
    public static Config getMessageConfig() {
        return message;
    }
    
    /**
     * reloads a configuration file, will load if the file is not loaded. Also saves defaults when they're missing
     */
    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(CourierPrime.getInstance().getDataFolder(), filename);
        }
        
        YMLConfig = YamlConfiguration.loadConfiguration(configFile);
        
        Reader defConfigStream = new InputStreamReader(
                CourierPrime.getInstance().getResource(filename), StandardCharsets.UTF_8);
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        YMLConfig.setDefaults(defConfig);
        YMLConfig.options().copyDefaults(true);
        saveConfig();
        
        if (filename.equals("messages.yml")) Message.reloadMessages();
    }
    
    /**
     * reloads config if YMLConfig is null
     *
     * @return YMLConfig YamlConfiguration
     */
    public YamlConfiguration getConfig() {
        if (YMLConfig == null) {
            reloadConfig();
        }
        return YMLConfig;
    }
    
    /**
     * saves a config file
     */
    public void saveConfig() {
        if (YMLConfig == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            CourierPrime.getInstance().getLogger().log(Level.SEVERE, "A config file failed to save!", ex);
        }
    }
    
    /**
     * saves the default config from the plugin jar if the file doesn't exist in the plugin folder
     */
    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(CourierPrime.getInstance().getDataFolder(), filename);
        }
        if (!configFile.exists()) {
            CourierPrime.getInstance().saveResource(filename, false);
            YMLConfig = YamlConfiguration.loadConfiguration(configFile);
        }
    }
}
