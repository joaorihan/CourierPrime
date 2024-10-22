package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.letter.LetterManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AnonymousLetterCommand extends AbstractCommand{


    public AnonymousLetterCommand(){
        super("anonymousletter",
                new String[]{"anonletter"},
                "Compose a new anonymous letter with the specified message.",
                "courierprime.letter.anonymous");
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        // Command checks
        if (!(sender instanceof Player player))
            return;

        if (!player.hasPermission("courierprime.letter.anonymous")) {
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

        LetterManager.writeBook(player, builder.toString(), true);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
