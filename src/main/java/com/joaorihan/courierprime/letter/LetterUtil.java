package com.joaorihan.courierprime.letter;


import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.config.Message;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

/**
 * Methods to check for letters in a player's inventory
 *
 * @author Joao Rihan
 */
@UtilityClass
public class LetterUtil {

    /**
     * Used to check if an item is a plugin Letter
     * @param item Item to be compared
     * @return if the item was a valid plugin Letter or not
     */
    public boolean isValidLetter(ItemStack item){
        if (item == null || !item.getType().equals(Material.WRITTEN_BOOK))
            return false;

        return ((BookMeta) item.getItemMeta()).getTitle().contains(CourierPrime.getPlugin().getMessageManager().getMessage(Message.LETTER_FROM)
                .replace("$PLAYER$", ""));
    }

    /**
     * Used to check if a player is holding a letter
     * @param player Command sender
     * @return {@code boolean} item in main hand is a Letter
     */
    public boolean isHoldingLetter(@NonNull Player player) {
        return isValidLetter(player.getInventory().getItemInMainHand());
    }

    /**
     * Used to check if a player is holding a letter written by them
     * @param player Command sender
     * @return {@code boolean} item in main hand is a letter written by @player
     */
    public boolean isHoldingOwnLetter(@NonNull Player player) {
        return isHoldingLetter(player) && Objects.equals(LetterUtil.getLetterOwner(player), player.getName());
    }

    public boolean wasAlreadySent(@NonNull ItemStack letter){
        return letter.getItemMeta().getLore().toString().contains("Destinatário: ") ||
                letter.getItemMeta().getLore().toString().contains("Recipient: ");
    }

    /**
     * Used to get the Player who wrote a Letter
     * @param player command sender
     * @return String name of the Letter's owner
     * might need to be changed to OfflinePlayer, in case of player name change issues
     */
    public String getLetterOwner(@NonNull Player player){
        ItemStack book = player.getInventory().getItemInMainHand();
        BookMeta bm = (BookMeta) book.getItemMeta();
        NamespacedKey key = LetterManager.getKey();

        if (bm.getPersistentDataContainer().has(key, PersistentDataType.STRING))
            return bm.getPersistentDataContainer().get(key, PersistentDataType.STRING);

        return null;
    }



}