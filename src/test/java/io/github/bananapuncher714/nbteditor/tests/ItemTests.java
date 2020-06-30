package io.github.bananapuncher714.nbteditor.tests;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.github.bananapuncher714.nbteditor.NBTEditor;

public class ItemTests {
	public static void ensureItemCustomStringSetAndGet() {
		String value = UUID.randomUUID().toString();
		Object[] keys = new String[] { "some", "arbitrary", "key" };
		ItemStack item = new ItemStack( Material.GRASS_BLOCK );
		
		item = NBTEditor.set( item, value, keys );
		String get = NBTEditor.getString( item, keys );
		
		assert value.equals( get ) : String.format( "Failed itemstack custom string set and get! Expected %s, got %s", value, get );
	}
	
	public static void ensureItemCustomIntSetAndGet() {
		int value = 894520;
		Object[] keys = new String[] { "some", "arbitrary", "key" };
		ItemStack item = new ItemStack( Material.GRASS_BLOCK );
		
		item = NBTEditor.set( item, value, keys );
		int get = NBTEditor.getInt( item, keys );
		
		assert value == get : String.format( "Failed itemstack custom int set and get! Expected %d, got %d", value, get );
	}
	
	public static void ensureItemCustomDoubleSetAndGet() {
		double value = 1.048596;
		Object[] keys = new String[] { "some", "arbitrary", "key" };
		ItemStack item = new ItemStack( Material.GRASS_BLOCK );
		
		item = NBTEditor.set( item, value, keys );
		double get = NBTEditor.getDouble( item, keys );
		
		assert value == get : String.format( "Failed itemstack custom double set and get! Expected %f, got %f", value, get );
	}
	
	public static void ensureItemCustomBooleanSetAndGet() {
		boolean value = true;
		Object[] keys = new String[] { "some", "arbitrary", "key" };
		ItemStack item = new ItemStack( Material.GRASS_BLOCK );
		
		item = NBTEditor.set( item, value, keys );
		boolean get = NBTEditor.getBoolean( item, keys );
		
		assert value == get : String.format( "Failed itemstack custom boolean set and get! Expected %b, got %b", value, get );
	}
}
