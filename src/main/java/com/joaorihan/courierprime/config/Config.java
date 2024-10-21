package com.joaorihan.courierprime.config;

import com.joaorihan.courierprime.CourierPrime;
import lombok.Getter;
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
    @Getter
    private static Config mainConfig = new Config("config.yml");
    
    /**
     * outgoing config instance
     */
    @Getter
    private static Config outgoingConfig = new Config("outgoing.yml");
    
    /**
     * message config instance
     */
    @Getter
    private static Config messageConfig = loadLanguageFile();
    
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


    public static Config loadLanguageFile(){

        String loadedLanguage = MainConfig.getLoadedLanguage();

        // Lang file checks
        if (loadedLanguage == null || loadedLanguage.isEmpty()){
            CourierPrime.getInstance().getLogger().severe("Please setup the plugin language in the config.yml!");
            MainConfig.loadedLanguage = "en-us";
            return new Config("en-us.yml");
        }

        // Path to the lang/ directory
        File langFile = new File(CourierPrime.getInstance().getDataFolder(), "lang/" + loadedLanguage.toLowerCase() + ".yml");

        // Check if the file exists
        if (!langFile.exists()) {
            CourierPrime.getInstance().getLogger().severe("Language file " + loadedLanguage + ".yml not found, defaulting to en-us.");
            MainConfig.loadedLanguage = "en-us";
            return new Config("en-us.yml");
        }


        return new Config(loadedLanguage.toLowerCase() + ".yml");

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
        
        if (filename.equals(MainConfig.loadedLanguage + ".yml")) Message.reloadMessages();
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
