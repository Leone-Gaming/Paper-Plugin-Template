package net.leonemc.example.user;

import com.google.common.base.Predicates;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import net.leonemc.example.ExamplePlugin;
import net.leonemc.example.utils.Tasks;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class UserManager {

    private final ExamplePlugin plugin;
    private final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();

    public UserManager(ExamplePlugin plugin) {
        this.plugin = plugin;

        // Automatic Saving
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (User user : users.values()) {
                boolean shouldSave = user.shouldSave();

                if (!shouldSave) {
                    continue;
                }

                user.save();
                plugin.getLogger().info("Saved user " + user.getName() + " to database.");
            }
        }, 20L, 20L);
    }

    /**
     * Fetches an offline player from the database
     * @param player the offline player
     * @return the user, null if the username is unknown
     */
    public @Nullable User getOffline(@NotNull OfflinePlayer player) {
        final UUID uuid = player.getUniqueId();
        final String name = player.getName();

        if (name == null) {
            return null;
        }

        return getOffline(player.getUniqueId(), player.getName());
    }

    /**
     * Fetches an offline player from the database. This will make a HTTP request to Mojang's API in some cases!
     * @param name the username of the player
     * @return the user
     */
    public @NotNull User getOffline(@NotNull String name) {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return getOffline(player.getUniqueId(), name);
    }

    /**
     * Fetches an offline player from the database
     * @param uuid the player's uuid
     * @param name the username of the player
     * @return the user
     */
    public @NotNull User getOffline(@NotNull UUID uuid, @NotNull String name) {
        if (users.containsKey(uuid)) {
            return users.get(uuid);
        }

        return new User(uuid, name);
    }

    /**
     * Fetches a user from the database using their UUID. Makes a database call if they're offline
     * @param uuid the player's uuid
     * @return the user
     */
    public @Nullable User getFromMongo(@NotNull UUID uuid) {
        if (users.containsKey(uuid)) {
            return users.get(uuid);
        }

        final Document document = plugin.getDatabase().getUsers().find(Filters.eq("_id", uuid.toString())).first();

        if (document == null) {
            return null;
        }

        return new User(uuid, document.getString("name"));
    }

    /**
     * Fetches a user from the database using their username. Makes a database call if they're offline
     * @param name the player's uuid
     * @return the user
     */
    public @Nullable User getFromMongo(@NotNull String name) {
        final Document document = plugin.getDatabase().getUsers().find(Filters.eq("lowercaseName", name.toLowerCase())).first();

        if (document == null) {
            return null;
        }

        return new User(UUID.fromString(document.getString("_id")), document.getString("name"));
    }

    /**
     * Fetches a user from the database using their UUID and returns a future
     * @param uuid the player's uuid
     * @return a future with the user. The future will be run on the main thread by default!
     */
    public CompletableFuture<@Nullable User> fetch(UUID uuid) {
        final CompletableFuture<User> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            final Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                Tasks.runSync(() -> future.complete(get(player)));
                return;
            }

            final Document document = plugin.getDatabase().getUsers().find(Filters.eq("_id", uuid.toString())).first();

            if (document == null) {
                Tasks.runSync(() -> future.complete(null));
                return;
            }

            Tasks.runSync(() -> future.complete(new User(uuid, document.getString("name"))));
        });

        return future;
    }

    /**
     * Fetches a user from the database using their username and returns a future
     * @param name the player's uuid
     * @return a future with the user. The future will be run on the main thread by default!
     */
    public CompletableFuture<@Nullable User> fetch(@NotNull String name) {
        final CompletableFuture<User> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            final Player player = Bukkit.getPlayer(name);

            if (player != null) {
                Tasks.runSync(() -> future.complete(get(player)));
                return;
            }

            final Document document = plugin.getDatabase().getUsers().find(Filters.eq("lowercaseName", name.toLowerCase())).first();

            if (document == null) {
                Tasks.runSync(() -> future.complete(null));
                return;
            }

            Tasks.runSync(() -> future.complete(new User(UUID.fromString(document.getString("_id")), document.getString("name"))));
        });

        return future;
    }

    /**
     * Fetches a user from the database using an offline player and returns a future
     * @param offline the player
     * @return a future with the user. The future will be run on the main thread by default!
     */
    public CompletableFuture<@Nullable User> fetch(@NotNull OfflinePlayer offline) {
        final CompletableFuture<User> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            final Player player = Bukkit.getPlayer(offline.getUniqueId());

            if (player != null) {
                Tasks.runSync(() -> future.complete(get(player)));
                return;
            }

            final Document document = plugin.getDatabase().getUsers().find(Filters.eq("_id", offline.getUniqueId().toString())).first();

            if (document == null) {
                Tasks.runSync(() -> future.complete(null));
                return;
            }

            Tasks.runSync(() -> future.complete(new User(offline.getUniqueId(), document.getString("name"))));
        });

        return future;
    }

    /**
     * Fetches a user from the database using an offline player and returns a future
     * The fetched user will be created if it does not exist already.
     * @param uuid the uuid
     * @param name the name
     * @return a future with the user. The future will be run on the main thread by default!
     */
    public CompletableFuture<User> fetchOrNew(@NotNull UUID uuid, @NotNull String name) {
        final CompletableFuture<User> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            final Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                Tasks.runSync(() -> future.complete(get(player)));
                return;
            }

            final Document document = plugin.getDatabase().getUsers().find(Filters.eq("_id", uuid.toString())).first();

            if (document == null) {
                Tasks.runSync(() -> future.complete(create(uuid, name)));
                return;
            }

            Tasks.runSync(() -> future.complete(new User(uuid, document.getString("name"))));
        });

        return future;
    }

    /**
     * Creates a new user for the player and adds the user to the cache
     * @param uuid the uuid of the player
     * @param name the name of the player
     * @return the user
     */
    public @NotNull User create(@NotNull UUID uuid, @NotNull String name) {
        if (!users.containsKey(uuid)) {
            users.put(uuid, new User(uuid, name));
        }

        return users.get(uuid);
    }

    /**
     * Fetches a user from the cache.
     * @param player the player
     * @return the user
     */
    public @NotNull User get(@NotNull Player player) {
        final User user = users.get(player.getUniqueId());

        if (user == null) {
            throw new IllegalStateException("No available user found for " + player.getName() + "!");
        }

        return user;
    }

    /**
     * Fetches a user from the cache. This will return null if the player is not online yet.
     * @param uuid the uuid
     * @return the user, null if not online
     */
    public @Nullable User get(@NotNull UUID uuid) {
        return users.get(uuid);
    }

    /**
     * Removes a user from the cache
     * @param user the user
     * @param save whether to save the user before removing
     */
    public void destroy(@NotNull User user, boolean save) {
        if (save) {
            user.save();
        }

        if (Bukkit.getPlayer(user.getUuid()) != null) {
            return;
        }

        users.remove(user.getUuid());
        plugin.getLogger().info("Destroyed user " + user.getName() + " from cache.");
    }

}
