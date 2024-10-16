package com.joaorihan.courierprime.letter;

import org.apache.commons.text.WordUtils;
import org.bukkit.inventory.meta.BookMeta;
import com.joaorihan.courierprime.CourierPrime;
import com.joaorihan.courierprime.Message;
import com.joaorihan.courierprime.courier.Courier;
import com.joaorihan.courierprime.courier.CourierOptions;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Send the letters to players
 *
 * @author Jeremy Noesen
 */
public class LetterSender implements Listener {
    
    /**
     * Send a letter to a player. The letter will have the recipient added to the lore, preventing it from being sent
     * again. It also adds it to a yml file of letters to be received. If the player receiving is online, they may
     * receive their letter.
     *
     * @param sender    player sending the letter
     * @param recipient player(s) to receive the letter
     */
    @SuppressWarnings("deprecation")
    public static void send(Player sender, String recipient) {
        if (LetterUtil.isHoldingOwnLetter(sender) &&
                !LetterUtil.wasAlreadySent(sender.getInventory().getItemInMainHand())) {

            ItemStack letter = new ItemStack(Material.WRITTEN_BOOK, 1);
            BookMeta letterMeta = (BookMeta) letter.getItemMeta();

            letterMeta.setAuthor(sender.getName());
            letterMeta.setTitle(Message.LETTER_FROM.replace("$PLAYER$", sender.getName()));
            letterMeta.setPages(((BookMeta) sender.getInventory().getItemInMainHand().getItemMeta()).getPages());

            ArrayList<String> lore = new ArrayList<>();
            Calendar currentDate = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat(Message.DATE_TIME_FORMAT);
            String dateNow = formatter.format(currentDate.getTime());
            String wrapped = WordUtils.wrap(Message.unformat(letterMeta.getPage(1)), 30, "<split>", true);
            String[] lines = wrapped.split("<split>");
            lore.add("");
            lore.add(Message.PREVIEW_FORMAT + lines[0]);
            if (lines.length >= 2) lore.add(Message.PREVIEW_FORMAT + lines[1]);
            if (lines.length >= 3) lore.add(Message.PREVIEW_FORMAT + lines[2]);
            lore.add("");
            lore.add(Message.PREVIEW_FOOTER.replace("$DATE$", dateNow)
                    .replace("$PAGES$", Integer.toString(letterMeta.getPages().size())));

            Collection<OfflinePlayer> offlinePlayers = null;

            if (recipient.equals("*")) {
                
                if (sender.hasPermission("couriernew.post.allonline")) {
                    
                    lore.add("§T" + Message.LETTER_TO_ALLONLINE);
                    offlinePlayers = new ArrayList<>();
                    offlinePlayers.addAll(Bukkit.getOnlinePlayers());
                    sender.sendMessage(Message.SUCCESS_SENT_ALLONLINE);
                    
                } else sender.sendMessage(Message.ERROR_NO_PERMS);
                
            } else if (recipient.equals("**")) {
                
                if (sender.hasPermission("couriernew.post.all")) {
                    
                    lore.add("§T" + Message.LETTER_TO_ALL);
                    offlinePlayers = new ArrayList<>();
                    offlinePlayers.addAll(Arrays.asList(Bukkit.getOfflinePlayers()));
                    sender.sendMessage(Message.SUCCESS_SENT_ALL);
                    
                } else sender.sendMessage(Message.ERROR_NO_PERMS);
                
            } else if (recipient.contains(",")) {
                
                if (sender.hasPermission("couriernew.post.multiple")) {
                    
                    offlinePlayers = new ArrayList<>();
                    
                    for (String recipients : recipient.split(",")) {
                        OfflinePlayer op;
                        try {
                            op = Bukkit.getOfflinePlayer(recipients);
                        } catch (Exception e) {
                            sender.sendMessage(Message.ERROR_PLAYER_NO_EXIST
                                    .replace("$PLAYER$", recipients));
                            return;
                        }
                        offlinePlayers.add(op);
                    }
                    
                    lore.add("§T" + Message.LETTER_TO_MULTIPLE);
                    sender.sendMessage(Message.SUCCESS_SENT_MULTIPLE);
                    
                } else sender.sendMessage(Message.ERROR_NO_PERMS);
                
            } else {
                
                if (sender.hasPermission("couriernew.post.one")) {
                    OfflinePlayer op;
                    try {
                        op = Bukkit.getOfflinePlayer(recipient);
                    } catch (Exception e) {
                        sender.sendMessage(Message.ERROR_PLAYER_NO_EXIST
                                .replace("$PLAYER$", recipient));
                        return;
                    }
                    offlinePlayers = new ArrayList<>();
                    offlinePlayers.add(op);
                    lore.add("§T" + Message.LETTER_TO_ONE.replace("$PLAYER$", op.getName()));
                    sender.sendMessage(Message.SUCCESS_SENT_ONE.replace("$PLAYER$", op.getName()));
                    
                } else sender.sendMessage(Message.ERROR_NO_PERMS);
            }
            
            if (offlinePlayers != null && offlinePlayers.size() > 0) {
                
                letterMeta.setLore(lore);
                letter.setItemMeta(letterMeta);
                
                for (OfflinePlayer op : offlinePlayers) {
                    
                    if (offlinePlayers.size() > 1 && op.equals(sender)) continue;
                    
                    if (!Outgoing.getOutgoing().containsKey(op.getUniqueId()))
                        Outgoing.getOutgoing().put(op.getUniqueId(), new LinkedList<>());
                    Outgoing.getOutgoing().get(op.getUniqueId()).add(new ItemStack(letter));
                    
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Courier.spawn((Player) op);
                        }
                    }.runTaskLater(CourierPrime.getInstance(), CourierOptions.RECEIVE_DELAY);
                }
                
                sender.getInventory().getItemInMainHand().setAmount(0);
            }
            
        } else if (LetterUtil.isHoldingOwnLetter(sender)) {
            sender.sendMessage(Message.ERROR_SENT_BEFORE);
        } else if (LetterUtil.isHoldingLetter(sender)) {
            sender.sendMessage(Message.ERROR_NOT_YOUR_LETTER);
        } else
            sender.sendMessage(Message.ERROR_NO_LETTER);
    }
    
    /**
     * When clicking the courier, retrieve the letters from the file and give all of them to the player. If they have
     * space in their inventory, give them all, starting with their hand if they aren't holding anything. Letters not
     * taken will be delivered later.
     *
     * @param recipient player receiving the mail
     */
    public static void receive(Player recipient) {
        if (Outgoing.getOutgoing().containsKey(recipient.getUniqueId()) && Outgoing.getOutgoing().get(recipient.getUniqueId()).size() > 0) {
            LinkedList<ItemStack> letters = Outgoing.getOutgoing().get(recipient.getUniqueId());

            while (!letters.isEmpty()) {
                if (recipient.getInventory().firstEmpty() < 0) {
                    recipient.sendMessage(Message.ERROR_CANT_HOLD);
                    break;
                } else if (recipient.getInventory().getItemInMainHand().getAmount() == 0) {
                    recipient.getInventory().setItemInMainHand(letters.pop());
                } else {
                    recipient.getInventory().addItem(letters.pop());
                }
            }
            
            recipient.updateInventory();
        }
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
                receive(e.getPlayer());
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
     * Check when a player joins so they can retrieve unread mail
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                Courier.spawn(player);
            }
        }.runTaskLater(CourierPrime.getInstance(), CourierOptions.RECEIVE_DELAY);
    }
    
    /**
     * If a player is coming from a blocked world, they will retrieve their mail
     */
    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Set<World> worlds = CourierOptions.BLOCKED_WORLDS;
        World to = e.getTo().getWorld();
        World from = e.getFrom().getWorld();
        Player recipient = e.getPlayer();
        
        if (worlds.contains(from) && !worlds.contains(to)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Courier.spawn(recipient);
                }
            }.runTaskLater(CourierPrime.getInstance(), CourierOptions.RECEIVE_DELAY);
        }
    }
    
    /**
     * If a player is changing from a blocked gamemode, they will retrieve their mail
     */
    @EventHandler
    public void onGamemode(PlayerGameModeChangeEvent e) {
        Set<GameMode> modes = CourierOptions.BLOCKED_GAMEMODES;
        GameMode to = e.getNewGameMode();
        GameMode from = e.getPlayer().getGameMode();
        Player recipient = e.getPlayer();
        if (modes.contains(from) && !modes.contains(to)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Courier.spawn(recipient);
                }
            }.runTaskLater(CourierPrime.getInstance(), CourierOptions.RECEIVE_DELAY);
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
