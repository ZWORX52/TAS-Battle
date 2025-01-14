package com.minecrafttas.tasbattle.gui;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class GuiHandler implements Listener {

	public static final Map<Inventory, ClickableInventory> instances = new HashMap<>();
	
	@EventHandler
	public void onInteract(InventoryClickEvent e) {
		if (instances.containsKey(e.getClickedInventory())) {
			instances.get(e.getClickedInventory()).onInteract((Player) e.getWhoClicked(), e.getSlot());
			e.setCancelled(true);
		}
	}
	
}
