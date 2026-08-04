package com.joaorihan.courierprime.letter;

import com.joaorihan.courierprime.CourierPrime;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * class to handle loading and saving outgoing letters
 *
 * @author Jeremy Noesen
 */
public class OutgoingManager {

    @Getter
    private final HashMap<UUID, LinkedList<ItemStack>> outgoing;

    /**
     * reference to outgoing config
     */
    private final YamlConfiguration outgoingConfig;
    private final CourierPrime plugin;


    public OutgoingManager(CourierPrime plugin){
        this.plugin = plugin;
        outgoing = new HashMap<>();
        outgoingConfig = plugin.getConfigManager().getOutgoingConfig();
    }


    /**
     * save outgoing letters to file for a player
     *
     * @param uuid player to save outgoing data
     */
    private void savePlayer(UUID uuid) {
        LinkedList<ItemStack> letters = outgoing.get(uuid);
        if (hasPendingLetters(uuid) && letters != null) {
            List<ItemStack> copies = copyItems(letters);
            if (!copies.isEmpty()) {
                outgoingConfig.set(uuid.toString(), copies);
            }
        }
    }

    /**
     * save all outgoing letters to file
     */
    public void saveAll() {
        synchronized (outgoing) {
            for (String key : new LinkedHashSet<>(outgoingConfig.getKeys(false))) {
                outgoingConfig.set(key, null);
            }
            for (UUID player : new ArrayList<>(outgoing.keySet())) {
                savePlayer(player);
            }
            plugin.getConfigManager().saveOutgoingConfig();
        }
    }

    /**
     * load outgoing letters for a player
     *
     * @param player player to load data for
     */
    private void loadPlayer(UUID player, String key) {
        Object storedValue;
        try {
            storedValue = outgoingConfig.get(key);
        } catch (RuntimeException exception) {
            warn("Ignoring malformed outgoing value for recipient " + key, exception);
            return;
        }

        if (!(storedValue instanceof List<?> storedLetters)) {
            warn("Ignoring malformed outgoing value for recipient " + key, null);
            return;
        }

        LinkedList<ItemStack> letters = new LinkedList<>();
        for (Object storedLetter : storedLetters) {
            if (!(storedLetter instanceof ItemStack itemStack)) {
                warn("Ignoring malformed outgoing item for recipient " + key, null);
                continue;
            }

            try {
                if (itemStack.getType() == null || itemStack.getType().isAir() || itemStack.getAmount() <= 0) {
                    warn("Ignoring empty or malformed outgoing item for recipient " + key, null);
                    continue;
                }
                letters.add(itemStack.clone());
            } catch (RuntimeException exception) {
                warn("Ignoring malformed outgoing item for recipient " + key, exception);
            }
        }

        if (!letters.isEmpty()) {
            outgoing.put(player, letters);
        }
    }

    /**
     * load all outgoing letters from file
     */
    public void loadAll() {
        synchronized (outgoing) {
            outgoing.clear();

            for (String key : new LinkedHashSet<>(outgoingConfig.getKeys(false))) {
                final UUID player;
                try {
                    player = UUID.fromString(key);
                } catch (IllegalArgumentException | NullPointerException exception) {
                    warn("Ignoring invalid recipient UUID in outgoing.yml: " + key, exception);
                    continue;
                }

                loadPlayer(player, key);
            }
        }
    }

    public boolean hasPendingLetters(Player player){
        return player != null && hasPendingLetters(player.getUniqueId());
    }

    public boolean hasPendingLetters(UUID uuid){
        if (uuid == null) {
            return false;
        }
        synchronized (outgoing) {
            LinkedList<ItemStack> letters = outgoing.get(uuid);
            return letters != null && !letters.isEmpty();
        }
    }

    private List<ItemStack> copyItems(List<ItemStack> letters) {
        List<ItemStack> copies = new ArrayList<>();
        for (ItemStack letter : letters) {
            if (letter == null) {
                warn("Ignoring null outgoing item while saving outgoing.yml", null);
                continue;
            }

            try {
                if (letter.getType() == null || letter.getType().isAir() || letter.getAmount() <= 0) {
                    warn("Ignoring empty or malformed outgoing item while saving outgoing.yml", null);
                    continue;
                }
                copies.add(letter.clone());
            } catch (RuntimeException exception) {
                warn("Ignoring malformed outgoing item while saving outgoing.yml", exception);
            }
        }
        return copies;
    }

    private void warn(String message, Throwable exception) {
        if (exception == null) {
            plugin.getLogger().warning(message);
        } else {
            plugin.getLogger().warning(message + ": " + exception.getMessage());
        }
    }

}
