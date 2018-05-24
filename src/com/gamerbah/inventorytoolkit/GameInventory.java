package com.gamerbah.inventorytoolkit;
/* Created by GamerBah on 7/22/2017 */

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.HashMap;

public class GameInventory implements InventoryHolder {

    @Getter
    private final GameInventory previousInventory;

    @Getter
    @Setter
    private Inventory inventory;
    @Getter
    private String name;
    @Getter
    private int itemCount = 0;
    @Getter
    private int topOffset = 0;
    @Getter
    private int bottomOffset = 0;
    @Getter
    private int searchStart = -1;
    @Getter
    private int searchEnd = -1;
    @Getter
    private int pageRow = -1;
    @Getter
    private boolean inlineNav = false;
    @Getter
    private boolean backButton = true;
    @Getter
    private boolean allowCreative = false;
    @Getter
    private boolean showPageNumbers = false;
    @Getter
    private String pageNumberFormat = "(%c/%m)";

    private ArrayList<ItemBuilder> items = new ArrayList<>();
    private HashMap<Integer, ItemBuilder> buttons = new HashMap<>();
    private HashMap<Integer, DyeColor> borders = new HashMap<>();

    /**
     * Creates a new GameInventory object
     *
     * @param name              the name of the {@link Inventory}
     * @param itemCount         the amount of preset items in the {@link Inventory}
     * @param size              the size of the {@link Inventory}
     * @param previousInventory the GameInventory to go back to when a "Back" button is clicked
     */
    public GameInventory(final String name, final int itemCount, final int size, final GameInventory previousInventory) {
        this.inventory = Bukkit.getServer().createInventory(this, size, name);
        this.name = name;
        this.itemCount = itemCount;
        this.previousInventory = previousInventory;
    }

    /**
     * Creates a new GameInventory object sans a previous {@link Inventory}
     *
     * @param name      the name of the {@link Inventory}
     * @param itemCount the amount preset items in the {@link Inventory}
     * @param size      the size of the {@link Inventory}
     */
    public GameInventory(final String name, final int itemCount, final int size) {
        this(name, itemCount, size, null);
    }

    /**
     * Creates a new GameInventory object sans preset items
     *
     * @param name              the name of the {@link Inventory}
     * @param size              the size of the {@link Inventory}
     * @param previousInventory the GameInventory to go back to when a "Back" button is clicked
     */
    public GameInventory(final String name, final int size, final GameInventory previousInventory) {
        this(name, 0, size, previousInventory);
    }

    /**
     * Creates a new GameInventory object sans a previous {@link Inventory} and preset items
     *
     * @param name the name of the {@link Inventory}
     * @param size the size of the {@link Inventory}
     */
    public GameInventory(final String name, final int size) {
        this(name, 0, size, null);
    }

    /**
     * Creates a new GameInventory object of size 54 sans preset items
     *
     * @param name              the name of the {@link Inventory}
     * @param previousInventory the GameInventory to go back to when a "Back" button is clicked
     */
    public GameInventory(final String name, final GameInventory previousInventory) {
        this(name, 54, previousInventory);
    }

    /**
     * Adds a new sortable item
     * <p>
     * NOTE: Items are not physically added to the {@link Inventory} until it's opened
     *
     * @param itemBuilder the {@link ItemBuilder} to add
     * @see ItemBuilder
     * @see InventoryBuilder
     */
    protected void addItem(final ItemBuilder itemBuilder) {
        items.add(itemBuilder.clone());
    }

    /**
     * Gets the list of sortable items
     *
     * @return the list of items
     */
    public ArrayList<ItemBuilder> getItems() {
        ArrayList<ItemBuilder> items = new ArrayList<>();
        for (ItemBuilder item : this.items)
            items.add(item.clone());
        return items;
    }

    /**
     * Adds a static, unsortable item that is added to the
     * inventory once the inventory is opened
     *
     * @param slot        the {@link Inventory} slot this item will be held in
     * @param itemBuilder the {@link ItemBuilder} to add
     * @see ItemBuilder
     * @see InventoryBuilder
     */
    protected void addButton(final int slot, final ItemBuilder itemBuilder) {
        if (slot >= inventory.getSize())
            throw new IllegalArgumentException("slot must be < inventory size!");
        this.buttons.put(slot, itemBuilder);
    }

