package com.joaorihan.courierprime.config;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class MessageManager {

    private final ConfigManager configManager;


    public MessageManager(ConfigManager configManager){
        this.configManager = configManager;
    }


    public String getMessage(Message message){
        return getMessage(message, false);
    }

    public String getMessage(Message message, boolean prefix){
        if (prefix)
            return format(configManager.getLanguageConfig().getString("PREFIX")) + format(configManager.getLanguageConfig().getString(message.toString()));
        else return format(configManager.getLanguageConfig().getString(message.toString()));
    }

    /**
     * get the help message to send to a player, only showing what they are allowed to run
     *
     * @param player player viewing help message
     * @return help message
     */
    //todo: remake
    public String[] getHelpMessage(Player player) {
        ArrayList<String> help = new ArrayList<>();

        help.add("");
        help.add(getMessage(Message.HELP_HEADER));

        if (player.hasPermission("courierprime.letter")) help.add(getMessage(Message.HELP_LETTER));
        if (player.hasPermission("courierprime.post.one") ||
                player.hasPermission("courierprime.post.multiple") ||
                player.hasPermission("courierprime.post.allonline") ||
                player.hasPermission("courierprime.post.all")) help.add(getMessage(Message.HELP_POST));
        if (player.hasPermission("courierprime.unread")) help.add(getMessage(Message.HELP_UNREAD));
        if (player.hasPermission("courierprime.shred")) help.add(getMessage(Message.HELP_SHRED));
        if (player.hasPermission("courierprime.help")) help.add(getMessage(Message.HELP_HELP));
        if (player.hasPermission("courierprime.admin")) help.add(getMessage(Message.HELP_RELOAD));

        help.add(getMessage(Message.HELP_FOOTER));
        help.add("");

        String[] out = new String[help.size()];
        return help.toArray(out);
    }


    /**
     * Apply color codes and line breaks to a message
     *
     * @param msg message to format with color codes and line breaks
     * @return formatted message
     */
    public static String format(String msg) {
        if (msg == null) return null;
        return ChatColor.translateAlternateColorCodes('&', msg.replace("\\n", "\n"));
    }

    /**
     * Used to remove all minecraft color codes and line breaks from a message
     *
     * @param message message to remove all formatting from
     * @return unformatted message
     */
    public static String unformat(String message) {
        return message.replace("\\n", " ").replace("&0", "")
                .replace("&1", "").replace("&2", "")
                .replace("&3", "").replace("&4", "")
                .replace("&5", "").replace("&6", "")
                .replace("&7", "").replace("&8", "")
                .replace("&9", "").replace("&a", "")
                .replace("&b", "").replace("&c", "")
                .replace("&d", "").replace("&e", "")
                .replace("&f", "").replace("&k", "")
                .replace("&l", "").replace("&m", "")
                .replace("&n", "").replace("&o", "")
                .replace("&r", "");
    }



}
