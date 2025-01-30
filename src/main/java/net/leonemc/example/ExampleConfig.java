package net.leonemc.example;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.properties.PropertyInitializer;

public class ExampleConfig implements SettingsHolder {

    public static final Property<String> MONGO_URI;
    public static final Property<String> MONGO_DATABASE;

    static {
        MONGO_URI = PropertyInitializer.newProperty("database.mongo-uri", "mongodb://localhost:27017");
        MONGO_DATABASE = PropertyInitializer.newProperty("database.mongo-database", "example-plugin");
    }

}