    /**
     * Gets the list of static buttons
     *
     * @return the list of buttons, deep copied
     */
    public HashMap<Integer, ItemBuilder> getButtons() {
        return Maps.newHashMap(this.buttons);
    }

    /**
     * Clears the list of buttons
     */
    public void clearButtons() {
        this.buttons.clear();
    }

    /**
     * Opens the previous GameInventory specified in this class
     * <p>
     * If no previous GameInventory is set, the player's {@link Inventory} is closed
     *
     * @param player the {@link Player} to open the previous GameInventory for
     */
    protected void openPreviousInventory(final Player player) {
        if (this.getPreviousInventory() != null) {
            new InventoryBuilder(player, this.getPreviousInventory()).open();
        } else {
            player.closeInventory();
        }
    }

    /**
     * Manually sets the item count if that constructor was not used
     *
     * @param amount the amount of items
     */
    protected void setItemCount(final int amount) {
        itemCount = amount;
    }

    /**
     * Sets the amount of search rows for inventories that should act as "data libraries"
     * <p>
     * Not applicable for inventories that aren't meant for searching (i.e selection-type menus)<br>
     * (Slots 1-9 are row 1, 10-18: 2, etc.)
     *
     * @param startRow the {@link Inventory} row to start the search box at
     * @param stopRow  the {@link Inventory} row to stop the search box at
     * @throws IllegalArgumentException if the rows are {@literal >} max rows of the {@link Inventory}, and if startRow is {@literal >} stopRow
     */
    protected void setSearchRows(final int startRow, final int stopRow) {
        if (stopRow < startRow)
            throw new IllegalArgumentException("Start Row cannot be < Stop Row!");
        if (startRow > (inventory.getSize() / 9) || stopRow > (inventory.getSize() / 9))
            throw new IllegalArgumentException("Rows cannot be > max rows of inventory!");
        searchStart = startRow;
        searchEnd = stopRow;
    }

    /**
     * Gets the amount of rows in the search box
     *
     * @return the amount of rows
     */
    public int getSearchRows() {
        return (searchEnd - searchStart) + 1;
    }

    /**
     * Sets a search box offset
     * <p>
     * Each offset will create an blank slot starting on the outside going in<br>
     * i.e if the top offset is 2 and the bottom offset is 3 in a search box for rows 1 {@literal &} 2,
     * the search items will begin at slot 3 and end at slot 15
     *
     * @param top    the amount of slots to keep empty at the front of the search box
     * @param bottom the amount of slots to keep empty at the bottom of the search box
     */
    protected void setSearchOffset(final int top, final int bottom) {
        this.topOffset = top;
        this.bottomOffset = bottom;
    }

    /**
     * Sets a search box offset that is bi-directionally equidistant
     * <p>
     * Calls {@code setSearchOffset(int top, int bottom)} keeping both values the same
     *
     * @param offset the offset to apply to each end of the search box
     */
    protected void setSearchOffset(final int offset) {
        this.setSearchOffset(offset, offset);
    }

    /**
     * Adds a row of immovable colored stained glass to act as a border
     * <p>
     * This is mainly between a search box and search sorting items
     *
     * @param row   the {@link Inventory} row to place the border at
     * @param color the {@link DyeColor} that the stained glass should be
     * @throws IllegalArgumentException if the row is {@literal >} max rows of the {@link Inventory}, or if there is already a border at that row
     */
    protected void addBorder(final int row, final DyeColor color) {
        if (row > (inventory.getSize() / 9))
            throw new IllegalArgumentException("Row cannot be > max rows of inventory!");
        if (borders.containsKey(row))
            throw new IllegalArgumentException("That row is already a border!");
        borders.put(row, color);
    }

    /**
     * Adds a row of immovable black stained glass to act as a border
     * <p>
     * Makes a call to {@code addBorder(int row, DyeColor color)}
     *
     * @param row the {@link Inventory} row to place the border at
     */
    protected void addBorder(final int row) {
        addBorder(row, DyeColor.BLACK);
    }

