package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.ConfigManager;
import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.config.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminCommand extends AbstractCommand{


    public AdminCommand(){
        super("courieradmin",
                new String[]{"cpradmin", "ca"},
                "Command with server administrator tools",
                "courierprime.admin");
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player))
            return;

        if (!player.hasPermission("courierprime.admin")) return;

        if (args.length == 0) {
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
            return;
        }

        // reload
        if (args[0].equals("reload")){

            if (!player.hasPermission("courierprime.reload")) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
                return;
            }

            getPlugin().getOutgoingManager().saveAll();
            Courier.getCouriers().keySet().forEach(Entity::remove);
            Courier.getCouriers().clear();


            //todo fix reload messages
            getPlugin().setConfigManager(new ConfigManager(getPlugin()));



            MainConfig.load();
            getPlugin().getOutgoingManager().loadAll();
            player.sendMessage(getMessageManager().getMessage(Message.SUCCESS_RELOADED, true));

            return;
        }

        // block / unblock

        if (args.length < 2){
            player.sendMessage(getPlugin().getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(getPlugin().getMessageManager().getMessage(Message.ERROR_TARGET_NOT_FOUND, true));
            return;
        }

        if (args[0].equals("block")){

            if (getPlugin().getLetterManager().isInBlockedMode(target)){
                player.sendMessage(getPlugin().getMessageManager().getMessage(Message.ERROR_ALREADY_BLOCKED, true));
                return;
            }

            getPlugin().getLetterManager().addBlockedPlayer(target);
            player.sendMessage(getPlugin().getMessageManager().getMessage(Message.SUCCESS_ADD_BLOCKED, true).replace("$PLAYER$", target.getName()));
            return;
        }

        if (args[0].equals("unblock")){
            if (!getPlugin().getLetterManager().isInBlockedMode(target)){
                player.sendMessage(getPlugin().getMessageManager().getMessage(Message.ERROR_ALREADY_UNBLOCKED, true));
                return;
            }

            getPlugin().getLetterManager().removeBlockedPlayer(target);
            player.sendMessage(getPlugin().getMessageManager().getMessage(Message.SUCCESS_REMOVE_BLOCKED, true).replace("$PLAYER$", target.getName()));
            return;
        }

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {

        if (args.length == 1){
            return List.of("reload");
        }

        return List.of();
    }
}
