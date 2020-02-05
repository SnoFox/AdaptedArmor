package net.snofox.minecraft.adaptedarmor;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Created by Josh on 2018-12-30
 */
public class RepairCraftingListener implements Listener {

    private AdaptedArmor module;

    RepairCraftingListener(final AdaptedArmor module) {
        this.module = module;
    }

    @EventHandler
    public void onPrepareAnvil(final PrepareAnvilEvent ev) {
        final ItemStack[] itemStacks = ev.getInventory().getContents();
        if(module.isCustomItem(itemStacks[0])) {
            if(!Arrays.stream(itemStacks).allMatch(this::shouldActOn)) return;
            final ItemStack result = generateResultStack(itemStacks[0], itemStacks[1]);
            if(result == null) return;
            final ItemMeta resultMeta = result.getItemMeta();
            resultMeta.setDisplayName(ev.getInventory().getRenameText());
            result.setItemMeta(resultMeta);
            ev.getInventory().setRepairCost(1);
            ev.setResult(result);
        }
    }


    /**
     * Generate a result for Anvil crafting.
     * @param targetItem Item in left Anvil Inv slot
     * @param sacrificeItem Item in right Anvil Inv slot
     * @return
     */
    private ItemStack generateResultStack(final ItemStack targetItem, final ItemStack sacrificeItem) {
        if(module.isCustomItem(sacrificeItem))
            return sacrificeItem(targetItem, sacrificeItem);
        return repairItem(targetItem, sacrificeItem);
    }

    private ItemStack sacrificeItem(final ItemStack targetItem, final ItemStack sacrificeItem) {
        return new ItemStack(Material.LEATHER_HELMET);
    }

    private ItemStack repairItem(final ItemStack targetItem, final ItemStack repairStack) {
        if(!canRepair(targetItem, repairStack))
            return null;
        final int repairMax = module.getIntTag(module.ADAPTED_ARMOR_DAMAGE_MAX, targetItem);
        int currentDamage = module.getIntTag(module.ADAPTED_ARMOR_DAMAGE_CURRENT, targetItem);
        final int repairStep = (int)Math.ceil(repairMax * .25);
        final int newDamage = Math.min(repairStack.getAmount() * repairStep + currentDamage, repairMax);
        module.setIntTag(module.ADAPTED_ARMOR_DAMAGE_CURRENT, targetItem, newDamage);
        return targetItem;
    }

    private boolean canRepair(final ItemStack targetItem, final ItemStack repairStack) {
        final String repairStr = module.getStringTag(module.ADAPTED_ARMOR_REPAIR_MATERIAL, targetItem);
        if(repairStr == null) return false;
        final Material repairMaterial = Material.matchMaterial(repairStr);
        if(repairMaterial == null || !repairStack.getType().equals(repairMaterial)) return false;
        return true;
    }

    private boolean shouldActOn(final ItemStack itemStack) {
        return (itemStack != null) && (module.isCustomItem(itemStack) || !itemStack.getType().equals(Material.ENCHANTED_BOOK));
    }
}
