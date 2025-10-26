package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class PostCommand extends AbstractCommand{


    public PostCommand(){
        super("post",
                new String[]{"send"},
                "Send a letter to a specified player.",
                "courierprime.post.one");

        // placeholder fix for an exception
        Bukkit.getPluginManager().removePermission("courierprime.post.multiple");
        Bukkit.getPluginManager().removePermission("courierprime.post.allonline");
        Bukkit.getPluginManager().removePermission("courierprime.post.all");

        Bukkit.getPluginManager().addPermission(new Permission("courierprime.post.multiple"));
        Bukkit.getPluginManager().addPermission(new Permission("courierprime.post.allonline"));
        Bukkit.getPluginManager().addPermission(new Permission("courierprime.post.all"));
    }


    @Override
    public void execute(CommandSender sender, String[] args) {

        // Command checks
        if (!(sender instanceof Player player))
            return;

        if (!(player.hasPermission("courierprime.post.one") ||
                player.hasPermission("courierprime.post.multiple") ||
                player.hasPermission("courierprime.post.allonline") ||
                player.hasPermission("courierprime.post.all"))) {
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
            return;
        }

        if (args.length < 1){
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
            return;
        }

        // Command exec
        // Sending for a single player
        if (args.length == 1){
            getPlugin().getLetterManager().getLetterSender().send(player, args[0]);
            return;
        }

        // Sending for multiple players
        getPlugin().getLetterManager().getLetterSender().send(player, args);

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> names = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()){
            names.add(player.getName());
        }
        return StringUtil.copyPartialMatches(args[0], names, new ArrayList<>());
    }
}
