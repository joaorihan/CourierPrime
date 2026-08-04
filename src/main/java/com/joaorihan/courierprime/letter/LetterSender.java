package com.joaorihan.courierprime.letter;

import com.joaorihan.courierprime.config.MessageManager;
import org.apache.commons.text.WordUtils;
import org.bukkit.inventory.meta.BookMeta;
import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.config.MainConfig;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Send the letters to players
 *
 * @author Jeremy Noesen
 */
public class LetterSender {

    private final CourierPrime plugin;
    private final MessageManager messageManager;


    public LetterSender(CourierPrime plugin){
         this.plugin = plugin;
         this.messageManager = plugin.getMessageManager();
    }


    /**
     * Send a letter to a player. The letter will have the recipient added to the lore, preventing it from being sent
     * again. It also adds it to a yml file of letters to be received. If the player receiving is online, they may
     * receive their letter.
     *
     * @param sender    player sending the letter
     * @param recipient player(s) to receive the letter
     */
    public void send(Player sender, String recipient) {
        if (LetterUtil.isHoldingOwnLetter(sender) &&
                !LetterUtil.wasAlreadySent(sender.getInventory().getItemInMainHand())) {

            ItemStack letter = sender.getInventory().getItemInMainHand();
            List<String> lore = createLetterLore(letter);

            Collection<OfflinePlayer> offlinePlayers;

            switch (recipient) {
                case "*":
                    offlinePlayers = handleAllOnline(sender, lore);
                    break;
                case "**":
                    offlinePlayers = handleAll(sender, lore);
                    break;
                default:
                    offlinePlayers = handleSingleRecipient(sender, recipient, lore);
            }

            if (offlinePlayers != null && !offlinePlayers.isEmpty()) {
                sendLettersToPlayers(sender, letter, lore, offlinePlayers, true);
            }

        } else {
            handleLetterErrors(sender);
        }
    }


    public void send(Player sender, String[] recipients) {
        if (LetterUtil.isHoldingOwnLetter(sender) &&
                !LetterUtil.wasAlreadySent(sender.getInventory().getItemInMainHand())) {

            ItemStack letter = sender.getInventory().getItemInMainHand();
            List<String> lore = createLetterLore(letter);

            Collection<OfflinePlayer> offlinePlayers;

            offlinePlayers = handleMultipleRecipients(sender, recipients, lore);

            if (offlinePlayers != null && !offlinePlayers.isEmpty()) {
                sendLettersToPlayers(sender, letter, lore, offlinePlayers, true);
            }

        } else {
            handleLetterErrors(sender);
        }
    }

    public void forward(Player sender, String recipient) {
        if (!LetterUtil.isHoldingLetter(sender)){
            sender.sendMessage(messageManager.getMessage(Message.ERROR_NO_LETTER));
            return;
        }

        ItemStack letter = sender.getInventory().getItemInMainHand();

        if (LetterUtil.wasAlreadyForwarded(letter)){
            sender.sendMessage(messageManager.getMessage(Message.ERROR_ALREADY_FORWARDED, true));
            return;
        }

        BookMeta letterMeta = (BookMeta) letter.getItemMeta();
        letterMeta.setGeneration(BookMeta.Generation.COPY_OF_ORIGINAL);
        letter.setItemMeta(letterMeta);

        List<String> lore = createLetterLore(letter);

        lore.add("");
        lore.add(messageManager.getMessage(Message.LETTER_FORWARDED_BY).replace("$PLAYER$", sender.getName()));

        var offlinePlayerRecipient = handleSingleRecipient(sender, recipient, lore);

        if (offlinePlayerRecipient == null || offlinePlayerRecipient.isEmpty()){
            return;
        }

        sendLettersToPlayers(sender, letter, lore, offlinePlayerRecipient, false);
    }

    private List<String> createLetterLore(ItemStack letter) {
        List<String> lore = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(messageManager.getMessage(Message.DATE_TIME_FORMAT));
        String dateNow = formatter.format(currentDate.getTime());

        BookMeta letterMeta = (BookMeta) letter.getItemMeta();
        String firstPage = letterMeta.getPageCount() > 0 ? letterMeta.getPage(1) : "";
        String wrapped = WordUtils.wrap(MessageManager.unformat(firstPage), 30, "<split>", true);
        String[] lines = wrapped.split("<split>");
        lore.add("");
        lore.add(messageManager.getMessage(Message.PREVIEW_FORMAT) + lines[0]);
        if (lines.length >= 2) lore.add(messageManager.getMessage(Message.PREVIEW_FORMAT) + lines[1]);
        if (lines.length >= 3) lore.add(messageManager.getMessage(Message.PREVIEW_FORMAT) + lines[2]);
        lore.add("");
        lore.add(messageManager.getMessage(Message.PREVIEW_FOOTER).replace("$DATE$", dateNow)
                .replace("$PAGES$", Integer.toString(letterMeta.getPages().size())));

        return lore;
    }

    private Collection<OfflinePlayer> handleAllOnline(Player sender, List<String> lore) {
        if (sender.hasPermission("courierprime.post.allonline")) {
            lore.add("§T" + Message.LETTER_TO_ALLONLINE);
            Collection<OfflinePlayer> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            sender.sendMessage(messageManager.getMessage(Message.SUCCESS_SENT_ALLONLINE, true));
            return onlinePlayers;
        } else {
            sender.sendMessage(messageManager.getMessage(Message.ERROR_NO_PERMS, true));
            return null;
        }
    }

