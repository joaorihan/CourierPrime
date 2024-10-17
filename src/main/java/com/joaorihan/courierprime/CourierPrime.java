package com.joaorihan.courierprime;

import com.joaorihan.courierprime.command.CommandExec;
import com.joaorihan.courierprime.letter.Outgoing;
import com.joaorihan.courierprime.command.CommandTabComplete;
import com.joaorihan.courierprime.courier.CourierOptions;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.listener.LetterListener;
import com.joaorihan.courierprime.listener.PlayerListener;
import lombok.Getter;
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
    
    /**
     * get the plugin instance
     *
     * @return plugin instance
     */
    public static CourierPrime getInstance() {
        return plugin;
    }
    
    /**
     * initialize configurations, load messages, register commands and permissions
     */
    public void onEnable() {
        plugin = this;
        
        Config.getMessageConfig().saveDefaultConfig();
        Config.getOutgoingConfig().saveDefaultConfig();
        Config.getMainConfig().saveDefaultConfig();
        
        CourierOptions.load();
        Outgoing.loadAll();
        Message.reloadMessages();
        
        PluginManager pm = Bukkit.getPluginManager();

        // Register Listeners
        new LetterListener(plugin);
        new PlayerListener(plugin);

        pm.addPermission(new Permission("couriernew.letter"));
        pm.addPermission(new Permission("couriernew.post.one"));
        pm.addPermission(new Permission("couriernew.post.multiple"));
        pm.addPermission(new Permission("couriernew.post.allonline"));
        pm.addPermission(new Permission("couriernew.post.all"));
        pm.addPermission(new Permission("couriernew.unread"));
        pm.addPermission(new Permission("couriernew.shred"));
        pm.addPermission(new Permission("couriernew.shredall"));
        pm.addPermission(new Permission("couriernew.help"));
        pm.addPermission(new Permission("couriernew.reload"));
        
        CommandExec commandExec = new CommandExec();
        
        getCommand("letter").setExecutor(commandExec);
        getCommand("post").setExecutor(commandExec);
        getCommand("shred").setExecutor(commandExec);
        getCommand("shredall").setExecutor(commandExec);
        getCommand("unread").setExecutor(commandExec);
        getCommand("couriernew").setExecutor(commandExec);

        getCommand("couriernew").setTabCompleter(new CommandTabComplete());
    }
    
    /**
     * nullify the plugin instance
     */
    public void onDisable() {
        Courier.getCouriers().keySet().forEach(Entity::remove);
        Outgoing.saveAll();
        plugin = null;
    }
}
