package com.minecrafttas.tasbattle.managers;

import com.minecrafttas.tasbattle.TASBattleGameserver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

/**
 * Tickrate Changer module
 * @author Pancake
 */
public class TickrateChanger implements PluginMessageListener, Listener, CommandExecutor {

	private static final String TRC_CHANNEL = "tickratechanger:data";

	private final HashMap<UUID, BukkitTask> verificationTimers = new HashMap<>();
	private final TASBattleGameserver plugin;
	private float tickrate;

	/**
	 * Initialize tickrate changer
	 * @param plugin Plugin
	 */
	public TickrateChanger(TASBattleGameserver plugin) {
		this.plugin = plugin;

		// register plugin channels
		var messenger = Bukkit.getMessenger();
		messenger.registerOutgoingPluginChannel(plugin, TRC_CHANNEL);
		messenger.registerIncomingPluginChannel(plugin, TRC_CHANNEL, this);

		// register events
		Bukkit.getPluginManager().registerEvents(this, plugin);
		plugin.getCommand("tickrate").setExecutor(this);
		this.setTickrate(20.0f);
	}

	/**
	 * Update tickrate
	 * @param tickrate New tickrate between 0.1 and 100
	 */
	public void setTickrate(float tickrate) {
		if (tickrate < 0.1f || tickrate > 100.0f)
			return;

		// update server tickrate
		this.tickrate = tickrate;
		try {
			var msPerTick = Class.forName("net.minecraft.server.MinecraftServer").getField("MS_PER_TICK"); // unavoidable obfuscation
			msPerTick.setAccessible(true);
			msPerTick.setInt(null, (int) (1000 / tickrate));

			var gamespeed = Class.forName("net.minecraft.server.MinecraftServer").getField("GAMESPEED");
			gamespeed.setAccessible(true);
			gamespeed.setFloat(null, tickrate / 20.0f);
		} catch (NoSuchFieldException | SecurityException | ClassNotFoundException | IllegalArgumentException | IllegalAccessException e) {
			this.plugin.getSLF4JLogger().error("Unable to update server tickrate!", e);
		}

		// update client tickrate
		Bukkit.getOnlinePlayers().forEach(this::updatePlayer);
	}

	/**
	 * Update tickrate for player
	 * @param p Player
	 */
	public void updatePlayer(Player p) {
		p.sendPluginMessage(plugin, TickrateChanger.TRC_CHANNEL, ByteBuffer.allocate(4).putFloat(this.tickrate).array());
	}

	/**
	 * Schedule verification timer on player join
	 * @param e Player join event
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		// schedule new 8s kick timer for new player
		this.verificationTimers.put(e.getPlayer().getUniqueId(), new BukkitRunnable() {

			@Override
			public void run() {
				if (e.getPlayer().isOnline())
					e.getPlayer().kick(Component.text("Couldn't connect you to the game server. Please make sure you have installed the TAS Battle client properly.", NamedTextColor.RED));
			}

		}.runTaskLater(this.plugin, 8 * 20));

		this.updatePlayer(e.getPlayer());
	}

	/**
	 * Cancel verification timer for player on plugin message
	 */
	@Override
	public void onPluginMessageReceived(@NotNull String channel, Player player, byte[] message) {
		if (this.verificationTimers.containsKey(player.getUniqueId()))
			this.verificationTimers.remove(player.getUniqueId()).cancel();

		this.updatePlayer(player);
	}

	/**
	 * Handle /tickrate command
	 */
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!sender.isOp() || args.length != 1) {
			sender.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>»</aqua> <red>Insufficient permissions</red>"));
			return true;
		}

		try {
			// parse and update tickrate
			var tickrate = Float.parseFloat(args[0]);
			this.setTickrate(tickrate);
			sender.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>»</aqua> <white>Updated tickrate to <green>" + tickrate + "</green></white>"));
		} catch (NumberFormatException e) {
			sender.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>»</aqua> <red>Unable to parse tickrate: <green>" + args[0] + "</green></red>"));
		}
		return true;
	}

}
