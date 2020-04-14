package co.ignitus.bungeelist;

import co.ignitus.bungeelist.commands.StaffCMD;
import co.ignitus.bungeelist.files.ConfigFile;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

public final class BungeeList extends Plugin {

    private static BungeeList instance;
    private static ConfigFile configFile;

    private CommandSender cs = ProxyServer.getInstance().getConsole();

    @Override
    public void onEnable() {
        instance = this;
        cs.sendMessage(new TextComponent(ChatColor.GREEN + ChatColor.STRIKETHROUGH.toString() + "---------------------------"));
        cs.sendMessage(new TextComponent(ChatColor.GREEN + "  Enabling BungeeList"));
        cs.sendMessage(new TextComponent(ChatColor.GREEN + "Plugin Author: Ignitus Co."));
        cs.sendMessage(new TextComponent(ChatColor.GREEN + ChatColor.STRIKETHROUGH.toString() + "---------------------------"));
        configFile = new ConfigFile();
        getProxy().getPluginManager().registerCommand(this, new StaffCMD());
    }

    @Override
    public void onDisable() {
        cs.sendMessage(new TextComponent(ChatColor.RED + ChatColor.STRIKETHROUGH.toString() + "---------------------------"));
        cs.sendMessage(new TextComponent(ChatColor.RED + "  Disabling BungeeList"));
        cs.sendMessage(new TextComponent(ChatColor.RED + "Plugin Author: Ignitus Co."));
        cs.sendMessage(new TextComponent(ChatColor.RED + ChatColor.STRIKETHROUGH.toString() + "---------------------------"));
    }

    public static BungeeList getInstance() {
        return instance;
    }

    public ConfigFile getConfigFile() {
        return configFile;
    }
}
