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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.*;
import org.bukkit.map.MapView.Scale;

import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Method.MethodAccount;

public class MapCloneCommands implements CommandExecutor {
	
	MapClone plugin;
	
	public MapCloneCommands(MapClone plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel,
			String[] args) {
		if (sender instanceof Player) {
			Player player = (Player)sender;
			if(commandLabel.equalsIgnoreCase("mclone")){
				if(plugin.hasPermissions(player, "mapclone.clone") || plugin.hasPermissions(player, "mapclone.all")) {
					if(args.length > 0) {
						if(player.getItemInHand().getTypeId() == 358) {
							try {
								int mapnum = Integer.parseInt(args[0]);
								if(plugin.hasPermissions(player, "mapclone.all") || hasTwoOfItem(player, 358)) {
									if(plugin.hasPermissions(player, "mapclone.all") || hasMap(player, (short)mapnum)) {
										if(plugin.useiconomy && plugin.hasEconomy()) {
											if(plugin.hasPermissions(player, "mapclone.free")) {
												player.getItemInHand().setDurability((short)mapnum);
												player.sendMessage(ChatColor.GOLD + "Poof! You just cloned a map!");
											}else if(plugin.getEconomy().hasAccount(player.getName())) {
												MethodAccount balance = plugin.getEconomy().getAccount(player.getName());
												if(balance.hasEnough(plugin.cloneprice)) {
													balance.subtract(plugin.cloneprice);
													player.getItemInHand().setDurability((short)mapnum);
													player.sendMessage(ChatColor.GOLD + "Poof! You just cloned a map for " + plugin.getEconomy().format(plugin.cloneprice) + "!");
												}else {
													player.sendMessage(ChatColor.RED + "You need " + plugin.getEconomy().format(plugin.cloneprice) + " to clone that map!");
												}
										    } else {
										    	player.sendMessage(ChatColor.RED + "You need a bank account and " + plugin.getEconomy().format(plugin.cloneprice) + " to clone a map!");
										    }
										}else {
											ItemStack items = new ItemStack(plugin.useitem, 1, (short)plugin.useitemdamage);
											if(plugin.hasPermissions(player, "mapclone.free") || plugin.useitem == 0) {
												player.getItemInHand().setDurability((short)mapnum);
												player.sendMessage(ChatColor.GOLD + "Poof! You just cloned a map!");
											}else if(hasItem(player, plugin.useitem, (short)plugin.useitemdamage, plugin.useitemquantity)) {
												removeItem(player, plugin.useitem, (short)plugin.useitemdamage, plugin.useitemquantity);
												player.getItemInHand().setDurability((short)mapnum);
												player.sendMessage(ChatColor.GOLD + "Poof! You just cloned a map for " + plugin.useitemquantity + " " + items.getType().name().toLowerCase().replace('_', ' ') + "!");

											} else {
												player.sendMessage(ChatColor.RED + "You need " + plugin.useitemquantity + " " + items.getType().name().toLowerCase().replace('_', ' ') + " to clone a map!");
											}
										}
										return true;
									} else {
										player.sendMessage(ChatColor.RED + "You need the map in order to clone it!");
										return true;
									}

								} else {
									player.sendMessage(ChatColor.RED + "You need at least 2 maps in order to clone one!");
									return true;
								}
							} catch (Exception e) {
								//player.sendMessage(ChatColor.RED + "Uhoh, something went wrong!");
								System.out.println(e);
								return false;
							}
						} else {
							player.sendMessage(ChatColor.RED + "You need to be holding the destination map!");
						}
					} else {
						player.sendMessage(ChatColor.GREEN + "Please specify the number of the map you want to clone.");
					}
				} else {
					player.sendMessage(ChatColor.RED + "You don't have permission to clone maps.");
				}
			}else if(commandLabel.equalsIgnoreCase("mzoom")){
				if(plugin.hasPermissions(player, "mapscale.scale") || plugin.hasPermissions(player, "mapscale.all")) {
					if(args.length > 0) {
						if(player.getItemInHand().getTypeId() == 358) {
							try {
								int mapnum = player.getItemInHand().getDurability();
								if(args.length > 1 && plugin.hasPermissions(player, "mapscale.any")) {
									try {
										mapnum = Integer.parseInt(args[1]);
									}catch (Exception e) {
										player.sendMessage(ChatColor.RED + "Invalid map number. Please use a valid map number!");
									}
								}
								if(plugin.hasPermissions(player, "mapscale.all") || canScaleMap(player, (short)mapnum)) {
									if(plugin.useiconomy && plugin.hasEconomy()) {
										if(plugin.hasPermissions(player, "mapscale.free")) {
											if(scaleMap((short) mapnum, args[0])) {
												plugin.maps.put(String.valueOf(mapnum), String.valueOf(System.currentTimeMillis()));
												plugin.editedmaps.put(String.valueOf(mapnum), player.getName());
												plugin.savemaps();
												player.sendMessage(ChatColor.GOLD + "Poof! You just scaled a map!");
											}else {
												player.sendMessage(ChatColor.RED + "Oops! I'm sorry, something went wrong when scaling that map!");
											}
										}else if(plugin.getEconomy().hasAccount(player.getName())) {
											MethodAccount balance = plugin.getEconomy().getAccount(player.getName());
											boolean playeralreadypaid = false;
											if(plugin.editedmaps.containsKey(String.valueOf(mapnum))) {
												if(plugin.editedmaps.get(String.valueOf(mapnum)).equalsIgnoreCase(player.getName())) {
													playeralreadypaid = true;
												}
											}
											if(balance.hasEnough(plugin.scaleprice) || playeralreadypaid) {
												if(scaleMap((short) mapnum, args[0])) {
													plugin.maps.put(String.valueOf(mapnum), String.valueOf(System.currentTimeMillis()));
													plugin.editedmaps.put(String.valueOf(mapnum), player.getName());
													plugin.savemaps();
													if(!playeralreadypaid) {
														balance.subtract(plugin.scaleprice);
														player.sendMessage(ChatColor.GOLD + "Poof! You just scaled a map for " + plugin.getEconomy().format(plugin.scaleprice) + "!");
													}else {
														player.sendMessage(ChatColor.GOLD + "Poof! You just scaled the map again!");
													}
												}else {
													player.sendMessage(ChatColor.RED + "Oops! I'm sorry, something went wrong when scaling that map!");
												}
											}else {
												player.sendMessage(ChatColor.RED + "You need " + plugin.getEconomy().format(plugin.scaleprice) + " to scale that map!");
											}
										} else {
											player.sendMessage(ChatColor.RED + "You need a bank account and " + plugin.getEconomy().format(plugin.scaleprice) + " to clone a map!");
										}
									}else {
										ItemStack items = new ItemStack(plugin.scaleitem, 1, (short)plugin.scaleitemdamage);
										boolean playeralreadypaid = false;
										if(plugin.editedmaps.containsKey(String.valueOf(mapnum))) {
											if(plugin.editedmaps.get(String.valueOf(mapnum)).equalsIgnoreCase(player.getName())) {
												playeralreadypaid = true;
											}
										}
										if(plugin.hasPermissions(player, "mapscale.free") || plugin.scaleitem == 0) {
											if(scaleMap((short) mapnum, args[0])) {
												plugin.maps.put(String.valueOf(mapnum), String.valueOf(System.currentTimeMillis()));
												plugin.editedmaps.put(String.valueOf(mapnum), player.getName());
												plugin.savemaps();
												player.sendMessage(ChatColor.GOLD + "Poof! You just scaled a map!");
											}else {
												player.sendMessage(ChatColor.RED + "Oops! I'm sorry, something went wrong when scaling that map!");
											}
										}else if(playeralreadypaid || hasItem(player, plugin.scaleitem, (short)plugin.scaleitemdamage, plugin.scaleitemquantity)) {
											
											if(scaleMap((short) mapnum, args[0])) {
												plugin.maps.put(String.valueOf(mapnum), String.valueOf(System.currentTimeMillis()));
												plugin.editedmaps.put(String.valueOf(mapnum), player.getName());
												plugin.savemaps();
												if(!playeralreadypaid) {
													removeItem(player, plugin.scaleitem, (short)plugin.scaleitemdamage, plugin.scaleitemquantity);
													player.sendMessage(ChatColor.GOLD + "Poof! You just scaled a map for " + plugin.scaleitemquantity + " " + items.getType().name().toLowerCase().replace('_', ' ') + "!");
												}else {
													player.sendMessage(ChatColor.GOLD + "Poof! You just scaled the map again!");
												}
											}else {
												player.sendMessage(ChatColor.RED + "Oops! I'm sorry, something went wrong when scaling that map!");
											}
										} else {
											player.sendMessage(ChatColor.RED + "You need " + plugin.scaleitemquantity + " " + items.getType().name().toLowerCase().replace('_', ' ') + " to scale a map!");
										}
									}
									return true;
								} else {
									player.sendMessage(ChatColor.RED + "Oops! That map has already been scaled!");
									return true;
								}
							} catch (Exception e) {
								//player.sendMessage(ChatColor.RED + "Uhoh, something went wrong!");
								System.out.println(e);
								return false;
							}
						} else {
							player.sendMessage(ChatColor.RED + "You need to be holding the map that you want to scale!");
						}
					} else {
						player.sendMessage(ChatColor.GREEN + "Please specify the zoom level of map you want to scale.");
						player.sendMessage(ChatColor.GREEN + "Valid values are: closest, close, normal, far, farthest.");
					}
				} else {
					player.sendMessage(ChatColor.RED + "You don't have permission to scale maps.");
				}
			}
		}
		return false;
	}
	
