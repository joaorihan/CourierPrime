package com.joaorihan.courierprime.config;

import com.joaorihan.courierprime.CourierPrime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Controller for all config and message files.
 */
@Setter
@Getter
public class ConfigManager {

    private static final String DEFAULT_LANGUAGE = "en-us";

    private CourierPrime plugin;

    private FileConfiguration mainConfig;

    private File languageFile;
    private YamlConfiguration languageConfig;

    private File outgoingFile;
    private YamlConfiguration outgoingConfig;

    private File courierSelectFile;
    private YamlConfiguration courierSelectConfig;

    /**
     * These flags are only set when the malformed source could not be moved out
     * of the way. In that case a later save must not silently overwrite it.
     */
    @Setter(AccessLevel.NONE)
    private boolean outgoingSaveProtected;
    @Setter(AccessLevel.NONE)
    private boolean courierSaveProtected;

    /** Keep the data and language YAML getters non-null even if a caller uses
     * the generated-style setters during a test or an integration hook. */
    public void setLanguageConfig(YamlConfiguration languageConfig) {
        this.languageConfig = languageConfig == null ? new YamlConfiguration() : languageConfig;
    }

    public void setOutgoingConfig(YamlConfiguration outgoingConfig) {
        this.outgoingConfig = outgoingConfig == null ? new YamlConfiguration() : outgoingConfig;
    }

    public void setCourierSelectConfig(YamlConfiguration courierSelectConfig) {
        this.courierSelectConfig = courierSelectConfig == null ? new YamlConfiguration() : courierSelectConfig;
    }

    public ConfigManager(CourierPrime plugin) {
        setPlugin(plugin);
        setMainConfig(plugin.getConfig());

        mainConfig.options().copyDefaults();
        plugin.saveDefaultConfig();

        generateLanguageFiles();
        generateOutgoingConfiguration();
        generateCourierConfiguration();
    }

    public void generateLanguageFiles() {
        File langFolder = new File(this.plugin.getDataFolder(), "lang");
        if (!langFolder.exists() && !langFolder.mkdirs()) {
            plugin.getLogger().warning("Unable to create the lang directory.");
        }

        ensureBundledResource("lang/en-us.yml", new File(langFolder, "en-us.yml"));
        ensureBundledResource("lang/pt-br.yml", new File(langFolder, "pt-br.yml"));

        String requestedLanguage = mainConfig.getString("lang", DEFAULT_LANGUAGE);
        if (!isSafeLanguageName(requestedLanguage)) {
            plugin.getComponentLogger().warn(
                    Component.text("Invalid language ", NamedTextColor.YELLOW)
                            .append(Component.text(String.valueOf(requestedLanguage), NamedTextColor.GOLD))
                            .append(Component.text("; falling back to ", NamedTextColor.YELLOW))
                            .append(Component.text(DEFAULT_LANGUAGE, NamedTextColor.AQUA))
                            .append(Component.text(".", NamedTextColor.YELLOW))
            );
            requestedLanguage = DEFAULT_LANGUAGE;
        }

        File requestedFile = new File(langFolder, requestedLanguage + ".yml");
        if (requestedFile.isFile()) {
            YamlConfiguration selectedLanguage = loadLanguageConfiguration(requestedFile, requestedLanguage);
            if (selectedLanguage != null) {
                setLanguageFile(requestedFile);
                setLanguageConfig(selectedLanguage);
                plugin.getComponentLogger().info(
                        Component.text("Now loading ", NamedTextColor.GRAY)
                                .append(Component.text(requestedLanguage, NamedTextColor.AQUA))
                                .append(Component.text(" language", NamedTextColor.GRAY))
                );
                return;
            }

            plugin.getComponentLogger().warn(
                    Component.text("Language file ", NamedTextColor.YELLOW)
                            .append(Component.text(requestedFile.getName(), NamedTextColor.GOLD))
                            .append(Component.text(" is invalid; falling back to ", NamedTextColor.YELLOW))
                            .append(Component.text(DEFAULT_LANGUAGE, NamedTextColor.AQUA))
                            .append(Component.text(".", NamedTextColor.YELLOW))
            );
        } else {
            // Preserve the parent commit's colored startup fallback message.
            plugin.getComponentLogger().warn(
                    Component.text("Language file ", NamedTextColor.YELLOW)
                            .append(Component.text(requestedLanguage + ".yml", NamedTextColor.GOLD))
                            .append(Component.text(" was not found; falling back to ", NamedTextColor.YELLOW))
                            .append(Component.text(DEFAULT_LANGUAGE, NamedTextColor.AQUA))
                            .append(Component.text(".", NamedTextColor.YELLOW))
            );
        }

        File fallbackFile = new File(langFolder, DEFAULT_LANGUAGE + ".yml");
        YamlConfiguration fallbackLanguage = fallbackFile.isFile()
                ? loadLanguageConfiguration(fallbackFile, DEFAULT_LANGUAGE)
                : null;

        if (fallbackLanguage == null) {
            plugin.getLogger().warning(
                    "The en-us language file could not be loaded; using an empty language configuration."
            );
            fallbackLanguage = new YamlConfiguration();
        }

        setLanguageFile(fallbackFile);
        setLanguageConfig(fallbackLanguage);
    }

