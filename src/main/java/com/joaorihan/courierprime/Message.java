package com.joaorihan.courierprime;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * All messages used within the plugin
 *
 * @author Jeremy Noesen
 */
public class Message {

    private static final Config config = Config.getMessageConfig();

    public static String PREFIX;
    public static String ANONYMOUS;
    public static String SUCCESS_CREATED_HAND;
    public static String SUCCESS_CREATED_DROPPED;
    public static String SUCCESS_CREATED_ADDED;
    public static String SUCCESS_PAGE_ADDED;
    public static String SUCCESS_PAGE_EDITED;
    public static String SUCCESS_DELETED;
    public static String SUCCESS_DELETED_ALL;
    public static String SUCCESS_SENT_ONE;
    public static String SUCCESS_SENT_MULTIPLE;
    public static String SUCCESS_SENT_ALL;
    public static String SUCCESS_SENT_ALLONLINE;
    public static String SUCCESS_COURIER_ARRIVED;
    public static String SUCCESS_EXTRA_DELIVERIES;
    public static String SUCCESS_IGNORED;
    public static String SUCCESS_RELOADED;
    public static String ERROR_NO_PERMS;
    public static String ERROR_SENT_BEFORE;
    public static String ERROR_NO_MSG;
    public static String ERROR_NO_LETTER;
    public static String ERROR_NOT_YOUR_LETTER;
    public static String ERROR_UNKNOWN_ARGS;
    public static String ERROR_PLAYER_NO_EXIST;
    public static String ERROR_NO_MAIL;
    public static String ERROR_CANT_HOLD;
    public static String ERROR_VANISHED;
    public static String ERROR_WORLD;
    public static String COURIER_NAME;
    public static String COURIER_NAME_RECEIVED;
    public static String LETTER_FROM;
    public static String LETTER_BY;
    public static String PREVIEW_FORMAT;
    public static String PREVIEW_FOOTER;
    public static String DATE_TIME_FORMAT;
    public static String LETTER_TO_ONE;
    public static String LETTER_TO_MULTIPLE;
    public static String LETTER_TO_ALL;
    public static String LETTER_TO_ALLONLINE;
    public static String HELP_HEADER;
    public static String HELP_LETTER;
    public static String HELP_POST;
    public static String HELP_UNREAD;
    public static String HELP_SHRED;
    public static String HELP_SHREDALL;
    public static String HELP_HELP;
    public static String HELP_RELOAD;
    public static String HELP_FOOTER;

    /**
     * get the help message to send to a player, only showing what they are allowed to run
     *
     * @param player player viewing help message
     * @return help message
     */
    //todo: remake
    public static String[] getHelpMessage(Player player) {
        ArrayList<String> help = new ArrayList<>();

        help.add("");
        help.add(HELP_HEADER);

        if (player.hasPermission("courierprime.letter")) help.add(HELP_LETTER);
        if (player.hasPermission("courierprime.post.one") ||
                player.hasPermission("courierprime.post.multiple") ||
                player.hasPermission("courierprime.post.allonline") ||
                player.hasPermission("courierprime.post.all")) help.add(HELP_POST);
        if (player.hasPermission("courierprime.unread")) help.add(HELP_UNREAD);
        if (player.hasPermission("courierprime.shred")) help.add(HELP_SHRED);
        if (player.hasPermission("courierprime.shredall")) help.add(HELP_SHREDALL);
        if (player.hasPermission("courierprime.help")) help.add(HELP_HELP);
        if (player.hasPermission("courierprime.reload")) help.add(HELP_RELOAD);

        help.add(HELP_FOOTER);
        help.add("");

        String[] out = new String[help.size()];
        return help.toArray(out);
    }

