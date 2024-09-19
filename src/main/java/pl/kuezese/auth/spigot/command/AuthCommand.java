package pl.kuezese.auth.spigot.command;

import com.google.common.hash.Hashing;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kuezese.auth.spigot.SpigotPlugin;
import pl.kuezese.auth.shared.helper.ChatHelper;
import pl.kuezese.auth.spigot.object.User;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AuthCommand implements CommandExecutor {

    private final SpigotPlugin auth;
    private final Pattern passwordPattern;

    public AuthCommand(SpigotPlugin auth) {
        (this.auth = auth).getCommand("auth").setExecutor(this);
        passwordPattern = Pattern.compile("[!-~]*");
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission(auth.getAuthConfig().getAdminPermission())) {
            sender.sendMessage(ChatHelper.color("&8>> &7You do not have permission to this command. &8(&c" + auth.getAuthConfig().getAdminPermission() + "&8)"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);
        String subPermission = auth.getAuthConfig().getAdminPermission() + '.' + subCommand;

        if (!sender.hasPermission(subPermission)) {
            sender.sendMessage(ChatHelper.color("&8>> &7You do not have permission to this command. &8(&c" + subPermission + "&8)"));
            return true;
        }

        switch (subCommand) {
            case "register": {
                if (args.length < 3) {
                    sendUsage(sender);
                    return true;
                }
                User u = auth.getUserManager().get(args[1]);
                if (u == null) {
                    u = auth.getUserManager().create(args[1]);
                }
                if (u.isPremium())  {
                    return ChatHelper.send(sender, auth.getAuthConfig().getMsgCantUseAsPremium());
                }
                if (u.isRegistered()) {
                    return ChatHelper.send(sender, auth.getAuthConfig().getMsgAlreadyLogged());
                }
                if (!passwordPattern.matcher(args[2]).matches()) {
                    return ChatHelper.send(sender, auth.getAuthConfig().getMsgInvalidCharacters());
                }
                u.setPassword(Hashing.md5().hashBytes(args[2].getBytes(StandardCharsets.UTF_8)).toString());
                u.insert();
                return ChatHelper.send(sender, auth.getAuthConfig().getMsgRegistered());
            }
            case "unregister": {
                if (args.length < 2) {
                    sendUsage(sender);
                    return true;
                }
                User u = auth.getUserManager().get(args[1]);
                if (u == null || !u.isRegistered()) {
                    return ChatHelper.send(sender, auth.getAuthConfig().getMsgNotRegistered());
                }
                if (u.isPremium())  {
                    return ChatHelper.send(sender, auth.getAuthConfig().getMsgCantUseAsPremium());
                }
                u.setLogged(false);
                u.setPassword(null);
                Player p = auth.getServer().getPlayer(u.getName());
                if (p != null) {
                    p.kickPlayer(ChatHelper.color(auth.getAuthConfig().getMsgUnregistered()));
                }
                auth.getUserManager().remove(u);
                return ChatHelper.send(sender, auth.getAuthConfig().getMsgUnregistered());
            }
            case "changepassword": {
                if (args.length < 3) {
                    sendUsage(sender);
                    return true;
                }
                User u = auth.getUserManager().get(args[1]);
                if (u == null || !u.isRegistered()) {
                    return ChatHelper.send(sender, auth.getAuthConfig().getMsgNotRegistered());
                }
                if (u.isPremium())  {
                    return ChatHelper.send(sender, auth.getAuthConfig().getMsgCantUseAsPremium());
                }
                u.setPassword(Hashing.md5().hashBytes(args[2].getBytes(StandardCharsets.UTF_8)).toString(), true);
                u.setLogged(false);
                return ChatHelper.send(sender, auth.getAuthConfig().getMsgChangedPassword());
            }
            case "accounts": {
                if (args.length < 2) {
                    sendUsage(sender);
                    return true;
                }

                String name = args[1];
                User u;

                if (name.contains(".")) {
                    u = auth.getUserManager().getUsers().values().stream().filter(user -> user.getLastIp() != null && user.getLastIp().equals(name)).findAny().orElse(null);
                } else {
                    u = auth.getUserManager().get(name);
                }

                if (u == null || u.getLastIp() == null) {
                    return ChatHelper.send(sender, auth.getAuthConfig().getMsgNotRegistered());
                }

                List<User> users = auth.getUserManager().getUsers().values().stream()
                        .filter(other -> other.getLastIp() != null)
                        .filter(other -> other.getLastIp().equals(u.getLastIp()))
                        .collect(Collectors.toList());

                String accounts = users.stream()
                        .map(User::getName)
                        .collect(Collectors.collectingAndThen(
                                Collectors.joining(", "),
                                result -> result.isEmpty() ? "not found" : result
                        ));

                return ChatHelper.send(sender, auth.getAuthConfig().getMsgAccounts().replace("{ACCOUNTS}", accounts));
            }
            default: {
                sendUsage(sender);
                return true;
            }
        }
    }

    private void sendUsage(CommandSender sender) {
        ChatHelper.send(sender, "&8&m-------[--&r &6&l" + auth.getDescription().getFullName() + "&r &8&m--]-------&r");
        ChatHelper.send(sender, "&8>> &6/auth register <nick> <password> &8- &7register player");
        ChatHelper.send(sender, "&8>> &6/auth unregister <nick> &8- &7unregister player");
        ChatHelper.send(sender, "&8>> &6/auth changepassword <nick> <password> &8- &7change player password");
        ChatHelper.send(sender, "&8>> &6/auth accounts <nick/ip> &8- &7view accounts of player");
        ChatHelper.send(sender, "&8&m-------[--&r &6&l" + auth.getDescription().getFullName() + "&r &8&m--]-------&r");
    }
}
