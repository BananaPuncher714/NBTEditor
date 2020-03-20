package io.github.bananapuncher714.nbteditor;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.bananapuncher714.nbteditor.NBTEditor.NBTCompound;

public class NBTEditorMain extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents( this, this );
		{
			ItemStack item = new ItemStack( Material.DIAMOND_HOE );
			System.out.println( "Setting value..." );
			item = NBTEditor.set( item, "Hello, world!", "io", "github", "bananapuncher714", "nbteditor", "test" );
			System.out.println( "Getting value..." );
			System.out.println( NBTEditor.getString( item, "io", "github", "bananapuncher714", "nbteditor", "test" ) );
			System.out.println( NBTEditor.getItemNBTTag( item ) );
		}
		
		{
			ItemStack item = new ItemStack( Material.DIAMOND_SWORD );
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName( "Test" );
			item.setItemMeta( meta );
			NBTCompound compound = NBTEditor.getItemNBTTag( item );

			// To add one attribute
			compound.set( "generic.attackDamage", "tag", "AttributeModifiers", null, "AttributeName" );
			compound.set( "Attack Damage", "tag", "AttributeModifiers", 0, "Name" );
			compound.set( "mainhand", "tag", "AttributeModifiers", 0, "Slot" );
			compound.set( 0, "tag", "AttributeModifiers", 0, "Operation" );
			compound.set( 20.0, "tag", "AttributeModifiers", 0, "Amount" );
			compound.set( 99L, "tag", "AttributeModifiers", 0, "UUIDMost" );
			compound.set( 77530600L, "tag", "AttributeModifiers", 0, "UUIDLeast" );

			// To add another attribute
			compound.set( "generic.attackKnockback", "tag", "AttributeModifiers", null, "AttributeName" );
			compound.set( "Knockback", "tag", "AttributeModifiers", 1, "Name" );
			compound.set( "mainhand", "tag", "AttributeModifiers", 1, "Slot" );
			compound.set( 0, "tag", "AttributeModifiers", 1, "Operation" );
			compound.set( 5.0, "tag", "AttributeModifiers", 1, "Amount" );
			compound.set( 99L, "tag", "AttributeModifiers", 1, "UUIDMost" );
			compound.set( 77530600L, "tag", "AttributeModifiers", 1, "UUIDLeast" );

			// A third attribute
			compound.set( "generic.movementSpeed", "tag", "AttributeModifiers", null, "AttributeName" );
			compound.set( "Movement", "tag", "AttributeModifiers", 2, "Name" );
			compound.set( "mainhand", "tag", "AttributeModifiers", 2, "Slot" );
			compound.set( 1, "tag", "AttributeModifiers", 2, "Operation" );
			compound.set( 2.0, "tag", "AttributeModifiers", 2, "Amount" );
			compound.set( 99L, "tag", "AttributeModifiers", 2, "UUIDMost" );
			compound.set( 77530600L, "tag", "AttributeModifiers", 2, "UUIDLeast" );

			// Now convert it back into an item
			System.out.println( compound );
			item = NBTEditor.getItemFromTag( compound );
			System.out.println( item );
			System.out.println( NBTEditor.getItemNBTTag( item ) );
			
			for ( Player p : Bukkit.getOnlinePlayers() ) {
				p.getInventory().addItem( item );
			}
		}
        
		ItemStack skull1 = NBTEditor.getHead( "http://textures.minecraft.net/texture/7fe9725c950472e469b9fccae32f61bcefebdb5ea9ce9c92d58171ffb7a336fe" );
		ItemStack skull2 = skull1.clone();
		
		System.out.println( "isSimilar skull check: " + ( skull1.isSimilar( skull2 ) ) );
	}

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
		if ( sender instanceof Player ) {
			Player player = ( Player ) sender;
			if ( args.length == 0 ) {
				Block block = player.getTargetBlock( ( Set< Material > ) null, 100 );
				if ( block.getState() instanceof InventoryHolder ) {
					String rand = String.valueOf( ThreadLocalRandom.current().nextInt( 8999999 ) + 1000000 );
					String legacized = legacyEncode( rand );
					NBTEditor.set( block, ChatColor.RESET + "Key" + legacized, "Lock" );
					player.sendMessage( "Set lock to '" + rand + "'" );
					player.sendMessage( "" + NBTEditor.getString( block, "Lock" ) );
					ItemStack item = new ItemStack( Material.TRIPWIRE_HOOK );
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName( ChatColor.RESET + "Key" + legacized );
					item.setItemMeta( meta );
					player.getInventory().addItem( item );
				} else if ( block.getType() == Material.LEGACY_MOB_SPAWNER ) {
					NBTEditor.set( block, 25f, "SpawnRange");
					player.sendMessage( "Changed SpawnRange to 25f" );
				}
			} else if ( args.length == 1 ) {
				if ( args[ 0 ].equalsIgnoreCase( "enchant" ) ) {
					ItemStack item = player.getItemInHand();
					if ( item != null ) {
						Map< Object, Object > tags = ( Map< Object, Object > ) NBTEditor.getItemTag( item );
						Object enchants = tags.get( "ench" );
						if ( enchants instanceof Map< ?, ? > ) {
							Map< Object, Object > enchantments = ( Map< Object, Object > ) enchants;
							
							for ( Object enchantment : enchantments.values() ) {
								if ( enchantment instanceof Map< ?, ? > ) {
									Map< Object, Object > values = ( Map< Object, Object > ) enchantment;
									for ( Object obj : values.keySet() ) {
										System.out.println( obj + ":" + values.get( obj ) );
									}
								}
							}
							if ( enchantments.size() < 2 ) {
								item = NBTEditor.set( item, 2, "ench", null, "id" );
								item = NBTEditor.set( item, ( short ) 3, "ench", 1, "lvl" );
								player.setItemInHand( item );
							}
						} else {
							printNestedMap( "base", tags );
						}
					}
				} else if ( args[ 0 ].equalsIgnoreCase( "settag" ) ) {
					ItemStack item = player.getItemInHand();
					if ( item != null ) {
						item = NBTEditor.set( item, "A VALUE", "test", "value" );
						player.setItemInHand( item );
						player.sendMessage( "" + NBTEditor.getString( item, "test", "value" ) );
					}
				} else if ( args[ 0 ].equalsIgnoreCase( "fireball" ) ) {
					Location loc = player.getEyeLocation().add( 0, 2, 0 );
					Fireball fireball = ( Fireball ) player.getWorld().spawnEntity( loc, EntityType.FIREBALL );
					ItemStack item = player.getInventory().getItemInMainHand();
					NBTEditor.set( fireball, NBTEditor.getItemNBTTag( item ), "Item" );
				}
			}
		}
		return false;
	}

	@EventHandler
	public void onPlayerInteractEvent( PlayerInteractAtEntityEvent event ) {
		if ( event.getHand() != EquipmentSlot.HAND ) {
			return;
		}
		Entity entity = event.getRightClicked();
		byte noAi;
		if ( !NBTEditor.contains( entity, "NoAI" ) ) {
			noAi = 0;
		} else {
			noAi = NBTEditor.getByte( entity, "NoAI" );
		}
		event.getPlayer().sendMessage( "Set NoAI to " + noAi );
		if ( noAi == 1 ) {
			NBTEditor.set( entity, ( byte ) 0, "NoAI" );
		} else {
			NBTEditor.set( entity, ( byte ) 1, "NoAI" );
		}
	}
	
	public static void printNestedMap( Object base, Map< Object, Object > map ) {
	    for ( Iterator< Object > it = map.keySet().iterator(); it.hasNext(); ) {
	        Object base2 = it.next();
	        Object mm = map.get( base2 );
	        if ( mm instanceof Map ) {
	            printNestedMap( base + "." + base2, ( Map< Object, Object > ) mm );
	        } else {
	            System.out.println( base + "." + base2 + "." + mm );
	        }
	    }
	}
	
	public static String legacyEncode( String string ) {
		StringBuilder builder = new StringBuilder();
		for ( char ch : string.toCharArray() ) {
			builder.append( "\u00a7" + ch );
		}
		return builder.toString();
	}
}
