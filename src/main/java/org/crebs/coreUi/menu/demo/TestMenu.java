package org.crebs.coreUi.menu.demo;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.crebs.coreUi.CoreUi;
import org.crebs.coreUi.menu.CoreMenu;
import org.crebs.coreUi.menu.style.ThemeColour;

public final class TestMenu {
    public static CoreMenu create(CoreUi plugin, Player player) {
        return CoreMenu.builder("coreui")
                .title(Component.text("Cool UI"))
                .header()
                .theme(plugin.getTheme(player))
                .rememberValues()
                .size(300, 245)
                .label("heading", Component.text("A very cool UI"))
                    .position(10, 12).add().checkbox("checkbox")
                    .position(9, 26)
                    .label(Component.text("A checkbox"))
                    .description(Component.text("This is a checkbox"))
                    .initialValue(false)
                    .onChange((p, enabled) -> {
                        p.sendMessage("Checkbox is now " + (enabled ? "enabled" : "disabled"));
                    })
                    .add()
                .slider("slider")
                    .position(9, 68)
                    .label(Component.text("A slider"))
                    .description(Component.text("This is a <gold>slider</gold>"))
                    .range(0, 100).step(1).initialValue(50)
                    .onChange((p, value) -> {
                        p.sendMessage("Slider is now " + value);
                    })
                    .add()
                .dropdown("dropdown")
                    .position(9, 110)
                    .label(Component.text("A dropdown"))
                    .description(Component.text("This is a dropdown"))
                    .options(Component.text("Option 1"), Component.text("Option 2"), Component.text("Option 3"))
                    .initialValue(0)
                    .onChange((p, value) -> {
                        p.sendMessage("Dropdown is now " + value);
                    })
                    .add()
                .label("heading2", Component.text("Another label"))
                    .position(10, 151).add()
                .toggle("toggle")
                    .position(9, 160)
                    .label(Component.text("A toggle"))
                    .description(Component.text("this button will change the theme to black"))
                    .initialValue(plugin.getTheme(player) == ThemeColour.DARK)
                    .onChange((p, enabled) -> plugin.setTheme(p, enabled ? ThemeColour.DARK : ThemeColour.LIGHT))
                    .add()
                .button("reset").compact()
                    .position(9, 202)
                    .label(Component.text("Reset to defaults"))
                    .description(Component.text("This will reset all values to their defaults"))
                    .resetValues("checkbox", "slider", "dropdown", "toggle")
                    .add()
                .build();

    }
}