    /**
     * Removes a border
     *
     * @param row the {@link Inventory} row to place the border at
     * @throws IllegalArgumentException if the row is {@literal >} max rows of the {@link Inventory}, or if there isn't a border at that row
     */
    protected void removeBorder(final int row) {
        if (row > (inventory.getSize() / 9))
            throw new IllegalArgumentException("Row cannot be > max rows of inventory!");
        if (!borders.containsKey(row))
            throw new IllegalArgumentException("Row " + row + " isn't a border!");
        borders.remove(row);
    }

    /**
     * Removes all borders, if any are set
     */
    protected void removeBorders() {
        borders.clear();
    }

    /**
     * Sets whether page navigation buttons are placed in offset slots
     * This will add 1 to the offsets
     *
     * @param backButton sets if a "Back" button will be visible on page 1
     * @throws UnsupportedOperationException if there are no search rows
     */
    protected void setBackButton(boolean backButton) {
        this.backButton = backButton;
    }

    /**
     * Sets whether page navigation buttons are placed inline
     * <p>This adds 1 to the current offsets</p>
     *
     * @param inline forces page navigation buttons to be placed in-line with search boxes
     * @throws UnsupportedOperationException if there are no search rows
     */
    protected void setInlineNavigation(final boolean inline) {
        if (searchStart == -1 && searchEnd == -1)
            throw new UnsupportedOperationException("Cannot alter navigation is no search box is set!");
        if (inline) {
            this.topOffset += 1;
            this.bottomOffset += 1;
        }
        this.inlineNav = inline;
    }

    /**
     * Sets whether this inventory is able to be clicked while in creative mode
     *
     * @param allowCreative whether to allow creative clicks
     */
    protected void setAllowCreative(final boolean allowCreative) {
        this.allowCreative = allowCreative;
    }

    /**
     * Sets whether to display page numbers in the title of the inventory using the default format (%c/%m)
     *
     * @param showPageNumbers whether to show page numbers
     */
    protected void setShowPageNumbers(final boolean showPageNumbers) {
        this.showPageNumbers = showPageNumbers;
    }

    /**
     * Sets whether to display page numbers in the title of the inventory
     * using the provided format
     * <p>
     * The format can be provided two placeholders:
     * <br>%c - the current page
     * <br>%m - the max pages
     * <br>
     * <br>ChatColors are able to be used, in addition to any other natural characters
     * </p>
     *
     * @param showPageNumbers whether to show page numbers in the title or not
     * @param format          the format to use
     */
    protected void setShowPageNumbers(final boolean showPageNumbers, final String format) {
        this.showPageNumbers = showPageNumbers;
        this.pageNumberFormat = format;
    }

    /**
     * Sets which row page navigation buttons will appear on
     *
     * @param row the inventory row (0-based) to put the buttons on
     * @throws UnsupportedOperationException if there are no search rows, or if {@code inlineNav == true}
     * @throws IllegalArgumentException      if row is {@literal >} the max rows in the inventory
     */
    protected void setPageRow(final int row) {
        if (searchStart == -1 && searchEnd == -1)
            throw new UnsupportedOperationException("Cannot alter navigation is no search box is set!");
        if (row > (inventory.getSize() / 9))
            throw new IllegalArgumentException("Row cannot be > max rows of inventory!");
        this.pageRow = row;
    }

    /**
     * Builds a new {@link InventoryBuilder} object from the current GameInventory
     *
     * @param player the {@link Player} to build this {@link Inventory} for
     * @return a new {@link InventoryBuilder} instance of this GameInventory
     */
    public InventoryBuilder build(final Player player) {
        if (!borders.isEmpty()) {
            borders.forEach((row, color) -> {
                for (int i = row * 9; i < ((row * 9) + 9); i++)
                    addButton(i, new ItemBuilder(Material.STAINED_GLASS_PANE).name(" ").durability(Byte.toUnsignedInt(color.getWoolData())));
            });
        }
        return new InventoryBuilder(player, this);
    }
}