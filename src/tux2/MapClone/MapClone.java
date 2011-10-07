/*
 * Copyright (C) 2011  Joshua Reetz

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tux2.MapClone;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijikokun.register.Register;
import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Methods;

/**
 * MapClone for Bukkit
 *
 * @author tux2
 */
public class MapClone extends JavaPlugin {
    //private final MapClonePlayerListener playerListener = new MapClonePlayerListener(this);
    //private final MapCloneBlockListener blockListener = new MapCloneBlockListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    private static PermissionHandler Permissions;
    public ConcurrentHashMap<String, String> maps = new ConcurrentHashMap<String, String>();
    public ConcurrentHashMap<String, String> editedmaps = new ConcurrentHashMap<String, String>();
    boolean enablerecipe = false;
    boolean useiconomy = false;
    double cloneprice = 0.0;
    double scaleprice = 0.0;
    int scaletime = 25;
    String version = "0.5";
    public Register iConomy = null;
	public int useitem = 0;
	public int useitemdamage = 0;
	public int useitemquantity = 1;
	public int scaleitem = 0;
	public int scaleitemdamage = 0;
	public int scaleitemquantity = 1;


    public MapClone() {
        super();
    	loadconfig();
    	loadmaps();

        // NOTE: Event registration should be done in onEnable not here as all events are unregistered when a plugin is disabled
    }

   

    public void onEnable() {

        // Register our events
    	setupPermissions();
        PluginManager pm = getServer().getPluginManager();
        //Only do this if they want to use iConomy
        if(useiconomy) {
        	pm.registerEvent(Type.PLUGIN_ENABLE, new MapCloneServerListener(this), Priority.Monitor, this);
        	pm.registerEvent(Type.PLUGIN_DISABLE, new MapCloneServerListener(this), Priority.Monitor, this);
        }
        final MapCloneCommands commandL = new MapCloneCommands( this );
        if(enablerecipe) {
            for(int i = 0; i < 65536; i++) {
            	ShapedRecipe mapRecipe = new ShapedRecipe(new ItemStack(Material.MAP, 2, (short)i));
            	mapRecipe.shape("mpp","prp","ppp");
            	mapRecipe.setIngredient('p', Material.PAPER);
            	mapRecipe.setIngredient('r', Material.INK_SACK);
            	mapRecipe.setIngredient('m', Material.MAP, i);
            	getServer().addRecipe(mapRecipe);
            }
        }
        
        PluginCommand batchcommand = this.getCommand("mclone");
		batchcommand.setExecutor(commandL);
        PluginCommand batchcommand2 = this.getCommand("mzoom");
		batchcommand2.setExecutor(commandL);
       

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
    public void onDisable() {

        // NOTE: All registered events are automatically unregistered when a plugin is disabled

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        //System.out.println("Goodbye world!");
    }
    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }

    public void setDebugging(final Player player, final boolean value) {
        debugees.put(player, value);
    }
    
    private void setupPermissions() {
        Plugin permissions = this.getServer().getPluginManager().getPlugin("Permissions");

        if (Permissions == null) {
            if (permissions != null) {
                Permissions = ((Permissions)permissions).getHandler();
            } else {
            }
        }
    }
    
    public static boolean hasPermissions(Player player, String node) {
        if (Permissions != null) {
            return Permissions.has(player, node);
        } else {
            return player.hasPermission(node);
        }
    }
    
    private void loadmaps() {
		// check for existing file
		File configFile = new File("plugins/MapClone/maps.zoom");
		
		if (configFile.exists()) {
			try {
				Properties themapSettings = new Properties();
				themapSettings.load(new FileInputStream(new File("plugins/MapClone/maps.zoom")));
				
				Set<Entry<Object, Object>> mapszoomed= themapSettings.entrySet();
				Iterator<Entry<Object, Object>> mapiterator = mapszoomed.iterator();
				while(mapiterator.hasNext()) {
					Entry<Object, Object> map = mapiterator.next();
					String mapnumber = map.getKey().toString();
					String mapedittime = map.getValue().toString();
					maps.put(mapnumber, mapedittime);
				}
			} catch (IOException e) {
				
			}
		}else {
			//If it doesn't exist, no big deal.
		}
    	
    }
    
    boolean savemaps() {
    	File folder = new File("plugins/MapClone");
    	if(!folder.exists()) {
    		System.out.println("[MapClone] + creating folder plugins/MapClone");
    		folder.mkdir();
    	}
		File configFile = new File("plugins/MapClone/maps.zoom");
		
		try {
			BufferedWriter outChannel = new BufferedWriter(new FileWriter(configFile));
			outChannel.write("#This file contains all the names of the maps that have been zoomed in.\n" +
					"#Please do not edit this file as it's automatically created!\n");
			Set<Entry<String, String>> mapset = maps.entrySet();
			Iterator<Entry<String, String>> mapiterator = mapset.iterator();
			while(mapiterator.hasNext()) {
				Entry<String, String> themap = mapiterator.next();
				outChannel.write(themap.getKey() + " = " + themap.getValue() + "\n");
			}
			outChannel.close();
		} catch (Exception e) {
			System.out.println("MapClone: - Failed to save map edit times.");
	    	return false;
		}
		return true;
    }
    
