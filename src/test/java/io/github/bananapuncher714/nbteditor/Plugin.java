package io.github.bananapuncher714.nbteditor;

import java.io.File;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;

public final class Plugin extends JavaPlugin {

    public Plugin() {
    }

    public Plugin(@NotNull final JavaPluginLoader loader, @NotNull final PluginDescriptionFile description,
                  @NotNull final File dataFolder, @NotNull final File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() {

    }

}
