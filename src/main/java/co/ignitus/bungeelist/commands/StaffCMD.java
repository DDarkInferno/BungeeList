package co.ignitus.bungeelist.commands;

import co.ignitus.bungeelist.BungeeList;
import co.ignitus.bungeelist.files.ConfigFile;
import co.ignitus.bungeelist.util.HookUtil;
import co.ignitus.bungeelist.util.MessageUtil;
import de.myzelyam.api.vanish.BungeeVanishAPI;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StaffCMD extends Command {

    final private BungeeList bungeeList = BungeeList.getInstance();
    final private ConfigFile configFile = bungeeList.getConfigFile();

    public StaffCMD(Configuration config) {
        super("staff",
                config.getString("command.permission", ""),
                config.getStringList("command.aliases").toArray(new String[0])
        );
    }

    private static List<UUID> hiddenStaff = new ArrayList<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        final Configuration config = configFile.getConfiguration();
        if (args.length == 0) {
            HashMap<String, StringBuilder> result = new HashMap<>();
            String serverFormat = config.getString("formats.server");
            String groupFormat = config.getString("formats.group");
            int playerIndex;
            if (sender instanceof ProxiedPlayer) {
                String rank = getRank((ProxiedPlayer) sender);
                playerIndex = getRankIndex(rank);
            } else {
                playerIndex = Integer.MAX_VALUE;
            }

            AtomicInteger onlineStaff = new AtomicInteger(0);
            ProxyServer.getInstance().getPlayers().stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(player -> getRankIndex(getRank((ProxiedPlayer) player))))
                    .forEach(player -> {
                        Server server = player.getServer();
                        if (server == null || server.getInfo() == null)
                            return;
                        String serverName = server.getInfo().getName();
                        if (config.getStringList("blacklist").stream().anyMatch(blacklistServer -> blacklistServer.equalsIgnoreCase(serverName)))
                            return;
                        String rankName = getRank(player);
                        if (rankName == null)
                            return;
                        int rankIndex = getRankIndex(rankName);
                        if (isVanished(player) && rankIndex < playerIndex)
                            return;
                        String rankFormat = config.getString("ranks." + rankName + ".format");
                        String playerName = player.getName();
                        String serverGroup = config.getSection("groups").getKeys().stream()
                                .filter(group -> config.getStringList("groups." + group).stream().anyMatch(groupServer -> groupServer.equalsIgnoreCase(serverName)))
                                .findFirst().orElse(null);
                        onlineStaff.incrementAndGet();
                        if (serverGroup != null) {
                            StringBuilder builder = result.get(serverGroup);
                            if (builder == null)
                                builder = new StringBuilder();
                            builder.append(groupFormat
                                    .replace("%rank%", rankFormat)
                                    .replace("%player%", playerName)
                                    .replace("%displayname%", player.getDisplayname())
                                    .replace("%name%", serverName));
                            result.put(serverGroup, builder);
                            return;
                        }
                        StringBuilder builder = result.get(serverName);
                        if (builder == null)
                            builder = new StringBuilder();
                        builder.append(serverFormat
                                .replace("%rank%", rankFormat)
                                .replace("%displayname%", player.getDisplayname())
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
                int players;
                if (config.contains("groups." + name)) {
                    players = config.getStringList("groups." + name).stream()
                            .mapToInt(this::getStaffOnline)
                            .sum();
                } else {
                    players = getStaffOnline(name);
                }
                message.append(titleFormat
                        .replace("%name%", name)
                        .replace("%total_online%", Integer.toString(ProxyServer.getInstance().getPlayers().size()))
                        .replace("%total_online_group%", Integer.toString(players))
                        .replace("%total_online_staff%", Integer.toString(onlineStaff.get())));
                message.append(builder.toString());
                message.append(divider);
            });
            message.append(config.getString("formats.footer", ""));
            sender.sendMessage(MessageUtil.format(message.toString()));
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("bungeelist.reload")) {
                sender.sendMessage(MessageUtil.getMessage("no-permission"));
                return;
            }
            configFile.reloadConfig();
            sender.sendMessage(MessageUtil.getMessage("reload"));
            return;
        }

        if (args[0].equalsIgnoreCase("vanish")) {
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
            return;
        }

        sender.sendMessage(MessageUtil.getMessage("invalid-argument"));
    }

    private String getRank(ProxiedPlayer player) {
        final Configuration config = configFile.getConfiguration();
        return config.getSection("ranks").getKeys()
                .stream()
                .filter(rank -> player.hasPermission(config.getString("ranks." + rank + ".permission")))
                .findFirst().orElse(null);
    }

    private int getRankIndex(String rank) {
        final Configuration config = configFile.getConfiguration();
        int index = new LinkedList<>(config.getSection("ranks").getKeys()).indexOf(rank);
        return index < 0 ? 999 : index;
    }

    private int getStaffOnline(String name) {
        ServerInfo serverInfo = ProxyServer.getInstance().getServers().values().stream()
                .filter(server -> server.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
        return serverInfo != null ? (int) serverInfo.getPlayers().stream()
                .filter(player -> getRank(player) != null).count() : 0;
    }

    private boolean isVanished(ProxiedPlayer player) {
        if (hiddenStaff.contains(player.getUniqueId()))
            return true;
        if (!HookUtil.premiumVanishEnabled())
            return false;
        return BungeeVanishAPI.isInvisible(player);
    }

}
