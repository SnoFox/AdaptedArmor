package net.snofox.minecraft.adaptedarmor;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;

/**
 * Created by Josh on 2018-12-16
 */
public class DamageListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEvent(final EntityDamageEvent ev) {
        if(!(ev.getEntity() instanceof LivingEntity)) return;
        boolean shouldIgnore = true;
        switch(ev.getCause()) {
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
            case PROJECTILE:
            case LAVA:
            case FIRE_TICK:
            case FIRE:
            case ENTITY_EXPLOSION:
            case BLOCK_EXPLOSION:
            case LIGHTNING:
            case CONTACT:
            case FALLING_BLOCK:
                shouldIgnore = false;
        }
        if(shouldIgnore) return;
        damageEquipment((LivingEntity)ev.getEntity());
    }

    private void damageEquipment(final LivingEntity entity) {
        final ItemStack[] armorList = entity.getEquipment().getArmorContents();
        for(ItemStack armor : armorList) {
            if(AdaptedArmor.getInstance().isCustomItem(armor))
                decrementDamage(armor);
        }
        entity.getEquipment().setArmorContents(armorList);
    }

    private void decrementDamage(final ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        Integer damage = itemMeta.getCustomTagContainer().getCustomTag(AdaptedArmor.getDamageCurrentKey(), ItemTagType.INTEGER);
        itemMeta.getCustomTagContainer().setCustomTag(AdaptedArmor.getDamageCurrentKey(), ItemTagType.INTEGER, --damage);
        Integer maxDamage = itemMeta.getCustomTagContainer().getCustomTag(AdaptedArmor.getDamageMaxKey(), ItemTagType.INTEGER);
        if(itemStack.getType().getMaxDurability() > 0) {
            Double convertedDamage = damage.doubleValue() / maxDamage * itemStack.getType().getMaxDurability();
            ((Damageable)itemMeta).setDamage(itemStack.getType().getMaxDurability() - convertedDamage.intValue());
        }
        itemStack.setItemMeta(itemMeta);
    }
}
