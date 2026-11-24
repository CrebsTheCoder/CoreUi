package org.crebs.coreui.pack;

import org.crebs.coreui.glyph.GlyphManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.concurrent.CompletionException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipFile;
import static org.junit.jupiter.api.Assertions.*;

class ResourcePackGeneratorTest {
    @TempDir Path temporary;

    @Test
    void customOutputContainsAssetsAndKeepsUnrelatedFiles() throws Exception {
        Path data = temporary.resolve("data");
        Path output = temporary.resolve("another-plugin/imports");
        Files.createDirectories(output);
        Files.writeString(output.resolve("existing.txt"), "keep");
        createGlyph(data);
        var result = new ResourcePackGenerator(data, assetJar()).generate(output);
        assertEquals(output.resolve("CoreUi.zip"), result.path());
        assertEquals("keep", Files.readString(output.resolve("existing.txt")));
        try (ZipFile zip = new ZipFile(result.path().toFile())) {
            assertNotNull(zip.getEntry("pack.mcmeta"));
            assertNotNull(zip.getEntry("assets/coreui/textures/custom/icons/coin.png"));
            assertNotNull(zip.getEntry("assets/coreui/font/custom/icons/coin.json"));
        }
        assertEquals(12, result.glyphs().get("icons/coin").height());
        assertEquals(8, result.glyphs().get("icons/coin").ascent());
    }

    @Test
    void failedReloadPreservesPreviousZipAndRegistry() throws Exception {
        Path data = temporary.resolve("data");
        createGlyph(data);
        GlyphManager manager = new GlyphManager(data, assetJar(), temporary.resolve("pack"));
        Path zip = manager.reload().join();
        byte[] previous = Files.readAllBytes(zip);
        var registry = manager.all();
        Files.writeString(data.resolve("Glyphs/broken.png"), "invalid");
        assertThrows(CompletionException.class, () -> manager.reload().join());
        assertArrayEquals(previous, Files.readAllBytes(zip));
        assertSame(registry, manager.all());
    }

    @Test
    void oneOffOutputDoesNotChangeDefaultDestination() throws Exception {
        Path data = temporary.resolve("data");
        createGlyph(data);
        GlyphManager manager = new GlyphManager(data, assetJar());
        Path custom = temporary.resolve("custom");
        assertEquals(custom.resolve("CoreUi.zip"), manager.reload(custom).join());
        assertEquals(data.resolve("Pack/CoreUi.zip"), manager.reload().join());
    }

    @Test
    void malformedSettingsCannotReplaceExistingPack() throws Exception {
        Path data = temporary.resolve("data");
        createGlyph(data);
        ResourcePackGenerator generator = new ResourcePackGenerator(data, assetJar());
        Path zip = generator.generate().path();
        byte[] previous = Files.readAllBytes(zip);
        Files.writeString(data.resolve("glyphs.yml"), "glyphs: [invalid]");
        assertThrows(java.io.IOException.class, generator::generate);
        assertArrayEquals(previous, Files.readAllBytes(zip));
    }

    private void createGlyph(Path data) throws Exception {
        Path image = data.resolve("Glyphs/icons/coin.png");
        Files.createDirectories(image.getParent());
        BufferedImage bitmap = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        bitmap.setRGB(15, 15, 0xffffffff);
        ImageIO.write(bitmap, "png", image.toFile());
        Files.writeString(data.resolve("glyphs.yml"), "glyphs:\n  icons/coin:\n    height: 12\n    ascent: 8\n");
    }

    private Path assetJar() throws Exception {
        Path jar = temporary.resolve("assets.jar");
        try (JarOutputStream stream = new JarOutputStream(Files.newOutputStream(jar))) {
            stream.putNextEntry(new JarEntry("resource-pack/pack.mcmeta"));
            stream.write("{\"pack\":{\"pack_format\":75,\"description\":\"Test\"}}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            stream.closeEntry();
        }
        return jar;
    }
}
