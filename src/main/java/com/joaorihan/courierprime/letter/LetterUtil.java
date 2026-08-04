package com.joaorihan.courierprime.letter;


import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.config.Message;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;
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
        if (item == null || item.getType() != Material.WRITTEN_BOOK)
            return false;

        if (!(item.getItemMeta() instanceof BookMeta bookMeta)) {
            return false;
        }

        CourierPrime plugin = CourierPrime.getPlugin();
        if (plugin == null || plugin.getLetterManager() == null) {
            return false;
        }

        NamespacedKey ownerKey = plugin.getLetterManager().getOwnerKey();
        String ownerUuid = readPersistentString(bookMeta, ownerKey);
        if (hasPersistentKey(bookMeta, ownerKey) || ownerUuid != null) {
            return normalizeUuid(ownerUuid) != null;
        }

        String ownerName = readPersistentString(bookMeta, plugin.getLetterManager().getKey());
        if (ownerName != null && !ownerName.isBlank()) {
            return true;
        }

        // Very old delivered copies did not retain the PDC. Keep a strict presentation
        // fallback, but never accept a title merely because it contains a phrase.
        return hasLegacyLetterPresentation(bookMeta, plugin);
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

        if (hasLetterOwnerUuid(player)) {
            String ownerUuid = getLetterOwnerUuid(player);
            return ownerUuid != null && ownerUuid.equals(player.getUniqueId().toString());
        }

        // Preserve compatibility with letters created before UUID ownership was stored.
        return Objects.equals(getLetterOwner(player), player.getName());
    }

    public boolean wasAlreadySent(ItemStack letter){
        BookMeta bookMeta = getBookMeta(letter);
        if (bookMeta == null) {
            return false;
        }

        if (bookMeta.getGeneration() == BookMeta.Generation.COPY_OF_ORIGINAL) {
            return true;
        }

        if (bookMeta.getLore() == null) {
            return false;
        }

        for (String loreLine : bookMeta.getLore()) {
            if (isSentMarker(loreLine)) {
                return true;
            }
        }
        return false;
    }

    public boolean wasAlreadyForwarded(ItemStack letter){
        BookMeta bookMeta = getBookMeta(letter);
        if (bookMeta == null) {
            return false;
        }

        if (bookMeta.getGeneration() == BookMeta.Generation.COPY_OF_ORIGINAL) {
            return true;
        }

        if (bookMeta.getLore() == null) {
            return false;
        }

        for (String loreLine : bookMeta.getLore()) {
            if (isForwardedMarker(loreLine)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used to get the Player who wrote a Letter
     * @param player command sender
     * @return String name of the Letter's owner
     * might need to be changed to OfflinePlayer, in case of player name change issues
     */
    public String getLetterOwner(@NonNull Player player){
        BookMeta bookMeta = getHoldingBookMeta(player);
        CourierPrime plugin = CourierPrime.getPlugin();
        if (bookMeta == null || plugin == null || plugin.getLetterManager() == null) {
            return null;
        }
        return readPersistentString(bookMeta, plugin.getLetterManager().getKey());
    }

    public String getLetterAuthor(@NonNull Player player) {
        BookMeta bookMeta = getHoldingBookMeta(player);
        return bookMeta == null ? null : bookMeta.getAuthor();
    }

    private String getLetterOwnerUuid(@NonNull Player player) {
        BookMeta bookMeta = getHoldingBookMeta(player);
        CourierPrime plugin = CourierPrime.getPlugin();
        if (bookMeta == null || plugin == null || plugin.getLetterManager() == null) {
            return null;
        }

        return normalizeUuid(readPersistentString(bookMeta, plugin.getLetterManager().getOwnerKey()));
    }

    private BookMeta getBookMeta(ItemStack item) {
        if (item == null || !(item.getItemMeta() instanceof BookMeta bookMeta)) {
            return null;
        }
        return bookMeta;
    }

    private BookMeta getHoldingBookMeta(Player player) {
        if (player == null || player.getInventory() == null) {
            return null;
        }
        return getBookMeta(player.getInventory().getItemInMainHand());
    }

    private String readPersistentString(BookMeta bookMeta, NamespacedKey key) {
        if (bookMeta == null || key == null || bookMeta.getPersistentDataContainer() == null) {
            return null;
        }

        try {
            return bookMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private boolean hasPersistentKey(BookMeta bookMeta, NamespacedKey key) {
        if (bookMeta == null || key == null || bookMeta.getPersistentDataContainer() == null) {
            return false;
        }

        try {
            return bookMeta.getPersistentDataContainer().has(key);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private boolean hasLetterOwnerUuid(@NonNull Player player) {
        BookMeta bookMeta = getHoldingBookMeta(player);
        CourierPrime plugin = CourierPrime.getPlugin();
        if (plugin == null || plugin.getLetterManager() == null) {
            return false;
        }

        NamespacedKey ownerKey = plugin.getLetterManager().getOwnerKey();
        return hasPersistentKey(bookMeta, ownerKey) || readPersistentString(bookMeta, ownerKey) != null;
    }

    private String normalizeUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(value).toString();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private boolean hasLegacyLetterPresentation(BookMeta bookMeta, CourierPrime plugin) {
        String title = bookMeta.getTitle();
        String author = bookMeta.getAuthor();
        if (title == null || title.isBlank() || author == null || author.isBlank()
                || bookMeta.getPageCount() <= 0 || plugin.getMessageManager() == null) {
            return false;
        }

        String titlePattern = plugin.getMessageManager().getMessage(Message.LETTER_FROM);
        String titlePrefix = titlePattern == null ? "" : titlePattern.replace("$PLAYER$", "");
        return !titlePrefix.isBlank() && title.equals(titlePrefix + author);
    }

    private boolean isSentMarker(String loreLine) {
        if (loreLine == null || loreLine.isBlank()) {
            return false;
        }

        // §T is the current stable destination marker, including multiple/all variants.
        if (loreLine.startsWith("§T") || loreLine.startsWith("&T")) {
            return true;
        }

        // Keep the raw and formatted legacy English marker used by older versions.
        if (loreLine.contains("&8To") || loreLine.contains("§8To")) {
            return true;
        }

        CourierPrime plugin = CourierPrime.getPlugin();
        boolean legacyDestinationStyle = loreLine.startsWith("§8") || loreLine.startsWith("&8");
        if (plugin != null && plugin.getMessageManager() != null) {
            for (Message message : new Message[]{
                    Message.LETTER_TO_ONE,
                    Message.LETTER_TO_MULTIPLE,
                    Message.LETTER_TO_ALL,
                    Message.LETTER_TO_ALLONLINE
            }) {
                String marker = plugin.getMessageManager().getMessage(message);
                marker = marker == null ? "" : marker.replace("$PLAYER$", "");
                if (legacyDestinationStyle && !marker.isBlank() && loreLine.contains(marker)) {
                    return true;
                }

                String normalizedMarker = normalizeText(marker);
                if (legacyDestinationStyle && !normalizedMarker.isBlank()
                        && normalizeText(loreLine).startsWith(normalizedMarker)) {
                    return true;
                }
            }
        }

        // The shipped Portuguese locale is also used by legacy letters when the server locale changes.
        if (!legacyDestinationStyle) {
            return false;
        }
        String normalized = normalizeText(loreLine).toLowerCase(Locale.ROOT);
        return normalized.startsWith("to ") || normalized.startsWith("para ");
    }

    private boolean isForwardedMarker(String loreLine) {
        if (loreLine == null || loreLine.isBlank()) {
            return false;
        }

        boolean forwardedStyle = loreLine.startsWith("§7") || loreLine.startsWith("&7");
        if (!forwardedStyle) {
            return false;
        }

        CourierPrime plugin = CourierPrime.getPlugin();
        if (plugin != null && plugin.getMessageManager() != null) {
            String marker = plugin.getMessageManager().getMessage(Message.LETTER_FORWARDED_BY);
            marker = marker == null ? "" : marker.replace("$PLAYER$", "");
            if ((!marker.isBlank() && loreLine.contains(marker))
                    || (!normalizeText(marker).isBlank()
                    && normalizeText(loreLine).contains(normalizeText(marker)))) {
                return true;
            }
        }

        String normalized = normalizeText(loreLine).toLowerCase(Locale.ROOT);
        return normalized.contains("forwarded") || normalized.contains("encaminhad");
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        String translated = ChatColor.translateAlternateColorCodes('&', text);
        String stripped = ChatColor.stripColor(translated);
        return stripped == null ? "" : stripped.trim();
    }



}
