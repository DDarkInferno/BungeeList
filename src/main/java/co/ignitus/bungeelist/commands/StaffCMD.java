package co.ignitus.bungeelist.commands;

import co.ignitus.bungeelist.BungeeList;
import co.ignitus.bungeelist.files.ConfigFile;
import co.ignitus.bungeelist.util.MessageUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.util.*;

public class StaffCMD extends Command {

    final private BungeeList bungeeList = BungeeList.getInstance();
    final private ConfigFile configFile = bungeeList.getConfigFile();

    public StaffCMD() {
        super("staff");
    }

    private static List<UUID> hiddenStaff = new ArrayList<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        final Configuration config = configFile.getConfiguration();
        if (args.length == 0) {
            HashMap<String, StringBuilder> result = new HashMap<>();
            String serverFormat = config.getString("formats.server", "%rank% &f%player%");
            String groupFormat = config.getString("formats.group", "%rank% &f %player% &e(%name%)");
            int playerIndex;
            if (sender instanceof ProxiedPlayer) {
                String rank = getRank((ProxiedPlayer) sender, config);
                playerIndex = getRankIndex(rank, config);
            } else {
                playerIndex = Integer.MAX_VALUE;
            }
            ProxyServer.getInstance().getPlayers().forEach(player -> {
                String serverName = player.getServer().getInfo().getName();
                if (config.getStringList("blacklist").stream().anyMatch(server -> server.equalsIgnoreCase(serverName)))
                    return;
                String rankName = getRank(player, config);
                if (rankName == null)
                    return;
                int rankIndex = getRankIndex(rankName, config);
                if (hiddenStaff.contains(player.getUniqueId()) && rankIndex > playerIndex)
                    return;
                String rankFormat = config.getString("ranks." + rankName + ".format");
                String playerName = player.getName();
                String serverGroup = config.getSection("groups").getKeys().stream()
                        .filter(group -> config.getStringList("groups." + group).stream().anyMatch(server -> server.equalsIgnoreCase(serverName)))
                        .findFirst().orElse(null);
                if (serverGroup != null) {
                    StringBuilder builder = result.get(serverGroup);
                    if (builder == null)
                        builder = new StringBuilder();
                    builder.append(groupFormat
                            .replace("%rank%", rankFormat)
                            .replace("%player%", playerName)
                            .replace("%name%", serverName));
                    result.put(serverGroup, builder);
                    return;
                }
                StringBuilder builder = result.get(serverName);
                if (builder == null)
                    builder = new StringBuilder();
                builder.append(serverFormat
                        .replace("%rank%", rankFormat)
                        .replace("%player%", playerName));
                result.put(serverName, builder);
            });
            if (result.size() == 0) {
                sender.sendMessage(MessageUtil.getMessage("no-staff"));
                return;
            }
            String titleFormat = config.getString("formats.title");
            String divider = config.getString("formats.divider", "");
            StringBuilder message = new StringBuilder();
            message.append(config.getString("formats.header") + "\n");
            result.forEach((name, builder) -> {
                message.append(titleFormat.replace("%name%", name));
                message.append(builder.toString());
                message.append(divider);
            });
            sender.sendMessage(MessageUtil.format(message.toString()));
            return;
        }
        if (!args[0].equalsIgnoreCase("vanish")) {
            sender.sendMessage(MessageUtil.getMessage("invalid-argument"));
            return;
        }
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(MessageUtil.getMessage("no-console"));
            return;
        }
        final ProxiedPlayer player = (ProxiedPlayer) sender;
        if (!player.hasPermission("bungeelist.vanish")) {
            player.sendMessage(MessageUtil.getMessage("no-permission"));
            return;
        }
        if (hiddenStaff.contains(player.getUniqueId())) {
            hiddenStaff.remove(player.getUniqueId());
            player.sendMessage(MessageUtil.getMessage("vanish.disabled"));
            return;
        }
        hiddenStaff.add(player.getUniqueId());
        player.sendMessage(MessageUtil.getMessage("vanish.enabled"));
    }

    private String getRank(ProxiedPlayer player, Configuration config) {
        return config.getSection("ranks").getKeys()
                .stream()
                .filter(rank -> player.hasPermission(config.getString("ranks." + rank + ".permission")))
                .findFirst().orElse(null);
    }

    public int getRankIndex(String rank, Configuration config) {
        return new LinkedList<>(config.getSection("ranks").getKeys()).indexOf(rank);
    }


}
