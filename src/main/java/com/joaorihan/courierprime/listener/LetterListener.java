package com.joaorihan.courierprime.listener;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.courier.CourierManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * Listens to letter related events
 *
 * @author João Rihan
 */
public class LetterListener implements Listener {

    private final CourierPrime plugin;

    public LetterListener(CourierPrime plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }


    /**
     * Check when a player right-clicks their courier entity
     */
    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        Entity en = e.getRightClicked();
        if (CourierManager.getActiveCouriers().containsKey(en)) {
            if (CourierManager.getActiveCouriers().get(en).getRecipient().equals(e.getPlayer())
                    && !CourierManager.getActiveCouriers().get(en).isDelivered()) {
                CourierManager.getActiveCouriers().get(en).setDelivered();
                e.setCancelled(true);
                plugin.getLetterManager().getLetterSender().receive(e.getPlayer());
                en.getWorld().playSound(en.getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 1);
                en.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                        en.getLocation().add(0, en.getHeight() / 2, 0), 20,
                        en.getWidth() / 2, en.getHeight() / 2, en.getWidth() / 2);
            } else if (!CourierManager.getActiveCouriers().get(en).getRecipient().equals(e.getPlayer())) {
                e.setCancelled(true);
                en.getWorld().playSound(en.getLocation(), Sound.UI_TOAST_OUT, 1, 1);
            } else if (CourierManager.getActiveCouriers().get(en).isDelivered()) e.setCancelled(true);
        }
    }


    /**
     * prevent villager couriers from changing profession
     */
    @EventHandler
    public void onVillagerProfession(VillagerCareerChangeEvent e) {
        if (CourierManager.getActiveCouriers().containsKey(e.getEntity())) e.setCancelled(true);
    }


}
