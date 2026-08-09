package com.joaorihan.courierprime.listener;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.courier.CourierManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.logging.Level;

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
        Courier courier = CourierManager.getActiveCouriers().get(en);
        if (courier == null) {
            return;
        }

        // Every configured Bukkit Entity kind is a courier interaction target;
        // no LivingEntity cast is needed for delivery.
        e.setCancelled(true);
        if (!courier.getRecipient().getUniqueId().equals(e.getPlayer().getUniqueId())) {
            en.getWorld().playSound(en.getLocation(), Sound.UI_TOAST_OUT, 1, 1);
            return;
        }
        if (courier.isDelivered()) {
            return;
        }

        if (!courier.beginDelivery()) {
            return;
        }

        boolean delivered = false;
        try {
            plugin.getLetterManager().getLetterSender().receive(e.getPlayer());
        } catch (RuntimeException ex) {
            plugin.getLogger().log(Level.WARNING,
                    "Courier delivery failed for " + e.getPlayer().getName()
                            + "; pending mail will remain retryable.", ex);
        } finally {
            delivered = courier.completeDeliveryAttempt();
        }

        if (delivered) {
            en.getWorld().playSound(en.getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 1);
            en.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                    en.getLocation().add(0, en.getHeight() / 2, 0), 20,
                    en.getWidth() / 2, en.getHeight() / 2, en.getWidth() / 2);
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
