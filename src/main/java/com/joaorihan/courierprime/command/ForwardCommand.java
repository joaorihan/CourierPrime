package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.letter.LetterSender;
import com.joaorihan.courierprime.letter.LetterUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class ForwardCommand extends AbstractCommand{

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

        ItemStack letter = player.getInventory().getItemInMainHand();
        if (LetterUtil.isHoldingLetter(player) && !LetterUtil.wasAlreadyForwarded(letter)) {
            if (!player.hasPermission("courierprime.post.one")) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
                return;
            }

            if (!isResolvableRecipient(player, args[0])) {
                return;
            }
        }

        // Command exec
        getPlugin().getLetterManager().getLetterSender().forward(player, args[0]);

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> names = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()){
            names.add(player.getName());
        }
        String partial = args.length == 0 ? "" : args[0];
        return StringUtil.copyPartialMatches(partial, names, new ArrayList<>());
    }

    private boolean isResolvableRecipient(Player sender, String recipient) {
        String trimmedRecipient = recipient == null ? "" : recipient.trim();
        OfflinePlayer target = trimmedRecipient.isEmpty() ? null : Bukkit.getOfflinePlayer(trimmedRecipient);
        if (target == null || target.getName() == null
                || (!target.isOnline() && !target.hasPlayedBefore())) {
            sender.sendMessage(getMessageManager().getMessage(Message.ERROR_PLAYER_NO_EXIST, true)
                    .replace("$PLAYER$", trimmedRecipient));
            return false;
        }
        return true;
    }
}
