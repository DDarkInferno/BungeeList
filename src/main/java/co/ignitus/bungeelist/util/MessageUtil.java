package co.ignitus.bungeelist.util;


import co.ignitus.bungeelist.BungeeList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class MessageUtil {

    public static TextComponent format(String message) {
        return new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static TextComponent getMessage(String path) {
        return format(BungeeList.getInstance().getConfigFile().getConfiguration().getString("messages." + path, "&cUnknown Message. Please update your config."));
    }
}
