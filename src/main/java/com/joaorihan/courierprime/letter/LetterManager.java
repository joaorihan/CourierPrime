package com.joaorihan.courierprime.letter;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.config.MainConfig;
import com.joaorihan.courierprime.config.Message;

import com.joaorihan.courierprime.config.MessageManager;
import lombok.Getter;
import org.apache.commons.text.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Methods to create, edit, and delete letters
 *
 * @author Jeremy Noesen
 */
public class LetterManager {

    private static final int MAX_BOOK_PAGE_LENGTH = 256;

    private final List<Player> playersInBlockedMode;
    private final CourierPrime plugin;

    @Getter
    private LetterSender letterSender;

    @Getter
    private final NamespacedKey key;

    @Getter
    private final NamespacedKey ownerKey;


    public LetterManager(CourierPrime plugin){
        this.plugin = plugin;
        this.playersInBlockedMode = new ArrayList<>();
        this.key =  new NamespacedKey(plugin, "playerName");
        this.ownerKey = new NamespacedKey(plugin, "playerUuid");

        this.letterSender = new LetterSender(plugin);
    }


    public void removeBlockedPlayer(Player player){ playersInBlockedMode.remove(player); }

    public boolean addBlockedPlayer(Player player){
        if (isInBlockedMode(player))
            return false;

        playersInBlockedMode.add(player);
        return true;
    }

    public boolean isInBlockedMode(Player player) { return playersInBlockedMode.contains(player); }


    /**
     * Create a new letter with a specified message and places it in the player's inventory. Also set's the lore of the
     * item to a preview of the message
     *
     * @param player  player writing the letter
     * @param message the message the player is writing to the letter
     */
    public void writeBook(Player player, String message, boolean anonymous) {
        String sourceMessage = message == null ? "" : message;
        String finalMessage = MessageManager.format(sourceMessage);
        if (finalMessage == null) {
            finalMessage = "";
        }
        
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
        if (!(book.getItemMeta() instanceof BookMeta bm)) {
            return;
        }

        PersistentDataContainer pdc = bm.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.STRING, player.getName());
        pdc.set(ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());

        String author = anonymous ? plugin.getMessageManager().getMessage(Message.ANONYMOUS) : player.getName();
        bm.setAuthor(author);
        bm.setTitle(plugin.getMessageManager().getMessage(Message.LETTER_FROM).replace("$PLAYER$", author));

        ArrayList<String> pages = new ArrayList<>();
        pages.addAll(splitIntoPages(finalMessage));
        bm.setPages(pages);

        bm.setLore(createPreviewLore(sourceMessage, pages.size()));

        // Sets the letter custom model data, if enabled in the config
        if (MainConfig.isCustomModelData()){
            int modelData = anonymous ? MainConfig.getAnonLetterCustomModelData() : MainConfig.getLetterCustomModelData();
            bm.setCustomModelData(modelData);
        }

        book.setItemMeta(bm);
        
