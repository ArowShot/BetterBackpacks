package com.areuxonline.bbackpacks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class BBackpacks extends JavaPlugin implements Listener {
	public Plugin pl;
	public BukkitScheduler scheduler;
	Map<String, Inventory> backpacks = new HashMap<String, Inventory>();
    Map<Player, String> openedBackpacks = new HashMap<Player, String>();
    List<ShapedRecipe> bpRecipes = new ArrayList<ShapedRecipe>();
    List<ShapedRecipe> bpLargeRecipes = new ArrayList<ShapedRecipe>();
	String bpStr;
    String lbpStr;
	
    private ItemStack initiateBackpack(ItemStack backpack) {
        String newName = getNewName(0);
        ItemMeta im = backpack.getItemMeta();
        List<String> lore = im.getLore();
        lore.set(0, lore.get(0).substring(0,lore.get(0).length()-2)+formatName(newName));
        File newNameDir = new File(getDataFolder()+"/backpacks/");
        File newNameFile = new File(getDataFolder()+"/backpacks/", newName+".bp");
        try {
            newNameDir.mkdirs();
            newNameFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        im.setLore(lore);
        backpack.setItemMeta(im);
        return backpack;
    }
    
	private void openBackpack(String n, ItemStack bp, Player pl) {
        if(backpacks.containsKey(n)) {
            pl.openInventory(backpacks.get(n));
            openedBackpacks.put(pl, n);
        } else {
            Inventory newInv;
            int slots = 27;
            if(bpType(bp)==2) {
                slots = 54;
            }
            newInv = Bukkit.createInventory(null, slots, "Backpack");
            backpacks.put(n, newInv);
            File bpFile = new File(getDataFolder()+"/backpacks/", n+".bp");
            FileConfiguration bpC = YamlConfiguration.loadConfiguration(bpFile);
            for(int i = 0;i<slots;i++) {
                ItemStack is = bpC.getItemStack(i+"");
                if(is!=null) {
                    newInv.setItem(i, is);
                }
            }
            pl.openInventory(newInv);
            openedBackpacks.put(pl, n);
        }
	}
	
    private void saveBackpack(String n, Inventory in) {
        File bpFile = new File(getDataFolder()+"/backpacks/", n+".bp");
        FileConfiguration bpC = YamlConfiguration.loadConfiguration(bpFile);
        int slots = in.getSize();
        for(int i = 0;i<slots;i++) {
            ItemStack is = in.getItem(i);
            if(is!=null) {
                bpC.set(i+"", is);
                }
        }
        try {
            bpC.save(bpFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String formatName(String str) {
        String rstr = "";
        for (int i=0;i < str.length();i++) { 
            rstr+=ChatColor.COLOR_CHAR+str.substring(i,i+1); 
        }
        return rstr;
    }
    
    private String deformatName(String str) {
        return str.replace("Backpack","").replace(ChatColor.COLOR_CHAR+"", "");
    }
    
    private String getNewName(int off) {
        String rstr;
        rstr = (System.currentTimeMillis()+off)+"";
        File bps = new File(getDataFolder()+"/backpacks/");
        bps.mkdirs();
        for(File f:bps.listFiles()) {
            if(f.getName().equalsIgnoreCase(rstr+".bp")) {
                return getNewName(off+1);
            }
        }
        return rstr;
    }
    
    private int bpType(ItemStack is) {
        ItemMeta im = is.getItemMeta();
        if(im!=null) {
            List<String> lore = im.getLore();
            if(lore!=null&&lore.get(0)!=null&&lore.get(0).startsWith(bpStr)) {
                return 1;
            }
            if(lore!=null&&lore.get(0)!=null&&lore.get(0).startsWith(lbpStr)) {
                return 2;
            }
        }
        return 0;
    }
    
    private boolean openBackpack(Player pl) {
        ItemStack potentialBp = pl.getInventory().getChestplate();
        if(potentialBp!=null) {
            if(bpType(potentialBp)>0) {
                if(potentialBp.getItemMeta().getLore().get(0).endsWith(ChatColor.RED+"")) {
                    pl.getInventory().setChestplate(initiateBackpack(potentialBp));
                }
                String str = potentialBp.getItemMeta().getLore().get(0);
                String bpKey = deformatName(str.substring(str.length()-26));
                openBackpack(bpKey, potentialBp, pl);
                return true;
            }
        }
        return false;
    }
    
	@EventHandler
	public void backpackClose(InventoryCloseEvent e) {
	    String backpackKey = openedBackpacks.get(e.getPlayer());
	    if(e.getPlayer() instanceof Player&&backpackKey!=null&&backpacks.keySet().contains(backpackKey)) {
	        saveBackpack(backpackKey, e.getInventory());
	        openedBackpacks.put((Player) e.getPlayer(), null);
	    }
	}
	
	private int bprType(ShapedRecipe r) {
	    for(ShapedRecipe re:bpRecipes) {
	        if(re.getResult().equals(r.getResult())) {
	            if(r.getResult().getAmount()==2) {
	                return 1;
	            }
	        }
	    }
        for(ShapedRecipe re:bpLargeRecipes) {
            if(re.getResult().equals(r.getResult())) {
                if(r.getResult().getAmount()==3) {
                    return 2;
                }
            }
        }
	    return 0;
	}
	
    @EventHandler
    public void craftBackpack(PrepareItemCraftEvent e) {
        if(e.getRecipe() instanceof ShapedRecipe&&bprType((ShapedRecipe) e.getRecipe())==1) {
            ItemStack result = createBackpack(e.getInventory().getResult(), 27);
            for(Enchantment en:e.getInventory().getItem(5).getEnchantments().keySet()) {
                result.addUnsafeEnchantment(en, e.getInventory().getItem(5).getEnchantments().get(en));
            }
            ItemMeta im = result.getItemMeta();
            im.setDisplayName(e.getInventory().getItem(5).getItemMeta().getDisplayName());
            result.setItemMeta(im);
            result.setAmount(1);
            e.getInventory().setResult(result);
        } else if(e.getRecipe() instanceof ShapedRecipe&&bprType((ShapedRecipe) e.getRecipe())==2) {
            ItemStack result = createBackpack(e.getInventory().getResult(), 54);
            for(Enchantment en:e.getInventory().getItem(5).getEnchantments().keySet()) {
                result.addUnsafeEnchantment(en, e.getInventory().getItem(5).getEnchantments().get(en));
            }
            ItemMeta im = result.getItemMeta();
            im.setDisplayName(e.getInventory().getItem(5).getItemMeta().getDisplayName());
            result.setItemMeta(im);
            result.setAmount(1);
            e.getInventory().setResult(result);
        }
    }
	
    /*@EventHandler
    public void craftItem(PrepareItemCraftEvent e) {
        Material itemType = e.getRecipe().getResult().getType();
        @SuppressWarnings("deprecation")
        Byte itemData = e.getRecipe().getResult().getData().getData();
        if(itemType==Material.ENDER_CHEST||itemType==Material.HOPPER||(itemType==Material.GOLDEN_APPLE&&itemData==1)) {
            e.getInventory().setResult(new ItemStack(Material.AIR));
            for(HumanEntity he:e.getViewers()) {
                if(he instanceof Player) {
                    ((Player)he).sendMessage(ChatColor.RED+"You cannot craft this!");
                }
            }
        }
    }*/
    
	@Override
	public void onEnable() {
	    this.pl = this;
		getServer().getPluginManager().registerEvents(this, this);
		bpStr = formatName("backpack");
		lbpStr = formatName("largebackpack");
		
		ShapedRecipe leatherRecipe = new ShapedRecipe(new ItemStack(Material.LEATHER_CHESTPLATE, 2));
		leatherRecipe.shape("LLL", "LCL", "LBL").setIngredient('L', Material.LEATHER).setIngredient('C', Material.LEATHER_CHESTPLATE).setIngredient('B', Material.CHEST);
		getServer().addRecipe(leatherRecipe);
		bpRecipes.add(leatherRecipe);
		
        ShapedRecipe goldRecipe = new ShapedRecipe(new ItemStack(Material.GOLD_CHESTPLATE, 2));
        goldRecipe.shape("LLL", "LCL", "LBL").setIngredient('L', Material.LEATHER).setIngredient('C', Material.GOLD_CHESTPLATE).setIngredient('B', Material.CHEST);
        getServer().addRecipe(goldRecipe);
        bpRecipes.add(goldRecipe);
		
        ShapedRecipe ironRecipe = new ShapedRecipe(new ItemStack(Material.IRON_CHESTPLATE, 2));
        ironRecipe.shape("LLL", "LCL", "LBL").setIngredient('L', Material.LEATHER).setIngredient('C', Material.IRON_CHESTPLATE).setIngredient('B', Material.CHEST);
        getServer().addRecipe(ironRecipe);
        bpRecipes.add(ironRecipe);
        
        ShapedRecipe diamondRecipe = new ShapedRecipe(new ItemStack(Material.DIAMOND_CHESTPLATE, 2));
        diamondRecipe.shape("LLL", "LCL", "LBL").setIngredient('L', Material.LEATHER).setIngredient('C', Material.DIAMOND_CHESTPLATE).setIngredient('B', Material.CHEST);
        getServer().addRecipe(diamondRecipe);
        bpRecipes.add(diamondRecipe);
        
        ShapedRecipe leatherLargeRecipe = new ShapedRecipe(new ItemStack(Material.LEATHER_CHESTPLATE, 3));
        leatherLargeRecipe.shape("LLL", "BCB", "LLL").setIngredient('L', Material.LEATHER).setIngredient('C', Material.LEATHER_CHESTPLATE).setIngredient('B', Material.CHEST);
        getServer().addRecipe(leatherLargeRecipe);
        bpLargeRecipes.add(leatherLargeRecipe);
        
        ShapedRecipe goldLargeRecipe = new ShapedRecipe(new ItemStack(Material.GOLD_CHESTPLATE, 3));
        goldLargeRecipe.shape("LLL", "BCB", "LLL").setIngredient('L', Material.LEATHER).setIngredient('C', Material.GOLD_CHESTPLATE).setIngredient('B', Material.CHEST);
        getServer().addRecipe(goldLargeRecipe);
        bpLargeRecipes.add(goldLargeRecipe);
        
        ShapedRecipe ironLargeRecipe = new ShapedRecipe(new ItemStack(Material.IRON_CHESTPLATE, 3));
        ironLargeRecipe.shape("LLL", "BCB", "LLL").setIngredient('L', Material.LEATHER).setIngredient('C', Material.IRON_CHESTPLATE).setIngredient('B', Material.CHEST);
        getServer().addRecipe(ironLargeRecipe);
        bpLargeRecipes.add(ironLargeRecipe);
        
        ShapedRecipe diamondLargeRecipe = new ShapedRecipe(new ItemStack(Material.DIAMOND_CHESTPLATE, 3));
        diamondLargeRecipe.shape("LLL", "BCB", "LLL").setIngredient('L', Material.LEATHER).setIngredient('C', Material.DIAMOND_CHESTPLATE).setIngredient('B', Material.CHEST);
        getServer().addRecipe(diamondLargeRecipe);
        bpLargeRecipes.add(diamondLargeRecipe);
	}
	
	public ItemStack createBackpack(ItemStack backpack, int slots) {
        List<String> lore = new ArrayList<String>();
        if(slots==27){
            lore.add(bpStr+ChatColor.RESET+ChatColor.GOLD+"A normal sized backpack"+ChatColor.RED);
        } else if(slots==54){
            lore.add(lbpStr+ChatColor.RESET+ChatColor.GOLD+"A large backpack"+ChatColor.RED);
        }
        ItemMeta im = backpack.getItemMeta();
        im.setLore(lore);
        im.setDisplayName("Leather Backpack");
        backpack.setItemMeta(im);
        return backpack;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
		    sender.sendMessage(ChatColor.RED+"The console can't wear a backpack!");
		    return false;
		}
		Player pl = (Player) sender;
		if(cmd.getName().equalsIgnoreCase("backpack")) {
		    if(openBackpack(pl)) {
                pl.sendMessage(ChatColor.GREEN+"Opened backpack!");
		        return true;
		    } else {
                pl.sendMessage(ChatColor.RED+"You are not wearing a backpack!");
		        return false;
		    }
		}
		
		return false;
	}
}