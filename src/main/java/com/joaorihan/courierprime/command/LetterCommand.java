package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.letter.LetterUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
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
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
            return;
        }

        if (args.length < 1) {
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_MSG, true));
            return;
        }

        // Command exec
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg).append(" ");
        }

        // Checks if the command was used for a new letter or a letter edit
        if (LetterUtil.isHoldingOwnLetter(player) && !LetterUtil.wasAlreadySent(player.getInventory().getItemInMainHand())){
            getPlugin().getLetterManager().editBook(player, builder.toString());
            return;
        }

        // If not, writes a new letter
        getPlugin().getLetterManager().writeBook(player, builder.toString(), false);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> names = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()){
            names.add(player.getName());
        }
        return StringUtil.copyPartialMatches(args[1], names, new ArrayList<>());
    }
}