	private boolean canScaleMap(Player player, short mapnum) {
		if(plugin.maps.containsKey(String.valueOf(mapnum))) {
			long currenttime = System.currentTimeMillis();
			long lastedittime = Long.parseLong(plugin.maps.get(String.valueOf(mapnum)));
			long difference = currenttime - lastedittime;
			//If we have a negative time, let's flip it around...
			//This should never happen, but you know how clocks work sometimes...
			if(difference < 0) {
				difference = difference - difference - difference;
			}
			if(difference <= (plugin.scaletime * 1000)) {
				String editplayer = plugin.editedmaps.get(String.valueOf(mapnum));
				if(editplayer.equalsIgnoreCase(player.getName())) {
					return true;
				}
			}
			
		}else {
			return true;
		}
		return false;
	}

	boolean hasTwoOfItem(Player player, int item) {
		int num = 0;
		ItemStack[] contents = player.getInventory().getContents();
		for(int i = 0; i < contents.length && num < 2; i++) {
			if(contents[i] != null && contents[i].getTypeId() == item) {
				num++;
			}
		}
		if(num >= 2) {
			return true;
		}else {
			return false;
		}
	}
	
	boolean hasMap(Player player, short mapnumber) {
		ItemStack[] contents = player.getInventory().getContents();
		for(int i = 0; i < contents.length; i++) {
			if(contents[i] != null && contents[i].getTypeId() == 358 && contents[i].getDurability() == mapnumber) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Zoom in the particular map.
	 * @param mapnumber Map number to zoom in.
	 * @return true if it was able to, otherwise false if it is currently all the way zoomed in.
	 */
	boolean scaleMapIn(short mapnumber) {
		MapView themap = plugin.getServer().getMap(mapnumber);
		Scale mscale = themap.getScale();
		byte raw = mscale.getValue();
		raw++;
		Scale newzoom = Scale.valueOf(raw);
		if(newzoom != null) {
			themap.setScale(newzoom);
			return true;
		}
		else {
			return false;
		}
		
	}
	
	/**
	 * Zoom out the particular map.
	 * @param mapnumber Map number to zoom out.
	 * @return true if it was able to, otherwise false if it is currently all the way zoomed out.
	 */
	boolean scaleMapOut(short mapnumber) {
		MapView themap = plugin.getServer().getMap(mapnumber);
		Scale mscale = themap.getScale();
		byte raw = mscale.getValue();
		raw--;
		Scale newzoom = Scale.valueOf(raw);
		if(newzoom != null) {
			themap.setScale(newzoom);
			return true;
		}
		else {
			return false;
		}
		
	}
	
	boolean scaleMap(short mapnumber, String newscale) {
		Scale thescale;
		if(newscale.equalsIgnoreCase("1") || newscale.equalsIgnoreCase("farthest")) {
			thescale = Scale.FARTHEST;
		}else if(newscale.equalsIgnoreCase("2") || newscale.equalsIgnoreCase("far")) {
			thescale = Scale.FAR;
		}else if(newscale.equalsIgnoreCase("3") || newscale.equalsIgnoreCase("normal")) {
			thescale = Scale.NORMAL;
		}else if(newscale.equalsIgnoreCase("4") || newscale.equalsIgnoreCase("close")) {
			thescale = Scale.CLOSE;
		}else if(newscale.equalsIgnoreCase("5") || newscale.equalsIgnoreCase("closest")) {
			thescale = Scale.CLOSEST;
		}else if(newscale.equalsIgnoreCase("in")) {
			return scaleMapIn(mapnumber);
		}else if(newscale.equalsIgnoreCase("out")) {
			return scaleMapOut(mapnumber);
		}else {
			return false;
		}
		return scaleMap(mapnumber, thescale);
	}
	
	
	/**
	 * Scale a map to the value specified.
	 * @param mapnumber Map to scale
	 * @param newscale new scaled value
	 * @return true if scaling was successful, false if the map is already that scale.
	 */
	boolean scaleMap(short mapnumber, Scale newscale) {
		MapView themap = plugin.getServer().getMap(mapnumber);
		Scale mscale = themap.getScale();
		if(mscale == newscale) {
			return false;
		}
		themap.setScale(newscale);
		return true;
	}
	
	
	/**
	 * 
	 * @param mapnumber
	 * @return
	 */
	String getMapScale(short mapnumber) {
		MapView themap = plugin.getServer().getMap(mapnumber);
		Scale mscale = themap.getScale();
		return mscale.toString();
	}
	
	boolean hasItem(Player player, int itemid, short damage, int quantity) {
		ItemStack[] contents = player.getInventory().getContents();
		int q = 0;
		for(int i = 0; i < contents.length; i++) {
			if(contents[i] != null && contents[i].getTypeId() == itemid && contents[i].getDurability() == damage) {
				q += contents[i].getAmount();
				if(q >= quantity) {
					return true;
				}
			}
		}
		return false;
	}
	
	boolean removeItem(Player player, int itemid, short damage, int quantity) {
		ItemStack[] contents = player.getInventory().getContents();
		int q = 0;
		for(int i = 0; i < contents.length; i++) {
			if(contents[i] != null && contents[i].getTypeId() == itemid && contents[i].getDurability() == damage) {
				if(contents[i].getAmount() <= (quantity - q)) {
					q += contents[i].getAmount();
					player.getInventory().setItem(i, null);
				}else {
					contents[i].setAmount(contents[i].getAmount() - (quantity - q));
					q += (quantity - q);
				}
				if(q >= quantity) {
					return true;
				}
			}
		}
		return false;
	}

}
