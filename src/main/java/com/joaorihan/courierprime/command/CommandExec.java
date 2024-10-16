package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.letter.LetterManager;
import com.joaorihan.courierprime.letter.LetterSender;
import com.joaorihan.courierprime.letter.Outgoing;
import com.joaorihan.courierprime.Config;
import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.Message;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.courier.CourierOptions;
import com.joaorihan.courierprime.letter.LetterUtil;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

/**
 * Command class, runs commands if not in blocked worlds or gamemodes.
 *
 * @author Jeremy Noesen
 */
public class CommandExec implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {

            Player player = (Player) sender;
            Set<GameMode> modes = CourierOptions.BLOCKED_GAMEMODES;
            Set<World> worlds = CourierOptions.BLOCKED_WORLDS;

            if (!worlds.contains(player.getWorld()) && !modes.contains(player.getGameMode())) {

                switch (label.toLowerCase()) {

                    case "letter":
                        if (player.hasPermission("couriernew.letter")) {
                            if (args.length >= 1) {
                                StringBuilder sb = new StringBuilder();
                                for (String arg : args) {
                                    sb.append(arg).append(" ");
                                }
                                if (LetterUtil.isHoldingOwnLetter(player) &&
                                        !LetterUtil.wasAlreadySent(player.getInventory().getItemInMainHand()))
                                    LetterManager.editBook(player, sb.toString());
                                else LetterManager.writeBook(player, sb.toString());
                            } else
                                player.sendMessage(Message.ERROR_NO_MSG);
                        } else
                            player.sendMessage(Message.ERROR_NO_PERMS);
                        break;

                    case "post":
                        if (args.length == 1) {
                            if (player.hasPermission("couriernew.post.one") || player.hasPermission("couriernew" +
                                    ".post.multiple") ||
                                    player.hasPermission("couriernew.post.allonline") || player.hasPermission("couriernew" +
                                    ".post.all")) {
                                LetterSender.send(player, args[0]);
                            } else
                                player.sendMessage(Message.ERROR_NO_PERMS);
                        } else
                            player.sendMessage(Message.ERROR_UNKNOWN_ARGS);
                        break;

                    case "shred":
                        if (player.hasPermission("couriernew.shred")) {
                            LetterManager.delete(player);
                        } else
                            player.sendMessage(Message.ERROR_NO_PERMS);
                        break;

                    case "shredall":
                        if (player.hasPermission("couriernew.shredall")) {
                            LetterManager.deleteAll(player);
                        } else
                            player.sendMessage(Message.ERROR_NO_PERMS);
                        break;

                    case "unread":
                        if (player.hasPermission("couriernew.unread")) {
                            if (Outgoing.getOutgoing().containsKey(player.getUniqueId()) &&
                                    Outgoing.getOutgoing().get(player.getUniqueId()).size() > 0) {
                                player.sendMessage(Message.SUCCESS_EXTRA_DELIVERIES);
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        Courier.spawn(player);
                                    }
                                }.runTaskLater(CourierPrime.getInstance(), CourierOptions.RECEIVE_DELAY);
                            } else {
                                player.sendMessage(Message.ERROR_NO_MAIL);
                            }
                        } else
                            player.sendMessage(Message.ERROR_NO_PERMS);
                        break;

                    case "couriernew":
                    case "cn":
                        if (args.length == 1) {
                            switch (args[0].toLowerCase()) {
                                case "help":
                                    if (player.hasPermission("couriernew.help")) {
                                        player.sendMessage(Message.getHelpMessage(player));
                                    } else
                                        player.sendMessage(Message.ERROR_NO_PERMS);
                                    break;

                                case "reload":
                                    if (player.hasPermission("couriernew.reload")) {
                                        Outgoing.saveAll();
                                        Courier.getCouriers().keySet().forEach(Entity::remove);
                                        Courier.getCouriers().clear();
                                        Config.getMainConfig().reloadConfig();
                                        Config.getOutgoingConfig().reloadConfig();
                                        Config.getMessageConfig().reloadConfig();
                                        CourierOptions.load();
                                        Outgoing.loadAll();
                                        Message.reloadMessages();
                                        player.sendMessage(Message.SUCCESS_RELOADED);
                                    } else
                                        player.sendMessage(Message.ERROR_NO_PERMS);
                                    break;

                                default:
                                    player.sendMessage(Message.ERROR_UNKNOWN_ARGS);
                            }
                        } else
                            player.sendMessage(Message.ERROR_UNKNOWN_ARGS);
                        break;

                    default:
                        player.sendMessage(Message.ERROR_UNKNOWN_ARGS);

                }
            } else {
                player.sendMessage(Message.ERROR_WORLD);
            }
            return true;
        }
        return false;
    }
}
