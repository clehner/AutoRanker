package com.celehner.AutoRanker;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.QueryParams;
import de.diddiz.LogBlock.QueryParams.BlockChangeType;
import de.diddiz.LogBlock.BlockChange;

public final class Rank {
	String name;
	int hoursOnline;
	int daysOnline;
	int created; // block counts
	int destroyed;
	String hasPermission;
	String hasNotPermission;
	List<String> commands;
	AutoRanker plugin;
	LogBlock logblock;

	Rank(String name, int daysOnline, int hoursOnline,
		String hasPermission, String hasNotPermission,
		int created, int destroyed,
		List<String> commands, AutoRanker plugin) {

		this.name = name;
		this.daysOnline = daysOnline;
		this.hoursOnline = hoursOnline;
		this.hasPermission = hasPermission;
		this.hasNotPermission = hasNotPermission;
		this.created = created;
		this.destroyed = destroyed;
		this.commands = commands;
		this.plugin = plugin;
		this.logblock = plugin.getLogBlock();
	}

	// get days between first and last login for a player
	public int getPlayerDaysOnline(Player player) throws SQLException {
		final String playerName = player.getName().replaceAll("[^a-zA-Z0-9_]", "");
		if (playerName.length() == 0) return 0;
		final Connection conn = logblock.getConnection();
		Statement state = null;
		if (conn == null)
			throw new SQLException("No connection");
		try {
			state = conn.createStatement();
			final ResultSet rs = state.executeQuery(
					"SELECT DATEDIFF(lastlogin, firstlogin) AS daysonline " +
					"FROM `lb-players` WHERE playername='" + playerName + "'");
			if (!rs.next())
				return 0;
			return rs.getInt(1);
		} finally {
			if (state != null)
				state.close();
			conn.close();
		}
	}

	// get player online time in hours
	public int getPlayerHoursOnline(Player player) throws SQLException {
		final String playerName = player.getName().replaceAll("[^a-zA-Z0-9_]", "");
		if (playerName.length() == 0) return 0;
		final Connection conn = logblock.getConnection();
		Statement state = null;
		if (conn == null)
			throw new SQLException("No connection");
		try {
			state = conn.createStatement();
			final ResultSet rs = state.executeQuery(
					"SELECT onlinetime FROM `lb-players` " +
					"WHERE playername = '" + playerName + "'");
			if (!rs.next())
				return 0;
			return rs.getInt(1)/3600;
		} finally {
			if (state != null)
				state.close();
			conn.close();
		}
	}

	public boolean playerMeetsRequirements(Player player) {
		QueryParams p = new QueryParams(logblock);
		p.setPlayer(player.getName());
		p.world = player.getServer().getWorlds().get(0);
		p.since = 0;
		try {
			if (created > 0) {
				p.bct = BlockChangeType.CREATED;
				if (logblock.getCount(p) < created) return false;
			}
			if (destroyed > 0) {
				p.bct = BlockChangeType.DESTROYED;
				if (logblock.getCount(p) < destroyed) return false;
			}
			if (daysOnline > 0) {
				if (getPlayerDaysOnline(player) < daysOnline) return false;
			}
			if (hoursOnline > 0) {
				if (getPlayerHoursOnline(player) < hoursOnline) return false;
			}
		} catch (final SQLException ex) {
			plugin.log.log(Level.WARNING, "Unable to lookup.", ex);
			return false;
		}
		if (hasPermission != "") {
			if (!player.hasPermission(hasPermission)) return false;
		}
		if (hasNotPermission != "") {
			if (player.hasPermission(hasNotPermission)) return false;
		}
		return true;
	}

	// give the promotion
	public void executeCommands(Player player) {
		Server server = player.getServer();
		plugin.log.info("[AutoRanker] Promoting player " + player.getName() +
			" to " + name);
		for (String commandLine : commands) {
			server.dispatchCommand(server.getConsoleSender(),
				commandLine.replace("{PLAYER}", player.getName()));
		}
	}

	public void applyCheckToPlayer(Player player) {
		//plugin.log.info("[AutoRanker] testing " + player.getName());
		if (playerMeetsRequirements(player)) {
			executeCommands(player);
		}
	}
}
