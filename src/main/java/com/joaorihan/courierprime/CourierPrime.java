package com.joaorihan.courierprime;

import com.joaorihan.courierprime.command.CommandManager;
import com.joaorihan.courierprime.config.*;
import com.joaorihan.courierprime.letter.LetterManager;
import com.joaorihan.courierprime.letter.OutgoingManager;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.listener.LetterListener;
import com.joaorihan.courierprime.listener.PlayerListener;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for plugin, registers and initializes all files, listeners, and commands
 *
 * @author Jeremy Noesen
 */
public class CourierPrime extends JavaPlugin {
    
    /**
     * plugin instance
     */
    @Getter
    public static CourierPrime plugin;

    public static CourierPrime getInstance() {
        return plugin;
    }

    @Getter @Setter
    private ConfigManager configManager;

    @Getter @Setter
    private MessageManager messageManager;

    @Getter @Setter
    private LetterManager letterManager;

    @Getter @Setter
    private OutgoingManager outgoingManager;

    /**
     * initialize configurations, load messages, register commands and permissions
     */
    public void onEnable() {
        plugin = this;

        MainConfig.load();

        setConfigManager(new ConfigManager(plugin));
        setMessageManager(new MessageManager(configManager));

        setLetterManager(new LetterManager(plugin));
        setOutgoingManager(new OutgoingManager(plugin));

        getOutgoingManager().loadAll();


        // Register Listeners
        new LetterListener(plugin);
        new PlayerListener(plugin);

        // Register Commands
        new CommandManager();

    }
    
    /**
     * nullify the plugin instance
     */
    public void onDisable() {
        Courier.getCouriers().keySet().forEach(Entity::remove);
        getOutgoingManager().saveAll();

        plugin = null;
    }
}
