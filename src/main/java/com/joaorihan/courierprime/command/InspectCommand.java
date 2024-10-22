package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.Message;
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
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
            return;
        }

        if (!LetterUtil.isHoldingLetter(player)){
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_LETTER, true));

            return;
        }

        // Command execution
        player.sendMessage(getMessageManager().getMessage(Message.LETTER_BY, true).replace("$PLAYER$", LetterUtil.getLetterOwner(player)));

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
