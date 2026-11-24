package org.crebs.coreui.glyph;

import org.crebs.coreui.pack.ResourcePackGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class GlyphManager {
    private final ResourcePackGenerator packGenerator;
    private volatile Path outputDirectory;
    private final AtomicBoolean rebuilding = new AtomicBoolean();
    private volatile Map<String, Glyph> glyphs = Map.of();

    public GlyphManager(Path directory, Path pluginJar) {
        this(directory, pluginJar, directory.resolve("Pack"));
    }

    public GlyphManager(Path directory, Path pluginJar, Path outputDirectory) {
        packGenerator = new ResourcePackGenerator(directory, pluginJar);
        setOutputDirectory(outputDirectory);
    }

    public Glyph get(String id) {
        Glyph glyph = glyphs.get(id);
        if (glyph == null) throw new IllegalArgumentException("Unknown glyph: " + id);
        return glyph;
    }

    public Map<String, Glyph> all() {
        return glyphs;
    }

    public Path getOutputDirectory() { return outputDirectory; }

    public void setOutputDirectory(Path directory) {
        outputDirectory = Objects.requireNonNull(directory).toAbsolutePath().normalize();
    }

    public CompletableFuture<Path> reload() {
        return reload(outputDirectory);
    }

    public CompletableFuture<Path> reload(Path outputDirectory) {
        Path target = Objects.requireNonNull(outputDirectory).toAbsolutePath().normalize();
        if (!rebuilding.compareAndSet(false, true)) {
            return CompletableFuture.failedFuture(new IllegalStateException("A pack rebuild is already running"));
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                ResourcePackGenerator.Result result = packGenerator.generate(target);
                glyphs = result.glyphs();
                return result.path();
            } catch (IOException exception) {
                throw new CompletionException(exception);
            } finally {
                rebuilding.set(false);
            }
        });
    }
}
