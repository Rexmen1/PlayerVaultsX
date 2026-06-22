/*
 * PlayerVaultsX
 * Copyright (C) 2013 Trent Hensler
 */

package com.drtshock.playervaults.vaultmanagement;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.config.file.Config;
import com.drtshock.playervaults.util.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.OptionalInt;
import java.util.logging.Level;

public final class VaultMenuManager {

    private static final String META_ROOT = "menu-meta";

    private VaultMenuManager() {
    }

    public static boolean isEnabled() {
        return PlayerVaults.getInstance().getConf().getVaultMenu().isEnabled();
    }

    public static void openMenu(Player player) {
        if (!isEnabled()) {
            PlayerVaults.getInstance().getTL().help().title().send(player);
            return;
        }
        if (!player.hasPermission(Permission.COMMANDS_USE)) {
            PlayerVaults.getInstance().getTL().noPerms().title().send(player);
            return;
        }

        Config.VaultMenu menu = PlayerVaults.getInstance().getConf().getVaultMenu();
        int slots = normalizeSlots(menu.getSlots());
        VaultMenuHolder holder = new VaultMenuHolder(player.getUniqueId());
        String title = colorize(menu.getTitle());
        Inventory inventory = Bukkit.createInventory(holder, slots, title);
        holder.setInventory(inventory);
        populateMenu(player, inventory);
        player.openInventory(inventory);
        PlayerVaults.getInstance().addInVaultMenu(player.getUniqueId());
    }

    public static void populateMenu(Player player, Inventory inventory) {
        Config.VaultMenu menu = PlayerVaults.getInstance().getConf().getVaultMenu();
        int slots = inventory.getSize();
        String uuid = player.getUniqueId().toString();

        for (int slot = 0; slot < slots; slot++) {
            int vaultNumber = slot + 1;
            boolean accessible = VaultOperations.checkPerms(player, vaultNumber);
            boolean hasData = VaultManager.getInstance().vaultExists(uuid, vaultNumber);
            VaultCustomization customization = getCustomization(uuid, vaultNumber);
            inventory.setItem(slot, createMenuItem(menu, vaultNumber, customization, accessible, hasData));
        }
    }

    public static ItemStack createMenuItem(Config.VaultMenu menu, int vaultNumber, VaultCustomization customization, boolean accessible, boolean hasData) {
        ItemStack item = resolveIcon(menu, customization, accessible);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        String name = customization != null && customization.hasDisplayName()
                ? customization.getDisplayName()
                : menu.getDefaultName().replace("<vault>", String.valueOf(vaultNumber));
        meta.setDisplayName(colorize(name));

        List<String> lore = new ArrayList<>();
        for (String line : menu.getLore()) {
            lore.add(colorize(line
                    .replace("<vault>", String.valueOf(vaultNumber))
                    .replace("<status>", hasData ? menu.getStatusHasItems() : menu.getStatusEmpty())));
        }
        if (!accessible) {
            lore.add(colorize(menu.getLockedLore()));
        } else {
            lore.add(colorize(menu.getEditLore()));
        }
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(menuKey(), PersistentDataType.INTEGER, vaultNumber);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack resolveIcon(Config.VaultMenu menu, VaultCustomization customization, boolean accessible) {
        if (!accessible) {
            return new ItemStack(parseMaterial(menu.getLockedMaterial(), Material.GRAY_STAINED_GLASS_PANE));
        }
        if (customization != null && customization.hasIcon()) {
            ItemStack custom = deserializeIcon(customization.getIconData());
            if (custom != null) {
                custom.setAmount(1);
                return custom;
            }
        }
        return new ItemStack(parseMaterial(menu.getDefaultIcon(), Material.CHEST));
    }

    public static VaultCustomization getCustomization(String uuid, int vaultNumber) {
        YamlConfiguration yaml = getMetaFile(uuid);
        String path = metaPath(vaultNumber);
        if (!yaml.contains(path)) {
            return new VaultCustomization();
        }
        VaultCustomization customization = new VaultCustomization();
        customization.setDisplayName(yaml.getString(path + ".name"));
        customization.setIconData(yaml.getString(path + ".icon"));
        return customization;
    }

    public static void setDisplayName(String uuid, int vaultNumber, String name) {
        YamlConfiguration yaml = getMetaFile(uuid);
        String path = metaPath(vaultNumber);
        if (name == null || name.isEmpty()) {
            yaml.set(path + ".name", null);
        } else {
            yaml.set(path + ".name", name);
        }
        saveMeta(uuid, yaml);
    }

    public static void setIcon(String uuid, int vaultNumber, ItemStack icon) {
        YamlConfiguration yaml = getMetaFile(uuid);
        String path = metaPath(vaultNumber);
        if (icon == null || icon.getType() == Material.AIR) {
            yaml.set(path + ".icon", null);
        } else {
            ItemStack copy = icon.clone();
            copy.setAmount(1);
            yaml.set(path + ".icon", serializeIcon(copy));
        }
        saveMeta(uuid, yaml);
    }

    public static void clearCustomization(String uuid, int vaultNumber) {
        YamlConfiguration yaml = getMetaFile(uuid);
        yaml.set(metaPath(vaultNumber), null);
        saveMeta(uuid, yaml);
    }

    public static OptionalInt getVaultNumber(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return OptionalInt.empty();
        }
        Integer value = item.getItemMeta().getPersistentDataContainer().get(menuKey(), PersistentDataType.INTEGER);
        if (value == null || value < 1) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(value);
    }

    public static boolean isMenuItem(ItemStack item) {
        return getVaultNumber(item).isPresent();
    }

    private static YamlConfiguration getMetaFile(String uuid) {
        File file = metaFile(uuid);
        if (!file.exists()) {
            return new YamlConfiguration();
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private static void saveMeta(String uuid, YamlConfiguration yaml) {
        File file = metaFile(uuid);
        file.getParentFile().mkdirs();
        try {
            yaml.save(file);
        } catch (IOException e) {
            PlayerVaults.getInstance().getLogger().log(Level.SEVERE, "Failed to save vault menu meta for " + uuid, e);
        }
    }

    private static File metaFile(String uuid) {
        return new File(PlayerVaults.getInstance().getVaultData(), META_ROOT + File.separator + uuid + ".yml");
    }

    private static String metaPath(int vaultNumber) {
        return "vaults." + vaultNumber;
    }

    private static int normalizeSlots(int slots) {
        if (slots < 9) {
            return 9;
        }
        if (slots > 54) {
            return 54;
        }
        int rows = (int) Math.ceil(slots / 9.0);
        return rows * 9;
    }

    private static String serializeIcon(ItemStack item) {
        try {
            return Base64.getMimeEncoder().encodeToString(ItemSerialization.serializeItem(item));
        } catch (Exception e) {
            PlayerVaults.getInstance().getLogger().log(Level.WARNING, "Failed to serialize vault menu icon", e);
            return null;
        }
    }

    private static ItemStack deserializeIcon(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            return ItemSerialization.deserializeItem(Base64.getMimeDecoder().decode(data));
        } catch (Exception e) {
            PlayerVaults.getInstance().getLogger().log(Level.WARNING, "Failed to deserialize vault menu icon", e);
            return null;
        }
    }

    private static Material parseMaterial(String name, Material fallback) {
        Material material = Material.matchMaterial(name);
        return material == null ? fallback : material;
    }

    private static String colorize(String input) {
        return input.replace('&', '§');
    }

    private static NamespacedKey menuKey() {
        return new NamespacedKey(PlayerVaults.getInstance(), "vault_menu");
    }
}
