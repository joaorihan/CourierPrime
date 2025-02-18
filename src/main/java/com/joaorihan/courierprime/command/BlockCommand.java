package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.config.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BlockCommand extends AbstractCommand{


    public BlockCommand(){
        super("blockletters",
                new String[]{},
                "Blocks the courier from arriving for the player.",
                "courierprime.block");
    }


    @Override
    public void execute(CommandSender sender, String[] args) {

        // Command checks
        if (!(sender instanceof Player player)) return;

        if (args.length != 0){
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
            return;
        }


        // Command exec
        if (getPlugin().getLetterManager().addBlockedPlayer(player)){
            player.sendMessage(getMessageManager().getMessage(Message.DISABLE_LETTERS, true));

            // Checks if the player has any pending letters
            if (!(getPlugin().getOutgoingManager().getOutgoing().containsKey(player.getUniqueId()) && getPlugin().getOutgoingManager().getOutgoing().get(player.getUniqueId()).size() > 0)) {
                return;
            }

            // If so, sends a courier
            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                player.sendMessage(getMessageManager().getMessage(Message.SUCCESS_EXTRA_DELIVERIES, true));
                new Courier(player);
            }, MainConfig.getReceiveDelay());

        } else {
            getPlugin().getLetterManager().removeBlockedPlayer(player);
            player.sendMessage(getMessageManager().getMessage(Message.ENABLE_LETTERS, true));
        }


    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
