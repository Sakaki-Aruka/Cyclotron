package com.github.aruka.cyclotron;

import com.github.aruka.cyclotron.command.Filter;

import com.github.aruka.cyclotron.listener.ContainerBreak;
import com.github.aruka.cyclotron.listener.ContainerClick;
import com.github.aruka.cyclotron.listener.HopperTransfer;
import com.github.aruka.cyclotron.listener.PlayerItemMove;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class Cyclotron extends JavaPlugin {

    public static Set<String> ALL_CRAFTING_RECIPES_NAMESPACE = new HashSet<>();
    public static Map<String, Recipe> NAME_RECIPE_MAP = new HashMap<>();
    public static Cyclotron INSTANCE;
    public static final int CLICK_EXPIRE_DURATION = 60; // seconds

    public static int FILTER_LIMIT = 5;
    public static final String CyclotronKey = "CyclotronJsonData";
    public static final String CyclotronIndexKey = "CyclotronIndex";
    public static final Set<Material> CARGO_CONTAINER = Set.of(
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.BARREL,
            Material.SHULKER_BOX,
            Material.WHITE_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.PINK_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.BLACK_SHULKER_BOX
    );

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveConfig();
        setInstance();
        SettingsLoad.readConfig();
        getCommand("cyclotron").setExecutor(new Filter());
        getCommand("cyclotron").setTabCompleter(new Filter());
        getServer().getPluginManager().registerEvents(new ContainerClick(), this);
        getServer().getPluginManager().registerEvents(new HopperTransfer(), this);
        getServer().getPluginManager().registerEvents(new ContainerBreak(), this);

        getServer().getPluginManager().registerEvents(new PlayerItemMove(), this);

        getAllCraftingNamespace();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void setInstance() {
        INSTANCE = this;
    }


    private void getAllCraftingNamespace() {
        Iterator<Recipe> allRecipes = Bukkit.recipeIterator();
        while (allRecipes.hasNext()) {
            Recipe recipe = allRecipes.next();
            if (!(recipe instanceof CraftingRecipe)) continue;
            // e.g.) minecraft:stone -> stone
            ALL_CRAFTING_RECIPES_NAMESPACE.add(((CraftingRecipe) recipe).getKey().getKey());

            test(recipe);
        }
        map.entrySet().stream().filter(e -> e.getValue().size() > 1).forEach(e -> ALL_CRAFTING_RECIPES_NAMESPACE.removeAll(e.getValue()));
        for (String recipe : ALL_CRAFTING_RECIPES_NAMESPACE) {
            NamespacedKey nk = new NamespacedKey(NamespacedKey.MINECRAFT, recipe);
            NAME_RECIPE_MAP.put(recipe, Bukkit.getRecipe(nk));
        }
    }


    private static final Map<Material, Set<String>> map = new HashMap<>();
    private void test(Recipe recipe) {
        Material m = recipe.getResult().getType();
        if (!map.containsKey(m)) map.put(m, new HashSet<>());
        map.get(m).add(((CraftingRecipe) recipe).getKey().getKey());
    }

}
