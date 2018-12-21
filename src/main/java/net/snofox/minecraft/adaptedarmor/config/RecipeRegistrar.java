package net.snofox.minecraft.adaptedarmor.config;

import net.snofox.minecraft.adaptedarmor.AdaptedArmor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.util.List;
import java.util.UUID;

/**
 * Created by Josh on 2018-12-17
 */
public class RecipeRegistrar {
    public static boolean registerRecipes(ConfigurationSection config) {
        boolean addedAll = true;
        for(String recipeKey : config.getKeys(false)) {
            Bukkit.getLogger().info("Registering: " + recipeKey);
            ConfigurationSection recipeConfig = config.getConfigurationSection(recipeKey);
            final NamespacedKey namespacedKey = getNamespacedKey(recipeKey);
            final ItemStack itemStack = generateItemStack(recipeConfig.getString("name"), recipeConfig.getString("resultMaterial"),
                    recipeConfig.getInt("maxDamage"), recipeConfig.getInt("armor"), recipeConfig.getBoolean("unbreakable", false));
            final ShapedRecipe recipe = generateShapedRecipe(namespacedKey, recipeConfig.getStringList("shape"),
                    recipeConfig.getConfigurationSection("key"), itemStack);
            addedAll = AdaptedArmor.getInstance().addRecipe(recipe) & addedAll;
        }
        return addedAll;
    }

    private static NamespacedKey getNamespacedKey(final String key) {
        return new NamespacedKey(AdaptedArmor.getInstance(), key);
    }

    private static ItemStack generateItemStack(final String name, final String materialStr, final Integer maxDamage,
                                               final Integer armorPoints, final boolean isUnbreakable) {
        final Material resultMaterial = Material.matchMaterial(materialStr);
        final ItemStack itemStack = new ItemStack(resultMaterial);
        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setUnbreakable(isUnbreakable);
        final EquipmentSlot detectedSlot = getSlotForMaterial(resultMaterial);
        final UUID attributeSlotUuid = getUuidForSlot(detectedSlot);
        final AttributeModifier modifier = new AttributeModifier(attributeSlotUuid, "adaptedarmor.armor",
                armorPoints.doubleValue(), AttributeModifier.Operation.ADD_NUMBER, detectedSlot);
        itemMeta.addAttributeModifier(Attribute.GENERIC_ARMOR, modifier);
        final CustomItemTagContainer customTagsContainer = itemMeta.getCustomTagContainer();
        customTagsContainer.setCustomTag(AdaptedArmor.getInstance().ADAPTED_ARMOR_DAMAGE_CURRENT, ItemTagType.INTEGER, maxDamage);
        customTagsContainer.setCustomTag(AdaptedArmor.getInstance().ADAPTED_ARMOR_DAMAGE_MAX, ItemTagType.INTEGER, maxDamage);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private static ShapedRecipe generateShapedRecipe(final NamespacedKey key, final List<String> shape,
                                                     final ConfigurationSection recipeKeyConfig, final ItemStack result) {
        final ShapedRecipe recipe = new ShapedRecipe(key, result);
        final String[] shapeArr = new String[shape.size()];
        shape.toArray(shapeArr);
        recipe.shape(shapeArr);
        for(final String configKey : recipeKeyConfig.getKeys(false))
            recipe.setIngredient(configKey.charAt(0), Material.matchMaterial(recipeKeyConfig.getString(configKey)));
        return recipe;
    }

    private static EquipmentSlot getSlotForMaterial(final Material material) {
        switch(material) {
            case LEATHER_HELMET:
            case GOLDEN_HELMET:
            case CHAINMAIL_HELMET:
            case IRON_HELMET:
            case DIAMOND_HELMET:
            case TURTLE_HELMET:
                return EquipmentSlot.HEAD;
            case LEATHER_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
            case IRON_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case ELYTRA:
                return EquipmentSlot.CHEST;
            case LEATHER_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case IRON_LEGGINGS:
            case DIAMOND_LEGGINGS:
                return EquipmentSlot.LEGS;
            case LEATHER_BOOTS:
            case GOLDEN_BOOTS:
            case CHAINMAIL_BOOTS:
            case IRON_BOOTS:
            case DIAMOND_BOOTS:
                return EquipmentSlot.FEET;
            default:
                return EquipmentSlot.HAND;
        }
    }

    private static UUID getUuidForSlot(final EquipmentSlot slot) {
        switch(slot) {
            case HEAD:
                return AdaptedArmor.HELM_UUID_V1;
            case CHEST:
                return AdaptedArmor.CHEST_UUID_V1;
            case LEGS:
                return AdaptedArmor.PANTS_UUID_V1;
            case FEET:
                return AdaptedArmor.BOOTS_UUID_V1;
            default:
                return AdaptedArmor.UNKNOWN_UUID;
        }
    }
}