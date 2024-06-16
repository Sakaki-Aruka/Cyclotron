package com.github.aruka.cyclotron;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static void loadMetaData() {
        File file = new File(Cyclotron.MetaDataFilePath);
        if (!file.exists() || !file.canRead()) return;
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
        } catch (Exception e) {
            Cyclotron.INSTANCE.getLogger().info("THE METADATA FILE NOT FOUND.");
            return;
        }
        BufferedReader reader = new BufferedReader(fileReader);
        Pattern pattern = Pattern.compile("x=(-?[0-9]+),?y=(-?[0-9]+),?z=(-?[0-9]+),?\\|world=([^|]+)\\|data=([^|]+)\\|index=([0-9]+)");
        reader
            .lines()
            .map(pattern::matcher)
            .filter(Matcher::matches)
            .forEach(matcher -> {
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                int z = Integer.parseInt(matcher.group(3));
                String worldName = matcher.group(4);
                if (isCorrectWorldName(worldName)) {
                    Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                    Block block = location.getBlock();
                    if (Cyclotron.CARGO_CONTAINER.contains(block.getType())) {
                        String data = matcher.group(5);
                        int i = Integer.parseInt(matcher.group(6));
                        MetadataValue recipes = new FixedMetadataValue(Cyclotron.INSTANCE, data);
                        MetadataValue index = new FixedMetadataValue(Cyclotron.INSTANCE, i);
                        block.setMetadata(Cyclotron.CyclotronKey, recipes);
                        block.setMetadata(Cyclotron.CyclotronIndexKey, index);
                        block.getState().update();
                        Cyclotron.MetadataBlocks.add(block.getLocation());
                    }
                }
            });
        Cyclotron.INSTANCE.getLogger().info("COMPLETE TO LOAD METADATA. (size=" + Cyclotron.MetadataBlocks.size() + ")");
    }

    private static boolean isCorrectWorldName(String name) {
        return Bukkit.getWorlds().stream().anyMatch(e -> e.getName().equals(name));
    }

    private static void fileReset() {
        File file = new File(Cyclotron.MetaDataFilePath);
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write("");
            writer.flush();
        } catch (Exception e) {
            Cyclotron.INSTANCE.getLogger().info("FILE RESET ERROR.");
        }
    }

    public static void saveMetaData() {
        if (Cyclotron.MetadataBlocks.isEmpty()) return;
        File file = new File(Cyclotron.MetaDataFilePath);
        try {
            file.createNewFile();
        } catch (Exception e) {
            Cyclotron.INSTANCE.getLogger().warning("FAILED TO CREATE " + Cyclotron.MetaDataFilePath);
            return;
        }
        if (!file.exists() || !file.isFile() || !file.canWrite()) return;
        final String NL = System.lineSeparator();
        fileReset();

        try (FileWriter writer = new FileWriter(file, true)) {
            for (Location location : Cyclotron.MetadataBlocks) {
                Block block = location.getBlock();
                if (!block.hasMetadata(Cyclotron.CyclotronKey)) continue;
                // data format: "x=~~~,y=~~~,z=~~~|world=~~~|data=~~~|index=~~~\n"
                Location loc = block.getLocation();
                String data = String.format("x=%d,y=%d,z=%d|world=%s|data=%s|index=%d%s",
                        loc.getBlockX(),
                        loc.getBlockY(),
                        loc.getBlockZ(),
                        loc.getWorld().getName(),
                        block.getMetadata(Cyclotron.CyclotronKey).get(0).asString(),
                        block.hasMetadata(Cyclotron.CyclotronIndexKey) ? block.getMetadata(Cyclotron.CyclotronIndexKey).get(0).asInt() : 0,
                        NL
                );
                writer.write(data);
                writer.flush();
            }
        } catch (Exception e) {
            Cyclotron.INSTANCE.getLogger().warning("FILE ERROR. (" + Cyclotron.MetaDataFilePath + "). Cyclotron's data will be deleted.");
            return;
        }
    }
}
