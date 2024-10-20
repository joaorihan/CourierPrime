package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.Message;
import com.joaorihan.courierprime.letter.LetterUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class InspectCommand extends AbstractCommand{


    public InspectCommand(){
        super("inspect",
                new String[]{"infoletter"},
                "Provides information about a certain letter.",
                "courierprime.inspect");
    }


    @Override
    public void execute(CommandSender sender, String[] args) {

        // Command checks
        if (!(sender instanceof Player player)) return;

        if (args.length != 0){
            player.sendMessage(Message.ERROR_UNKNOWN_ARGS);
            return;
        }

        if (!LetterUtil.isHoldingLetter(player)){
            player.sendMessage(Message.ERROR_NO_LETTER);
            return;
        }

        // Command execution
        player.sendMessage(Message.LETTER_BY.replace("$PLAYER$", LetterUtil.getLetterOwner(player)));

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
