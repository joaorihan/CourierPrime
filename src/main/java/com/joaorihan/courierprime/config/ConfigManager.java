package com.joaorihan.courierprime.config;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.courier.CourierManager;
import lombok.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
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

        generateOutgoingConfiguration();
        generateLanguageFiles();
        generateCourierConfiguration();
    }


    public void generateLanguageFiles(){
        File langFolder = new File(this.getPlugin().getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
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
            getPlugin().getComponentLogger().info(
                    Component.text("Now loading ", NamedTextColor.GRAY)
                            .append(Component.text(getMainConfig().getString("lang"), NamedTextColor.AQUA))
                            .append(Component.text(" language", NamedTextColor.GRAY))
            );
        } else {
            File fallbackFile = new File(langFolder, "en-us.yml");
            if (fallbackFile.exists()) {
                setLanguageConfig(YamlConfiguration.loadConfiguration(fallbackFile));
                getPlugin().getComponentLogger().warn(
                        Component.text("Language file ", NamedTextColor.YELLOW)
                                .append(Component.text(getMainConfig().getString("lang") + ".yml", NamedTextColor.GOLD))
                                .append(Component.text(" was not found; falling back to ", NamedTextColor.YELLOW))
                                .append(Component.text("en-us", NamedTextColor.AQUA))
                                .append(Component.text(".", NamedTextColor.YELLOW))
                );
            } else {
                throw new IllegalStateException("No language file is available");
            }
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
        CourierManager.getActiveCouriers().clear();
        Bukkit.getScheduler().cancelTasks(getPlugin());

        if (getPlugin().getOutgoingManager() != null) {
            getPlugin().getOutgoingManager().saveAll();
        }

        try {
            getPlugin().reloadState();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (getPlugin().getOutgoingManager().hasPendingLetters(player)) {
                    Bukkit.getScheduler().runTaskLater(getPlugin(),
                            () -> new Courier(player),
                            MainConfig.getReceiveDelay());
                }
            }
        } catch (Exception e) {
            getPlugin().getLogger().log(Level.SEVERE,
                    "An error occurred while attempting to reload. Check logs", e);
            return;
        }

        getPlugin().getLogger().info("Plugin reloaded successfully");

    }


}
