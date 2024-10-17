package com.joaorihan.courierprime.listener;

import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.letter.LetterSender;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;


public class LetterListener implements Listener {


    public LetterListener(CourierPrime plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }


    /**
     * Check when a player right-clicks their courier entity
     */
    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        Entity en = e.getRightClicked();
        if (Courier.getCouriers().containsKey(en)) {
            if (Courier.getCouriers().get(en).getRecipient().equals(e.getPlayer())
                    && !Courier.getCouriers().get(en).isDelivered()) {
                Courier.getCouriers().get(en).setDelivered();
                e.setCancelled(true);
                LetterSender.receive(e.getPlayer());
                en.getWorld().playSound(en.getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 1);
                en.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                        en.getLocation().add(0, en.getHeight() / 2, 0), 20,
                        en.getWidth() / 2, en.getHeight() / 2, en.getWidth() / 2);
            } else if (!Courier.getCouriers().get(en).getRecipient().equals(e.getPlayer())) {
                e.setCancelled(true);
                en.getWorld().playSound(en.getLocation(), Sound.UI_TOAST_OUT, 1, 1);
            } else if (Courier.getCouriers().get(en).isDelivered()) e.setCancelled(true);
        }
    }


    /**
     * prevent villager couriers from changing profession
     */
    @EventHandler
    public void onVillagerProfession(VillagerCareerChangeEvent e) {
        if (Courier.getCouriers().keySet().contains(e.getEntity())) e.setCancelled(true);
    }


}
