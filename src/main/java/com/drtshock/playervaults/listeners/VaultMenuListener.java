/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler
 */

package com.drtshock.playervaults.listeners;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.util.Permission;
import com.drtshock.playervaults.vaultmanagement.VaultMenuHolder;
import com.drtshock.playervaults.vaultmanagement.VaultMenuManager;
import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import com.drtshock.playervaults.vaultmanagement.VaultViewInfo;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.OptionalInt;

public class VaultMenuListener implements Listener {

    private final PlayerVaults plugin;

    public VaultMenuListener(PlayerVaults plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof VaultMenuHolder holder)) {
            return;
        }
        if (!holder.getOwner().equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() != top) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        OptionalInt vaultNumber = VaultMenuManager.getVaultNumber(clicked);
        if (vaultNumber.isEmpty()) {
            return;
        }

        int number = vaultNumber.getAsInt();
        if (!VaultOperations.checkPerms(player, number)) {
            this.plugin.getTL().noPerms().title().send(player);
            return;
        }

        if (event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_RIGHT) {
            handleRightClick(player, top, number, event.getClick(), event.getCursor());
            return;
        }

        if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT) {
            openVaultFromMenu(player, number);
        }
    }

    private void handleRightClick(Player player, Inventory menu, int vaultNumber, ClickType click, ItemStack cursor) {
        String uuid = player.getUniqueId().toString();

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand != null && hand.getType() != Material.AIR) {
            VaultMenuManager.setIcon(uuid, vaultNumber, hand);
            VaultMenuManager.populateMenu(player, menu);
            this.plugin.getTL().vaultMenuIconSet().title().with("vault", String.valueOf(vaultNumber)).send(player);
            return;
        }

        if (click == ClickType.SHIFT_RIGHT) {
            VaultMenuManager.clearCustomization(uuid, vaultNumber);
            VaultMenuManager.populateMenu(player, menu);
            this.plugin.getTL().vaultMenuReset().title().with("vault", String.valueOf(vaultNumber)).send(player);
            return;
        }

        this.plugin.beginVaultNameEdit(player.getUniqueId(), vaultNumber);
        this.plugin.getTL().vaultMenuNamePrompt().title().with("vault", String.valueOf(vaultNumber)).send(player);
    }

    private void openVaultFromMenu(Player player, int number) {
        if (PlayerVaults.getInstance().getInVault().containsKey(player.getUniqueId().toString())) {
            return;
        }

        player.closeInventory();
        PlayerVaults.getInstance().removeInVaultMenu(player.getUniqueId());

        if (VaultOperations.openOwnVault(player, String.valueOf(number), true)) {
            PlayerVaults.getInstance().getInVault().put(player.getUniqueId().toString(),
                    new VaultViewInfo(player.getUniqueId().toString(), number));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof VaultMenuHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (event.getInventory().getHolder() instanceof VaultMenuHolder) {
            PlayerVaults.getInstance().removeInVaultMenu(player.getUniqueId());
            this.plugin.cancelVaultNameEdit(player.getUniqueId());
        }
    }
}
