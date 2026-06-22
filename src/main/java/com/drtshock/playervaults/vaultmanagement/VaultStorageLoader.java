/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler
 */

package com.drtshock.playervaults.vaultmanagement;

import com.drtshock.playervaults.PlayerVaults;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class VaultStorageLoader {

    private VaultStorageLoader() {
    }

    /**
     * Loads vault item data into the storage area, migrating overflow from legacy full-size saves.
     */
    public static void loadIntoInventory(Inventory inventory, ItemStack[] deserialized, int storageSize, Player viewer) {
        if (deserialized == null) {
            return;
        }

        int limit = VaultNavigation.isEnabled() ? storageSize : inventory.getSize();
        if (deserialized.length <= limit) {
            VaultNavigation.loadStorageContents(inventory, deserialized, storageSize);
            return;
        }

        List<ItemStack> items = new ArrayList<>();
        for (ItemStack stack : deserialized) {
            if (stack != null && stack.getType() != Material.AIR) {
                items.add(stack);
            }
        }

        ItemStack[] storage = new ItemStack[storageSize];
        List<ItemStack> overflow = new ArrayList<>();
        int index = 0;
        for (ItemStack stack : items) {
            if (index < storageSize) {
                storage[index++] = stack;
            } else {
                overflow.add(stack);
            }
        }
        VaultNavigation.loadStorageContents(inventory, storage, storageSize);

        if (!overflow.isEmpty() && viewer != null && viewer.isOnline()) {
            for (ItemStack stack : overflow) {
                viewer.getWorld().dropItemNaturally(viewer.getLocation(), stack);
            }
            PlayerVaults.getInstance().getTL().vaultRowMigrated().title()
                    .with("count", String.valueOf(overflow.size()))
                    .send(viewer);
        }
    }
}
