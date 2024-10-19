package com.joaorihan.courierprime.letter;

import org.apache.commons.text.WordUtils;
import org.bukkit.inventory.meta.BookMeta;
import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.Message;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.courier.CourierOptions;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Send the letters to players
 *
 * @author Jeremy Noesen
 */
public class LetterSender implements Listener {
    
    /**
     * Send a letter to a player. The letter will have the recipient added to the lore, preventing it from being sent
     * again. It also adds it to a yml file of letters to be received. If the player receiving is online, they may
     * receive their letter.
     *
     * @param sender    player sending the letter
     * @param recipient player(s) to receive the letter
     */
    public static void send(Player sender, String recipient) {
        if (LetterUtil.isHoldingOwnLetter(sender) &&
                !LetterUtil.wasAlreadySent(sender.getInventory().getItemInMainHand())) {

            ItemStack letter = sender.getInventory().getItemInMainHand();
            List<String> lore = createLetterLore(letter);

            Collection<OfflinePlayer> offlinePlayers = null;

            switch (recipient) {
                case "*":
                    offlinePlayers = handleAllOnline(sender, lore);
                    break;
                case "**":
                    offlinePlayers = handleAll(sender, lore);
                    break;
                default:
                    if (recipient.contains(",")) {
                        offlinePlayers = handleMultipleRecipients(sender, recipient, lore);
                    } else {
                        offlinePlayers = handleSingleRecipient(sender, recipient, lore);
                    }
            }

            if (offlinePlayers != null && !offlinePlayers.isEmpty()) {
                sendLettersToPlayers(sender, letter, lore, offlinePlayers);
            }

        } else {
            handleLetterErrors(sender);
        }
    }

    private static List<String> createLetterLore(ItemStack letter) {
        List<String> lore = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(Message.DATE_TIME_FORMAT);
        String dateNow = formatter.format(currentDate.getTime());

        BookMeta letterMeta = (BookMeta) letter.getItemMeta();
        String wrapped = WordUtils.wrap(Message.unformat(letterMeta.getPage(1)), 30, "<split>", true);
        String[] lines = wrapped.split("<split>");
        lore.add("");
        lore.add(Message.PREVIEW_FORMAT + lines[0]);
        if (lines.length >= 2) lore.add(Message.PREVIEW_FORMAT + lines[1]);
        if (lines.length >= 3) lore.add(Message.PREVIEW_FORMAT + lines[2]);
        lore.add("");
        lore.add(Message.PREVIEW_FOOTER.replace("$DATE$", dateNow)
                .replace("$PAGES$", Integer.toString(letterMeta.getPages().size())));

        return lore;
    }

    private static Collection<OfflinePlayer> handleAllOnline(Player sender, List<String> lore) {
        if (sender.hasPermission("couriernew.post.allonline")) {
            lore.add("§T" + Message.LETTER_TO_ALLONLINE);
            Collection<OfflinePlayer> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            sender.sendMessage(Message.SUCCESS_SENT_ALLONLINE);
            return onlinePlayers;
        } else {
            sender.sendMessage(Message.ERROR_NO_PERMS);
            return null;
        }
    }

    private static Collection<OfflinePlayer> handleAll(Player sender, List<String> lore) {
        if (sender.hasPermission("couriernew.post.all")) {
            lore.add("§T" + Message.LETTER_TO_ALL);
            Collection<OfflinePlayer> allPlayers = new ArrayList<>(Arrays.asList(Bukkit.getOfflinePlayers()));
            sender.sendMessage(Message.SUCCESS_SENT_ALL);
            return allPlayers;
        } else {
            sender.sendMessage(Message.ERROR_NO_PERMS);
            return null;
        }
    }

    private static Collection<OfflinePlayer> handleMultipleRecipients(Player sender, String recipient, List<String> lore) {
        if (sender.hasPermission("couriernew.post.multiple")) {
            Collection<OfflinePlayer> offlinePlayers = new ArrayList<>();
            for (String recipients : recipient.split(",")) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(recipients);
                if (op == null) {
                    sender.sendMessage(Message.ERROR_PLAYER_NO_EXIST.replace("$PLAYER$", recipients));
                    return null;
                }
                offlinePlayers.add(op);
            }
            lore.add("§T" + Message.LETTER_TO_MULTIPLE);
            sender.sendMessage(Message.SUCCESS_SENT_MULTIPLE);
            return offlinePlayers;
        } else {
            sender.sendMessage(Message.ERROR_NO_PERMS);
            return null;
        }
    }

    private static Collection<OfflinePlayer> handleSingleRecipient(Player sender, String recipient, List<String> lore) {
        if (sender.hasPermission("couriernew.post.one")) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(recipient);
            if (op == null) {
                sender.sendMessage(Message.ERROR_PLAYER_NO_EXIST.replace("$PLAYER$", recipient));
                return null;
            }
            lore.add("§T" + Message.LETTER_TO_ONE.replace("$PLAYER$", op.getName()));
            sender.sendMessage(Message.SUCCESS_SENT_ONE.replace("$PLAYER$", op.getName()));
            return Collections.singletonList(op);
        } else {
            sender.sendMessage(Message.ERROR_NO_PERMS);
            return null;
        }
    }

    private static void sendLettersToPlayers(Player sender, ItemStack letter, List<String> lore, Collection<OfflinePlayer> offlinePlayers) {
        BookMeta letterMeta = (BookMeta) letter.getItemMeta();
        letterMeta.setLore(lore);
        letter.setItemMeta(letterMeta);

        for (OfflinePlayer op : offlinePlayers) {
            if (offlinePlayers.size() > 1 && op.equals(sender)) continue;

            Outgoing.getOutgoing().computeIfAbsent(op.getUniqueId(), k -> new LinkedList<>()).add(new ItemStack(letter));

            new BukkitRunnable() {
                @Override
                public void run() {
                    Courier.spawn((Player) op);
                }
            }.runTaskLater(CourierPrime.getInstance(), CourierOptions.RECEIVE_DELAY);
        }

        sender.getInventory().getItemInMainHand().setAmount(0);
    }

    private static void handleLetterErrors(Player sender) {
        if (LetterUtil.isHoldingOwnLetter(sender)) {
            sender.sendMessage(Message.ERROR_SENT_BEFORE);
        } else if (LetterUtil.isHoldingLetter(sender)) {
            sender.sendMessage(Message.ERROR_NOT_YOUR_LETTER);
        } else {
            sender.sendMessage(Message.ERROR_NO_LETTER);
        }
    }

    
    /**
     * When clicking the courier, retrieve the letters from the file and give all of them to the player. If they have
     * space in their inventory, give them all, starting with their hand if they aren't holding anything. Letters not
     * taken will be delivered later.
     *
     * @param recipient player receiving the mail
     */
    public static void receive(Player recipient) {
        if (Outgoing.getOutgoing().containsKey(recipient.getUniqueId()) && Outgoing.getOutgoing().get(recipient.getUniqueId()).size() > 0) {
            LinkedList<ItemStack> letters = Outgoing.getOutgoing().get(recipient.getUniqueId());

            while (!letters.isEmpty()) {
                if (recipient.getInventory().firstEmpty() < 0) {
                    recipient.sendMessage(Message.ERROR_CANT_HOLD);
                    break;
                } else if (recipient.getInventory().getItemInMainHand().getAmount() == 0) {
                    recipient.getInventory().setItemInMainHand(letters.pop());
                } else {
                    recipient.getInventory().addItem(letters.pop());
                }
            }
            
            recipient.updateInventory();
        }
    }
    

}
