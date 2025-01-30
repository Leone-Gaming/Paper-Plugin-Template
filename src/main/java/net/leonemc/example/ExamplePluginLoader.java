package net.leonemc.example;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class ExamplePluginLoader implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(
                new RemoteRepository.Builder(
                        "paper",
                        "default",
                        "https://repo.papermc.io/repository/maven-public/"
                ).build()
        );
        resolver.addRepository(
                new RemoteRepository.Builder(
                        "aikar",
                        "default",
                        "https://repo.aikar.co/content/groups/aikar/"
                ).build()
        );

        resolver.addDependency(new Dependency(new DefaultArtifact("co.aikar:acf-paper:0.5.1-SNAPSHOT"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.mongodb:mongodb-driver-sync:5.0.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("ch.jalu:configme:1.4.1"), null));

        classpathBuilder.addLibrary(resolver);
    }
}
