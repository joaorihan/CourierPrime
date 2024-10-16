package com.joaorihan.courierprime.letter;

import com.joaorihan.courierprime.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * Methods to check for letters in a player's inventory
 *
 * @author Jeremy Noesen
 */
public class LetterChecker {
    
    /**
     * Test if a player is holding a letter they wrote and have not sent yet.
     *
     * @param player player holding the letter
     * @return true if player is holding their own unsent letter
     */
    public static boolean isHoldingOwnLetter(Player player) {
        return player.getInventory().getItemInMainHand() != null &&
                player.getInventory().getItemInMainHand().getType() == Material.WRITTEN_BOOK &&
                ((BookMeta) player.getInventory().getItemInMainHand().getItemMeta())
                        .getAuthor().equals(player.getName()) &&
                ((BookMeta) player.getInventory().getItemInMainHand().getItemMeta())
                        .getTitle().equals(Message.LETTER_FROM.replace("$PLAYER$", player.getName()));
    }
    
    /**
     * Check if a player is holding a letter in general
     *
     * @param player player holding letter
     * @return true if player is holding a letter
     */
    public static boolean isHoldingLetter(Player player) {
        return isLetter(player.getInventory().getItemInMainHand());
    }
    
    /**
     * Check if item is a letter
     *
     * @param item item to check
     * @return true if item is a letter
     */
    public static boolean isLetter(ItemStack item) {
        return item != null && item.getType() == Material.WRITTEN_BOOK &&
                ((BookMeta) item.getItemMeta()).getTitle().contains(Message.LETTER_FROM
                        .replace("$PLAYER$", ""));
    }
    
    /**
     * @param item item to check as a sent letter
     * @return true if letter was already sent before
     */
    public static boolean wasSent(ItemStack item) {
        return item.getType() == Material.WRITTEN_BOOK && item.getItemMeta().getLore() != null &&
                (item.getItemMeta().getLore().toString().contains("§T" + Message.LETTER_TO_ONE.replace("$PLAYER$", "")) ||
                        item.getItemMeta().getLore().toString().contains("§T" + Message.LETTER_TO_MULTIPLE) ||
                        item.getItemMeta().getLore().toString().contains("§T" + Message.LETTER_TO_ALL) ||
                        item.getItemMeta().getLore().toString().contains("§T" + Message.LETTER_TO_ALLONLINE));
    }
}