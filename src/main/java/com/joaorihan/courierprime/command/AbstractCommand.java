package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.config.MessageManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Setter
public abstract class AbstractCommand extends BukkitCommand {

    @Getter
    private CourierPrime plugin;

    private MessageManager messageManager;

    public MessageManager getMessageManager() {
        CourierPrime currentPlugin = CourierPrime.getPlugin();
        if (currentPlugin != null && currentPlugin.getMessageManager() != null) {
            return currentPlugin.getMessageManager();
        }
        return messageManager;
    }

    public AbstractCommand(String command, String[] aliases, String description, String permission) {
        super(command);

        setPlugin(CourierPrime.getPlugin());
        setMessageManager(getPlugin().getMessageManager());

        this.setAliases(Arrays.asList(aliases));
        this.setDescription(description);
        this.setPermission(permission);
        this.setPermissionMessage(buildPermissionMessage());

        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap map = (CommandMap) field.get(Bukkit.getServer());
            // This is the single command registration strategy. plugin.yml keeps
            // general metadata and permissions only; CommandManager constructs
            // these implementations once during onEnable.
            map.register(command, "courierprime", this);
        } catch (NoSuchFieldException | IllegalAccessException e){
            e.printStackTrace();
        }


    }

    /**
     * Bukkit calls this method when it builds the automatic permission failure
     * response. Resolve it dynamically so a reload cannot leave the command
     * holding the old language's localized text.
     */
    @Override
    public String getPermissionMessage() {
        return buildPermissionMessage();
    }

    /**
     * Paper stores the automatic permission message as an Adventure component,
     * so merely overriding the legacy String getter is not enough on newer
     * APIs. Route the failure path through the current localized manager too.
     */
    @Override
    public boolean testPermission(org.bukkit.command.CommandSender target) {
        if (testPermissionSilent(target)) {
            return true;
        }

        String permissionMessage = buildPermissionMessage();
        if (permissionMessage != null && !permissionMessage.isEmpty()) {
            target.sendMessage(permissionMessage);
        }
        return false;
    }

    private String buildPermissionMessage() {
        MessageManager currentMessageManager = getMessageManager();
        if (currentMessageManager == null) {
            return super.getPermissionMessage();
        }
        return ChatColor.RED + currentMessageManager.getMessage(Message.ERROR_NO_PERMS, true);
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        execute(commandSender, strings);
        return true;
    }

    public abstract void execute(CommandSender sender, String[] args);

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return onTabComplete(sender, args);
    }

    public abstract List<String> onTabComplete(CommandSender sender, String[] args);

}
