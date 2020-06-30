package io.github.bananapuncher714.nbteditor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import io.github.bananapuncher714.nbteditor.tests.ItemTests;

public final class NBTEditorTestPlugin extends JavaPlugin {
	@Override
	public void onEnable() {
		final Runnable[] tests = new Runnable[] {
				ItemTests::ensureItemCustomStringSetAndGet,
				ItemTests::ensureItemCustomIntSetAndGet,
				ItemTests::ensureItemCustomDoubleSetAndGet,
				ItemTests::ensureItemCustomBooleanSetAndGet,
		};
		
		final List< AssertionError > exceptions = new ArrayList< AssertionError >();
		
		getLogger().info( "Running NBTEditor Tests" );
		int i = 0;
		for ( Runnable test : tests ) {
			try {
				test.run();
				getLogger().info( String.format( "Completed %d out of %d", ++i, tests.length ) );
			} catch ( AssertionError e ) {
				getLogger().severe( String.format( "Failed test %d", ++i ) );
				exceptions.add( e );
			}
		}
		
		if ( !exceptions.isEmpty() ) {
			getLogger().severe( String.format( "Failed %d out of %d tests", exceptions.size(), tests.length ) );
			final File errorFile = new File( getDataFolder().getParentFile().getParentFile(), "error.txt" );
			try ( Writer writer = new FileWriter( errorFile ) ) {
				for ( AssertionError exception : exceptions ) {
					writer.append( exception.getMessage() );
					writer.append( '\n' );
				}
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		} else {
			getLogger().info( "Passed all tests!" );
		}
	}
}
