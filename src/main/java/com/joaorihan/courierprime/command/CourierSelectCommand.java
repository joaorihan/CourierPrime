package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.MainConfig;
import com.joaorihan.courierprime.config.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CourierSelectCommand extends AbstractCommand{


    public CourierSelectCommand() {
        super("courier",
                new String[]{},
                "Change a player's currently selected courier",
                "courierprime.courier.select");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        // Command checks
        if (!(sender instanceof Player player))
            return;

        if (!player.hasPermission("courierprime.courier.select")) {
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
            return;
        }

        if (args.length < 2){
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
            return;
        }

        // /courier select <entity>
        if (args[0].equalsIgnoreCase("select")) {
            EntityType courierType;
            try {
                courierType = EntityType.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_ENTITY_NOT_FOUND, true));
                return;
            }

            // if the courier selected is not enabled on the config
            if (!MainConfig.getEnabledCourierTypes().contains(courierType)){
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_ENTITY_NOT_FOUND, true));
                return;
            }

            getPlugin().getCourierSelectManager().setActiveCourier(player.getUniqueId(), courierType);
        }

        // /courier set <player> <entity>
        // THIS BYPASSES THE ENABLED COURIERS CONFIG
        if (args[0].equalsIgnoreCase("set")){
            if (player.hasPermission("courierprime.admin")) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
                return;
            }

            EntityType courierType;
            Player target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_PLAYER_NO_EXIST));
                return;
            }

            try {
                courierType = EntityType.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_ENTITY_NOT_FOUND, true));
                return;
            }
            getPlugin().getCourierSelectManager().setActiveCourier(target.getUniqueId(), courierType);
        }



    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("select", "set"), new ArrayList<>());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("select")) {
                return MainConfig.getEnabledCourierTypes().stream().map(Enum::name).collect(Collectors.toList());
            }

            if (args[0].equalsIgnoreCase("set")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            }
        }

        if (args.length == 3) {
            return MainConfig.getEnabledCourierTypes().stream().map(Enum::name).collect(Collectors.toList());
        }

        return List.of();
    }
}
