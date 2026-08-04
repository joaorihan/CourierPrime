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
import java.util.UUID;

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

        if (!(item.getItemMeta() instanceof BookMeta bookMeta) || bookMeta.getTitle() == null) {
            return false;
        }

        String letterTitle = CourierPrime.getPlugin().getMessageManager().getMessage(Message.LETTER_FROM)
                .replace("$PLAYER$", "");
        return bookMeta.getTitle().contains(letterTitle);
    }

    /**
     * Used to check if a player is holding a letter
     * @param player Command sender
     * @return {@code true} if the item in main hand is a Letter
     */
    public boolean isHoldingLetter(@NonNull Player player) {
        return isValidLetter(player.getInventory().getItemInMainHand());
    }

    /**
     * Used to check if a player is holding a letter written by them
     * @param player Command sender
     * @return {@code true} if the item in main hand is a letter written by @player
     */
    public boolean isHoldingOwnLetter(@NonNull Player player) {
        if (!isHoldingLetter(player)) {
            return false;
        }

        String ownerUuid = getLetterOwnerUuid(player);
        if (ownerUuid != null) {
            return ownerUuid.equals(player.getUniqueId().toString());
        }

        // Preserve compatibility with letters created before UUID ownership was stored.
        return Objects.equals(getLetterOwner(player), player.getName());
    }

    public boolean wasAlreadySent(@NonNull ItemStack letter){
        if (!(letter.getItemMeta() instanceof BookMeta bookMeta) || bookMeta.getLore() == null) {
            return false;
        }

        String lore = String.join("\n", bookMeta.getLore());
        String destinationMarker = CourierPrime.getPlugin().getMessageManager()
                .getMessage(Message.LETTER_TO_ONE).replace("$PLAYER$", "");
        return lore.contains("&8To")
                || lore.contains(destinationMarker)
                || lore.contains(CourierPrime.getPlugin().getMessageManager().getMessage(Message.LETTER_TO_MULTIPLE))
                || lore.contains(CourierPrime.getPlugin().getMessageManager().getMessage(Message.LETTER_TO_ALL))
                || lore.contains(CourierPrime.getPlugin().getMessageManager().getMessage(Message.LETTER_TO_ALLONLINE));
    }

    public boolean wasAlreadyForwarded(@NonNull ItemStack letter){
        if (!(letter.getItemMeta() instanceof BookMeta bookMeta) || bookMeta.getLore() == null) {
            return false;
        }

        String marker = CourierPrime.getPlugin().getMessageManager()
                .getMessage(Message.LETTER_FORWARDED_BY).replace("$PLAYER$", "");
        return String.join("\n", bookMeta.getLore()).contains(marker);
    }

    /**
     * Used to get the Player who wrote a Letter
     * @param player command sender
     * @return String name of the Letter's owner
     * might need to be changed to OfflinePlayer, in case of player name change issues
     */
    public String getLetterOwner(@NonNull Player player){
        ItemStack book = player.getInventory().getItemInMainHand();
        if (!(book.getItemMeta() instanceof BookMeta bm)) {
            return null;
        }
        NamespacedKey key = CourierPrime.getPlugin().getLetterManager().getKey();

        if (bm.getPersistentDataContainer().has(key, PersistentDataType.STRING))
            return bm.getPersistentDataContainer().get(key, PersistentDataType.STRING);

        return null;
    }

    public String getLetterAuthor(@NonNull Player player) {
        ItemStack book = player.getInventory().getItemInMainHand();
        if (!(book.getItemMeta() instanceof BookMeta bookMeta)) {
            return null;
        }
        return bookMeta.getAuthor();
    }

    private String getLetterOwnerUuid(@NonNull Player player) {
        ItemStack book = player.getInventory().getItemInMainHand();
        if (!(book.getItemMeta() instanceof BookMeta bookMeta)) {
            return null;
        }

        NamespacedKey ownerKey = CourierPrime.getPlugin().getLetterManager().getOwnerKey();
        String ownerUuid = bookMeta.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
        if (ownerUuid == null) {
            return null;
        }

        try {
            return UUID.fromString(ownerUuid).toString();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }



}
