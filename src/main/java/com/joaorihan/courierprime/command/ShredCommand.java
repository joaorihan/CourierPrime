package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.Message;
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
            player.sendMessage(Message.ERROR_NO_PERMS);
            return;
        }

        if (args.length > 1){
            player.sendMessage(Message.ERROR_UNKNOWN_ARGS);
            return;
        }

        // Command exec
        if (args.length == 0){
            LetterManager.delete(player);
            System.out.println("Letter deleted");
            return;
        }

        // Shred all exec
        // Shred all checks
        if (!args[0].equalsIgnoreCase("all")){
            player.sendMessage(Message.ERROR_UNKNOWN_ARGS);
            return;
        }

        if (!player.hasPermission("courierprime.shred.all")) {
            player.sendMessage(Message.ERROR_NO_PERMS);
            return;
        }

        // Shred all exec
        LetterManager.deleteAll(player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