    private Collection<OfflinePlayer> handleAll(Player sender, List<String> lore) {
        if (sender.hasPermission("courierprime.post.all")) {
            lore.add("§T" + Message.LETTER_TO_ALL);
            Collection<OfflinePlayer> allPlayers = new ArrayList<>(Arrays.asList(Bukkit.getOfflinePlayers()));
            sender.sendMessage(messageManager.getMessage(Message.SUCCESS_SENT_ALL, true));
            return allPlayers;
        } else {
            sender.sendMessage(messageManager.getMessage(Message.ERROR_NO_PERMS, true));
            return null;
        }
    }

    private Collection<OfflinePlayer> handleMultipleRecipients(Player sender, String[] recipients, List<String> lore) {
        if (sender.hasPermission("courierprime.post.multiple")) {
            Set<OfflinePlayer> offlinePlayers = new LinkedHashSet<>();

            for (String recipient : recipients) {
                OfflinePlayer op = resolveRecipient(sender, recipient);
                if (op == null) {
                    return null;
                }
                offlinePlayers.add(op);
            }

            if (offlinePlayers.isEmpty()) {
                return null;
            }

            lore.add("§T" + messageManager.getMessage(Message.LETTER_TO_MULTIPLE));
            sender.sendMessage(messageManager.getMessage(Message.SUCCESS_SENT_MULTIPLE, true));
            return offlinePlayers;
        } else {
            sender.sendMessage(messageManager.getMessage(Message.ERROR_NO_PERMS, true));
            return null;
        }
    }

    private Collection<OfflinePlayer> handleSingleRecipient(Player sender, String recipient, List<String> lore) {
        if (sender.hasPermission("courierprime.post.one")) {
            OfflinePlayer op = resolveRecipient(sender, recipient);
            if (op == null) {
                return null;
            }
            lore.add("§T" + messageManager.getMessage(Message.LETTER_TO_ONE).replace("$PLAYER$", op.getName()));
            sender.sendMessage(messageManager.getMessage(Message.SUCCESS_SENT_ONE, true).replace("$PLAYER$", op.getName()));
            return Collections.singletonList(op);
        } else {
            sender.sendMessage(messageManager.getMessage(Message.ERROR_NO_PERMS, true));
            return null;
        }
    }

    private OfflinePlayer resolveRecipient(Player sender, String name) {
        String trimmedName = name == null ? "" : name.trim();
        if (trimmedName.isEmpty()) {
            sender.sendMessage(messageManager.getMessage(Message.ERROR_PLAYER_NO_EXIST, true)
                    .replace("$PLAYER$", String.valueOf(name)));
            return null;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(trimmedName);
        if (player.getName() == null || (!player.isOnline() && !player.hasPlayedBefore())) {
            sender.sendMessage(messageManager.getMessage(Message.ERROR_PLAYER_NO_EXIST, true)
                    .replace("$PLAYER$", trimmedName));
            return null;
        }
        return player;
    }

    private void sendLettersToPlayers(Player sender, ItemStack letter, List<String> lore, Collection<OfflinePlayer> offlinePlayers, boolean shouldRemoveItem) {
        BookMeta letterMeta = (BookMeta) letter.getItemMeta();
        letterMeta.setLore(lore);
        letter.setItemMeta(letterMeta);

        for (OfflinePlayer op : offlinePlayers) {
            if (offlinePlayers.size() > 1 && op.equals(sender)) continue;

            plugin.getOutgoingManager().getOutgoing().computeIfAbsent(op.getUniqueId(), k -> new LinkedList<>()).add(new ItemStack(letter));

            Player onlineRecipient = op.getPlayer();
            if (onlineRecipient != null && onlineRecipient.isOnline()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (onlineRecipient.isOnline()) {
                            new Courier(onlineRecipient);
                        }
                    }
                }.runTaskLater(CourierPrime.getPlugin(), MainConfig.getReceiveDelay());
            }
        }

        plugin.getOutgoingManager().saveAll();

        if (shouldRemoveItem)
            sender.getInventory().getItemInMainHand().setAmount(0);
    }

    private void handleLetterErrors(Player sender) {
        if (LetterUtil.isHoldingOwnLetter(sender)) {
            sender.sendMessage(messageManager.getMessage(Message.ERROR_SENT_BEFORE, true));
        } else if (LetterUtil.isHoldingLetter(sender)) {
            sender.sendMessage(messageManager.getMessage(Message.ERROR_NOT_YOUR_LETTER, true));
        } else {
            sender.sendMessage(messageManager.getMessage(Message.ERROR_NO_LETTER, true));
        }
    }

    
    /**
     * When clicking the courier, retrieve the letters from the file and give all of them to the player. If they have
     * space in their inventory, give them all, starting with their hand if they aren't holding anything. Letters not
     * taken will be delivered later.
     *
     * @param recipient player receiving the mail
     */
    public void receive(Player recipient) {
        if (plugin.getOutgoingManager().hasPendingLetters(recipient)) {
            LinkedList<ItemStack> letters = plugin.getOutgoingManager().getOutgoing().get(recipient.getUniqueId());

            while (!letters.isEmpty()) {
                ItemStack letter = letters.pop();
                if (recipient.getInventory().getItemInMainHand().getAmount() == 0) {
                    recipient.getInventory().setItemInMainHand(letter);
                } else if (recipient.getInventory().firstEmpty() < 0) {
                    letters.push(letter);
                    recipient.sendMessage(messageManager.getMessage(Message.ERROR_CANT_HOLD, true));
                    break;
                } else {
                    Map<Integer, ItemStack> leftovers = recipient.getInventory().addItem(letter);
                    if (!leftovers.isEmpty()) {
                        letters.push(letter);
                        recipient.sendMessage(messageManager.getMessage(Message.ERROR_CANT_HOLD, true));
                        break;
                    }
                }
            }
            
            recipient.updateInventory();
            plugin.getOutgoingManager().saveAll();
        }
    }
    

}
