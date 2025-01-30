package net.leonemc.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Getter;
import org.bson.Document;

@Getter
public class Database {

    private final ExamplePlugin plugin;
    private final MongoClient client;
    private final MongoDatabase database;

    private final MongoCollection<Document> users;

    public Database(ExamplePlugin plugin) {
        this.plugin = plugin;

        client = MongoClients.create(plugin.getConfiguration().getProperty(ExampleConfig.MONGO_URI));
        database = client.getDatabase(plugin.getConfiguration().getProperty(ExampleConfig.MONGO_DATABASE));

        users = database.getCollection("users");

        // Create the indexes
        users.createIndex(Indexes.ascending("name"));
        users.createIndex(Indexes.ascending("lowercaseName"));
    }

    private final ReplaceOptions replaceOptions = new ReplaceOptions().upsert(true);

}
