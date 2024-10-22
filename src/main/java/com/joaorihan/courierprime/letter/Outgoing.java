package com.joaorihan.courierprime.letter;

import com.joaorihan.courierprime.CourierPrime;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

/**
 * class to handle loading and saving outgoing letters
 *
 * @author Jeremy Noesen
 */
public class Outgoing {

    @Getter
    private static HashMap<UUID, LinkedList<ItemStack>> outgoing = new HashMap<>();

    /**
     * reference to outgoing config
     */
    private static YamlConfiguration outgoingConfig = CourierPrime.getPlugin().getConfigManager().getOutgoingConfig();


    /**
     * save outgoing letters to file for a player
     *
     * @param player player to save outgoing data
     */
    private static void savePlayer(UUID player) {
        if (outgoing.containsKey(player) && outgoing.get(player).size() > 0)
            outgoingConfig.set(player.toString(), outgoing.get(player));
    }

    /**
     * save all outgoing letters to file
     */
    public static void saveAll() {
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
    private static void loadPlayer(UUID player) {
        outgoing.put(player, new LinkedList<>((Collection<ItemStack>) outgoingConfig.getList(player.toString())));
    }

    /**
     * load all outgoing letters from file
     */
    public static void loadAll() {
        for (String key : outgoingConfig.getKeys(false)) {
            loadPlayer(UUID.fromString(key));
        }
    }

}
