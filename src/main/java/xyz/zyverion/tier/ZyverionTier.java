
package xyz.zyverion.tier;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ZyverionTier extends JavaPlugin
        implements CommandExecutor, TabCompleter {

    private final List<String> tiers = Arrays.asList(
            "HT1", "HT2", "HT3", "HT4", "HT5",
            "LT1", "LT2", "LT3", "LT4", "LT5"
    );

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getCommand("tier").setExecutor(this);
        getCommand("tier").setTabCompleter(this);

        getLogger().info("ZyverionTier enabled!");
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private String getTier(OfflinePlayer player) {
        return getConfig().getString(
                "tiers." + player.getUniqueId() + ".tier",
                "Unranked"
        );
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (args.length == 0) {
            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage("Use /tier <player> from console.");
                return true;
            }

            OfflinePlayer player =
                    (OfflinePlayer) sender;

            sender.sendMessage(color(
                    "&8&m--------------------"
            ));
            sender.sendMessage(color(
                    "&b&lZYVERION TIER"
            ));
            sender.sendMessage(color(
                    "&fYour Tier: &a" + getTier(player)
            ));
            sender.sendMessage(color(
                    "&8&m--------------------"
            ));
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("zyveriontier.admin")) {
                sender.sendMessage(color("&cNo permission!"));
                return true;
            }

            sender.sendMessage(color("&b&lZYVERION TIER LIST"));

            if (getConfig().getConfigurationSection("tiers") == null) {
                sender.sendMessage(color("&7No ranked players."));
                return true;
            }

            for (String uuid : getConfig()
                    .getConfigurationSection("tiers").getKeys(false)) {

                String name = getConfig().getString(
                        "tiers." + uuid + ".name", "Unknown"
                );

                String tier = getConfig().getString(
                        "tiers." + uuid + ".tier", "Unranked"
                );

                sender.sendMessage(color(
                        "&f" + name + " &7- &a" + tier
                ));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (!sender.hasPermission("zyveriontier.admin")) {
                sender.sendMessage(color("&cNo permission!"));
                return true;
            }

            if (args.length != 3) {
                sender.sendMessage(color(
                        "&cUsage: /tier set <player> <HT1-LT5>"
                ));
                return true;
            }

            String tier = args[2].toUpperCase(Locale.ROOT);

            if (!tiers.contains(tier)) {
                sender.sendMessage(color("&cInvalid tier!"));
                sender.sendMessage(color(
                        "&7Available: " + String.join(", ", tiers)
                ));
                return true;
            }

            OfflinePlayer target =
                    Bukkit.getOfflinePlayer(args[1]);

            String path = "tiers." + target.getUniqueId();

            getConfig().set(path + ".name", args[1]);
            getConfig().set(path + ".tier", tier);
            saveConfig();

            sender.sendMessage(color(
                    "&aSuccessfully set " + args[1] +
                    "'s tier to &b" + tier
            ));

            if (target.isOnline()) {
                target.getPlayer().sendMessage(color(
                        "&bYour Zyverion Tier is now &a" + tier
                ));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("remove")) {
            if (!sender.hasPermission("zyveriontier.admin")) {
                sender.sendMessage(color("&cNo permission!"));
                return true;
            }

            if (args.length != 2) {
                sender.sendMessage(color(
                        "&cUsage: /tier remove <player>"
                ));
                return true;
            }

            OfflinePlayer target =
                    Bukkit.getOfflinePlayer(args[1]);

            String path = "tiers." + target.getUniqueId();

            if (!getConfig().contains(path)) {
                sender.sendMessage(color("&cPlayer has no tier."));
                return true;
            }

            getConfig().set(path, null);
            saveConfig();

            sender.sendMessage(color(
                    "&aRemoved " + args[1] + "'s tier!"
            ));
            return true;
        }

        OfflinePlayer target =
                Bukkit.getOfflinePlayer(args[0]);

        sender.sendMessage(color(
                "&b" + args[0] + "'s Tier: &a" + getTier(target)
        ));

        return true;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1 &&
                sender.hasPermission("zyveriontier.admin")) {
            suggestions.addAll(Arrays.asList(
                    "set", "remove", "list"
            ));
        }

        if (args.length == 2 &&
                args[0].equalsIgnoreCase("set")) {
            for (org.bukkit.entity.Player player :
                    Bukkit.getOnlinePlayers()) {
                suggestions.add(player.getName());
            }
        }

        if (args.length == 3 &&
                args[0].equalsIgnoreCase("set")) {
            suggestions.addAll(tiers);
        }

        return suggestions;
    }
        }
