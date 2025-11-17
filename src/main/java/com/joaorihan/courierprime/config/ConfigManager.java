package com.joaorihan.courierprime.config;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.courier.CourierManager;
import lombok.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.logging.Level;

/**
 * Controller for all config and message Files
 */
@Setter
@Getter
public class ConfigManager {

    private CourierPrime plugin;

    private FileConfiguration mainConfig;

    private File languageFile;
    private YamlConfiguration languageConfig;

    private File outgoingFile;
    private YamlConfiguration outgoingConfig;

    private File courierSelectFile;
    private YamlConfiguration courierSelectConfig;


    public ConfigManager(CourierPrime plugin){
        setPlugin(plugin);
        setMainConfig(plugin.getConfig());

        mainConfig.options().copyDefaults();
        plugin.saveDefaultConfig();

        // Generate configs first
        generateLanguageFiles();
        generateOutgoingConfiguration();
        generateCourierConfiguration();
        
        // Update existing configs to ensure all new keys are added
        updateConfigurations();
    }


    public void generateLanguageFiles(){
        File langFolder = new File(this.getPlugin().getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdir();
        }

        File enUSFile = new File(langFolder, "en-us.yml");
        if (!enUSFile.exists()) {
            plugin.saveResource("lang/en-us.yml", false);
        }

        File ptBRFile = new File(langFolder, "pt-br.yml");
        if (!ptBRFile.exists()) {
            plugin.saveResource("lang/pt-br.yml", false);
        }

        File langFile = new File(langFolder, getMainConfig().getString("lang") + ".yml");

        if (langFile.exists()) {
            setLanguageFile(langFile);
            setLanguageConfig(YamlConfiguration.loadConfiguration(langFile));
            getPlugin().getLogger().info("Now loading " + getMainConfig().getString("lang") + " language");
        } else {
            getPlugin().getLogger().severe("Error on starting language file. " + getMainConfig().getString("lang") + ".yml was not found!");
        }
    }

    public void generateOutgoingConfiguration(){
        setOutgoingFile(new File(this.getPlugin().getDataFolder(),"outgoing.yml"));
        if (!getOutgoingFile().exists()){
            this.getPlugin().saveResource("outgoing.yml", false);
        }

        setOutgoingConfig(new YamlConfiguration());
        try {
            getOutgoingConfig().load(getOutgoingFile());
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveOutgoingConfig() {
        if (getOutgoingConfig() == null || getOutgoingFile() == null) {
            return;
        }
        try {
            getOutgoingConfig().save(getOutgoingFile());
        } catch (IOException ex) {
            CourierPrime.getPlugin().getLogger().log(Level.SEVERE, "Outgoing.yml file failed to save!", ex);
        }
    }


    public void generateCourierConfiguration(){
        setCourierSelectFile(new File(this.getPlugin().getDataFolder(),"couriers.yml"));
        if (!getCourierSelectFile().exists()){
            this.getPlugin().saveResource("couriers.yml", false);
        }

        setCourierSelectConfig(new YamlConfiguration());
        try {
            getCourierSelectConfig().load(getCourierSelectFile());
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveCourierConfig() {
        if (getCourierSelectConfig() == null || getCourierSelectFile() == null) {
            return;
        }
        try {
            getCourierSelectConfig().save(getCourierSelectFile());
        } catch (IOException ex) {
            CourierPrime.getPlugin().getLogger().log(Level.SEVERE, "couriers.yml file failed to save!", ex);
        }
    }


    public void reloadConfigurations(){
        CourierManager.getActiveCouriers().keySet().forEach(Entity::remove);
        getPlugin().getOutgoingManager().saveAll();

        try {
            getPlugin().onEnable();
        } catch (Exception e) {
            getPlugin().getLogger().severe("An error occurred while attempting to reload. Check logs");
            return;
        }

        getPlugin().getLogger().info("Plugin reloaded successfully");

    }

    /**
     * Updates all configuration files to add missing keys from the default resources
     */
    private void updateConfigurations() {
        updateLanguageFiles();
        
        // Reload the currently active language config after updating files
        if (getLanguageFile() != null && getLanguageFile().exists()) {
            setLanguageConfig(YamlConfiguration.loadConfiguration(getLanguageFile()));
        }
    }

    /**
     * Updates language files to ensure all messages from the enum exist
     */
    private void updateLanguageFiles() {
        File langFolder = new File(this.getPlugin().getDataFolder(), "lang");
        if (!langFolder.exists()) {
            return;
        }

        // Update both language files
        updateLanguageFile(langFolder, "en-us.yml");
        updateLanguageFile(langFolder, "pt-br.yml");
    }

    /**
     * Updates a specific language file
     */
    private void updateLanguageFile(File langFolder, String fileName) {
        File langFile = new File(langFolder, fileName);
        if (!langFile.exists()) {
            return;
        }

        try {
            YamlConfiguration existingConfig = YamlConfiguration.loadConfiguration(langFile);
            
            // Load default language file from resources
            InputStream defaultStream = this.getPlugin().getResource("lang/" + fileName);
            if (defaultStream != null) {
                try {
                    YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                    updateConfigKeys(existingConfig, defaultConfig);
                    existingConfig.save(langFile);
                    getPlugin().getLogger().info("Updated language file: " + fileName);
                } finally {
                    defaultStream.close();
                }
            }
        } catch (IOException e) {
            getPlugin().getLogger().log(Level.WARNING, "Failed to update language file: " + fileName, e);
        }
    }

    /**
     * Recursively copies missing keys from default config to existing config
     */
    private void updateConfigKeys(YamlConfiguration existingConfig, YamlConfiguration defaultConfig) {
        Set<String> defaultKeys = defaultConfig.getKeys(true);
        
        for (String key : defaultKeys) {
            // Skip if the key already exists and is a section
            if (existingConfig.contains(key)) {
                continue;
            }
            
            // Add the missing key with its default value
            Object defaultValue = defaultConfig.get(key);
            existingConfig.set(key, defaultValue);
        }
    }


}

