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
import java.util.Properties;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.iConomy.*;

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
    boolean enablerecipe = false;
    boolean useiconomy = false;
    double iconomyprice = 0.0;
    String version = "0.4";
    public iConomy iConomy = null;
	public int useitem = 0;
	public int useitemdamage = 0;


    public MapClone() {
        super();
    	loadconfig();

        // NOTE: Event registration should be done in onEnable not here as all events are unregistered when a plugin is disabled
    }

   

    public void onEnable() {

        // Register our events
    	setupPermissions();
        PluginManager pm = getServer().getPluginManager();
        //Only do this if they want to use iConomy
        if(useiconomy) {
        	getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, new MapCloneServerListener(this), Priority.Monitor, this);
            getServer().getPluginManager().registerEvent(Type.PLUGIN_DISABLE, new MapCloneServerListener(this), Priority.Monitor, this);
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
            return player.isOp();
        }
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
		        String iconomy = themapSettings.getProperty("useiConomy", "false");
		        String price = themapSettings.getProperty("pricetoclone", "0.0");
		        String sitemprice = themapSettings.getProperty("itemtouse", "0");
		        //If the version isn't set, the file must be at 0.2
		        String theversion = themapSettings.getProperty("version", "0.2");
			    
			    enablerecipe = stringToBool(recipeenable);
			    useiconomy = stringToBool(iconomy);
			    try {
			    	iconomyprice = Double.parseDouble(price.trim());
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
			    //Let's see if we need to upgrade the config file
			    double dbversion = 0.2;
			    try {
			    	dbversion = Double.parseDouble(theversion.trim());
			    } catch (Exception ex) {
			    	
			    }
			    if(dbversion < 0.4) {
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
					"# useiConomy: Charge to clone the map using iConomy\n" +
					"useiConomy = " + useiconomy + "\n" +
					"# pricetoclone: The price to clone a map\n" +
					"pricetoclone = " + iconomyprice + "\n\n" +
					"# itemtouse: The item ID to use when cloning a map. (not used when iConomy is\n" +
					"#  enabled). If the item ID is set to 0 then no item is used. To specify a damage\n" +
					"#  value do: itemID:damage. Example using Cocoa Beans: 351:3\n" +
					"itemtouse = " + useitem + ":" + useitemdamage + "\n\n" +
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
}

