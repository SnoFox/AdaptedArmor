package net.snofox.minecraft.adaptedarmor;

import net.snofox.minecraft.adaptedarmor.config.RecipeRegistrar;
import net.snofox.minecraft.snolib.recipes.RecipeDiscoveryManager;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Josh on 2018-12-15
 */
public class AdaptedArmor extends JavaPlugin {
    public static final UUID HELM_UUID_V1 = UUID.nameUUIDFromBytes("sno's lether helm 2018-12-15".getBytes());
    public static final UUID CHEST_UUID_V1 = UUID.nameUUIDFromBytes("sno's lether chest 2018-12-15".getBytes());
    public static final UUID PANTS_UUID_V1 = UUID.nameUUIDFromBytes("sno's lether pants 2018-12-15".getBytes());
    public static final UUID BOOTS_UUID_V1 = UUID.nameUUIDFromBytes("sno's lether boots 2018-12-15".getBytes());
    public static final UUID UNKNOWN_UUID = UUID.nameUUIDFromBytes("sno's cool unknown item".getBytes());

    private static AdaptedArmor instance;

    public final NamespacedKey ADAPTED_ARMOR_REPAIR_MATERIAL = new NamespacedKey(this, "repair_material");
    public final NamespacedKey ADAPTED_ARMOR_DAMAGE_MAX = new NamespacedKey(this, "max_damage");
    public final NamespacedKey ADAPTED_ARMOR_DAMAGE_CURRENT = new NamespacedKey(this, "current_damage");

    @Contract(pure = true)
    public static AdaptedArmor getInstance() {
        return instance;
    }

    public static NamespacedKey getDamageMaxKey() {
        return getInstance().ADAPTED_ARMOR_DAMAGE_MAX;
    }

    public static NamespacedKey getDamageCurrentKey() {
        return getInstance().ADAPTED_ARMOR_DAMAGE_CURRENT;
    }

    public static NamespacedKey getRepairMaterialKey() { return getInstance().ADAPTED_ARMOR_REPAIR_MATERIAL; }

    @Override
    public void onLoad() {
        getLogger().info("Armor becoming more fair...");
        instance = this;
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        disableVanilla();
        initRecipes();
        getServer().getPluginManager().registerEvents(new DamageListener(), this);
        getServer().getPluginManager().registerEvents(new RepairCraftingListener(this), this);
        getLogger().info("Armor is now fair again");
    }

    private void initRecipes() {
        if(!RecipeRegistrar.registerRecipes(this, getConfig().getConfigurationSection("armorDefinitions")))
            getLogger().warning("Some (or all) recipes failed to register! This is sad :[");
    }

    public boolean addRecipe(final ShapedRecipe recipe) {
        if(getServer().addRecipe(recipe)) {
            for(Map.Entry<Character, ItemStack> ingredient : recipe.getIngredientMap().entrySet()) {
                RecipeDiscoveryManager.addRecipe(ingredient.getValue(), recipe.getKey());
            }
            return true;
        }
        return false;
    }

    private void disableVanilla() {
        final List<String> disabledRecipeList = getConfig().getStringList("disabledRecipes");
        final StringBuilder sb = new StringBuilder();
        sb.append("Disabling the following recipes: ");
        boolean comma = false;
        for(String disabledRecipeName : disabledRecipeList) {
            if(comma) sb.append(", ");
            sb.append(disabledRecipeName);
            comma = true;
        }
        getLogger().info(sb.toString());
        final Iterator<Recipe> itr = AdaptedArmor.getInstance().getServer().recipeIterator();
        while(itr.hasNext()) {
            final Recipe recipe = itr.next();
            if(disabledRecipeList.contains(recipe.getResult().getType().toString())) {
                getLogger().info("Removing recipe for " + recipe.getResult().getType());
                itr.remove();
            }
        }
    }

    /**
     * Check to see if an ItemStack is created from this plugin
     * @param itemStack
     * @return
     */
    public boolean isCustomItem(final ItemStack itemStack) {
        return hasIntTag(ADAPTED_ARMOR_DAMAGE_MAX, itemStack) && hasIntTag(ADAPTED_ARMOR_DAMAGE_CURRENT, itemStack);
    }

    /**
     * Returns a string value, or null if it doesn't exist.
     * @param key
     * @param itemStack
     * @return
     */
    public String getStringTag(final NamespacedKey key, final ItemStack itemStack) {
        return getKey(key, ItemTagType.STRING, itemStack);
    }

    public void setStringTag(final NamespacedKey key, final ItemStack itemStack, final String value) {
        setTag(key, itemStack, ItemTagType.STRING, value);
    }

    /**
     * Checks for existance of a string value
     * @param key
     * @param itemStack
     * @return
     */
    public boolean hasStringTag(final NamespacedKey key, final ItemStack itemStack) {
        return hasKey(key, ItemTagType.STRING, itemStack);
    }

    /**
     * Returns a string value, or null if it doesn't exist.
     * @param key
     * @param itemStack
     * @return
     */
    public Integer getIntTag(final NamespacedKey key, final ItemStack itemStack) {
        return getKey(key, ItemTagType.INTEGER, itemStack);
    }

    /**
     * Set the key to the specified integer value for the given itemstack
     * @param key
     * @param itemStack
     * @param value
     */
    public void setIntTag(final NamespacedKey key, final ItemStack itemStack, final Integer value) {
        setTag(key, itemStack, ItemTagType.INTEGER, value);
    }

    private <Z> void setTag(final NamespacedKey key, final ItemStack itemStack, final ItemTagType<?, Z> tagType, final Z value) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getCustomTagContainer().setCustomTag(key, tagType, value);
        itemStack.setItemMeta(itemMeta);
    }

    /**
     * Checks for existance of a string value
     * @param key
     * @param itemStack
     * @return
     */
    public boolean hasIntTag(final NamespacedKey key, final ItemStack itemStack) {
        return hasKey(key, ItemTagType.INTEGER, itemStack);
    }

    private <Z> Z getKey(final NamespacedKey key, final ItemTagType<?, Z> tagType, final ItemStack itemStack) {
        if(!hasMeta(itemStack)) return null;
        return itemStack.getItemMeta().getCustomTagContainer().getCustomTag(key, tagType);
    }

    private boolean hasKey(final NamespacedKey key, final ItemTagType<?, ?> tagType, final ItemStack itemStack) {
        if(!hasMeta(itemStack)) return false;
        return itemStack.getItemMeta().getCustomTagContainer().hasCustomTag(key, tagType);
    }

    private boolean hasMeta(final ItemStack itemStack) {
        if(itemStack == null) return false;
        if(!itemStack.hasItemMeta()) return false;
        return true;
    }
}
