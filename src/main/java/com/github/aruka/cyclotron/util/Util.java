package com.github.aruka.cyclotron.util;

import com.github.aruka.cyclotron.Cyclotron;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Util {
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

    public static boolean isNullOrAir(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

    public static @Nullable String convertFlat(String[] array) {
        if (array == null) return null;
        else if (array.length == 0) return "";
        return String.join(",", array);
    }

    public static void replaceMetadata(Block block, MetadataValue value) {
        replaceMetadataSpecifiedKey(block, Cyclotron.CyclotronKey, value);
    }

    public static void replaceMetadataSpecifiedKey(Block block, String key, MetadataValue value) {
        if (block.hasMetadata(key)) block.removeMetadata(key, Cyclotron.INSTANCE);
        block.setMetadata(key, value);
    }
}
