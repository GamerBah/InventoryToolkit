package com.gamerbah.inventorytoolkit;
/* Created by GamerBah on 3/11/2018 */

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class InventoryToolKit extends JavaPlugin {

    @Getter
    private static final HashMap<Player, InventoryBuilder> inventoryUsers = new HashMap<>();

    @Getter
    private static InventoryToolKit instance = null;
    @Getter
    private static Metrics metrics = null;

    public void onEnable() {
        instance = this;
        if (getConfig().getBoolean("useMetrics"))
            metrics = new Metrics(this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
    }

}
