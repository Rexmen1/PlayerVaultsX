/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.drtshock.playervaults.vaultmanagement;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.config.file.Config;
import com.drtshock.playervaults.util.Permission;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.OptionalInt;
import java.util.Set;

public final class VaultNavigation {

    public static final int NAV_ROW_SLOTS = 9;
    public static final int WINDOW_SIZE = 9;

    private VaultNavigation() {
    }

    public static boolean isEnabled() {
        return PlayerVaults.getInstance().getConf().getBottomRowNavigation().isEnabled();
    }

    public static int applyMinimumRows(int rows) {
        if (isEnabled() && rows < 2) {
            return 2;
        }
        return rows;
    }

    public static int getStorageSlotCount(int inventorySize) {
        if (!isEnabled()) {
            return inventorySize;
        }
        return Math.max(0, inventorySize - NAV_ROW_SLOTS);
    }

    public static int getNavRowStart(int inventorySize) {
        return getStorageSlotCount(inventorySize);
    }

    public static boolean isNavigationSlot(int slot, int inventorySize) {
        return isEnabled() && slot >= getNavRowStart(inventorySize) && slot < inventorySize;
    }

    public static ItemStack[] extractStorageContents(Inventory inventory) {
        ItemStack[] contents = inventory.getContents();
        if (!isEnabled()) {
            return contents;
        }
        int storageSize = getStorageSlotCount(inventory.getSize());
        return Arrays.copyOf(contents, storageSize);
    }

    public static void loadStorageContents(Inventory inventory, ItemStack[] deserialized, int storageSize) {
        ItemStack[] contents = new ItemStack[inventory.getSize()];
        if (deserialized != null) {
            for (int i = 0; i < Math.min(deserialized.length, storageSize); i++) {
                contents[i] = deserialized[i];
            }
        }
        inventory.setContents(contents);
    }

    public static void populateNavigationRow(Inventory inventory, Player viewer, String vaultOwner, int currentVault, boolean ownVault) {
        if (!isEnabled()) {
            return;
        }

        int navStart = getNavRowStart(inventory.getSize());
        Config.BottomRowNavigation nav = PlayerVaults.getInstance().getConf().getBottomRowNavigation();
        int maxVault = getMaxDisplayVault(viewer, vaultOwner, ownVault);
        int windowStart = getWindowStart(currentVault, maxVault);

        for (int i = 0; i < WINDOW_SIZE; i++) {
            int slot = navStart + i;
            int vaultNumber = windowStart + i;
            if (vaultNumber > maxVault) {
                inventory.setItem(slot, createFiller(nav));
                continue;
            }

            boolean accessible = canAccess(viewer, vaultOwner, vaultNumber, ownVault);
            boolean current = vaultNumber == currentVault;
            inventory.setItem(slot, createVaultButton(nav, vaultNumber, current, accessible));
        }
    }

    private static int getWindowStart(int currentVault, int maxVault) {
        int start = Math.max(1, currentVault - 4);
        if (start + WINDOW_SIZE - 1 > maxVault) {
            start = Math.max(1, maxVault - WINDOW_SIZE + 1);
        }
        return start;
    }

    private static int getMaxDisplayVault(Player viewer, String vaultOwner, boolean ownVault) {
        if (ownVault) {
            return Math.max(VaultOperations.countVaults(viewer), currentVaultFloor(viewer));
        }
        Set<Integer> vaults = VaultManager.getInstance().getVaultNumbers(vaultOwner);
        if (vaults.isEmpty()) {
            return PlayerVaults.getInstance().getMaxVaultAmountPermTest();
        }
        int max = 0;
        for (int vault : vaults) {
            max = Math.max(max, vault);
        }
        return max;
    }

    private static int currentVaultFloor(Player viewer) {
        for (int x = PlayerVaults.getInstance().getMaxVaultAmountPermTest(); x >= 1; x--) {
            if (viewer.hasPermission(Permission.amount(x))) {
                return x;
            }
        }
        return 1;
    }

    private static boolean canAccess(Player viewer, String vaultOwner, int vaultNumber, boolean ownVault) {
        if (ownVault) {
            return viewer.hasPermission(Permission.amount(vaultNumber));
        }
        return VaultManager.getInstance().vaultExists(vaultOwner, vaultNumber)
                || viewer.hasPermission(Permission.ADMIN);
    }

    public static OptionalInt getTargetVault(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return OptionalInt.empty();
        }
        Integer value = item.getItemMeta().getPersistentDataContainer().get(navKey(), PersistentDataType.INTEGER);
        if (value == null || value <= 0) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(value);
    }

    public static boolean isNavigationItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(navKey(), PersistentDataType.INTEGER);
    }

    private static ItemStack createVaultButton(Config.BottomRowNavigation nav, int vaultNumber, boolean current, boolean accessible) {
        Material material;
        String name;
        if (current) {
            material = parseMaterial(nav.getCurrentMaterial(), Material.LIME_STAINED_GLASS_PANE);
            name = nav.getCurrentName().replace("<vault>", String.valueOf(vaultNumber));
        } else if (accessible) {
            material = parseMaterial(nav.getVaultMaterial(), Material.LIGHT_BLUE_STAINED_GLASS_PANE);
            name = nav.getVaultName().replace("<vault>", String.valueOf(vaultNumber));
        } else {
            material = parseMaterial(nav.getLockedMaterial(), Material.GRAY_STAINED_GLASS_PANE);
            name = nav.getLockedName().replace("<vault>", String.valueOf(vaultNumber));
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(colorize(name));
        if (accessible && !current) {
            meta.getPersistentDataContainer().set(navKey(), PersistentDataType.INTEGER, vaultNumber);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createFiller(Config.BottomRowNavigation nav) {
        ItemStack item = new ItemStack(parseMaterial(nav.getFillerMaterial(), Material.BLACK_STAINED_GLASS_PANE));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private static Material parseMaterial(String name, Material fallback) {
        Material material = Material.matchMaterial(name);
        return material == null ? fallback : material;
    }

    private static String colorize(String input) {
        return input.replace('&', '§');
    }

    private static NamespacedKey navKey() {
        return new NamespacedKey(PlayerVaults.getInstance(), "vault_nav");
    }
}
