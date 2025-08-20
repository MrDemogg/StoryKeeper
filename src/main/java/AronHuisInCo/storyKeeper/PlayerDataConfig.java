// Файл: PlayerDataConfig.java
package AronHuisInCo.storyKeeper;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

public class PlayerDataConfig {
    private final JavaPlugin plugin;
    private YamlConfiguration config;
    private final File configFile;

    public PlayerDataConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "players.yml");
        reload();
    }

    public void reload() {
        if (!configFile.exists()) {
            plugin.saveResource("players.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save players.yml", e);
        }
    }

    public boolean has(String path) {
        return config.contains(path);
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public void set(String path, Object value) {
        config.set(path, value);
        save();
    }

    public Set<String> getKeys(String path) {
        if (config.contains(path)) {
            return config.getConfigurationSection(path).getKeys(false);
        }
        return Set.of();
    }
}