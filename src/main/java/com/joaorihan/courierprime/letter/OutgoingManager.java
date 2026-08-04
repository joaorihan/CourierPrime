package com.joaorihan.courierprime.letter;

import com.joaorihan.courierprime.CourierPrime;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
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
    private HashMap<UUID, LinkedList<ItemStack>> outgoing;

    /**
     * reference to outgoing config
     */
    private final YamlConfiguration outgoingConfig;


    public OutgoingManager(CourierPrime plugin){
        outgoing = new HashMap<>();
        outgoingConfig = plugin.getConfigManager().getOutgoingConfig();
    }


    /**
     * save outgoing letters to file for a player
     *
     * @param uuid player to save outgoing data
     */
    private void savePlayer(UUID uuid) {
        if (hasPendingLetters(uuid))
            outgoingConfig.set(uuid.toString(), outgoing.get(uuid));
    }

    /**
     * save all outgoing letters to file
     */
    public void saveAll() {
        for (String key : outgoingConfig.getKeys(false)) {
            outgoingConfig.set(key, null);
        }
        for (UUID player : outgoing.keySet()) {
            savePlayer(player);
        }
        CourierPrime.getPlugin().getConfigManager().saveOutgoingConfig();
    }

    /**
     * load outgoing letters for a player
     *
     * @param player player to load data for
     */
    private void loadPlayer(UUID player) {
        List<?> storedLetters = outgoingConfig.getList(player.toString());
        LinkedList<ItemStack> letters = new LinkedList<>();
        if (storedLetters != null) {
            for (Object storedLetter : storedLetters) {
                if (storedLetter instanceof ItemStack itemStack) {
                    letters.add(itemStack);
                }
            }
        }
        outgoing.put(player, letters);
    }

    /**
     * load all outgoing letters from file
     */
    public void loadAll() {
        for (String key : outgoingConfig.getKeys(false)) {
            try {
                loadPlayer(UUID.fromString(key));
            } catch (IllegalArgumentException e) {
                CourierPrime.getPlugin().getLogger().warning("Ignoring invalid recipient UUID in outgoing.yml: " + key);
            }
        }
    }

    public boolean hasPendingLetters(Player player){
        return (getOutgoing().containsKey(player.getUniqueId()) && getOutgoing().get(player.getUniqueId()).size() > 0);
    }

    public boolean hasPendingLetters(UUID uuid){
        return (getOutgoing().containsKey(uuid) && getOutgoing().get(uuid).size() > 0);
    }

}
