package net.leonemc.example;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import lombok.SneakyThrows;
import net.leonemc.example.user.UserListener;
import net.leonemc.example.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
public class ExamplePlugin extends JavaPlugin {

    @Getter
    private static ExamplePlugin instance;
    private SettingsManager configuration;

    private Database database;
    private PaperCommandManager commandManager;

    private UserManager userManager;

    @Override
    public void onEnable() {
        instance = this;

        this.loadConfig();

        this.database = new Database(this);
        this.commandManager = new PaperCommandManager(this);

        this.userManager = new UserManager(this);

        this.registerCommands();
        this.registerListeners();
    }

    @Override
    public void onDisable() {

    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new UserListener(this, userManager), this);
    }

    private void registerCommands() {
        this.commandManager.registerDependency(UserManager.class, this.userManager);
    }

    @SneakyThrows
    private void loadConfig() {
        Path pluginFolder = Paths.get("plugins", this.getName());
        Files.createDirectories(pluginFolder);

        Path configFile = pluginFolder.resolve("config.yml");
        this.configuration = SettingsManagerBuilder
                .withYamlFile(configFile.toFile())
                .configurationData(ExampleConfig.class)
                .useDefaultMigrationService()
                .create();
    }

}
