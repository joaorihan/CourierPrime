package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.MainConfig;
import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.courier.CourierType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CourierSelectCommand extends AbstractCommand{


    public CourierSelectCommand() {
        super("courier",
                new String[]{},
                "Change a player's currently selected courier",
                null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        // Command checks
        if (!(sender instanceof Player player))
            return;

        boolean isAdmin = player.hasPermission("courierprime.admin");
        if (!isAdmin && !player.hasPermission("courierprime.courier.select")) {
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
            return;
        }

        if (args.length < 2){
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
            return;
        }

        // /courier select <entity>
        if (args[0].equalsIgnoreCase("select")) {
            if (!player.hasPermission("courierprime.courier.select")) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
                return;
            }

            CourierType courierType = CourierType.parse(args[1]);

            if (courierType == null) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_ENTITY_NOT_FOUND, true));
                return;
            }

            // Check if the courier type is enabled in config
            boolean isEnabled = MainConfig.getEnabledCourierTypes().stream()
                    .anyMatch(type -> type.toString().equalsIgnoreCase(args[1]));

            if (!isEnabled) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_ENTITY_NOT_FOUND, true));
                return;
            }

            // Check if custom entity plugin is available
            if (courierType.isMythicMobs() && !getPlugin().getCustomEntityManager().isMythicMobsAvailable()) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_PLUGIN_NOT_AVAILABLE, true)
                        .replace("$PLUGIN$", "MythicMobs"));
                return;
            }

            if (courierType.isModelEngine() && !getPlugin().getCustomEntityManager().isModelEngineAvailable()) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_PLUGIN_NOT_AVAILABLE, true)
                        .replace("$PLUGIN$", "ModelEngine"));
                return;
            }

            if (!getPlugin().getCustomEntityManager().isEntityAvailable(courierType)) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_ENTITY_NOT_FOUND, true));
                return;
            }

            getPlugin().getCourierSelectManager().setActiveCourier(player.getUniqueId(), courierType);
            player.sendMessage(getMessageManager().getMessage(Message.SUCCESS_COURIER_SELECTED, true)
                    .replace("$COURIER$", courierType.getDisplayName()));
            return;
        }

        // /courier set <player> <entity>
        // THIS BYPASSES THE ENABLED COURIERS CONFIG
        if (args[0].equalsIgnoreCase("set")){
            if (!isAdmin) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
                return;
            }

            if (args.length < 3) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
                return;
            }

            Player target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_PLAYER_NO_EXIST, true));
                return;
            }

            CourierType courierType = CourierType.parse(args[2]);

            if (courierType == null) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_ENTITY_NOT_FOUND, true));
                return;
            }

            if (courierType.isMythicMobs() && !getPlugin().getCustomEntityManager().isMythicMobsAvailable()) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_PLUGIN_NOT_AVAILABLE, true)
                        .replace("$PLUGIN$", "MythicMobs"));
                return;
            }

            if (courierType.isModelEngine() && !getPlugin().getCustomEntityManager().isModelEngineAvailable()) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_PLUGIN_NOT_AVAILABLE, true)
                        .replace("$PLUGIN$", "ModelEngine"));
                return;
            }

            if (!getPlugin().getCustomEntityManager().isEntityAvailable(courierType)) {
                player.sendMessage(getMessageManager().getMessage(Message.ERROR_ENTITY_NOT_FOUND, true));
                return;
            }

            getPlugin().getCourierSelectManager().setActiveCourier(target.getUniqueId(), courierType);
            player.sendMessage(getMessageManager().getMessage(Message.SUCCESS_COURIER_SET, true)
                    .replace("$PLAYER$", target.getName())
                    .replace("$COURIER$", courierType.getDisplayName()));
            return;
        }

        player.sendMessage(getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("select", "set"), new ArrayList<>());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("select")) {
                return StringUtil.copyPartialMatches(args[1], MainConfig.getEnabledCourierTypeStrings(), new ArrayList<>());
            }

            if (args[0].equalsIgnoreCase("set")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            return StringUtil.copyPartialMatches(args[2], MainConfig.getEnabledCourierTypeStrings(), new ArrayList<>());
        }

        return List.of();
    }
}
