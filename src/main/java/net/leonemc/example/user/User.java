package net.leonemc.example.user;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Data;
import net.leonemc.example.ExamplePlugin;
import net.leonemc.example.utils.Tasks;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

@Data
public class User {

    private ExamplePlugin plugin = ExamplePlugin.getInstance();

    private final UUID uuid;
    private final String name;

    private boolean shouldSave;
    private long lastSave;

    // Player Data
    private int example;

    private AtomicBoolean isSaving = new AtomicBoolean(false);

    public User(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;

        this.lastSave = System.currentTimeMillis();

        load();
    }

    private void load() {
        Document document = plugin.getDatabase().getUsers().find(Filters.eq("_id", uuid.toString())).first();

        if (document == null) {
            document = new Document("_id", uuid.toString());
        }

        this.example = document.getInteger("example", 0);
    }

    private void saveData() {
        shouldSave = false;
        lastSave = System.currentTimeMillis();

        Document document = new Document("_id", uuid.toString());

        document.put("name", name);
        document.put("lowercaseName", name.toLowerCase());

        document.put("example", example);

        plugin.getDatabase().getUsers().replaceOne(Filters.eq("_id", uuid.toString()), document, new ReplaceOptions().upsert(true));
    }

    //----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Saves the user
     */
    public void save() {
        isSaving.set(true);

        try {
            this.saveData();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save " + name + " (" + uuid + ")'s data!", e);
        } finally {
            isSaving.set(false);
        }
    }

    /**
     * Saves the player async
     */
    public void saveAsync() {
        Tasks.runAsync(this::save);
    }

    /**
     * Returns if the user is currently being saved
     * @return whether the user is being saved or not
     */
    public boolean isSaving() {
        return isSaving.get();
    }

    /**
     * Flags the user for saving.
     */
    public synchronized void flagForSave() {
        shouldSave = true;
    }

    /**
     * Checks if the user should be saved. This will also return true if the user hasn't been saved recently
     * and is due for an automatic save.
     *
     * @return whether the user should be saved
     */
    public boolean shouldSave() {
        if (isShouldSave()) {
            return true;
        }

        return System.currentTimeMillis() - lastSave > TimeUnit.MINUTES.toMillis(5);
    }

    /**
     * Converts the user to a player
     *
     * @return the player
     */
    public Player toPlayer() {
        return Bukkit.getPlayer(uuid);
    }

}
