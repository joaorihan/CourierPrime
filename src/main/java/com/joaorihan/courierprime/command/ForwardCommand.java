package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.letter.LetterSender;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class ForwardCommand extends AbstractCommand{

    private final LetterSender letterSender = getPlugin().getLetterManager().getLetterSender();

    public ForwardCommand(){
        super("forward",
                new String[]{"resend"},
                "Forwards a letter sent to you, to a specific player.",
                "courierprime.forward");
    }


    @Override
    public void execute(CommandSender sender, String[] args) {

        // Command checks
        if (!(sender instanceof Player player))
            return;

        if (!player.hasPermission("courierprime.forward")) {
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
            return;
        }

        if (args.length != 1){
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
            return;
        }

        // Command exec
        letterSender.forward(player, args[0]);

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> names = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()){
            names.add(player.getName());
        }
        return StringUtil.copyPartialMatches(args[0], names, new ArrayList<>());
    }
}
