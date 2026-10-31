<div align="center">

# CoreUi

**An Ore UI-style menu library for Paper plugins.**

Create menus with buttons, toggles, sliders, dropdowns and custom images using a Java builder API.

[![Features](https://img.shields.io/badge/Navigation-Features-007ACC?style=for-the-badge)](#features)
[![Requirements](https://img.shields.io/badge/Navigation-Requirements-007ACC?style=for-the-badge)](#requirements)
[![Example](https://img.shields.io/badge/Navigation-Example-007ACC?style=for-the-badge)](#example)
[![Custom Glyphs](https://img.shields.io/badge/Navigation-Custom_Glyphs-007ACC?style=for-the-badge)](#custom-glyphs)
[![Credits](https://img.shields.io/badge/Navigation-Credits-007ACC?style=for-the-badge)](#credits)

</div>

---

## Features

| Feature | Description |
| :--- | :--- |
| **Menus** | Build menus with positioned elements and click callbacks. |
| **Controls** | Add buttons, checkboxes, toggles, sliders and dropdowns. |
| **Themes** | Use light and dark themes with Adventure components and MiniMessage colours. |
| **Custom glyphs** | Display your own PNG images in menus, chat and action bars. |
| **Resource pack** | Generate a ZIP containing fonts, textures, shaders and custom glyphs. |
| **Version overlays** | Include shaders targeting Minecraft 1.21.11 through 26.3 in one pack. |
| **Menu values** | Remember control values during the server session and reset them through the builder. |

## Requirements

| Dependency | Version |
| :--- | :--- |
| Paper | **1.21.11 through 26.3** |
| Java | **21+** |

---

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

Install the CoreUi plugin JAR in your server's `plugins` folder. Depend on its API from your own plugin without bundling CoreUi into your JAR.

### Gradle

Add this to your plugin's `build.gradle.kts`.

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.CrebsTheCoder:CoreUi:1.0.0") {
        isTransitive = false
    }
}
```

Keep your plugin's existing Paper API dependency. CoreUi uses Adventure from Paper.

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
        <groupId>com.github.CrebsTheCoder</groupId>
        <artifactId>CoreUi</artifactId>
        <version>1.0.0</version>
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

## Resource Pack

CoreUi generates `plugins/CoreUi/Pack/CoreUi.zip` asynchronously on startup. The built-in pack sources are included in the plugin JAR from `src/main/resources/resource-pack`.

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/coreui reload` | `coreui.reload` | Rebuild the pack and reload custom glyphs. |

The permission defaults to operators. Reloading is also available from the console.

The ZIP contains shared assets and shader overlays for `1.21.11`, `26.1.x`, `26.2.x` and `26.3`. Minecraft selects the matching overlay from one ZIP. Check rendering in-game on each version you deploy.

## Custom Glyphs

1. Add a PNG to `plugins/CoreUi/Glyphs`, such as `heart.png` or `icons/coin.png`.
2. Run `/coreui reload` and wait for the rebuild to finish.
3. Update your hosted resource pack and have players load it.

Glyph IDs are relative paths without `.png`: `heart` and `icons/coin`. Use lowercase letters, numbers, underscores and hyphens. Each glyph has its own font, keeping IDs stable when other images are added or removed.

Optional settings in `plugins/CoreUi/glyphs.yml` control display height and baseline alignment. Width scales proportionally. Defaults use the image height for both values.

```yaml
glyphs:
  heart:
    height: 16
    ascent: 12
```

Height must be between `1` and `256`. Ascent must be between `-256` and the configured height.

<details>
<summary><strong>View example</strong> · Display a custom glyph</summary>

After pack generation has finished, look up the glyph and use its Adventure component:

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

To place it in a menu, use the glyph builder before calling `build()`:

```java
builder.glyph("health-icon", heart)
    .position(20, 40)
    .add();
```

Coordinates specify the top-left corner. The element uses the dimensions configured for that glyph.

</details>

`coreUi.getGlyphs().all()` returns the loaded glyphs. `coreUi.reloadPack()` returns a `CompletableFuture<Path>` that completes after the ZIP and registry have been updated. Failed rebuilds preserve the previous pack and registry. Reacquire glyphs and recreate menus after changing their dimensions. Schedule menu operations back onto the server thread after an asynchronous rebuild.

Use glyph elements for menu images; label measurements assume the built-in font. Custom images inside dropdown overlay text are not supported.

## Text Colours

`title`, `header`, `label`, `description` and `indicatorLabel` accept MiniMessage strings. Adventure components are also supported; `Component.text(...)` remains literal text.

```java
builder.label("heading", "<gold>Health</gold> <green>100</green>")
    .position(10, 12)
    .add();
```

Light and dark menus share one text font. Uncoloured text uses the theme's default colour. Animated dropdown overlay text currently uses its existing theme palette.

## Credits

- [Packer](https://github.com/DRAGNIL68/Packer) by **DRAGNIL68** is used to generate the resource-pack ZIP.
