package com.joaorihan.courierprime.letter;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.config.MainConfig;
import com.joaorihan.courierprime.config.Message;
import com.joaorihan.courierprime.config.MessageManager;
import com.joaorihan.courierprime.courier.Courier;
import org.apache.commons.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        if (!canSendOwnLetter(sender)) {
            return;
        }

        List<String> recipients = splitRecipients(recipient);
        if (recipients.size() > 1) {
            send(sender, recipients.toArray(new String[0]));
            return;
        }

        String requestedRecipient = recipients.isEmpty() ? recipient : recipients.get(0);
        Collection<OfflinePlayer> offlinePlayers;
        Message successMessage;
        List<String> lore = createLetterLore(sender.getInventory().getItemInMainHand());

        if ("*".equals(requestedRecipient)) {
            offlinePlayers = handleAllOnline(sender);
            if (offlinePlayers == null || offlinePlayers.isEmpty()) {
                return;
            }
            lore.add(messageManager.getMessage(Message.LETTER_TO_ALLONLINE));
            successMessage = Message.SUCCESS_SENT_ALLONLINE;
        } else if ("**".equals(requestedRecipient)) {
            offlinePlayers = handleAll(sender);
            if (offlinePlayers == null || offlinePlayers.isEmpty()) {
                return;
            }
            lore.add(messageManager.getMessage(Message.LETTER_TO_ALL));
            successMessage = Message.SUCCESS_SENT_ALL;
        } else {
            if (!sender.hasPermission("courierprime.post.one")) {
                sender.sendMessage(messageManager.getMessage(Message.ERROR_NO_PERMS, true));
                return;
            }

            OfflinePlayer offlinePlayer = resolveRecipient(sender, requestedRecipient);
            if (offlinePlayer == null) {
                return;
            }

            offlinePlayers = Collections.singletonList(offlinePlayer);
            lore.add(messageManager.getMessage(Message.LETTER_TO_ONE)
                    .replace("$PLAYER$", offlinePlayer.getName()));
            successMessage = Message.SUCCESS_SENT_ONE;
        }

        ItemStack letter = sender.getInventory().getItemInMainHand();
        if (sendLettersToPlayers(sender, letter, lore, offlinePlayers, true, false)) {
            String success = messageManager.getMessage(successMessage, true);
            if (successMessage == Message.SUCCESS_SENT_ONE) {
                success = success.replace("$PLAYER$", offlinePlayers.iterator().next().getName());
            }
            sender.sendMessage(success);
        }
    }


    public void send(Player sender, String[] recipients) {
        if (!canSendOwnLetter(sender)) {
            return;
        }

        List<String> requestedRecipients = splitRecipients(recipients);
        if (requestedRecipients.isEmpty()) {
            sender.sendMessage(messageManager.getMessage(Message.ERROR_UNKNOWN_ARGS, true));
            return;
        }

        if (!sender.hasPermission("courierprime.post.multiple")) {
            sender.sendMessage(messageManager.getMessage(Message.ERROR_NO_PERMS, true));
            return;
        }

        Collection<OfflinePlayer> offlinePlayers = handleMultipleRecipients(sender, requestedRecipients);
        if (offlinePlayers == null || offlinePlayers.isEmpty()) {
            return;
        }

        ItemStack letter = sender.getInventory().getItemInMainHand();
        List<String> lore = createLetterLore(letter);
        lore.add(messageManager.getMessage(Message.LETTER_TO_MULTIPLE));

        if (sendLettersToPlayers(sender, letter, lore, offlinePlayers, true, false)) {
            sender.sendMessage(messageManager.getMessage(Message.SUCCESS_SENT_MULTIPLE, true));
        }
    }

    public void forward(Player sender, String recipient) {
        if (sender == null || !sender.hasPermission("courierprime.forward")) {
            if (sender != null) {
                sender.sendMessage(messageManager.getMessage(Message.ERROR_NO_PERMS, true));
            }
            return;
        }

        if (!LetterUtil.isHoldingLetter(sender)){
            sender.sendMessage(messageManager.getMessage(Message.ERROR_NO_LETTER));
            return;
        }

        ItemStack letter = sender.getInventory().getItemInMainHand();

        if (LetterUtil.wasAlreadyForwarded(letter)){
            sender.sendMessage(messageManager.getMessage(Message.ERROR_ALREADY_FORWARDED, true));
            return;
        }

        OfflinePlayer offlinePlayer = resolveRecipient(sender, recipient);
        if (offlinePlayer == null) {
            return;
        }

        List<String> lore = createLetterLore(letter);
        lore.add("");
        lore.add(messageManager.getMessage(Message.LETTER_FORWARDED_BY)
                .replace("$PLAYER$", sender.getName()));

        // The source item is only changed by sendLettersToPlayers after validation,
        // queue insertion, and persistence have succeeded. A failed lookup therefore
        // cannot change its generation or lore.
        sendLettersToPlayers(sender, letter, lore, Collections.singletonList(offlinePlayer), false, true);
    }

    private List<String> createLetterLore(ItemStack letter) {
        List<String> lore = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(messageManager.getMessage(Message.DATE_TIME_FORMAT));
        String dateNow = formatter.format(currentDate.getTime());

        BookMeta letterMeta = (BookMeta) letter.getItemMeta();
        String firstPage = letterMeta.getPageCount() > 0 ? letterMeta.getPage(1) : "";
        if (firstPage == null) {
            firstPage = "";
        }
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

    private Collection<OfflinePlayer> handleAllOnline(Player sender) {
        if (!sender.hasPermission("courierprime.post.allonline")) {
            sender.sendMessage(messageManager.getMessage(Message.ERROR_NO_PERMS, true));
            return null;
        }

        Map<UUID, OfflinePlayer> onlinePlayers = new LinkedHashMap<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            addUsableRecipient(onlinePlayers, onlinePlayer);
        }
        return onlinePlayers.values();
    }

    private Collection<OfflinePlayer> handleAll(Player sender) {
        if (!sender.hasPermission("courierprime.post.all")) {
            sender.sendMessage(messageManager.getMessage(Message.ERROR_NO_PERMS, true));
            return null;
        }

        Map<UUID, OfflinePlayer> allPlayers = new LinkedHashMap<>();
        for (OfflinePlayer offlinePlayer : Arrays.asList(Bukkit.getOfflinePlayers())) {
            addUsableRecipient(allPlayers, offlinePlayer);
        }
        return allPlayers.values();
    }

    private Collection<OfflinePlayer> handleMultipleRecipients(Player sender, List<String> recipients) {
        Map<UUID, OfflinePlayer> offlinePlayers = new LinkedHashMap<>();

        for (String recipient : recipients) {
            OfflinePlayer offlinePlayer = resolveRecipient(sender, recipient);
            if (offlinePlayer == null) {
                return null;
            }
            addUsableRecipient(offlinePlayers, offlinePlayer);
        }

        return offlinePlayers.values();
    }

    private OfflinePlayer resolveRecipient(Player sender, String name) {
        String trimmedName = name == null ? "" : name.trim();
        if (trimmedName.isEmpty()) {
            sender.sendMessage(messageManager.getMessage(Message.ERROR_PLAYER_NO_EXIST, true)
                    .replace("$PLAYER$", String.valueOf(name)));
            return null;
        }

        try {
            // Resolve online players first so a current player is never treated as a
            // synthetic offline profile created by getOfflinePlayer(String).
            Player onlinePlayer = Bukkit.getPlayer(trimmedName);
            if (onlinePlayer != null && hasUsableName(onlinePlayer.getName()) && onlinePlayer.getUniqueId() != null) {
                return onlinePlayer;
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(trimmedName);
            if (offlinePlayer != null
                    && hasUsableName(offlinePlayer.getName())
                    && offlinePlayer.getUniqueId() != null
                    && (offlinePlayer.isOnline() || offlinePlayer.hasPlayedBefore())) {
                return offlinePlayer;
            }
        } catch (RuntimeException ignored) {
            // Treat malformed or unsupported lookups as an unknown player. The sender
            // still receives the normal user-facing error below.
        }

        sender.sendMessage(messageManager.getMessage(Message.ERROR_PLAYER_NO_EXIST, true)
                .replace("$PLAYER$", trimmedName));
        return null;
    }

    private boolean hasUsableName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    private void addUsableRecipient(Map<UUID, OfflinePlayer> recipients, OfflinePlayer recipient) {
        if (recipient == null || recipient.getUniqueId() == null || !hasUsableName(recipient.getName())) {
            return;
        }
        try {
            if (!recipient.isOnline() && !recipient.hasPlayedBefore()) {
                return;
            }
        } catch (RuntimeException ignored) {
            return;
        }
        recipients.putIfAbsent(recipient.getUniqueId(), recipient);
    }

    private boolean sendLettersToPlayers(Player sender,
                                         ItemStack letter,
                                         List<String> lore,
                                         Collection<OfflinePlayer> offlinePlayers,
                                         boolean shouldRemoveItem,
                                         boolean shouldMarkAsForwarded) {
        ItemStack preparedLetter;
        ItemStack sourceSnapshot;
        try {
            preparedLetter = letter.clone();
            sourceSnapshot = letter.clone();
            if (!(preparedLetter.getItemMeta() instanceof BookMeta letterMeta)) {
                return false;
            }
            if (shouldMarkAsForwarded) {
                letterMeta.setGeneration(BookMeta.Generation.COPY_OF_ORIGINAL);
            }
            letterMeta.setLore(new ArrayList<>(lore));
            preparedLetter.setItemMeta(letterMeta);
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("Unable to prepare outgoing letter: " + exception.getMessage());
            return false;
        }

        Map<UUID, List<ItemStack>> pendingLetters = new LinkedHashMap<>();
        List<OfflinePlayer> queuedRecipients = new ArrayList<>();
        boolean skipSender = offlinePlayers.size() > 1;
        UUID senderUuid = sender.getUniqueId();

        for (OfflinePlayer offlinePlayer : offlinePlayers) {
            if (offlinePlayer == null || offlinePlayer.getUniqueId() == null) {
                continue;
            }
            if (skipSender && senderUuid != null && senderUuid.equals(offlinePlayer.getUniqueId())) {
                continue;
            }

            // Clone once per recipient so no recipient can mutate another recipient's
            // BookMeta or ItemStack through a shared object reference.
            pendingLetters.computeIfAbsent(offlinePlayer.getUniqueId(), ignored -> new ArrayList<>())
                    .add(preparedLetter.clone());
            queuedRecipients.add(offlinePlayer);
        }

        if (pendingLetters.isEmpty()) {
            return false;
        }

        OutgoingManager outgoingManager = plugin.getOutgoingManager();
        HashMap<UUID, LinkedList<ItemStack>> outgoing = outgoingManager.getOutgoing();
        Map<UUID, Integer> originalSizes = new LinkedHashMap<>();

        synchronized (outgoing) {
            try {
                for (Map.Entry<UUID, List<ItemStack>> entry : pendingLetters.entrySet()) {
                    LinkedList<ItemStack> letters = outgoing.computeIfAbsent(entry.getKey(), ignored -> new LinkedList<>());
                    originalSizes.put(entry.getKey(), letters.size());
                    letters.addAll(entry.getValue());
                }

                // Persist the queue before changing the source item. A failed lookup
                // or queue operation therefore leaves the held item untouched.
                outgoingManager.saveAll();

                BookMeta sourceMeta = (BookMeta) letter.getItemMeta();
                sourceMeta.setLore(new ArrayList<>(lore));
                if (shouldMarkAsForwarded) {
                    sourceMeta.setGeneration(BookMeta.Generation.COPY_OF_ORIGINAL);
                }
                letter.setItemMeta(sourceMeta);
                if (shouldRemoveItem) {
                    letter.setAmount(0);
                }
            } catch (RuntimeException exception) {
                rollbackQueuedLetters(outgoing, pendingLetters, originalSizes);
                try {
                    outgoingManager.saveAll();
                } catch (RuntimeException ignored) {
                    // Preserve the original failure in the log; the in-memory queue
                    // has still been rolled back as far as the map permits.
                }
                try {
                    letter.setItemMeta(sourceSnapshot.getItemMeta());
                    letter.setAmount(sourceSnapshot.getAmount());
                } catch (RuntimeException ignored) {
                    // The source was already a valid BookMeta item; if a host inventory
                    // rejects restoration, retain the original queue failure evidence.
                }
                plugin.getLogger().warning("Unable to queue outgoing letters: " + exception.getMessage());
                return false;
            }
        }

        for (OfflinePlayer offlinePlayer : queuedRecipients) {
            scheduleDelivery(offlinePlayer);
        }
        return true;
    }

    private void rollbackQueuedLetters(HashMap<UUID, LinkedList<ItemStack>> outgoing,
                                       Map<UUID, List<ItemStack>> pendingLetters,
                                       Map<UUID, Integer> originalSizes) {
        for (Map.Entry<UUID, List<ItemStack>> entry : pendingLetters.entrySet()) {
            LinkedList<ItemStack> letters = outgoing.get(entry.getKey());
            if (letters == null) {
                continue;
            }

            int originalSize = originalSizes.getOrDefault(entry.getKey(), 0);
            while (letters.size() > originalSize) {
                letters.removeLast();
            }
            if (letters.isEmpty() && originalSize == 0) {
                outgoing.remove(entry.getKey());
            }
        }
    }

    private void scheduleDelivery(OfflinePlayer offlinePlayer) {
        Player onlineRecipient;
        try {
            onlineRecipient = offlinePlayer.getPlayer();
        } catch (RuntimeException ignored) {
            return;
        }
        if (onlineRecipient == null || !onlineRecipient.isOnline()) {
            return;
        }

        try {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (onlineRecipient.isOnline()) {
                        new Courier(onlineRecipient);
                    }
                }
            }.runTaskLater(plugin, MainConfig.getReceiveDelay());
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("Unable to schedule mail delivery for "
                    + offlinePlayer.getName() + ": " + exception.getMessage());
        }
    }

    private boolean canSendOwnLetter(Player sender) {
        if (sender != null
                && LetterUtil.isHoldingOwnLetter(sender)
                && !LetterUtil.wasAlreadySent(sender.getInventory().getItemInMainHand())) {
            return true;
        }

        if (sender != null) {
            handleLetterErrors(sender);
        }
        return false;
    }

    private List<String> splitRecipients(String recipient) {
        if (recipient == null) {
            return Collections.emptyList();
        }
        return splitRecipients(new String[]{recipient});
    }

    private List<String> splitRecipients(String[] recipients) {
        List<String> tokens = new ArrayList<>();
        if (recipients == null) {
            return tokens;
        }

        for (String recipient : recipients) {
            if (recipient == null) {
                continue;
            }
            for (String token : recipient.split("[,\\s]+")) {
                String trimmed = token.trim();
                if (!trimmed.isEmpty()) {
                    tokens.add(trimmed);
                }
            }
        }
        return tokens;
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
        if (recipient == null) {
            return;
        }

        OutgoingManager outgoingManager = plugin.getOutgoingManager();
        HashMap<UUID, LinkedList<ItemStack>> outgoing = outgoingManager.getOutgoing();
        boolean hadPendingLetters = false;

        synchronized (outgoing) {
            LinkedList<ItemStack> letters = outgoing.get(recipient.getUniqueId());
            if (letters != null && !letters.isEmpty()) {
                hadPendingLetters = true;
                boolean inventoryFull = false;

                while (!letters.isEmpty()) {
                    ItemStack letter = letters.pollFirst();
                    if (letter == null) {
                        plugin.getLogger().warning("Ignoring null pending outgoing item for " + recipient.getUniqueId());
                        continue;
                    }

                    ItemStack queuedCopy;
                    try {
                        queuedCopy = letter.clone();
                        if (queuedCopy.getType() == null || queuedCopy.getType().isAir() || queuedCopy.getAmount() <= 0) {
                            plugin.getLogger().warning("Ignoring malformed pending outgoing item for " + recipient.getUniqueId());
                            continue;
                        }
                    } catch (RuntimeException exception) {
                        plugin.getLogger().warning("Ignoring malformed pending outgoing item for "
                                + recipient.getUniqueId() + ": " + exception.getMessage());
                        continue;
                    }

                    ItemStack hand = recipient.getInventory().getItemInMainHand();
                    boolean handEmpty = hand == null || hand.getType() == null || hand.getType().isAir()
                            || hand.getAmount() <= 0;

                    if (handEmpty) {
                        try {
                            recipient.getInventory().setItemInMainHand(queuedCopy);
                            continue;
                        } catch (RuntimeException exception) {
                            letters.addFirst(queuedCopy);
                            plugin.getLogger().warning("Unable to place pending outgoing item in the recipient's hand: "
                                    + exception.getMessage());
                            inventoryFull = true;
                            break;
                        }
                    }

                    if (recipient.getInventory().firstEmpty() < 0) {
                        letters.addFirst(queuedCopy);
                        inventoryFull = true;
                        break;
                    }

                    try {
                        ItemStack deliveryAttempt = queuedCopy.clone();
                        Map<Integer, ItemStack> leftovers = recipient.getInventory().addItem(deliveryAttempt);
                        if (leftovers != null && !leftovers.isEmpty()) {
                            requeueLeftovers(letters, queuedCopy, leftovers);
                            inventoryFull = true;
                            break;
                        }
                    } catch (RuntimeException exception) {
                        letters.addFirst(queuedCopy);
                        plugin.getLogger().warning("Unable to deliver pending outgoing item for "
                                + recipient.getUniqueId() + ": " + exception.getMessage());
                        inventoryFull = true;
                        break;
                    }
                }

                if (inventoryFull) {
                    recipient.sendMessage(messageManager.getMessage(Message.ERROR_CANT_HOLD, true));
                }

                if (letters.isEmpty()) {
                    outgoing.remove(recipient.getUniqueId());
                }
            }

            if (hadPendingLetters) {
                recipient.updateInventory();
            }
            outgoingManager.saveAll();
        }
    }

    private void requeueLeftovers(LinkedList<ItemStack> letters,
                                  ItemStack original,
                                  Map<Integer, ItemStack> leftovers) {
        List<ItemStack> undelivered = new ArrayList<>();
        for (ItemStack leftover : leftovers.values()) {
            if (leftover == null) {
                continue;
            }
            try {
                if (leftover.getType() == null || leftover.getType().isAir() || leftover.getAmount() <= 0) {
                    continue;
                }
                undelivered.add(leftover.clone());
            } catch (RuntimeException ignored) {
                // A malformed leftover is not safe to put back into the queue.
            }
        }

        if (undelivered.isEmpty()) {
            // A compliant Bukkit inventory returns the undelivered stack. If a custom
            // inventory violates that contract, retain the original rather than lose
            // the mail; this path is only used when no usable leftover was returned.
            letters.addFirst(original.clone());
            return;
        }

        for (int index = undelivered.size() - 1; index >= 0; index--) {
            letters.addFirst(undelivered.get(index));
        }
    }

}
