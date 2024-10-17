package com.joaorihan.courierprime.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * class to enable tab complete functionality for commands
 *
 * @author Jeremy Noesen
 */
public class CommandTabComplete implements TabCompleter {

    /**
     * method to implement the tab list for couriernew command
     *
     * @param sender  command sender
     * @param command command
     * @param label   command label followed after the /
     * @param args    command arguments
     * @return tab list
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        ArrayList<String> tabList = new ArrayList<>();

        if (sender instanceof Player player && (label.equalsIgnoreCase("couriernew") || label.equalsIgnoreCase("cn"))) {

            if (args.length == 1) {

                if (args[0].equalsIgnoreCase("")) {

                    if (player.hasPermission("couriernew.help")) tabList.add("help");
                    if (player.hasPermission("couriernew.reload")) tabList.add("reload");

                } else if (args[0].startsWith("h") && player.hasPermission("couriernew.help")) {

                    tabList.add("help");

                } else if (args[0].startsWith("r") && player.hasPermission("couriernew.reload")) {

                    tabList.add("reload");

                }

            }

            return tabList;

        }

        return null;

    }

}