    public void generateOutgoingConfiguration() {
        setOutgoingFile(new File(this.plugin.getDataFolder(), "outgoing.yml"));
        ensureBundledResource("outgoing.yml", getOutgoingFile());

        setOutgoingConfig(new YamlConfiguration());
        outgoingSaveProtected = !loadDataConfiguration(
                getOutgoingConfig(), getOutgoingFile(), "outgoing.yml"
        );
    }

    public void saveOutgoingConfig() {
        if (getOutgoingConfig() == null || getOutgoingFile() == null) {
            return;
        }
        if (outgoingSaveProtected) {
            plugin.getLogger().warning(
                    "Not saving outgoing.yml because its malformed original could not be quarantined."
            );
            return;
        }

        try {
            getOutgoingConfig().save(getOutgoingFile());
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "outgoing.yml file failed to save!", ex);
        }
    }

    public void generateCourierConfiguration() {
        setCourierSelectFile(new File(this.plugin.getDataFolder(), "couriers.yml"));
        ensureBundledResource("couriers.yml", getCourierSelectFile());

        setCourierSelectConfig(new YamlConfiguration());
        courierSaveProtected = !loadDataConfiguration(
                getCourierSelectConfig(), getCourierSelectFile(), "couriers.yml"
        );
    }

    public void saveCourierConfig() {
        if (getCourierSelectConfig() == null || getCourierSelectFile() == null) {
            return;
        }
        if (courierSaveProtected) {
            plugin.getLogger().warning(
                    "Not saving couriers.yml because its malformed original could not be quarantined."
            );
            return;
        }

        try {
            getCourierSelectConfig().save(getCourierSelectFile());
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "couriers.yml file failed to save!", ex);
        }
    }

    /**
     * Reload all runtime state through the plugin lifecycle boundary. The plugin
     * performs cleanup, persistence, reconstruction, and online-mail rescheduling
     * in that order so this method cannot register duplicate commands/listeners.
     *
     * @throws IllegalStateException when the plugin could not rebuild its runtime
     *                               state; callers must not report success
     */
    public void reloadConfigurations() {
        try {
            plugin.reloadState();
        } catch (Exception exception) {
            plugin.getLogger().log(Level.SEVERE,
                    "An error occurred while attempting to reload. Check logs.", exception);
            throw new IllegalStateException("CourierPrime reload failed", exception);
        }

        plugin.getLogger().info("Plugin reloaded successfully");
    }

    private void ensureBundledResource(String resourcePath, File target) {
        if (target.exists()) {
            return;
        }

        try {
            plugin.saveResource(resourcePath, false);
        } catch (RuntimeException exception) {
            plugin.getLogger().log(Level.WARNING,
                    "Unable to create bundled resource " + resourcePath + ".", exception);
        }
    }

    private boolean isSafeLanguageName(String language) {
        return language != null && language.matches("[A-Za-z0-9_-]+");
    }

    private YamlConfiguration loadLanguageConfiguration(File file, String languageName) {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(file);
            return configuration;
        } catch (IOException | InvalidConfigurationException | RuntimeException exception) {
            plugin.getLogger().log(Level.WARNING,
                    "Unable to load language " + languageName + " from " + file.getName() + ".",
                    exception);
            return null;
        }
    }

    /**
     * Load a data YAML into an already-created non-null configuration. A syntax
     * failure is isolated by moving the original to a timestamped quarantine
     * file. If that move fails, the original is retained and future saves are
     * blocked so it cannot be overwritten silently.
     *
     * @return true when it is safe for the in-memory replacement to be saved
     */
    private boolean loadDataConfiguration(YamlConfiguration configuration, File file, String displayName) {
        if (!file.isFile()) {
            return true;
        }

        try {
            configuration.load(file);
            return true;
        } catch (IOException | InvalidConfigurationException | RuntimeException exception) {
            plugin.getLogger().log(Level.WARNING,
                    "Unable to parse " + displayName + "; starting with an empty configuration.",
                    exception);

            for (String key : new ArrayList<>(configuration.getKeys(false))) {
                configuration.set(key, null);
            }

            File quarantine = quarantine(file);
            if (quarantine == null) {
                plugin.getLogger().warning(
                        "The malformed " + displayName + " was retained because it could not be quarantined. "
                                + "Future saves for this file are disabled to protect the original."
                );
                return false;
            }

            plugin.getLogger().warning(
                    "Quarantined malformed " + displayName + " as " + quarantine.getName()
                            + "; continuing with an empty configuration."
            );

            try {
                configuration.save(file);
            } catch (IOException saveException) {
                plugin.getLogger().log(Level.WARNING,
                        "The empty replacement for " + displayName + " could not be created.",
                        saveException);
            }
            return true;
        }
    }

    private File quarantine(File file) {
        try {
            Path source = file.toPath();
            String baseName = file.getName() + ".corrupt-" + System.currentTimeMillis();
            Path target = source.resolveSibling(baseName);
            int suffix = 1;

            while (Files.exists(target)) {
                target = source.resolveSibling(baseName + "-" + suffix++);
            }

            Files.move(source, target);
            return target.toFile();
        } catch (IOException | RuntimeException exception) {
            plugin.getLogger().log(Level.WARNING,
                    "Unable to quarantine malformed " + file.getName() + ".", exception);
            return null;
        }
    }
}
