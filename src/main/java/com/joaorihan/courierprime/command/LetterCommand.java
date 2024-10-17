package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.Message;
import com.joaorihan.courierprime.letter.LetterManager;
import com.joaorihan.courierprime.letter.LetterUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class LetterCommand extends AbstractCommand{


    public LetterCommand(){
        super("letter",
                new String[]{},
                "Compose a new letter with the specified message.",
                "courierprime.letter");
    }


    @Override
    public void execute(CommandSender sender, String[] args) {

        // Command checks
        if (!(sender instanceof Player player))
            return;

        if (!player.hasPermission("courierprime.letter")) {
            player.sendMessage(Message.ERROR_NO_PERMS);
            return;
        }

        if (args.length < 1) {
            player.sendMessage(Message.ERROR_NO_MSG);
            return;
        }

        // Command exec
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg).append(" ");
        }

        // Checks if the command was used for a new letter or a letter edit
        if (LetterUtil.isHoldingOwnLetter(player) && !LetterUtil.wasAlreadySent(player.getInventory().getItemInMainHand())){
            LetterManager.editBook(player, builder.toString());
            return;
        }

        // If not, writes a new letter
        LetterManager.writeBook(player, builder.toString());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
