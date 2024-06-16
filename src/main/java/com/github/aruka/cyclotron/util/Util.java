package com.github.aruka.cyclotron.util;

import com.github.aruka.cyclotron.Cyclotron;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {

    public static boolean isCyclotronCargo(@Nullable Block block) {
        if (block == null) return false;
        else if (!Cyclotron.CARGO_CONTAINER.contains(block.getType())) return false;
        return block.hasMetadata(Cyclotron.CyclotronKey);
    }

    public static @Nullable String[] getHeldData(@NotNull Block block) {
        if (!block.hasMetadata(Cyclotron.CyclotronKey)) return null;
        String data = block.getMetadata(Cyclotron.CyclotronKey).get(0).asString();
        if (data.isEmpty()) return null;
        return data.split(",");
    }

    public static String[] getAvailableRecipes(@NotNull String[] recipes) {
        List<String> list = new ArrayList<>();
        for (String r : recipes) {
            if (Cyclotron.ALL_CRAFTING_RECIPES_NAMESPACE.contains(r)) list.add(r);
        }
        return list.toArray(new String[0]);
    }

    public static String[] getNotAvailableRecipes(@NotNull String[] recipes) {
        String[] availableRecipes = getAvailableRecipes(recipes);
        List<String> list = new ArrayList<>();
        for (String r : availableRecipes) {
            if (!Cyclotron.ALL_CRAFTING_RECIPES_NAMESPACE.contains(r)) list.add(r);
        }
        return list.toArray(new String[0]);
    }

    public static boolean isNullOrAir(@Nullable ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

    public static @Nullable String convertFlat(String[] array) {
        if (array == null) return null;
        else if (array.length == 0) return "";
        return String.join(",", array);
    }

    public static void replaceMetadata(@NotNull Block block, @NotNull MetadataValue value) {
        replaceMetadataSpecifiedKey(block, Cyclotron.CyclotronKey, value);
    }

    public static void replaceMetadataSpecifiedKey(Block block, String key, MetadataValue value) {
        if (block.hasMetadata(key)) block.removeMetadata(key, Cyclotron.INSTANCE);
        block.setMetadata(key, value);
    }

    public static void blockDataClear(Block block) {
        block.removeMetadata(Cyclotron.CyclotronKey, Cyclotron.INSTANCE);
        block.removeMetadata(Cyclotron.CyclotronIndexKey, Cyclotron.INSTANCE);
    }

    public static void doubleChest(Block block, boolean syncWithSource) {
        if (((Container) block.getState()).getInventory() instanceof DoubleChestInventory doubleInventory) {
            DoubleChest chest = doubleInventory.getHolder();
            Block[] blocks = getDoubleChest(chest.getLocation());
            int sourceIndex = Arrays.stream(blocks).map(Block::getLocation).toList().indexOf(block.getLocation());
            int clientIndex = sourceIndex == 0 ? 1 : 0;

            if (!syncWithSource) {
                sourceIndex = sourceIndex == 0 ? 1 : 0;
                clientIndex = clientIndex == 0 ? 1 : 0;
            }
            sync(blocks[sourceIndex], blocks[clientIndex]);

            Cyclotron.MetadataBlocks.add(blocks[0].getLocation());
            Cyclotron.MetadataBlocks.add(blocks[1].getLocation());
        }
    }

    private static void sync(Block source, Block client) {
        if (!source.hasMetadata(Cyclotron.CyclotronKey) && !client.hasMetadata(Cyclotron.CyclotronKey)) {
            return;
        } else if (!source.hasMetadata(Cyclotron.CyclotronKey)) {
            // clear
            Util.blockDataClear(client);
            return;
        }

        MetadataValue value = source.getMetadata(Cyclotron.CyclotronKey).get(0);
        client.setMetadata(Cyclotron.CyclotronKey, value);

        if (source.hasMetadata(Cyclotron.CyclotronIndexKey)) {
            MetadataValue index = source.getMetadata(Cyclotron.CyclotronIndexKey).get(0);
            client.setMetadata(Cyclotron.CyclotronIndexKey, index);
        }

    }

    private static Block[] getDoubleChest(Location location) {
        World world = location.getWorld();
        double x1 = Math.floor(location.getX());
        double x2 = Math.ceil(location.getX());
        double y = location.getY();
        double z1 = Math.floor(location.getZ());
        double z2 = Math.ceil(location.getZ());
        Location loc1 = new Location(world, x1, y, z1);
        Location loc2 = new Location(world, x2, y, z2);
        return new Block[]{world.getBlockAt(loc1), world.getBlockAt(loc2)};
    }
}
