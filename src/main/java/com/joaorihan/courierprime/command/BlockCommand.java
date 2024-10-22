package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.config.CourierOptions;
import com.joaorihan.courierprime.letter.LetterManager;
import com.joaorihan.courierprime.letter.Outgoing;
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
            player.sendMessage(Message.ERROR_UNKNOWN_ARGS);
            return;
        }


        // Command exec
        if (LetterManager.addBlockedPlayer(player)){
            player.sendMessage(Message.ENABLE_LETTERS);

            // Checks if the player has any pending letters
            if (!(Outgoing.getOutgoing().containsKey(player.getUniqueId()) && Outgoing.getOutgoing().get(player.getUniqueId()).size() > 0)) {
                return;
            }

            // If so, sends a courier
            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                player.sendMessage(Message.SUCCESS_EXTRA_DELIVERIES);
                Courier.spawn(player);
            }, CourierOptions.RECEIVE_DELAY);

        } else {
            LetterManager.removeBlockedPlayer(player);
            player.sendMessage(Message.DISABLE_LETTERS);
        }


    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
