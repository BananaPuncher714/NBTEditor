package io.github.bananapuncher714.nbteditor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class NBTEditorTestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        try {
            // TODO Do all test here.
            throw new RuntimeException("Test close");
        } catch (final Throwable e) {
            e.printStackTrace();
            final File serverDir = this.getDataFolder().getParentFile().getParentFile();
            final File errorFile = new File(serverDir, "error.txt");
            try {
                Files.write(Files.createFile(errorFile.toPath()), e.toString().getBytes());
            } catch (final IOException ioException) {
                ioException.printStackTrace();
            }
            Bukkit.getServer().shutdown();
        }
    }

}
