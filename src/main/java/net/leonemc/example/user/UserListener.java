package net.leonemc.example.user;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.leonemc.example.ExamplePlugin;
import net.leonemc.example.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Level;

public class UserListener implements Listener {

    private final ExamplePlugin plugin;
    private final UserManager userManager;

    public UserListener(ExamplePlugin plugin, UserManager userManager) {
        this.plugin = plugin;
        this.userManager = userManager;
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        final Player player = Bukkit.getPlayer(event.getUniqueId());
        final long time = System.currentTimeMillis();

        if (player != null && player.isOnline()) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.kickMessage(Component.text("You tried to login too quickly after disconnecting. Try again in a few seconds.", NamedTextColor.RED));
            Tasks.runSync(() -> player.kick(Component.text("Logged in from another location.", NamedTextColor.RED)));
            return;
        }

        User user;

        try {
            user = userManager.create(event.getUniqueId(), event.getName());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load " + event.getName() + "'s player data!", e);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("Failed to load your player data! Please contact an administrator."));
            return;
        }

        // Intentional null check to maintain safety
        if (user == null) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.kickMessage(Component.text("Your user data was not loaded at the pre login stage, please try logging in again. [#1]", NamedTextColor.RED));
            return;
        }

        // Unload the user from memory later in-case they were disconnected before logging in properly.
        User finalUser = user;
        Tasks.runAsyncLater(() -> {
            final Player p = Bukkit.getPlayer(event.getUniqueId());

            if (p == null || !p.isOnline()) {
                userManager.destroy(finalUser, true);
            }
        }, 20 * 20L);

        plugin.getLogger().info("Loaded user " + user.getName() + " in " + (System.currentTimeMillis() - time) + "ms");
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        final User user = userManager.get(event.getPlayer());

        // Intentional null check to maintain safety
        if (user == null) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("Your user data was not loaded at the pre login stage, please try logging in again. [#2]", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final User user = userManager.get(event.getPlayer());
        final Player player = event.getPlayer();

        // Intentional null check to maintain safety
        if (user == null) {
            player.kick(Component.text("Your user data was not loaded at the pre login stage, please try logging in again. [#3]", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final User user = userManager.get(player);

        Tasks.runAsync(() -> {
            if (!player.isOnline()) {
                userManager.destroy(user, true);
            }
        });
    }

}
