package com.joaorihan.courierprime.letter;

import com.joaorihan.courierprime.Message;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Methods to create, edit, and delete letters
 *
 * @author Jeremy Noesen
 */
public class LetterCreation {
    
    /**
     * Create a new letter with a specified message and places it in the player's inventory. Also set's the lore of the
     * item to a preview of the message
     *
     * @param player  player writing the letter
     * @param message the message the player is writing to the letter
     */
    public static void writeBook(Player player, String message) {
        String finalMessage = Message.format(message);
        
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta bm = (BookMeta) book.getItemMeta();
        
        bm.setAuthor(player.getName());
        bm.setTitle(Message.LETTER_FROM.replace("$PLAYER$", player.getName()));
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
            StringBuilder sb = new StringBuilder();
            sb.append(pages.get(pages.size() - 1));
            sb.append(finalMessage);
            pages.set(pages.size() - 1, sb.toString());
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
        if (LetterChecker.isHoldingLetter(player)) {
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
            if (LetterChecker.isLetter(item)) item.setAmount(0);
        }
        player.sendMessage(Message.SUCCESS_DELETED_ALL);
    }
}
