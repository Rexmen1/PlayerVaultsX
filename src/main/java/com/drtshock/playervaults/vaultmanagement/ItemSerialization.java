package com.drtshock.playervaults.vaultmanagement;

import dev.kitteh.cardboardbox.CardboardBox;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

/**
 * Item serialization with CardboardBox when available, otherwise Paper's native bytes API.
 */
public final class ItemSerialization {

    private static final Method SERIALIZE_AS_BYTES;
    private static final Method DESERIALIZE_BYTES;
    private static final boolean PAPER_BYTES;

    static {
        Method serialize = null;
        Method deserialize = null;
        try {
            serialize = ItemStack.class.getMethod("serializeAsBytes");
            deserialize = ItemStack.class.getMethod("deserializeBytes", byte[].class);
        } catch (ReflectiveOperationException ignored) {
        }
        SERIALIZE_AS_BYTES = serialize;
        DESERIALIZE_BYTES = deserialize;
        PAPER_BYTES = serialize != null && deserialize != null;
    }

    private ItemSerialization() {
    }

    public static boolean isAvailable() {
        return CardboardBox.isReady() || PAPER_BYTES;
    }

    public static boolean usesPaperBytes() {
        return !CardboardBox.isReady() && PAPER_BYTES;
    }

    public static byte[] serializeItem(ItemStack item) {
        if (CardboardBox.isReady()) {
            return CardboardBox.serializeItem(item);
        }
        if (item == null || item.getType() == Material.AIR) {
            return new byte[]{0x0};
        }
        if (PAPER_BYTES) {
            try {
                return (byte[]) SERIALIZE_AS_BYTES.invoke(item);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to serialize item with Paper bytes API", e);
            }
        }
        throw new IllegalStateException("No item serialization backend is available", CardboardBox.getException());
    }

    public static ItemStack deserializeItem(byte[] data) {
        if (CardboardBox.isReady()) {
            return CardboardBox.deserializeItem(data);
        }
        if (data == null || data.length == 0 || (data.length == 1 && data[0] == 0x0)) {
            return new ItemStack(Material.AIR);
        }
        if (PAPER_BYTES) {
            try {
                return (ItemStack) DESERIALIZE_BYTES.invoke(null, data);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to deserialize item with Paper bytes API", e);
            }
        }
        throw new IllegalStateException("No item serialization backend is available", CardboardBox.getException());
    }
}