        if (player.getInventory().firstEmpty() < 0) {
            //todo change
            player.getWorld().dropItemNaturally(player.getEyeLocation(), book);
            player.sendMessage(plugin.getMessageManager().getMessage(Message.SUCCESS_CREATED_DROPPED, true));
        } else if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            player.getInventory().setItemInMainHand(book);
            player.sendMessage(plugin.getMessageManager().getMessage(Message.SUCCESS_CREATED_HAND, true));
        } else {
            player.getInventory().addItem(book);
            player.sendMessage(plugin.getMessageManager().getMessage(Message.SUCCESS_CREATED_ADDED, true));

        }
    }
    
    /**
     * Add a new page to an existing letter that the player is writing. Adds page count to lore
     *
     * @param player  player editing the letter
     * @param message message player is adding to the letter
     */
    public void editBook(Player player, String message) {
        if (player == null) {
            return;
        }

        String sourceMessage = message == null ? "" : message;
        String finalMessage = MessageManager.format(sourceMessage);
        if (finalMessage == null) {
            finalMessage = "";
        }
        
        ItemStack writtenBook = player.getInventory().getItemInMainHand();
        if (writtenBook == null || !(writtenBook.getItemMeta() instanceof BookMeta wbm)) {
            return;
        }

        ArrayList<String> pages = normalizePages(wbm.getPages());
        if (pages.isEmpty()) {
            pages.add("");
        }

        trimTrailingEmptyPages(pages);
        int pageCountBeforeEdit = pages.size();
        boolean addedPage = appendToPages(pages, finalMessage);
        if (addedPage || pages.size() > pageCountBeforeEdit) {
            player.sendMessage(plugin.getMessageManager().getMessage(Message.SUCCESS_PAGE_ADDED, true));
        } else {
            player.sendMessage(plugin.getMessageManager().getMessage(Message.SUCCESS_PAGE_EDITED, true));
        }
        wbm.setPages(pages);

        String firstPage = pages.isEmpty() ? "" : pages.get(0);
        wbm.setLore(createPreviewLore(firstPage, pages.size()));
        writtenBook.setItemMeta(wbm);
        
        player.getInventory().setItemInMainHand(writtenBook);
    }

    private List<String> splitIntoPages(String message) {
        ArrayList<String> pages = new ArrayList<>();
        String safeMessage = message == null ? "" : message;
        if (safeMessage.isEmpty()) {
            pages.add("");
            return pages;
        }

        for (int start = 0; start < safeMessage.length(); start += MAX_BOOK_PAGE_LENGTH) {
            pages.add(safeMessage.substring(start, Math.min(start + MAX_BOOK_PAGE_LENGTH, safeMessage.length())));
        }
        return pages;
    }

    private ArrayList<String> normalizePages(List<String> existingPages) {
        ArrayList<String> pages = new ArrayList<>();
        if (existingPages == null) {
            return pages;
        }

        for (String page : existingPages) {
            pages.addAll(splitIntoPages(page == null ? "" : page));
        }
        return pages;
    }

    private void trimTrailingEmptyPages(List<String> pages) {
        while (pages.size() > 1 && pages.get(pages.size() - 1).isEmpty()) {
            pages.remove(pages.size() - 1);
        }
    }

    private boolean appendToPages(List<String> pages, String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        if (pages.isEmpty()) {
            pages.add("");
        }

        int lastPageIndex = pages.size() - 1;
        String lastPage = pages.get(lastPageIndex);
        if (lastPage == null) {
            lastPage = "";
        }

        int remainingCapacity = Math.max(0, MAX_BOOK_PAGE_LENGTH - lastPage.length());
        int firstPageLength = Math.min(remainingCapacity, message.length());
        if (firstPageLength > 0) {
            pages.set(lastPageIndex, lastPage + message.substring(0, firstPageLength));
        }

        if (firstPageLength == message.length()) {
            return false;
        }

        pages.addAll(splitIntoPages(message.substring(firstPageLength)));
        return true;
    }

    private ArrayList<String> createPreviewLore(String content, int pageCount) {
        ArrayList<String> lore = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(plugin.getMessageManager().getMessage(Message.DATE_TIME_FORMAT));
        String dateNow = formatter.format(currentDate.getTime());

        String safeContent = content == null ? "" : content;
        String preview = ChatColor.stripColor(MessageManager.unformat(safeContent));
        if (preview == null) {
            preview = "";
        }
        String wrapped = WordUtils.wrap(preview, 30, "<split>", true);
        if (wrapped == null) {
            wrapped = "";
        }
        String[] lines = wrapped.isEmpty() ? new String[]{""} : wrapped.split("<split>", -1);
        lore.add("");
        lore.add(plugin.getMessageManager().getMessage(Message.PREVIEW_FORMAT) + lines[0]);
        if (lines.length >= 2) lore.add(plugin.getMessageManager().getMessage(Message.PREVIEW_FORMAT) + lines[1]);
        if (lines.length >= 3) lore.add(plugin.getMessageManager().getMessage(Message.PREVIEW_FORMAT) + lines[2]);
        lore.add("");
        lore.add(plugin.getMessageManager().getMessage(Message.PREVIEW_FOOTER).replace("$DATE$", dateNow)
                .replace("$PAGES$", Integer.toString(pageCount)));
        return lore;
    }
    
    /**
     * Delete a letter in the player's hand
     *
     * @param player player deleting the letter in their hand
     */
    public void delete(Player player) {
        if (LetterUtil.isHoldingLetter(player)) {
            player.getInventory().getItemInMainHand().setAmount(0);
            player.sendMessage(plugin.getMessageManager().getMessage(Message.SUCCESS_DELETED, true));
        } else
            player.sendMessage(plugin.getMessageManager().getMessage(Message.ERROR_NO_LETTER, true));
    }
    
    /**
     * Delete all letters in the player's inventory
     *
     * @param player player deleting the letters in their inventory
     */
    public void deleteAll(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (LetterUtil.isValidLetter(item)) item.setAmount(0);
        }
        player.sendMessage(plugin.getMessageManager().getMessage(Message.SUCCESS_DELETED_ALL, true));
    }
}
