package com.gamerbah.inventorytoolkit;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import java.lang.reflect.Field;
import java.util.*;

/**
 * A chainable {@link ItemStack} builder
 *
 * @author MiniDigger, computerwizjared, GamerBah
 * @version 2.0
 */
public class ItemBuilder extends ItemStack {

    @Getter
    private HashSet<ClickEvent> clickEvents = new HashSet<>();
    @Getter
    private HashMap<Class, Object> storedObjects = new HashMap<>();
    @Getter
    private HashMap<String, String> requiredPermissions = new HashMap<>();

    public ItemBuilder(final Material mat) {
        super(mat);
    }

    public ItemBuilder(final ItemStack is) {
        super(is);
    }

    public ItemBuilder(final ItemBuilder ib) {
        setType(ib.getType());
        setData(ib.getData());
        setAmount(ib.getAmount());
        setItemMeta(ib.getItemMeta());
        setDurability(ib.getDurability());
        for (ClickEvent clickEvent : ib.clickEvents)
            onClick(clickEvent);
        storedObjects.putAll(ib.getStoredObjects());
    }

    public ItemBuilder amount(final int amount) {
        setAmount(amount);
        return this;
    }

    public ItemBuilder name(final String name) {
        final ItemMeta meta = getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder lore(final String text) {
        final ItemMeta meta = getItemMeta();
        if (meta != null) {
            List<String> lore = (meta.getLore() != null ? meta.getLore() : new ArrayList<>());
            String[] split = text.split("\n");
            lore.addAll(Arrays.asList(split));
            meta.setLore(lore);
            setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder durability(final int durability) {
        setDurability((short) durability);
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder data(final int data) {
        setData(new MaterialData(getType(), (byte) data));
        return this;
    }

    public ItemBuilder enchantment(final Enchantment enchantment, final int level) {
        addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder enchantment(final Enchantment enchantment) {
        addUnsafeEnchantment(enchantment, 1);
        return this;
    }

    public ItemBuilder type(final Material material) {
        setType(material);
        return this;
    }

    public ItemBuilder clearLore() {
        final ItemMeta meta = getItemMeta();
        meta.setLore(new ArrayList<>());
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder clearEnchantments() {
        getEnchantments().keySet().forEach(this::removeEnchantment);
        return this;
    }

    public ItemBuilder color(final Color color) {
        if (getType() == Material.LEATHER_BOOTS || getType() == Material.LEATHER_CHESTPLATE || getType() == Material.LEATHER_HELMET
                || getType() == Material.LEATHER_LEGGINGS) {
            LeatherArmorMeta meta = (LeatherArmorMeta) getItemMeta();
            meta.setColor(color);
            setItemMeta(meta);
            return this;
        } else {
            throw new IllegalArgumentException("color is only applicable for leather armor");
        }
    }

    public ItemBuilder flag(final ItemFlag flag) {
        final ItemMeta meta = getItemMeta();
        meta.addItemFlags(flag);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder clearFlags() {
        final ItemMeta meta = getItemMeta();
        meta.getItemFlags().forEach(meta::removeItemFlags);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder unbreakable() {
        final ItemMeta meta = getItemMeta();
        meta.setUnbreakable(true);
        setItemMeta(meta);
        return this;
    }

    /**
     * Allows simple creation of an InventoryClickEvent without
     * the need to mess around in an actual InventoryClickEvent class
     *
     * @param event the ClickEvent
     * @return this class for chaining
     * @see ClickEvent
     */
    public ItemBuilder onClick(final ClickEvent event) {
        getClickEvents().add(event);
        return this;
    }

    /**
     * Allows the ItemBuilder to store an object for use in sorting or data-retention
     * <p>
     * Only 1 object per class is allowed
     * </p>
     *
     * @param clazz  the class that object is an instance of
     * @param object the object to store
     * @return this class for chaining
     */
    public ItemBuilder storeObject(final Class clazz, final Object object) {
        this.storedObjects.put(clazz, object);
        return this;
    }

    /**
     * Adds a permission required to click on this item, using a custom disallow message
     *
     * @param permission the permission node to add
     * @param message    the disallow message
     * @return this class for chaining
     */
    public ItemBuilder addPermission(final String permission, final String message) {
        this.requiredPermissions.put(Objects.requireNonNull(permission, "permission cannot be null"),
                Objects.requireNonNull(message, "message cannot be null"));
        return this;
    }

    /**
     * Adds a permission required to click on this item
     * <p>
     * Makes a call to {@code addPermission(String permission, String message)}
     * </p>
     *
     * @param permission the permission node to add
     * @return this class for chaining
     */
    public ItemBuilder addPermission(final String permission) {
        this.requiredPermissions.put(Objects.requireNonNull(permission, "permission cannot be null"),
                ChatColor.RED + "You don't have permission to use that!");
        return this;
    }

    /**
     * Creates a new instance of the current ItemBuilder
     *
     * @return the new instance of this class
     */
    @Override
    public ItemBuilder clone() {
        return new ItemBuilder(this);
    }

    /**
     * Gets a skull item that has a custom texture
     *
     * @param url the url for the texture
     * @return this class for chaining
     * @throws Exception if the texture failed to resolve
     */
    public static ItemBuilder customSkull(final String url) throws Exception {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        ItemMeta meta = item.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        PropertyMap propertyMap = profile.getProperties();
        propertyMap.put("textures", new Property("textures", new String(Base64.getEncoder().encode(("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes()), Charsets.UTF_8)));
        Field f = meta.getClass().getDeclaredField("profile");
        f.setAccessible(true);
        f.set(meta, profile);
        item.setItemMeta(meta);
        return new ItemBuilder(item);
    }

    /**
     * Gets a player skull from an OfflinePlayer
     *
     * @param player the OfflinePlayer to grab from
     * @return this class for chaining
     */
    public static ItemBuilder playerSkull(final OfflinePlayer player) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        item.setItemMeta(meta);
        return new ItemBuilder(item);
    }

}
