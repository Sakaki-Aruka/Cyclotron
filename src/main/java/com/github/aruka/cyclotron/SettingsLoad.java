package com.github.aruka.cyclotron;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class SettingsLoad {
    public static void readConfig() {
        FileConfiguration config = Cyclotron.INSTANCE.getConfig();
        Cyclotron.FILTER_LIMIT = config.contains("recipes") ? config.getInt("recipes") : 5;
    }

    public static void reloadConfig() {
        Cyclotron.INSTANCE.reloadConfig();
        readConfig();
    }

    public static void writeRecipesLimit(int limit, CommandSender sender) {
        if (limit < 1) {
            sender.sendMessage("You must set the limit more than 1.");
            return;
        }
        FileConfiguration config = Cyclotron.INSTANCE.getConfig();
        int old = Cyclotron.FILTER_LIMIT;
        Cyclotron.FILTER_LIMIT = limit;
        config.set("recipes", limit);
        Cyclotron.INSTANCE.saveConfig();
        sender.sendMessage(String.format("Cyclotron recipes limit changed. (%d -> %d)", old, limit));
    }
}
