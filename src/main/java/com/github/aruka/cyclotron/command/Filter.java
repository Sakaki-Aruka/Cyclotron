package com.github.aruka.cyclotron.command;

import com.github.aruka.cyclotron.Cyclotron;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Filter implements CommandExecutor, TabCompleter {
    public static Map<UUID, String> FILTER_QUERIES = new HashMap<>();
    public static Map<UUID, String> FILTER_QUERY_KIND = new HashMap<>();
    private static final List<String> FIRST_ARGUMENTS = List.of("register", "unregister", "info");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        /*
        1, /filter <register | unregister> <Result Item ID>
        2, click the container.
         */
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command only for players who are in a server.");
            return false;
        } else if (args.length != 2) {
            if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
                FILTER_QUERY_KIND.put(((Player) sender).getUniqueId(), "info");
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("clear") && sender.hasPermission("cyclotron.clear")) {
                FILTER_QUERY_KIND.put(((Player) sender).getUniqueId(), "clear");
                return true;
            }
            sender.sendMessage("This command requires 2 arguments.");
            return false;
        } else if (!args[0].matches("(?i)((un)?register|info)")) {
            sender.sendMessage("This command requires 'register', 'unregister' or 'info' at the first argument. ");
            return false;
        } else if (!Cyclotron.ALL_CRAFTING_RECIPES_NAMESPACE.contains(args[1].toLowerCase())) {
            sender.sendMessage("You can not register this item.");
            return false;
        }

        FILTER_QUERY_KIND.put(((Player) sender).getUniqueId(), args[0].toLowerCase());
        FILTER_QUERIES.put(((Player) sender).getUniqueId(), args[1].toLowerCase());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            String in = args[0];
            if (in.isEmpty()) return FIRST_ARGUMENTS;
            for (String a : FIRST_ARGUMENTS) if (a.startsWith(in)) list.add(a);
        } else if (args.length == 2) {
            String in = args[1];
            if (in.isEmpty()) {
                return new ArrayList<>(Cyclotron.ALL_CRAFTING_RECIPES_NAMESPACE);
            }
            for (String a : Cyclotron.ALL_CRAFTING_RECIPES_NAMESPACE) {
                if (!a.startsWith(in)) continue;
                list.add(a);
            }
        }
        return list;
    }
}
