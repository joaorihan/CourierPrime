package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.ConfigManager;
import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.config.CourierOptions;
import com.joaorihan.courierprime.letter.Outgoing;
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

        if (args[0].equals("reload")){

            if (!player.hasPermission("courierprime.reload")) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
                return;
            }

            Outgoing.saveAll();
            Courier.getCouriers().keySet().forEach(Entity::remove);
            Courier.getCouriers().clear();

//            Config.getMainConfig().reloadConfig();
//            Config.getOutgoingConfig().reloadConfig();
//            Config.getMessageConfig().reloadConfig();

            getPlugin().setConfigManager(new ConfigManager(getPlugin()));



            CourierOptions.load();
            Outgoing.loadAll();
//            Message.reloadMessages();
            player.sendMessage(getMessageManager().getMessage(Message.SUCCESS_RELOADED, true));

            return;
        }

//        Player target = Bukkit.getPlayer(args[1]);
//        if (target == null) {
//            player.sendMessage(Utilities.pullMessage("target-not-found"));
//            return;
//        }
//
//        if (args[0].equals("block")){
//
//            if (getPlugin().getLetterManager().isInBlockedMode(target)){
//                player.sendMessage(Utilities.pullMessage("error-already-blocked"));
//                return;
//            }
//
//            getPlugin().getLetterManager().addBlockedPlayer(target);
//            return;
//        }
//
//        if (args[0].equals("unblock")){
//            if (!getPlugin().getLetterManager().isInBlockedMode(target)){
//                player.sendMessage(Utilities.pullMessage("error-already-unblocked"));
//                return;
//            }
//
//            getPlugin().getLetterManager().removeBlockedPlayer(target);
//            return;
//        }

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
