package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.config.MainConfig;
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
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
            return;
        }

        if (!(getPlugin().getOutgoingManager().getOutgoing().containsKey(player.getUniqueId()) && getPlugin().getOutgoingManager().getOutgoing().get(player.getUniqueId()).size() > 0)) {
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_MAIL, true));
            return;
        }

        // Command exec
        player.sendMessage(getMessageManager().getMessage(Message.SUCCESS_EXTRA_DELIVERIES, true));

        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> Courier.spawn(player), MainConfig.getReceiveDelay());


    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
