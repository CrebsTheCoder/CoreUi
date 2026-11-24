<div align="center">

# CoreUi

**Ore UI-style menus for Minecraft Java.**

Build menus with buttons, toggles, sliders, dropdowns and your own images. CoreUi handles the rendering and resource pack, with separate integrations for Paper and Minestom.

[![Features](https://img.shields.io/badge/Navigation-Features-007ACC?style=for-the-badge)](#features)
[![Requirements](https://img.shields.io/badge/Navigation-Requirements-007ACC?style=for-the-badge)](#requirements)
[![Example](https://img.shields.io/badge/Navigation-Example-007ACC?style=for-the-badge)](#example)
[![Custom Glyphs](https://img.shields.io/badge/Navigation-Custom_Glyphs-007ACC?style=for-the-badge)](#custom-glyphs)
[![Credits](https://img.shields.io/badge/Navigation-Credits-007ACC?style=for-the-badge)](#credits)

</div>

---

## Features

CoreUi comes with buttons, checkboxes, toggles, sliders, dropdowns, tabs and a few display elements. You choose where each element goes and what happens when a player interacts with it. Menus can remember a player's control values until the server restarts, with reset buttons for returning to the defaults.

There are light and dark themes, MiniMessage colours and support for custom PNG glyphs. Those glyphs can also be used in chat and action bars. CoreUi generates the resource pack for you, including shader overlays for Minecraft 1.21.11 through 26.3.

## Requirements

For Paper, use **1.21.11 through 26.3** with **Java 21 or newer**. The Minestom integration targets **2026.10.07-26.2** and needs **Java 25**. Minestom support is still experimental and needs further testing on a running server.

## Example

<img width="1333" height="872" alt="CoreUi menu example" src="https://github.com/user-attachments/assets/2924085e-00d4-43c9-b841-392f04737c4b" />

<details>
<summary><strong>View example</strong></summary>

```java
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.crebs.coreui.CoreUi;
import org.crebs.coreui.menu.CoreMenu;
import org.crebs.coreui.menu.style.ThemeColour;

public void openSettings(Player player) {
    CoreUi coreUi = JavaPlugin.getPlugin(CoreUi.class);
    CoreMenu menu = CoreMenu.builder("myplugin-settings")
        .title("Settings")
        .header()
        .theme(ThemeColour.DARK)
        .rememberValues()
        .size(300, 180)
        .label("heading", "<gold>Your settings</gold>")
            .position(10, 12)
            .add()
        .toggle("notifications")
            .position(9, 30)
            .label("Notifications")
            .description("Receive server announcements")
            .initialValue(true)
            .onChange((p, enabled) -> p.sendMessage("Notifications: " + enabled))
            .add()
        .slider("volume")
            .position(9, 72)
            .label("Volume")
            .range(0, 100)
            .step(1)
            .initialValue(50)
            .onChange((p, value) -> p.sendMessage("Volume: " + value))
            .add()
        .button("reset")
            .compact()
            .position(9, 130)
            .label("Reset to defaults")
            .resetValues("notifications", "volume")
            .add()
        .build();
    coreUi.open(player, menu);
}
```

</details>

## Usage

Put the CoreUi JAR in your server's `plugins` folder and add `depend: [CoreUi]` to your plugin's `plugin.yml`. Then add CoreUi as a compile-only dependency so your plugin can use its API.

### Gradle

Add this to your plugin's `build.gradle.kts`.

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.CrebsTheCoder.CoreUi:coreui-paper:dev-SNAPSHOT") {
        isTransitive = false
    }
}
```

Keep your Paper API dependency as well. Paper already provides the Adventure classes CoreUi uses.

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.CrebsTheCoder.CoreUi</groupId>
        <artifactId>coreui-paper</artifactId>
        <version>dev-SNAPSHOT</version>
        <scope>provided</scope>
        <exclusions>
            <exclusion>
                <groupId>*</groupId>
                <artifactId>*</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
</dependencies>
```

## Project Structure

The menu API lives in `coreui-api`, while `coreui-core` handles rendering and sessions. Neither depends on Bukkit. Pack generation is separate too, in `coreui-pack`, so it can be used by either platform.

`coreui-paper` contains the Paper plugin and `coreui-minestom` contains the Minestom integration. Both use the same menu engine, with their own code for showing dialogs, receiving clicks and scheduling tasks.

To build the project, use Java 25 and run `./gradlew build`. The Paper JAR will be in `coreui-paper/build/libs/CoreUi-<version>.jar`. You can also use `./gradlew runServer` to start the Paper test server. The dependency examples use `dev-SNAPSHOT`, which follows the pushed development branch. Use a release tag when you want a fixed version.

### Menu callbacks

You can still open a menu on Paper with `coreUi.open(player, menu)`. Callbacks receive a `MenuViewer`, which lets the same menu work on either platform. It supports Adventure messages, and you can get the platform's player object when you need it:

```java
builder.button("example")
    .position(10, 10)
    .onClick(viewer -> {
        viewer.sendMessage("Clicked!");
        Player player = viewer.unwrap(Player.class);
        player.setFoodLevel(20);
    })
    .add();
```

Here, `Player` is Bukkit's player class. Use Minestom's class in a Minestom application. If you're updating an older CoreUi menu, callbacks that explicitly used Bukkit `Player` will need this change.

On Paper, open and update menus on the server thread. Folia isn't supported yet.

### Minestom

Add `com.github.CrebsTheCoder.CoreUi:coreui-minestom:<version>` as an `implementation` dependency alongside Minestom. After initializing your server, create one CoreUi instance:

```java
import org.crebs.coreui.minestom.MinestomCoreUi;
import org.crebs.coreui.minestom.MinestomViewer;

MinestomCoreUi ui = new MinestomCoreUi();
ui.open(player, menu);
ui.execute(runtime -> runtime.navigate(new MinestomViewer(player), "settings"));
```

`open()` returns a future that completes once the menu operation has run. Call `ui.close()` before stopping your server to remove the listeners and close active menus. Menu callbacks run on the global scheduler; if a callback changes player or world state, schedule that work on the appropriate entity or instance scheduler.

For the resource pack, create a `GlyphManager` with `new GlyphManager(dataDirectory, packAssetsJar, outputDirectory)` and call `reload()`. Pass the CoreUi pack JAR, or your shaded JAR containing `resource-pack/`, as `packAssetsJar`. Once generation finishes, you can look up glyphs and send players your hosted pack. Hosting and sending the pack are handled by your application.

## Resource Pack

On Paper, CoreUi generates `plugins/CoreUi/Pack/CoreUi.zip` in the background when the plugin starts. Run `/coreui reload` to reload your glyphs and rebuild it. The command needs `coreui.reload`, which operators have by default, and also works from the console.

You can change where the ZIP is saved in `plugins/CoreUi/config.yml`:

```yaml
pack:
  output-directory: Pack
```

Paths in the config are relative to `plugins/CoreUi`, or you can use an absolute path. For example, `../SharedPacks/CoreUi` saves the ZIP at `plugins/SharedPacks/CoreUi/CoreUi.zip`. Run `/coreui reload` after changing it. Your source images still go in `plugins/CoreUi/Glyphs`.

From your plugin, you can also choose a folder for one rebuild:

```java
coreUi.reloadPack(Path.of("plugins", "SharedPacks", "CoreUi"));
```

`reloadPack(Path)` leaves the configured default alone. Unlike the config setting, relative paths passed to the Java API start at the server working directory. If you use `GlyphManager` directly, you can set the default folder through its constructor or `setOutputDirectory(Path)`.

If you're using Oraxen, ItemsAdder or another pack manager, merge `CoreUi.zip` into your pack through that manager's workflow. The output setting just chooses where CoreUi saves its ZIP; you'll still need to set up the merge. Include the fonts, textures, shader overlays and their metadata. If both packs change the same Minecraft shader, those changes need to be combined.

The ZIP includes overlays for `1.21.11`, `26.1.x`, `26.2.x` and `26.3`, so Minecraft can select the right shaders from one pack. Check the result in-game on the versions you plan to support.

## Custom Glyphs

1. Add a PNG to `plugins/CoreUi/Glyphs`, such as `heart.png` or `icons/coin.png`.
2. Run `/coreui reload` and wait for the rebuild to finish.
3. Update your hosted resource pack and have players load it.

The filename becomes the glyph ID: `heart.png` is `heart`, and `icons/coin.png` is `icons/coin`. Use lowercase letters, numbers, underscores and hyphens for names. Adding or removing another image won't change an existing glyph's ID.

To adjust a glyph's size or vertical alignment, add it to `plugins/CoreUi/glyphs.yml`. Width scales with the height. If you leave these settings out, both values default to the image's height.

```yaml
glyphs:
  heart:
    height: 16
    ascent: 12
```

Height must be between `1` and `256`. Ascent must be between `-256` and the configured height.

<details>
<summary><strong>Display a custom glyph</strong></summary>

Once the pack has finished generating, get the glyph by its ID:

```java
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.crebs.coreui.CoreUi;
import org.crebs.coreui.glyph.Glyph;

public void showHeart(Player player) {
    CoreUi coreUi = JavaPlugin.getPlugin(CoreUi.class);
    if (!coreUi.getGlyphs().all().containsKey("heart")) return;
    Glyph heart = coreUi.getGlyphs().get("heart");
    player.sendMessage(Component.text("Health: ").append(heart.component()));
    player.sendActionBar(heart.component());
}
```

To put the same glyph in a menu:

```java
builder.glyph("health-icon", heart)
    .position(20, 40)
    .add();
```

The position is the glyph's top-left corner. Its size comes from the glyph settings.

</details>

You can get all loaded glyphs with `coreUi.getGlyphs().all()`. `reloadPack()` returns a `CompletableFuture<Path>`, so wait for it to finish before using newly added images. If a rebuild fails, CoreUi keeps the previous pack and glyphs.

After changing a glyph's dimensions, look it up again and rebuild any menus using it. Reload callbacks run asynchronously, so schedule menu updates back onto the server thread.

Use `.glyph(...)` for menu images. Labels are measured using the built-in font, and custom images inside dropdown overlay text aren't supported yet.

## Text Colours

You can use MiniMessage in titles, headers, labels, descriptions and indicator labels:

```java
builder.label("heading", "<gold>Health</gold> <green>100</green>")
    .position(10, 12)
    .add();
```

These methods also accept Adventure components. Use `Component.text(...)` when you want literal text rather than MiniMessage formatting.

Text without a colour uses the current theme's default. Animated dropdown overlay text still uses the theme palette.

## Credits

Resource packs are generated with [Packer](https://github.com/DRAGNIL68/Packer) by **DRAGNIL68**.
