package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.Message;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.courier.CourierOptions;
import com.joaorihan.courierprime.letter.Outgoing;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class UnreadCommand extends AbstractCommand{


    public UnreadCommand(){
        super("unread",
                new String[]{"unreadletters"},
                "Retrieve unread mail, if any.",
                "courierprime.unread");
    }


    @Override
    public void execute(CommandSender sender, String[] args) {

        // Command checks
        if (!(sender instanceof Player player))
            return;

        if (!player.hasPermission("courierprime.unread")) {
            player.sendMessage(Message.ERROR_NO_PERMS);
            return;
        }

        if (!(Outgoing.getOutgoing().containsKey(player.getUniqueId()) && Outgoing.getOutgoing().get(player.getUniqueId()).size() > 0)) {
            player.sendMessage(Message.ERROR_NO_MAIL);
            return;
        }

        // Command exec
        player.sendMessage(Message.SUCCESS_EXTRA_DELIVERIES);

        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> Courier.spawn(player), CourierOptions.RECEIVE_DELAY);


//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                Courier.spawn(player);
//            }
//        }.runTaskLater(CourierPrime.getInstance(), CourierOptions.RECEIVE_DELAY);
//
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
