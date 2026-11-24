package org.crebs.coreui.pack;

import com.google.gson.Gson;
import net.outmoded.packer.ResourcePack;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.crebs.coreui.glyph.Glyph;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.Deflater;

public final class ResourcePackGenerator {
    private final Path directory;
    private final Path pluginJar;

    public ResourcePackGenerator(Path directory, Path pluginJar) {
        this.directory = directory;
        this.pluginJar = pluginJar;
    }

    public record Result(Path path, Map<String, Glyph> glyphs) {}

    public Result generate() throws IOException {
        return generate(directory.resolve("Pack"));
    }

    public Result generate(Path outputDirectory) throws IOException {
        Path input = directory.resolve("Glyphs");
        Path output = Objects.requireNonNull(outputDirectory).toAbsolutePath().normalize();
        Files.createDirectories(input);
        Files.createDirectories(output);
        Path settings = directory.resolve("glyphs.yml");
        if (Files.notExists(settings)) Files.writeString(settings, "glyphs: {}\n");
        Map<?, ?> glyphSettings;
        try (Reader reader = Files.newBufferedReader(settings, StandardCharsets.UTF_8)) {
            LoaderOptions options = new LoaderOptions();
            options.setAllowDuplicateKeys(false);
            Object document = new Yaml(new SafeConstructor(options)).load(reader);
            if (document == null) document = Map.of();
            if (!(document instanceof Map<?, ?> config)) throw new IllegalArgumentException("Expected a mapping");
            Object entries = config.containsKey("glyphs") ? config.get("glyphs") : Map.of();
            if (!(entries instanceof Map<?, ?> map)) throw new IllegalArgumentException("glyphs must be a mapping");
            glyphSettings = map;
        } catch (RuntimeException exception) {
            throw new IOException("Invalid glyphs.yml", exception);
        }
        ResourcePack pack = new ResourcePack();
        try (JarFile jar = new JarFile(pluginJar.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().startsWith("resource-pack/")) {
                    try (InputStream stream = jar.getInputStream(entry)) {
                        pack.writeInputStream(entry.getName().substring("resource-pack/".length()), stream);
                    }
                }
            }
        }


        Map<String, Glyph> loaded = new TreeMap<>();
        Gson gson = new Gson();
        try (var files = Files.walk(input)) {
            for (Path file : files.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)).sorted().toList()) {
                String relative = input.relativize(file).toString().replace('\\', '/');
                if (!relative.endsWith(".png")) continue;
                String id = relative.substring(0, relative.length() - 4);
                if (!id.matches("[a-z0-9_/-]+")) throw new IOException("Glyph names must use lowercase letters, numbers, _, - or /: " + relative);
                var image = ImageIO.read(file.toFile());
                if (image == null) throw new IOException("Invalid PNG: " + relative);
                Object entry = glyphSettings.get(id);
                if (entry != null && !(entry instanceof Map<?, ?>)) throw new IOException("Invalid glyph settings: " + id);
                Map<?, ?> values = entry == null ? Map.of() : (Map<?, ?>) entry;
                int height = setting(values, "height", image.getHeight());
                int ascent = setting(values, "ascent", height);
                if (height < 1 || height > 256 || ascent < -256 || ascent > height) throw new IOException("Invalid height/ascent for glyph: " + id);
                int width = (int) Math.round((double) image.getWidth() * height / image.getHeight());
                int right = 0;
                for (int x = image.getWidth() - 1; x >= 0 && right == 0; x--) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        if ((image.getRGB(x, y) >>> 24) != 0) {
                            right = x + 1;
                            break;
                        }
                    }
                }
                int advance = (int) (right * ((float) height / image.getHeight()) + 0.5F) + 1;
                Glyph glyph = new Glyph(id, width, height, ascent, advance);
                loaded.put(id, glyph);
                pack.write("assets/coreui/textures/custom/" + relative, file);
                List<Map<String, Object>> providers = new ArrayList<>();
                for (int offset = 0; offset < 9; offset++) {
                    providers.add(Map.of("type", "bitmap", "file", "coreui:custom/" + relative,
                            "height", height, "ascent", ascent - offset,
                            "chars", List.of(String.valueOf((char) ('\uE000' + offset)))));
                }
                String json = gson.toJson(Map.of("providers", providers));
                try (InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
                    pack.writeInputStream("assets/coreui/font/custom/" + id + ".json", stream);
                }
            }
        }
        Path temporary = Files.createTempFile(output, "coreui-", ".zip");
        try {
            pack.generate(temporary.toString(), Deflater.DEFAULT_COMPRESSION, false).join();
            Path target = output.resolve("CoreUi.zip");
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new Result(target, Map.copyOf(loaded));
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static int setting(Map<?, ?> values, String key, int fallback) throws IOException {
        Object value = values.get(key);
        if (value == null) return fallback;
        if (!(value instanceof Number number) || number.doubleValue() != number.intValue()) {
            throw new IOException("Glyph " + key + " must be an integer");
        }
        return number.intValue();
    }
}
