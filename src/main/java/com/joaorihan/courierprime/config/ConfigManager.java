package com.joaorihan.courierprime.config;

import com.joaorihan.courierprime.CourierPrime;
import lombok.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
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
        new File(this.getPlugin().getDataFolder(), "lang").mkdir();

        plugin.saveResource("lang/en-us.yml", false);
        plugin.saveResource("lang/pt-br.yml", false);

        File langFile = new File(getPlugin().getDataFolder(),  "lang/" + getMainConfig().getString("lang") + ".yml");

        if (langFile.exists()){
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


}

