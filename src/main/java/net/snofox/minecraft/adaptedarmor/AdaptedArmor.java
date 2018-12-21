package net.snofox.minecraft.adaptedarmor;

import net.snofox.minecraft.adaptedarmor.config.RecipeRegistrar;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
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
        getServer().getPluginManager().registerEvents(new RecipeDiscoveryListener(), this);
        getServer().getPluginManager().registerEvents(new DamageListener(), this);

        getLogger().info("Combat is now fair again");
    }

    private void initRecipes() {
        if(!RecipeRegistrar.registerRecipes(getConfig().getConfigurationSection("armorDefinitions")))
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
}
