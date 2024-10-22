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
@Getter
public abstract class AbstractCommand extends BukkitCommand {

    private CourierPrime plugin;

    private MessageManager messageManager;

    public AbstractCommand(String command, String[] aliases, String description, String permission) {
        super(command);

        setPlugin(CourierPrime.getPlugin());
        setMessageManager(getPlugin().getMessageManager());

        this.setAliases(Arrays.asList(aliases));
        this.setDescription(description);
        this.setPermission(permission);
        this.setPermissionMessage(ChatColor.RED + messageManager.getMessage(Message.ERROR_NO_PERMS, true));

        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap map = (CommandMap) field.get(Bukkit.getServer());
            map.register(command, "courierprime", this);
        } catch (NoSuchFieldException | IllegalAccessException e){
            e.printStackTrace();
        }


    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        execute(commandSender, strings);
        return false;
    }

    public abstract void execute(CommandSender sender, String[] args);

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return onTabComplete(sender, args);
    }

    public abstract List<String> onTabComplete(CommandSender sender, String[] args);

}