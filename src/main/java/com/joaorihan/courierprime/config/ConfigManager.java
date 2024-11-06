package com.joaorihan.courierprime.config;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.courier.Courier;
import lombok.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Controller for all config and message Files
 */
public class ConfigManager {

    @Getter @Setter private CourierPrime plugin;

    @Getter @Setter private FileConfiguration mainConfig;

    @Getter @Setter private File languageFile;
    @Getter @Setter private YamlConfiguration languageConfig;

    @Getter @Setter private File outgoingFile;
    @Getter @Setter private YamlConfiguration outgoingConfig;


    public ConfigManager(CourierPrime plugin){
        setPlugin(plugin);
        setMainConfig(plugin.getConfig());

        mainConfig.options().copyDefaults();
        plugin.saveDefaultConfig();

        generateOutgoingConfiguration();
        generateLanguageFiles();
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
            CourierPrime.getInstance().getLogger().log(Level.SEVERE, "Outgoing.yml file failed to save!", ex);
        }
    }


    public void reloadConfigurations(){
        Courier.getCouriers().keySet().forEach(Entity::remove);
        getPlugin().getOutgoingManager().saveAll();

        try {
            getPlugin().onEnable();
        } catch (Exception e) {
            getPlugin().getLogger().severe("An error occurred while attempting to reload. Check logs");
            return;
        }

        getPlugin().getLogger().info("Plugin reloaded successfully");

    }


}