    private void loadconfig() {
		File folder = new File("plugins/MapClone");

		// check for existing file
		File configFile = new File("plugins/MapClone/settings.ini");
		
		//if it exists, let's read it, if it doesn't, let's create it.
		if (configFile.exists()) {
			try {
				Properties themapSettings = new Properties();
				themapSettings.load(new FileInputStream(new File("plugins/MapClone/settings.ini")));
		        
		        String recipeenable = themapSettings.getProperty("enable-recipe", "false");
		        String iconomy = themapSettings.getProperty("useEconomy", "false");
		        String price = themapSettings.getProperty("pricetoclone", "0.0");
		        String scale = themapSettings.getProperty("pricetoscale", "0.0");
		        String stime = themapSettings.getProperty("timetoscale", "25");
		        String sitemprice = themapSettings.getProperty("itemtouse", "0");
		        String sitemquantity = themapSettings.getProperty("itemquantity", "1");
		        String sscaleprice = themapSettings.getProperty("scaleitem", "0");
		        String sscalequantity = themapSettings.getProperty("scaleitemquantity", "1");
		        //If the version isn't set, the file must be at 0.2
		        String theversion = themapSettings.getProperty("version", "0.2");
			    
			    enablerecipe = stringToBool(recipeenable);
			    useiconomy = stringToBool(iconomy);
			    try {
			    	cloneprice = Double.parseDouble(price.trim());
			    } catch (Exception ex) {
			    	
			    }
			    

			    try {
			    	scaleprice = Double.parseDouble(scale.trim());
			    } catch (Exception ex) {
			    	
			    }
			    String[] itemstuff = sitemprice.split(":");
			    try {
			    	useitem = Integer.parseInt(itemstuff[0].trim());
			    } catch (Exception ex) {
			    	
			    }			    
			    try {
			    	useitemdamage = Integer.parseInt(itemstuff[1].trim());
			    } catch (Exception ex) {
			    	
			    }
			    try {
			    	scaletime = Integer.parseInt(stime.trim());
			    } catch (Exception ex) {
			    	
			    }
			    try {
			    	useitemquantity = Integer.parseInt(sitemquantity.trim());
			    } catch (Exception ex) {
			    	
			    }

			    String[] scalestuff = sscaleprice.split(":");
			    try {
			    	scaleitem = Integer.parseInt(scalestuff[0].trim());
			    } catch (Exception ex) {
			    	
			    }			    
			    try {
			    	scaleitemdamage = Integer.parseInt(scalestuff[1].trim());
			    } catch (Exception ex) {
			    	
			    }
			    try {
			    	scaleitemquantity = Integer.parseInt(sscalequantity.trim());
			    } catch (Exception ex) {
			    	
			    }
			    //Let's see if we need to upgrade the config file
			    double dbversion = 0.2;
			    try {
			    	dbversion = Double.parseDouble(theversion.trim());
			    } catch (Exception ex) {
			    	
			    }
			    if(dbversion < 0.5) {
			    	if(dbversion < 0.4) {
			    		//Conversion of old iConomy value to new value.
				        iconomy = themapSettings.getProperty("useiConomy", "false");
				        useiconomy = stringToBool(iconomy);
			    	}
			    	updateIni();
			    }
			} catch (IOException e) {
				
			}
		}else {
			System.out.println("MapClone: Configuration file not found");

			System.out.println("MapClone: + creating folder plugins/MapClone");
			folder.mkdir();

			System.out.println("Mapclone: - creating file settings.ini");
			updateIni();
		}
	}

	private void updateIni() {
		try {
			BufferedWriter outChannel = new BufferedWriter(new FileWriter("plugins/MapClone/settings.ini"));
			outChannel.write("#This is the main MapClone config file\n" +
					"#\n" +
					"# enable-recipe: Enable the Crafing Recipe. Note: This will make it so\n" +
					"# anyone can clone a map using the recipe. All other settings and permissions\n" +
					"# are ignored if you use this option.\n" +
					"enable-recipe = " + enablerecipe + "\n" +
					"# useEconomy: Charge to clone the map using your economy plugin\n" +
					"useEconomy = " + useiconomy + "\n" +
					"# pricetoclone: The price to clone a map\n" +
					"pricetoclone = " + cloneprice + "\n\n" +
					"# pricetoscale: The price to scale a map\n" +
					"pricetoscale = " + scaleprice + "\n\n" +
					"# timetoscale: The grace period in seconds for changing the scale of a map\n" +
					"timetoscale = " + scaletime + "\n\n" +
					"# itemtouse: The item ID to use when cloning a map. (not used when iConomy is\n" +
					"#  enabled). If the item ID is set to 0 then no item is used. To specify a damage\n" +
					"#  value do: itemID:damage. Example using Cocoa Beans: 351:3\n" +
					"itemtouse = " + useitem + ":" + useitemdamage + "\n" +
					"# itemquantity: If using an item, how many should we use for a clone\n" +
					"itemquantity = " + useitemquantity + "\n\n" +
					"# scaleitem: The item ID to use when scaling a map. (not used when iConomy is\n" +
					"#  enabled). If the item ID is set to 0 then no item is used. To specify a damage\n" +
					"#  value do: itemID:damage. Example using Cocoa Beans: 351:3\n" +
					"scaleitem = " + scaleitem + ":" + scaleitemdamage + "\n" +
					"# scaleitemquantity: If using an item, how many should we use for scaling\n" +
					"scaleitemquantity = " + scaleitemquantity + "\n\n" +
					"#Do not change anything below this line unless you know what you are doing!\n" +
					"version = " + version);
			outChannel.close();
		} catch (Exception e) {
			System.out.println("MapClone: - file creation failed, using defaults.");
		}
		
	}
	private synchronized boolean stringToBool(String thebool) {
		boolean result;
		if (thebool.trim().equalsIgnoreCase("true") || thebool.trim().equalsIgnoreCase("yes")) {
	    	result = true;
	    } else {
	    	result = false;
	    }
		return result;
	}
	
	public boolean hasEconomy() {
		if(iConomy != null) {
			return Methods.hasMethod();
		}else {
			return false;
		}
	}
	
	public Method getEconomy() {
		if(iConomy != null) {
			return Methods.getMethod();
		}else {
			return null;
		}
	}
}

