package me.Jeremaster101.CourierNew.Letter;

import me.Jeremaster101.CourierNew.Message;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * Methods to check for letters in a player's inventory
 */
public class LetterChecker {

    /**
     * Test if a player is holding a letter they wrote and have not sent yet.
     *
     * @param player player holding the letter
     * @return true if player is holding their own unsent letter
     */
    public boolean isHoldingOwnLetter(Player player) {
        return player.getInventory().getItemInMainHand() != null &&
                player.getInventory().getItemInMainHand().getType() == Material.WRITTEN_BOOK &&
                ((BookMeta) player.getInventory().getItemInMainHand().getItemMeta()).getAuthor().equals(player.getName()) &&
                ((BookMeta) player.getInventory().getItemInMainHand().getItemMeta()).getTitle().equals(Message.LETTER_FROM + player.getName());
    }

    /**
     * Check if a player is holding a letter in general
     *
     * @param player player holding letter
     * @return true if player is holding a letter
     */
    public boolean isHoldingLetter(Player player) {
        return player.getInventory().getItemInMainHand() != null &&
                player.getInventory().getItemInMainHand().getType() == Material.WRITTEN_BOOK &&
                ((BookMeta) player.getInventory().getItemInMainHand().getItemMeta()).getTitle().contains(Message.LETTER_FROM);
    }

    /**
     * Check if item is a letter
     *
     * @param item item to check
     * @return true if item is a letter
     */
    public boolean isLetter(ItemStack item) {
        return item != null && item.getType() == Material.WRITTEN_BOOK &&
                ((BookMeta) item.getItemMeta()).getTitle().contains(Message.LETTER_FROM);
    }
    
    /**
     * @param item item to check as a sent letter
     * @return true if letter was already sent before
     */
    public boolean wasSent(ItemStack item) {
        return item.getType() == Material.WRITTEN_BOOK && item.getItemMeta().getLore() != null &&
                item.getItemMeta().getLore().toString().contains("§TTo ");
    }
}
