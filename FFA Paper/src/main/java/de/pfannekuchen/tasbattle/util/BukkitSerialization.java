package de.pfannekuchen.tasbattle.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

/**
 * Serialize Stuff
 * @author graywolf336
 */
public class BukkitSerialization {
	
	/**
	 * Converts the player inventory to a String array of Base64 strings. First string is the content and second string is the armor.
	 * 
	 * @param playerInventory to turn into an array of strings.
	 * @return Array of strings: [ main content, armor content ]
	 * @throws IllegalStateException
	 */
	public static String[] playerInventoryToBase64(PlayerInventory playerInventory) throws IllegalStateException {
		//get the main content part, this doesn't return the armor
		String content = itemStackArrayToBase64(playerInventory.getContents());
		String additional = itemStackArrayToBase64(playerInventory.getExtraContents());
		String armor = itemStackArrayToBase64(playerInventory.getArmorContents());
		
		return new String[] { content, additional, armor };
	}
	
	
	/**
	 * Converts the base 64 array to a Player Inventory. First string is the content and second string is the armor.
	 * 
	 * @param playerInventory to turn into an array of strings.
	 * @return Array of strings: [ main content, armor content ]
	 * @throws IllegalStateException
	 * @throws IOException 
	 */
	public static void playerInventoryToBase64(Player p, String[] data) throws IllegalStateException, IOException {
		//get the main content part, this doesn't return the armor
		ItemStack[] content = itemStackArrayFromBase64(data[0]);
		ItemStack[] additional = itemStackArrayFromBase64(data[1]);
		ItemStack[] armor = itemStackArrayFromBase64(data[2]);
		p.getInventory().setContents(content);
		p.getInventory().setExtraContents(additional);
		p.getInventory().setArmorContents(armor);
	}
	
	/**
	 * 
	 * A method to serialize an {@link ItemStack} array to Base64 String.
	 * 
	 * <p />
	 * 
	 * Based off of {@link #toBase64(Inventory)}.
	 * 
	 * @param items to turn into a Base64 String.
	 * @return Base64 string of the items.
	 * @throws IllegalStateException
	 */
	public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
		try {
	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
	        
	        // Write the size of the inventory
	        dataOutput.writeInt(items.length);
	        
	        // Save every element in the list
	        for (int i = 0; i < items.length; i++) {
	            dataOutput.writeObject(items[i]);
	        }
	        
	        // Serialize that array
	        dataOutput.close();
	        return Base64Coder.encodeLines(outputStream.toByteArray());
	    } catch (Exception e) {
	        throw new IllegalStateException("Unable to save item stacks.", e);
	    }
	}
	
	/**
	 * Gets an array of ItemStacks from Base64 string.
	 * 
	 * <p />
	 * 
	 * Base off of {@link #fromBase64(String)}.
	 * 
	 * @param data Base64 string to convert to ItemStack array.
	 * @return ItemStack array created from the Base64 string.
	 * @throws IOException
	 */
	public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
		try {
	        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
	        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
	        ItemStack[] items = new ItemStack[dataInput.readInt()];
	
	        // Read the serialized inventory
	        for (int i = 0; i < items.length; i++) {
	        	items[i] = (ItemStack) dataInput.readObject();
	        }
	        
	        dataInput.close();
	        return items;
	    } catch (ClassNotFoundException e) {
	        throw new IOException("Unable to decode class type.", e);
	    }
	}
	
}