    /**
     * Reloads all plugin messages
     */
    public static void reloadMessages() {
        PREFIX = format(config.getConfig().getString("PREFIX"));
        ANONYMOUS = format(config.getConfig().getString("ANONYMOUS"));
        SUCCESS_CREATED_HAND = PREFIX + format(config.getConfig().getString("SUCCESS_CREATED_HAND"));
        SUCCESS_CREATED_DROPPED = PREFIX + format(config.getConfig().getString("SUCCESS_CREATED_DROPPED"));
        SUCCESS_CREATED_ADDED = PREFIX + format(config.getConfig().getString("SUCCESS_CREATED_ADDED"));
        SUCCESS_PAGE_ADDED = PREFIX + format(config.getConfig().getString("SUCCESS_PAGE_ADDED"));
        SUCCESS_PAGE_EDITED = PREFIX + format(config.getConfig().getString("SUCCESS_PAGE_EDITED"));
        SUCCESS_DELETED = PREFIX + format(config.getConfig().getString("SUCCESS_DELETED"));
        SUCCESS_DELETED_ALL = PREFIX + format(config.getConfig().getString("SUCCESS_DELETED_ALL"));
        SUCCESS_SENT_ONE = PREFIX + format(config.getConfig().getString("SUCCESS_SENT_ONE"));
        SUCCESS_SENT_MULTIPLE = PREFIX + format(config.getConfig().getString("SUCCESS_SENT_MULTIPLE"));
        SUCCESS_SENT_ALL = PREFIX + format(config.getConfig().getString("SUCCESS_SENT_ALL"));
        SUCCESS_SENT_ALLONLINE = PREFIX + format(config.getConfig().getString("SUCCESS_SENT_ALLONLINE"));
        SUCCESS_COURIER_ARRIVED = PREFIX + format(config.getConfig().getString("SUCCESS_COURIER_ARRIVED"));
        SUCCESS_EXTRA_DELIVERIES = PREFIX + format(config.getConfig().getString("SUCCESS_EXTRA_DELIVERIES"));
        SUCCESS_IGNORED = PREFIX + format(config.getConfig().getString("SUCCESS_IGNORED"));
        SUCCESS_RELOADED = PREFIX + format(config.getConfig().getString("SUCCESS_RELOADED"));
        ERROR_NO_PERMS = PREFIX + format(config.getConfig().getString("ERROR_NO_PERMS"));
        ERROR_SENT_BEFORE = PREFIX + format(config.getConfig().getString("ERROR_SENT_BEFORE"));
        ERROR_NO_MSG = PREFIX + format(config.getConfig().getString("ERROR_NO_MSG"));
        ERROR_NO_LETTER = PREFIX + format(config.getConfig().getString("ERROR_NO_LETTER"));
        ERROR_NOT_YOUR_LETTER = PREFIX + format(config.getConfig().getString("ERROR_NOT_YOUR_LETTER"));
        ERROR_UNKNOWN_ARGS = PREFIX + format(config.getConfig().getString("ERROR_UNKNOWN_ARGS"));
        ERROR_PLAYER_NO_EXIST = PREFIX + format(config.getConfig().getString("ERROR_PLAYER_NO_EXIST"));
        ERROR_NO_MAIL = PREFIX + format(config.getConfig().getString("ERROR_NO_MAIL"));
        ERROR_CANT_HOLD = PREFIX + format(config.getConfig().getString("ERROR_CANT_HOLD"));
        ERROR_VANISHED = PREFIX + format(config.getConfig().getString("ERROR_VANISHED"));
        ERROR_WORLD = PREFIX + format(config.getConfig().getString("ERROR_WORLD"));
        COURIER_NAME = format(config.getConfig().getString("COURIER_NAME"));
        COURIER_NAME_RECEIVED = format(config.getConfig().getString("COURIER_NAME_RECEIVED"));
        LETTER_FROM = format(config.getConfig().getString("LETTER_FROM"));
        LETTER_BY = PREFIX + format(config.getConfig().getString("LETTER_BY"));
        PREVIEW_FORMAT = format(config.getConfig().getString("PREVIEW_FORMAT"));
        PREVIEW_FOOTER = format(config.getConfig().getString("PREVIEW_FOOTER"));
        DATE_TIME_FORMAT = config.getConfig().getString("DATE_TIME_FORMAT");
        LETTER_TO_ONE = format(config.getConfig().getString("LETTER_TO_ONE"));
        LETTER_TO_MULTIPLE = format(config.getConfig().getString("LETTER_TO_MULTIPLE"));
        LETTER_TO_ALL = format(config.getConfig().getString("LETTER_TO_ALL"));
        LETTER_TO_ALLONLINE = format(config.getConfig().getString("LETTER_TO_ALLONLINE"));
        HELP_HEADER = format(config.getConfig().getString("HELP_HEADER"));
        HELP_LETTER = format(config.getConfig().getString("HELP_LETTER"));
        HELP_POST = format(config.getConfig().getString("HELP_POST"));
        HELP_UNREAD = format(config.getConfig().getString("HELP_UNREAD"));
        HELP_SHRED = format(config.getConfig().getString("HELP_SHRED"));
        HELP_SHREDALL = format(config.getConfig().getString("HELP_SHREDALL"));
        HELP_HELP = format(config.getConfig().getString("HELP_HELP"));
        HELP_RELOAD = format(config.getConfig().getString("HELP_RELOAD"));
        HELP_FOOTER = format(config.getConfig().getString("HELP_FOOTER"));
    }


    /**
     * Apply color codes and line breaks to a message
     *
     * @param msg message to format with color codes and line breaks
     * @return formatted message
     */
    public static String format(String msg) {
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