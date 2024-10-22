package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.letter.LetterSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PostCommand extends AbstractCommand{


    public PostCommand(){
        super("post",
                new String[]{"send"},
                "Send a letter to a specified player.",
                "courierprime.post");
    }


    @Override
    public void execute(CommandSender sender, String[] args) {

        // Command checks
        if (!(sender instanceof Player player))
            return;

        if (!(player.hasPermission("couriernew.post.one") ||
                player.hasPermission("couriernew.post.multiple") ||
                player.hasPermission("couriernew.post.allonline") ||
                player.hasPermission("couriernew.post.all"))) {
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
            return;
        }

        if (args.length != 1){
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
            return;
        }

        // Command exec
        getPlugin().getLetterManager().getLetterSender().send(player, args[0]);

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
