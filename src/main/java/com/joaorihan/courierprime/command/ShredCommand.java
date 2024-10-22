package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.letter.LetterManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ShredCommand extends AbstractCommand{


    public ShredCommand(){
        super("shred",
                new String[]{},
                "Delete the letter in your hand.",
                "courierprime.shred");
    }


    @Override
    public void execute(CommandSender sender, String[] args) {

        // Command checks
        if (!(sender instanceof Player player))
            return;

        if (!player.hasPermission("courierprime.shred")) {
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
            return;
        }

        if (args.length > 1){
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
            return;
        }

        // Command exec
        if (args.length == 0){
            getPlugin().getLetterManager().delete(player);
            return;
        }

        // Shred all exec
        // Shred all checks
        if (!args[0].equalsIgnoreCase("all")){
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
            return;
        }

        if (!player.hasPermission("courierprime.shred.all")) {
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
            return;
        }

        // Shred all exec
        getPlugin().getLetterManager().deleteAll(player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
