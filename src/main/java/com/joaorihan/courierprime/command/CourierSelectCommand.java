package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.MainConfig;
import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.courier.CourierType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CourierSelectCommand extends AbstractCommand {

    public CourierSelectCommand() {
        super("courier",
                new String[]{},
                "Change a player's currently selected courier",
                null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
            return;
        }

        if (args[0].equalsIgnoreCase("select")) {
            executeSelect(sender, args);
            return;
        }

        if (args[0].equalsIgnoreCase("set")) {
            executeSet(sender, args);
            return;
        }

        sender.sendMessage(getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
    }

    private void executeSelect(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return;
        }
        if (!player.hasPermission("courierprime.courier.select")) {
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
            return;
        }

        CourierType courierType = CourierType.parse(args[1]);
        if (courierType == null || !MainConfig.isCourierTypeEnabled(courierType)) {
            player.sendMessage(getMessageManager().getMessage(Message.ERROR_ENTITY_NOT_FOUND, true));
            return;
        }
        if (!validateProvider(player, courierType)) {
            return;
        }

        getPlugin().getCourierSelectManager().setActiveCourier(player.getUniqueId(), courierType);
        player.sendMessage(getMessageManager().getMessage(Message.SUCCESS_COURIER_SELECTED, true)
                .replace("$COURIER$", courierType.getDisplayName()));
    }

    private void executeSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("courierprime.admin")) {
            sender.sendMessage(getMessageManager().getMessage(Message.ERROR_NO_PERMS, true));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(getMessageManager().getMessage(Message.ERROR_UNKNOWN_ARGS, true));
            return;
        }

        OfflinePlayer target = resolveKnownPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(getMessageManager().getMessage(Message.ERROR_PLAYER_NO_EXIST, true)
                    .replace("$PLAYER$", args[1]));
            return;
        }

        CourierType courierType = CourierType.parse(args[2]);
        if (courierType == null) {
            sender.sendMessage(getMessageManager().getMessage(Message.ERROR_ENTITY_NOT_FOUND, true));
            return;
        }
        if (!validateProvider(sender, courierType)) {
            return;
        }

        getPlugin().getCourierSelectManager().setActiveCourier(target.getUniqueId(), courierType);
        sender.sendMessage(getMessageManager().getMessage(Message.SUCCESS_COURIER_SET, true)
                .replace("$PLAYER$", target.getName())
                .replace("$COURIER$", courierType.getDisplayName()));
    }

    private boolean validateProvider(CommandSender sender, CourierType courierType) {
        if (courierType.isMythicMobs()
                && !getPlugin().getCustomEntityManager().isMythicMobsAvailable()) {
            sender.sendMessage(getMessageManager().getMessage(Message.ERROR_PLUGIN_NOT_AVAILABLE, true)
                    .replace("$PLUGIN$", "MythicMobs"));
            return false;
        }

        if (courierType.isModelEngine()
                && !getPlugin().getCustomEntityManager().isModelEngineAvailable()) {
            sender.sendMessage(getMessageManager().getMessage(Message.ERROR_PLUGIN_NOT_AVAILABLE, true)
                    .replace("$PLUGIN$", "ModelEngine"));
            return false;
        }

        if (!getPlugin().getCustomEntityManager().isEntityAvailable(courierType)) {
            sender.sendMessage(getMessageManager().getMessage(Message.ERROR_ENTITY_NOT_FOUND, true));
            return false;
        }
        return true;
    }

    private OfflinePlayer resolveKnownPlayer(String name) {
        Player onlinePlayer = Bukkit.getPlayer(name);
        if (onlinePlayer != null) {
            return onlinePlayer;
        }

        OfflinePlayer candidate = Bukkit.getOfflinePlayer(name);
        if (isKnownOfflinePlayer(candidate)) {
            return candidate;
        }

        // Some server implementations return a synthetic OfflinePlayer from
        // getOfflinePlayer(String) even when a cached player with a different
        // case exists. Prefer the server's known-player cache in that case.
        for (OfflinePlayer knownPlayer : Bukkit.getOfflinePlayers()) {
            if (knownPlayer != null && knownPlayer.getName() != null
                    && knownPlayer.getName().equalsIgnoreCase(name)
                    && knownPlayer.hasPlayedBefore()) {
                return knownPlayer;
            }
        }
        return null;
    }

    private boolean isKnownOfflinePlayer(OfflinePlayer player) {
        return player != null && player.getName() != null && player.hasPlayedBefore();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("select", "set"), new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("select")) {
            if (!sender.hasPermission("courierprime.courier.select")) {
                return List.of();
            }
            return StringUtil.copyPartialMatches(args[1], MainConfig.getEnabledCourierTypeStrings(), new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            if (!sender.hasPermission("courierprime.admin")) {
                return List.of();
            }
            return StringUtil.copyPartialMatches(args[1], knownPlayerNames(), new ArrayList<>());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")
                && sender.hasPermission("courierprime.admin")) {
            return StringUtil.copyPartialMatches(
                    args[2], MainConfig.getAllSpawnableCourierTypeStrings(), new ArrayList<>());
        }

        return List.of();
    }

    private List<String> knownPlayerNames() {
        Set<String> names = new LinkedHashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null && player.getName() != null) {
                names.add(player.getName());
            }
        }
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (isKnownOfflinePlayer(player)) {
                names.add(player.getName());
            }
        }
        return new ArrayList<>(names);
    }
}
