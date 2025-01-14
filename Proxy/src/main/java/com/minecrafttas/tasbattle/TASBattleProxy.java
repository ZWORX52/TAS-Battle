package com.minecrafttas.tasbattle;

import com.google.inject.Inject;
import com.minecrafttas.tasbattle.managers.*;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Proxy plugin class
 * @author Pancake
 */
@Plugin(name = "TAS-Battle-Proxy", version = "1.0.0", id = "proxy", authors = { "Pancake" },
		url = "https://github.com/MinecraftTAS/TAS-Battle", description = "tas battle proxy plugin")
@Getter
public class TASBattleProxy {

	private final ProxyServer server;
	private final Logger logger;
	private DataManager dataManager;
	private LobbyCommand lobbyCommand;
	private PermissionManager permissionManager;
	private ChatSystem chatSystem;
	private CustomTabList customTabList;

	/**
     * Construct proxy plugin
     * @param server Proxy server instance
	 * @param logger Logger instance
	 * @param dataDirectory Data directory
     */
	@Inject
	public TASBattleProxy(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
		this.server = server;
		this.logger = logger;
	}

	/**
	 * Initialize proxy plugin
	 * @param e Proxy initialization event
	 */
	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent e) {
		// initialize managers
		this.dataManager = new DataManager(this);
		this.lobbyCommand = new LobbyCommand(this);
		this.permissionManager = new PermissionManager(this);
		this.chatSystem = new ChatSystem(this);
		this.customTabList = new CustomTabList(this);
	}

}
