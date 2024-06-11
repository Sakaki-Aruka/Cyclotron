package com.github.aruka.cyclotron.listener;

import com.github.aruka.cyclotron.Cyclotron;
import com.github.aruka.cyclotron.command.Filter;
import com.github.aruka.cyclotron.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ContainerClick implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChestClick(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (
                (event.useInteractedBlock().equals(Event.Result.DENY) || event.useItemInHand().equals(Event.Result.DENY)) ||
                block == null ||
                block.getType().equals(Material.AIR) ||
                !event.getAction().equals(Action.RIGHT_CLICK_BLOCK) ||
                !Cyclotron.CARGO_CONTAINER.contains(block.getType()) ||
                (!Filter.FILTER_QUERIES.containsKey(player.getUniqueId()) && !Filter.FILTER_QUERY_KIND.containsKey(player.getUniqueId()))
        ) {
            removeFlags(player.getUniqueId());
            return;
        }

        event.setCancelled(true);

        String type = Filter.FILTER_QUERY_KIND.get(player.getUniqueId()).toLowerCase();
        switch (type) {
            case "register" -> register(block, player);
            case "unregister" -> unregister(block, player);
            case "info" -> info(block, player);
            case "clear" -> {
                if (player.hasPermission("cyclotron.clear")) Util.blockDataClear(block);
            }
        }
        removeFlags(player.getUniqueId());

    }

    private void removeFlags(UUID uuid) {
        Filter.FILTER_QUERY_KIND.remove(uuid);
        Filter.FILTER_QUERIES.remove(uuid);
    }


    private void register(Block container, Player player) {
        String query = Filter.FILTER_QUERIES.get(player.getUniqueId());

        String[] hold = !container.hasMetadata(Cyclotron.CyclotronKey) ? new String[0] : Util.getHeldData(container);
        if (hold == null) return;
        else if (hold.length >= Cyclotron.FILTER_LIMIT) {
            player.sendMessage("You can not register a recipe to this container, cause this has already reached the limit of registration.");
            player.sendMessage(String.format("(limit = %d, container = %d)", Cyclotron.FILTER_LIMIT, hold.length));
            return;
        }

        for (String e : hold) {
            if (e.equals(query)) {
                player.sendMessage("You can not register a recipe to this container, cause this has already contained same one what you specified.");
                return;
            }
        }
        String[] newData = Arrays.copyOf(hold, hold.length + 1);
        newData[hold.length] = query;

        Util.replaceMetadata(container, new FixedMetadataValue(Cyclotron.INSTANCE, Util.convertFlat(newData)));
        Util.doubleChest(container);
        player.sendMessage(String.format("Successful to register '%s'.", query));
    }

    private void unregister(Block container, Player player) {
        String query = Filter.FILTER_QUERIES.get(player.getUniqueId());
        String[] data = Util.getHeldData(container);
        if (data == null || data.length == 0) {
            player.sendMessage("This container has not any data.");
            return;
        }
        List<String> oldData = Arrays.stream(data).toList();
        if (!oldData.contains(query)) {
            player.sendMessage("This container has not the specified recipe.");
            return;
        } else if (data.length == 1) {
            // oldData.contains(query) -> true, length = 1
            Util.blockDataClear(container);
            Util.doubleChest(container);
            player.sendMessage(String.format("Successful to unregister '%s'.", query));
            player.sendMessage("Now recipes='[]'.");
            return;
        }
        String[] newData = new String[data.length - 1];
        int index = 0;
        for (String e : oldData) {
            if (e.equals(query)) continue;
            newData[index] = e;
            index++;
        }
        Util.replaceMetadata(container, new FixedMetadataValue(Cyclotron.INSTANCE, Util.convertFlat(newData)));
        Util.doubleChest(container);
        player.sendMessage(String.format("Successful to unregister '%s'.", query));
        player.sendMessage("Now recipes=" + Arrays.toString(newData));
    }

    private void info(Block container, Player player) {
        String[] hold = Util.getHeldData(container);
        if (hold == null) {
            player.sendMessage("This container has not any data.");
            return;
        }

        String[] available = Util.getAvailableRecipes(hold);
        String[] notAvailable = Util.getNotAvailableRecipes(hold);

        player.sendMessage("This container has these recipes. ↓");
        player.sendMessage("Now recipes=" + Arrays.toString(available));
        if (notAvailable.length > 0) player.sendMessage(Component.text("Not available recipes=" + Arrays.toString(notAvailable), NamedTextColor.RED));
    }

    private void clear(Block container) {
        container.removeMetadata(Cyclotron.CyclotronKey, Cyclotron.INSTANCE);
        container.removeMetadata(Cyclotron.CyclotronIndexKey, Cyclotron.INSTANCE);
    }
}
