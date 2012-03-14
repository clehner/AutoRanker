package com.celehner.AutoRanker;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.event.Listener;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import de.diddiz.LogBlock.LogBlock;
import org.bukkit.event.EventHandler;

public class AutoRanker extends JavaPlugin
{

	private LogBlock logblock = null;
	List<Rank> ranks;
	Logger log = Logger.getLogger("AutoRanker");

	@Override
	public void onEnable() {
		final PluginManager pm = getServer().getPluginManager();
		if (pm.getPlugin("LogBlock") == null) return;
		logblock = (LogBlock)pm.getPlugin("LogBlock");

		updateRanksFromConfig();

		getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onPlayerJoin(PlayerJoinEvent event) {
				updatePlayerRanks(event.getPlayer());
			}
		}, this);

		this.getCommand("autorank").setExecutor(new CommandExecutor() {
			public boolean onCommand(CommandSender sender,
				Command command, String label, String[] args) {
				if (label.equals("reload")) {
					if (sender.isOp() || sender.hasPermission("autorank.reload")) {
						updateRanksFromConfig();
						sender.sendMessage("Reloaded AutoRanker config.");
						return true;
					} else {
						sender.sendMessage(ChatColor.RED +
							"No permission to do that.");
					}
				}
				return false;
			}
		});

		log.info("[AutoRanker] Enabled.");
	}

	@Override
	public void onDisable() {
	}

	public LogBlock getLogBlock() {
		return logblock;
	}

	public void updateRanksFromConfig() {
		ranks = new ArrayList<Rank>();
		FileConfiguration config = getConfig();
		ConfigurationSection ranksSec = null;
		if (config.isSet("ranks")) {
			ranksSec = config.getConfigurationSection("ranks");
		} else {
			ranksSec = config.createSection("ranks");
			saveConfig();
		}

 		for (final String rankName : ranksSec.getKeys(false)) {
			try {
				ConfigurationSection rSec =
					ranksSec.getConfigurationSection(rankName);

				if (!rSec.isSet("requirements")) {
					log.info("[AutoRanker] Rank is missing requirements.");
					continue;
				}

				ConfigurationSection reqs =
					rSec.getConfigurationSection("requirements");

				int daysOnline = reqs.getInt("daysonline", 0);
				int created = reqs.getInt("created", 0);
				int destroyed = reqs.getInt("destroyed", 0);
				String onlineTime = reqs.getString("onlinetime", "");
				String hasPermission = reqs.getString("haspermission", "");
				String hasNotPermission = reqs.getString("hasnotpermission", "");

				List<String> commands = rSec.getStringList("commands");

				ranks.add(new Rank(rankName, daysOnline, onlineTime,
					hasPermission, hasNotPermission,
					created, destroyed, commands, this));
				log.info("[AutoRanker] Added rank \"" + rankName + "\".");
			} catch (final Exception ex) {
				log.log(Level.WARNING, "[AutoRanker] Error at parsing rank '" +
					rankName + "': ", ex);
			}
		}
	}

	public void updatePlayerRanks(Player player) {
		//log.info("[AutoRanker] updating ranks for " + player.getName() + ".");
		//log.info("[AutoRanker] " + ranks.size() + ".");
		for (final Rank rank : ranks) {
			//log.info("[AutoRanker] rank.");
			rank.applyCheckToPlayer(player);
		}
	}
}
