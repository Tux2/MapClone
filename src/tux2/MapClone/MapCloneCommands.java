package tux2.MapClone;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.iConomy.system.Holdings;

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
										if(plugin.useiconomy && plugin.iConomy != null) {
											if(plugin.hasPermissions(player, "mapclone.free")) {
												player.getItemInHand().setDurability((short)mapnum);
												player.sendMessage(ChatColor.GOLD + "Poof! You just cloned a map!");
											}else if(plugin.iConomy.hasAccount(player.getName())) {
												Holdings balance = plugin.iConomy.getAccount(player.getName()).getHoldings();
												if(balance.hasEnough(plugin.iconomyprice)) {
													balance.subtract(plugin.iconomyprice);
													player.getItemInHand().setDurability((short)mapnum);
													player.sendMessage(ChatColor.GOLD + "Poof! You just cloned a map for " + plugin.iConomy.format(plugin.iconomyprice) + "!");
												}else {
													player.sendMessage(ChatColor.RED + "You need " + plugin.iConomy.format(plugin.iconomyprice) + " to clone that map!");
												}
										    } else {
										    	player.sendMessage(ChatColor.RED + "You need a bank account and " + plugin.iConomy.format(plugin.iconomyprice) + " to clone a map!");
										    }
										}else {
											ItemStack items = new ItemStack(plugin.useitem, 1, (short)plugin.useitemdamage);
											if(plugin.hasPermissions(player, "mapclone.free") || plugin.useitem == 0) {
												player.getItemInHand().setDurability((short)mapnum);
												player.sendMessage(ChatColor.GOLD + "Poof! You just cloned a map!");
											}else if(hasItem(player, plugin.useitem, (short)plugin.useitemdamage)) {
												removeItem(player, plugin.useitem, (short)plugin.useitemdamage);
												player.getItemInHand().setDurability((short)mapnum);
												player.sendMessage(ChatColor.GOLD + "Poof! You just cloned a map for 1 " + items.getType().name().toLowerCase().replace('_', ' ') + "!");

											} else {
												player.sendMessage(ChatColor.RED + "You need 1 " + items.getType().name().toLowerCase().replace('_', ' ') + " to clone a map!");
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
			}
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
	
	boolean hasItem(Player player, int itemid, short damage) {
		ItemStack[] contents = player.getInventory().getContents();
		for(int i = 0; i < contents.length; i++) {
			if(contents[i] != null && contents[i].getTypeId() == itemid && contents[i].getDurability() == damage) {
				return true;
			}
		}
		return false;
	}
	
	boolean removeItem(Player player, int itemid, short damage) {
		ItemStack[] contents = player.getInventory().getContents();
		for(int i = 0; i < contents.length; i++) {
			if(contents[i] != null && contents[i].getTypeId() == itemid && contents[i].getDurability() == damage) {
				if(contents[i].getAmount() <= 1) {
					player.getInventory().setItem(i, null);
				}else {
					contents[i].setAmount(contents[i].getAmount() - 1);
				}
				return true;
			}
		}
		return false;
	}

}
