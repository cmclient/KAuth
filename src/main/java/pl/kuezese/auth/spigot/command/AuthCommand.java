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
import java.util.Locale;
import java.util.regex.Pattern;

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

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "register":
            case "reg": {
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
                return ChatHelper.send(sender, auth.getAuthConfig().getMsgRegistered());
            }
            case "unregister":
            case "unreg": {
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
            case "changepassword":
            case "changepass": {
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
                u.setPassword(Hashing.md5().hashBytes(args[2].getBytes(StandardCharsets.UTF_8)).toString());
                u.setLogged(false);
                return ChatHelper.send(sender, auth.getAuthConfig().getMsgChangedPassword());
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
        ChatHelper.send(sender, "&8&m-------[--&r &6&l" + auth.getDescription().getFullName() + "&r &8&m--]-------&r");
    }
}
