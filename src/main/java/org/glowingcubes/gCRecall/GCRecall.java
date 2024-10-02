package org.glowingcubes.gCRecall;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

public class GCRecall extends JavaPlugin {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new RecallPotionListener(this), this);
        getLogger().info("RecallPotion has been enabled!");

        // Add custom crafting recipe
        addCraftingRecipe();
    }

    @Override
    public void onDisable() {
        getLogger().info("RecallPotion has been disabled!");
    }

    private void addCraftingRecipe() {
        // Create the item for the recall potion
        ItemStack recallPotion = new ItemStack(Material.POTION);
        // Set custom display name
        ItemMeta meta = recallPotion.getItemMeta();
        meta.setDisplayName("§aRecall Potion"); // Set custom display name

        // Set custom lore
        List<String> lore = new ArrayList<>();
        lore.add("§7A magical potion that");
        lore.add("§7teleports you back to your");
        lore.add("§7spawn point.");
        meta.setLore(lore);

        recallPotion.setItemMeta(meta);

        // Create a NamespacedKey for the recipe
        NamespacedKey key = new NamespacedKey(this, "recall_potion");

        // Define the recipe
        ShapedRecipe recipe = new ShapedRecipe(key, recallPotion);
        recipe.shape(" G ", " C ", " G ");
        recipe.setIngredient('G', Material.GOLDEN_CARROT);
        recipe.setIngredient('C', Material.CHORUS_FRUIT);

        // Add the recipe to the server
        Bukkit.addRecipe(recipe);
    }
}
