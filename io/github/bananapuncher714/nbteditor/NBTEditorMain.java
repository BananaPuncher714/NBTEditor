package io.github.bananapuncher714.nbteditor;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class NBTEditorMain extends JavaPlugin implements Listener {
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents( this, this );
	}

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
		if ( sender instanceof Player ) {
			Player player = ( Player ) sender;
			Block block = player.getTargetBlock( ( Set< Material > ) null, 100 );
			if ( block.getType() == Material.CHEST ) {
				NBTEditor.setBlockTag( block, "Notch", "Lock" );
				player.sendMessage( "Set lock to 'Notch'" );
			}
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerInteractEvent( PlayerInteractAtEntityEvent event ) {
		Entity entity = event.getRightClicked();
		Object ai = NBTEditor.getEntityTag( entity, "NoAI" );
		byte noAi;
		if ( ai == null ) {
			noAi = 0;
		} else {
			noAi = ( byte ) ai;
		}
		event.getPlayer().sendMessage( "Set NoAI to " + noAi );
		if ( noAi == 1 ) {
			NBTEditor.setEntityTag( entity, ( byte ) 0, "NoAI" );
		} else {
			NBTEditor.setEntityTag( entity, ( byte ) 1, "NoAI" );
		}
	}
}
