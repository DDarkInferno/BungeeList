package co.ignitus.bungeelist.files;

import co.ignitus.bungeelist.BungeeList;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;

public class FileManager {

    private BungeeList bungeeList = BungeeList.getInstance();

    private String fileName;
    private File file;
    private Configuration configuration;

    public FileManager(String fileName) {
        this.fileName = fileName;
        File folder = bungeeList.getDataFolder();
        if(!folder.exists())
            folder.mkdir();
        this.file = new File(folder, fileName);
        saveDefaultConfig();
        reloadConfig();
    }

    public void saveDefaultConfig() {
        try {
            if (!file.exists()) {
                file.createNewFile();
                try (InputStream in = bungeeList.getResourceAsStream(fileName);
                     OutputStream out = new FileOutputStream(file)) {
                    ByteStreams.copy(in, out);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void reloadConfig() {
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Configuration getConfiguration() {
        if (configuration == null)
            reloadConfig();
        return configuration;
    }

    public void saveConfiguration() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
