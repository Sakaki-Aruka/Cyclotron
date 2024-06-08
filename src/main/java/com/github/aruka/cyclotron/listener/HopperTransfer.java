package com.github.aruka.cyclotron.listener;

import com.github.aruka.cyclotron.Cyclotron;
import com.github.aruka.cyclotron.util.Util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class HopperTransfer implements Listener {

    private static final Predicate<ItemStack> NULL_OR_AIR = (e -> (e == null || e.getType().equals(Material.AIR)));


    @EventHandler
    public void hopperTransfer(InventoryMoveItemEvent event) {
        Location to = event.getDestination().getLocation();
        if (to == null || !Cyclotron.CARGO_CONTAINER.contains(to.getBlock().getType())) return;
        Block block = to.getBlock();
        if (!block.hasMetadata(Cyclotron.CyclotronKey)) return;
        Inventory destination = event.getDestination();

        int startIndex = getRecipeIndex(block);
        if (!block.hasMetadata(Cyclotron.CyclotronIndexKey)) {
            Util.replaceMetadataSpecifiedKey(block, Cyclotron.CyclotronIndexKey, new FixedMetadataValue(Cyclotron.INSTANCE, 0));
        }

        String[] preRecipes = Util.getHeldData(block);
        if (preRecipes == null || preRecipes.length == 0) return;
        String[] recipes = Util.getAvailableRecipes(preRecipes);
        if (recipes.length == 0) return;
        core(recipes, startIndex, destination, block);
    }

    private void core(String[] recipes, int startIndex, Inventory destination, Block block) {
        for (int i = startIndex; i < startIndex + recipes.length; i++) {
            int index = i % recipes.length;
            CraftingRecipe recipe = (CraftingRecipe) Cyclotron.NAME_RECIPE_MAP.get(recipes[index]);
            if (recipe == null) continue;
            int placeSlot = getFirstPlacableSlot(recipe.getResult(), destination);
            if (placeSlot == -1) continue;
            if (!isCraftable(recipe, destination)) continue;
            make(recipe, destination);

            //debug
            System.out.println("made");

            if (Util.isNullOrAir(destination.getItem(placeSlot))) {
                destination.setItem(placeSlot, recipe.getResult());
            } else {
                int now = destination.getItem(placeSlot).getAmount();
                ItemStack replacer = recipe.getResult();
                replacer.setAmount(now + recipe.getResult().getAmount());
                destination.setItem(placeSlot, replacer);
            }
            break;
        }

        int nextIndex = (startIndex + 1) % recipes.length;
        Util.replaceMetadataSpecifiedKey(block, Cyclotron.CyclotronIndexKey, new FixedMetadataValue(Cyclotron.INSTANCE, nextIndex));
    }

    private void make(Recipe recipe, Inventory inventory) {
        Map<RecipeChoice, Integer> flat = flatten(recipe);
        Iterator<ItemStack> iterator = inventory.iterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (Util.isNullOrAir(item)) continue;
            for (Map.Entry<RecipeChoice, Integer> entry : flat.entrySet()) {
                if (entry.getValue() == 0 || !entry.getKey().test(item)) continue;
                if (item.getAmount() < entry.getValue()) {
                    flat.put(entry.getKey(), entry.getValue() - item.getAmount());
                    item.setAmount(0);
                } else {
                    item.setAmount(item.getAmount() - entry.getValue());
                    flat.put(entry.getKey(), 0);
                }

            }
        }
    }


    private int getFirstPlacableSlot(ItemStack result, Inventory inventory) {
        if (NULL_OR_AIR.test(result)) return -1;
        if (inventory.first(result.getType()) == -1) return inventory.firstEmpty();
        List<Integer> slots = new ArrayList<>(inventory.all(result.getType()).keySet());
        Collections.sort(slots);
        int maxSize = result.getMaxStackSize();
        for (int i : slots) {
            ItemStack item = inventory.getItem(i);
            if (item == null || !item.getType().equals(result.getType())) continue;
            if (item.getAmount() + result.getAmount() > maxSize) continue;
            return i;
        }

        return inventory.firstEmpty();
    }

    private boolean isCraftable(Recipe recipe, Inventory inventory) {
        Map<ItemStack, Integer> relation = getRelation(inventory);
        Map<RecipeChoice, Integer> flat = flatten(recipe);
        Set<RecipeChoice> passed = new HashSet<>();
        for (Map.Entry<ItemStack, Integer> r : relation.entrySet()) {
            for (Map.Entry<RecipeChoice, Integer> f : flat.entrySet()) {
                if (passed.contains(f.getKey())) continue;
                if (f.getKey().test(r.getKey())) {
                    if (r.getValue() < f.getValue()) return false;
                    passed.add(f.getKey());
                }
            }
        }
        return passed.size() == flat.size();
    }


    private Map<ItemStack, Integer> getRelation(Inventory inventory) {
        Map<ItemStack, Integer> result = new HashMap<>();
        for (ItemStack item : inventory.getContents()) {
            if (Util.isNullOrAir(item)) continue;
            ItemStack copy = item.clone().asOne();
            if (!result.containsKey(copy)) result.put(copy, 0);
            result.put(copy, result.get(copy) + item.getAmount());
        }
        return result;
    }

    private Map<RecipeChoice, Integer> flatten(Recipe recipe) {
        Map<RecipeChoice, Integer> result = new HashMap<>();
        if (recipe instanceof ShapedRecipe) {
            //shaped
            Map<Character, RecipeChoice> choiceMap = ((ShapedRecipe) recipe).getChoiceMap();
            for (char c : choiceMap.keySet()) {
                result.put(choiceMap.get(c), 0);
            }

            for (String s : ((ShapedRecipe) recipe).getShape()) {
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    result.put(choiceMap.get(c), result.get(choiceMap.get(c)) + 1);
                }
            }
        } else {
            // shapeless
            for (RecipeChoice choice : ((ShapelessRecipe) recipe).getChoiceList()) {
                if (!result.containsKey(choice)) result.put(choice, 0);
                result.put(choice, result.get(choice) + 1);
            }
        }
        return result;
    }


    private int getRecipeIndex(Block block) {
        int result = 0;
        String[] recipes = Util.getHeldData(block);

        int index = block.hasMetadata(Cyclotron.CyclotronIndexKey)
                ? block.getMetadata(Cyclotron.CyclotronIndexKey).get(0).asInt()
                : -1;
        if (!(index == -1 || index >= recipes.length)) result = index + 1;
        return result;
    }

}
