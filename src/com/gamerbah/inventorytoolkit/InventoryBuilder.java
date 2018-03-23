package com.gamerbah.inventorytoolkit;
/* Created by GamerBah on 7/17/2017 */

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;

public class InventoryBuilder {

    @Getter
    private Player player;
    @Getter
    private GameInventory gameInventory;
    @Getter
    private int page = 0, maxPage;
    @Getter
    private String search = "";
    @Getter
    private boolean onlineOnly = false;
    @Getter
    private ArrayList<ItemBuilder> items;
    @Getter
    @Setter
    private ItemBuilder nextPageItem = new ItemBuilder(Material.ARROW).name(ChatColor.GRAY + "Next Page");
    @Getter
    @Setter
    private ItemBuilder previousPageItem = new ItemBuilder(Material.ARROW).name(ChatColor.GRAY + "Previous Page");
    @Getter
    @Setter
    private ItemBuilder backItem = new ItemBuilder(Material.ARROW).name(ChatColor.GRAY + "Back");

    private int itemsPerPage;

    /**
     * Creates a new chainable builder for the specified {@link Player} and {@link GameInventory}
     *
     * @param player        the {@link Player} this {@link GameInventory} will be displayed to upon calling {@code open()}
     * @param gameInventory the {@link GameInventory} to build from
     * @see GameInventory
     */
    public InventoryBuilder(Player player, GameInventory gameInventory) {
        this.player = player;
        this.gameInventory = gameInventory;
        this.items = gameInventory.getItems();
        this.itemsPerPage = (gameInventory.getSearchRows() * 9) - gameInventory.getTopOffset() - gameInventory.getBottomOffset();
        this.maxPage = (items.size() <= itemsPerPage ? 0 : (int) Math.floor(items.size() / itemsPerPage));
        InventoryToolKit.getInventoryUsers().put(player, this);
    }


    /**
     * Sorts the items using the given {@link Comparator}
     *
     * @param comparator the {@link Comparator} to use for comparing items
     * @return this class for chaining
     */
    public InventoryBuilder sortItems(final Comparator<ItemBuilder> comparator) {
        items.sort(comparator);
        return this;
    }

    /**
     * Sets this builder instance to the specified page without opening it to the {@link Player}
     * <p>
     * Next Page, Previous Page, and Back button items are automatically added accordingly, so
     * there's no need to manually add them per-page
     *
     * @param number the page to go to
     * @return this class for chaining
     * @throws IllegalArgumentException when number is {@literal >} the max page and {@literal <} 0
     * @deprecated Use {@code open(int page)} instead
     */
    @Deprecated
    public InventoryBuilder page(final int number) {
        if (number > maxPage || number < 0)
            throw new IllegalArgumentException("Number must be <= maxPage and >= 0");
        this.page = number;

        if (gameInventory.isShowPageNumbers()) {
            final int size = gameInventory.getInventory().getSize();
            final String name = gameInventory.getInventory().getName();
            gameInventory.setInventory(Bukkit.getServer().createInventory(gameInventory, size,
                    name + " " + gameInventory.getPageNumberFormat().replace("%c", page + "").replace("%m", maxPage + "")));
        }

        int index = number * itemsPerPage;
        if (this.items.size() > 0) {
            for (int i = (gameInventory.getSearchStart() * 9) + gameInventory.getTopOffset(); i < itemsPerPage; i++) {
                if (index < this.items.size()) {
                    gameInventory.getInventory().setItem(i, this.items.get(index++));
                } else {
                    gameInventory.getInventory().setItem(i, null);
                }
            }
        }

        if (page < maxPage) {
            if (gameInventory.getPageRow() == -1) {
                gameInventory.addButton((gameInventory.getSearchRows() * 9) - 1,
                        getNextPageItem().clone().onClick(new ClickEvent(() -> this.nextPage().open())));
            } else gameInventory.addButton((9 * (gameInventory.getPageRow() + 1)) - 1,
                    getNextPageItem().clone().onClick(new ClickEvent(() -> this.nextPage().open())));
        } else {
            if (gameInventory.getPageRow() == -1) {
                gameInventory.getInventory().clear((gameInventory.getSearchRows() * 9) - 1);
            } else gameInventory.getInventory().clear((9 * (gameInventory.getPageRow() + 1)) - 1);
        }
        if (page > 0) {
            if (gameInventory.getPageRow() == -1) {
                gameInventory.addButton(gameInventory.getSearchRows() * 9,
                        getPreviousPageItem().clone().onClick(new ClickEvent(() -> this.previousPage().open())));
            } else gameInventory.addButton(gameInventory.getPageRow() * 9,
                    getPreviousPageItem().clone().onClick(new ClickEvent(() -> this.previousPage().open())));
        } else if (page == 0)
            if (gameInventory.isBackButton())
                if (gameInventory.getPageRow() != -1) {
                    gameInventory.addButton(9 * gameInventory.getPageRow(),
                            getBackItem().clone().onClick(new ClickEvent(() -> this.gameInventory.openPreviousInventory(player))));
                } else {
                    if (gameInventory.getSearchRows() > 0)
                        gameInventory.addButton(gameInventory.getSearchStart() * 9,
                                getBackItem().clone().onClick(new ClickEvent(() -> this.gameInventory.openPreviousInventory(player))));
                }
        return this;
    }

    /**
     * Searches the names of the items for the given keyword
     * <p>
     * Calling this will reset the page to 0. This is to prevent
     * exceptions that would occur if the current page didn't
     * exist after the search keyword changed.<br>
     * Setting the value to "" will clear the current search keyword.
     *
     * @param search the keyword to search item names for
     * @return this class for chaining
     */
    @SuppressWarnings("deprecation")
    public InventoryBuilder search(final String search) {
        if (search.equals("")) {
            this.search = "";
            items = gameInventory.getItems();
        }
        this.search = search;
        for (ItemBuilder item : items)
            if (!StringUtils.containsIgnoreCase(ChatColor.stripColor(item.getItemMeta().getDisplayName()), search))
                items.remove(item);
        maxPage = (int) Math.floor(items.size() / itemsPerPage);
        page(0);
        return this;
    }


    /**
     * Makes a cleaner call to <code>page(int number + 1)</code>
     *
     * @return this class for chaining
     */
    @SuppressWarnings("deprecation")
    public InventoryBuilder nextPage() {
        return this.page(this.page + 1);
    }

    /**
     * Makes a cleaner call to <code>page(int number - 1)</code>
     *
     * @return this class for chaining
     */
    @SuppressWarnings("deprecation")
    public InventoryBuilder previousPage() {
        return this.page(this.page - 1);
    }

    /**
     * Opens this {@link GameInventory} for the current {@link Player}
     * <p>
     * Opens to the page specified
     *
     * @param page
     */
    @SuppressWarnings("deprecation")
    public void open(final int page) {
        page(page);
        gameInventory.getButtons().forEach((slot, itemBuilder) -> gameInventory.getInventory().setItem(slot, itemBuilder));
        player.openInventory(this.gameInventory.getInventory());
        InventoryToolKit.getInventoryUsers().put(player, this);
    }

    /**
     * Opens this {@link GameInventory} for the current {@link Player}
     * <p>
     * Opens to the current page (zero, if previously unchanged)
     */
    public void open() {
        open(page);
    }

}
