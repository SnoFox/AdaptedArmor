package net.snofox.minecraft.adaptedarmor;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

/**
 * Created by Josh on 2018-12-15
 */
public class RecipeDiscoveryListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityPickUpItem(EntityPickupItemEvent ev) {
        if(!(ev.getEntity() instanceof Player)) return;
        Player p = (Player)ev.getEntity();
        p.discoverRecipes(RecipeDiscoveryManager.getRecipesForItem(ev.getItem().getItemStack()));
    }
}
