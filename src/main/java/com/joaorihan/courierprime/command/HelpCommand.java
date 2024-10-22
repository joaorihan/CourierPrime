package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HelpCommand extends AbstractCommand{


    public HelpCommand(){
        super("courierprime",
                new String[]{"cpr"},
                "Main command of CourierPrime",
                "courierprime.help");
    }


    @Override
    public void execute(CommandSender sender, String[] args) {

        // Command checks
        if (!(sender instanceof Player player))
            return;

        if (args.length != 1) {
            player.sendMessage(Message.ERROR_UNKNOWN_ARGS);
            return;
        }

        if (args[0].equalsIgnoreCase("help")) {
            if (player.hasPermission("courierprime.help")) {
                player.sendMessage(getPlugin().getConfigManager().getHelpMessage());
            } else
                player.sendMessage(Message.ERROR_NO_PERMS);
        } else {
            player.sendMessage(Message.ERROR_UNKNOWN_ARGS);
        }

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
