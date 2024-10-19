package com.joaorihan.courierprime.letter;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.Message;

import lombok.Getter;
import org.apache.commons.text.WordUtils;
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

    @Getter
    private static NamespacedKey key = new NamespacedKey(CourierPrime.getPlugin(), "playerName");

    //private List<Player> playersInBlockedMode = new ArrayList<>();
    //public void removeBlockedPlayer(Player player){ playersInBlockedMode.remove(player); }
    //public boolean isInBlockedMode(Player player) { return playersInBlockedMode.contains(player); }


    /**
     * Create a new letter with a specified message and places it in the player's inventory. Also set's the lore of the
     * item to a preview of the message
     *
     * @param player  player writing the letter
     * @param message the message the player is writing to the letter
     */
    public static void writeBook(Player player, String message, boolean anonymous) {
        String finalMessage = Message.format(message);
        
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta bm = (BookMeta) book.getItemMeta();

        PersistentDataContainer pdc = bm.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.STRING, player.getName());

        String author = anonymous ? Message.ANONYMOUS : player.getName();
        bm.setAuthor(author);
        bm.setTitle(Message.LETTER_FROM.replace("$PLAYER$", author));

        ArrayList<String> pages = new ArrayList<>();
        pages.add(finalMessage);
        bm.setPages(pages);

        ArrayList<String> lore = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(Message.DATE_TIME_FORMAT);
        String dateNow = formatter.format(currentDate.getTime());
        String wrapped = WordUtils.wrap(Message.unformat(message), 30, "<split>", true);
        String[] lines = wrapped.split("<split>");
        lore.add("");
        lore.add(Message.PREVIEW_FORMAT + lines[0]);
        if (lines.length >= 2) lore.add(Message.PREVIEW_FORMAT + lines[1]);
        if (lines.length >= 3) lore.add(Message.PREVIEW_FORMAT + lines[2]);
        lore.add("");
        lore.add(Message.PREVIEW_FOOTER.replace("$DATE$", dateNow)
                .replace("$PAGES$", Integer.toString(bm.getPages().size())));
        bm.setLore(lore);
        book.setItemMeta(bm);
        
        if (player.getInventory().firstEmpty() < 0) {
            //todo change
            player.getWorld().dropItemNaturally(player.getEyeLocation(), book);
            player.sendMessage(Message.SUCCESS_CREATED_DROPPED);
        } else if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            player.getInventory().setItemInMainHand(book);
            player.sendMessage(Message.SUCCESS_CREATED_HAND);
        } else {
            player.getInventory().addItem(book);
            player.sendMessage(Message.SUCCESS_CREATED_ADDED);
        }
    }
    
    /**
     * Add a new page to an existing letter that the player is writing. Adds page count to lore
     *
     * @param player  player editing the letter
     * @param message message player is adding to the letter
     */
    public static void editBook(Player player, String message) {
        String finalMessage = Message.format(message);
        
        ItemStack writtenBook = player.getInventory().getItemInMainHand();
        BookMeta wbm = (BookMeta) writtenBook.getItemMeta();
        
        ArrayList<String> pages = new ArrayList<>(wbm.getPages());
        if (pages.get(pages.size() - 1).length() < 256 && pages.get(pages.size() - 1).length() > 0) {
            String sb = pages.get(pages.size() - 1) +
                    finalMessage;
            pages.set(pages.size() - 1, sb);
            player.sendMessage(Message.SUCCESS_PAGE_EDITED);
        } else {
            pages.add(finalMessage);
            player.sendMessage(Message.SUCCESS_PAGE_ADDED);
        }
        wbm.setPages(pages);

        ArrayList<String> lore = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(Message.DATE_TIME_FORMAT);
        String dateNow = formatter.format(currentDate.getTime());
        String wrapped = WordUtils.wrap(Message.unformat(wbm.getPage(1)), 30, "<split>", true);
        String[] lines = wrapped.split("<split>");
        lore.add("");
        lore.add(Message.PREVIEW_FORMAT + lines[0]);
        if (lines.length >= 2) lore.add(Message.PREVIEW_FORMAT + lines[1]);
        if (lines.length >= 3) lore.add(Message.PREVIEW_FORMAT + lines[2]);
        lore.add("");
        lore.add(Message.PREVIEW_FOOTER.replace("$DATE$", dateNow)
                .replace("$PAGES$", Integer.toString(wbm.getPages().size())));
        wbm.setLore(lore);
        writtenBook.setItemMeta(wbm);
        
        player.getInventory().setItemInMainHand(writtenBook);
    }
    
    /**
     * Delete a letter in the player's hand
     *
     * @param player player deleting the letter in their hand
     */
    public static void delete(Player player) {
        if (LetterUtil.isHoldingLetter(player)) {
            player.getInventory().getItemInMainHand().setAmount(0);
            player.sendMessage(Message.SUCCESS_DELETED);
        } else
            player.sendMessage(Message.ERROR_NO_LETTER);
    }
    
    /**
     * Delete all letters in the player's inventory
     *
     * @param player player deleting the letters in their inventory
     */
    public static void deleteAll(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (LetterUtil.isValidLetter(item)) item.setAmount(0);
        }
        player.sendMessage(Message.SUCCESS_DELETED_ALL);
    }
}
