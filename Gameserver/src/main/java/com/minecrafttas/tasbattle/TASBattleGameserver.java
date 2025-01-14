package com.minecrafttas.tasbattle;

import com.minecrafttas.tasbattle.bedwars.Bedwars;
import com.minecrafttas.tasbattle.ffa.FFA;
import com.minecrafttas.tasbattle.gui.GuiHandler;
import com.minecrafttas.tasbattle.lobby.Lobby;
import com.minecrafttas.tasbattle.lobby.LobbyManager;
import com.minecrafttas.tasbattle.managers.SpectatingCommand;
import com.minecrafttas.tasbattle.managers.TickrateChanger;
import com.minecrafttas.tasbattle.stats.StatsManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TASBattleGameserver extends JavaPlugin implements CommandExecutor, Listener {
	
	public interface GameMode {
		interface CommandHandler extends CommandExecutor, TabCompleter {}
		void startGameMode(List<Player> players);
		List<LobbyManager> createManagers();
		List<Pair<String, CommandHandler>> createCommands();
	}

	private TickrateChanger tickrateChanger;
	private GameMode gameMode;
	private StatsManager statsManager;
	private Lobby lobby;
	private SpectatingCommand spectatingCommand;
	
	/**
	 * Enable TAS Battle mod
	 */
	@SneakyThrows
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new GuiHandler(), this);
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getWorlds().forEach(w -> w.setAutoSave(false));

		this.tickrateChanger = new TickrateChanger(this);
		this.spectatingCommand = new SpectatingCommand(this);

		var mode = System.getProperty("mode");
		this.gameMode = switch (mode) {
			case "ffa": yield new FFA(this);
			case "bedwars": yield new Bedwars(this);
			default: throw new IllegalArgumentException("Unsupported gamemode");
		};

		this.statsManager = new StatsManager(this, mode);

		this.getCommand("halt").setExecutor(this);

		for (var pair : this.gameMode.createCommands()) {
			this.getCommand(pair.getKey()).setExecutor(pair.getValue());
			this.getCommand(pair.getKey()).setTabCompleter(pair.getValue());
		}

		if (System.getProperty("dev") == null)
			this.lobby = new Lobby(this, this.gameMode);
		else
			this.gameMode.startGameMode(new ArrayList<>());
	}

	/**
	 * Halt gameserver on /halt
	 * @param sender Source of the command
	 * @param command Command which was executed
	 * @param label Alias of the command which was used
	 * @param args Passed command arguments
	 * @return Command success
	 */
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (sender.isOp())
			Runtime.getRuntime().halt(0);
		else
			sender.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>»</aqua> <red>Insufficient permissions</red>"));

		return true;
	}

	@EventHandler
	public void cancelChat(AsyncChatEvent e) {
		e.setCancelled(true);
	}

}
