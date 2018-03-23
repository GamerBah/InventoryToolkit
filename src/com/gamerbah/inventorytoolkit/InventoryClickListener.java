package com.gamerbah.inventorytoolkit;
/* Created by GamerBah on 12/18/2017 */

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Optional;

public class InventoryClickListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof GameInventory) {
            Inventory inventory = event.getInventory();
            final Player player = (Player) event.getWhoClicked();

            if (event.getSlotType() == null || event.getCurrentItem() == null
                    || event.getCurrentItem().getType() == null || event.getCurrentItem().getItemMeta() == null)
                event.setCancelled(true);

            if (inventory == player.getInventory())
                event.setCancelled(true);

            for (ItemStack itemStack : player.getInventory().getArmorContents())
                if (itemStack == null || event.getCurrentItem().equals(itemStack))
                    event.setCancelled(true);

            if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null && event.getCurrentItem().getItemMeta().getDisplayName() != null) {
                if (InventoryToolKit.getInventoryUsers().containsKey(player)) {
                    ItemBuilder itemBuilder = null;
                    GameInventory gameInventory = InventoryToolKit.getInventoryUsers().get(player).getGameInventory();
                    if (player.getGameMode() != GameMode.CREATIVE || gameInventory.isAllowCreative()) {
                        if (gameInventory.getButtons().keySet().contains(event.getSlot()))
                            itemBuilder = gameInventory.getButtons().get(event.getSlot());

                        if (itemBuilder == null)
                            for (ItemBuilder items : gameInventory.getItems())
                                if (items.isSimilar(event.getCurrentItem()))
                                    itemBuilder = items;

                        if (itemBuilder != null)
                            if (itemBuilder.getRequiredPermissions().isEmpty() || itemBuilder.getRequiredPermissions().keySet().stream().allMatch(player::hasPermission)) {
                                if (itemBuilder.getClickEvents() != null && !itemBuilder.getClickEvents().isEmpty())
                                    for (ClickEvent clickEvent : itemBuilder.getClickEvents()) {
                                        boolean contains = false;
                                        for (ClickEvent.Type clickType : clickEvent.getClickTypes())
                                            if (event.getClick() == clickType.getClickType())
                                                contains = true;
                                        if (contains || Arrays.asList(clickEvent.getClickTypes()).contains(ClickEvent.Type.ANY))
                                            clickEvent.getAction().run();
                                    }
                            } else {
                                player.playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1, 1);
                                Optional<String> optional = itemBuilder.getRequiredPermissions().keySet().stream().filter(player::hasPermission).findFirst();
                                player.sendMessage(optional.isPresent() ? itemBuilder.getRequiredPermissions().get(optional.get()) : ChatColor.RED + "You don't have permission to use that!");
                                event.setCancelled(true);
                            }
                    } else event.setCancelled(true);
                }
            }
        }
    }

}
