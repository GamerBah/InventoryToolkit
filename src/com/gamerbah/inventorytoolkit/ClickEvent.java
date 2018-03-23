package com.gamerbah.inventorytoolkit;
/* Created by GamerBah on 9/22/2017 */

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class ClickEvent {

    @Getter
    private Type[] clickTypes;
    @Getter
    private Runnable action;

    /**
     * Creates a new Click Event with the given {@link Type} and {@link Runnable}
     *
     * @param types  Types of clicks that will run this event
     * @param action {@link Runnable} to be executed if types are satisfied
     */
    public ClickEvent(final Runnable action, final Type... types) {
        this.clickTypes = Objects.requireNonNull(types, "cannot create ClickEvent with no types");
        this.action = Objects.requireNonNull(action, "action cannot be null");
    }

    /**
     * Creates a new Click Event using the default {@link Type} (ANY) and a {@link Runnable}
     *
     * @param action {@link Runnable} to be executed
     */
    public ClickEvent(final Runnable action) {
        this(action, Type.ANY);
    }

    /**
     * Enum class to simplify Bukkit's own {@link ClickType} enum
     * <p>
     * Since Bukkit's own ClickType has many values that aren't very useful for
     * Inventory management, this enum helps simply it a little bit.
     * The basic ClickTypes are there, but the added "ANY"
     * value provides an all-encompassing value for instances when the type
     * shouldn't matter
     */
    @AllArgsConstructor
    @Getter
    public enum Type {
        RIGHT(ClickType.RIGHT),
        LEFT(ClickType.LEFT),
        MIDDLE(ClickType.MIDDLE),
        SHIFT_RIGHT(ClickType.SHIFT_RIGHT),
        SHIFT_LEFT(ClickType.SHIFT_LEFT),
        DROP(ClickType.DROP),
        ANY(ClickType.UNKNOWN);

        private ClickType clickType;
    }

}
