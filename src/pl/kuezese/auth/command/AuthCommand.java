package pl.kuezese.auth.command;

import com.google.common.hash.Hashing;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kuezese.auth.AuthPlugin;
import pl.kuezese.auth.helper.ChatHelper;
import pl.kuezese.auth.object.User;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

public class AuthCommand implements CommandExecutor {

    private final AuthPlugin auth;
    private final Pattern passwordPattern;

    public AuthCommand(AuthPlugin auth) {
        (this.auth = auth).getCommand("auth").setExecutor(this);
        this.passwordPattern = Pattern.compile("[!-~]*");
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("auth.admin")) {
            sender.sendMessage(ChatHelper.color("&8>> &7You do not have permission to this command. &8(&cauth.admin&8)"));
            return true;
        }

        if (args.length == 0) {
            this.sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "register":
            case "reg": {
                if (args.length < 3) {
                    this.sendUsage(sender);
                    return true;
                }
                User u = this.auth.getUserManager().get(args[1]);
                if (u == null) {
                    u = this.auth.getUserManager().create(args[1]);
                }
                if (u.isRegistered()) {
                    return ChatHelper.send(sender, this.auth.getConfiguration().already_registered);
                }
                if (!passwordPattern.matcher(args[2]).matches()) {
                    return ChatHelper.send(sender, this.auth.getConfiguration().invalid_characters);
                }
                u.setPassword(Hashing.md5().hashBytes(args[2].getBytes(StandardCharsets.UTF_8)).toString());
                u.setRegistered(true);
                return ChatHelper.send(sender, this.auth.getConfiguration().registered);
            }
            case "unregister":
            case "unreg": {
                if (args.length < 2) {
                    this.sendUsage(sender);
                    return true;
                }
                User u = this.auth.getUserManager().get(args[1]);
                if (u == null) {
                    return ChatHelper.send(sender, this.auth.getConfiguration().not_registered);
                }
                if (!u.isRegistered()) {
                    return ChatHelper.send(sender, this.auth.getConfiguration().not_registered);
                }
                u.setRegistered(false);
                u.setLogged(false);
                u.setPassword(null);
                Player p = this.auth.getServer().getPlayer(u.getName());
                if (p != null) {
                    p.kickPlayer(ChatHelper.color(this.auth.getConfiguration().unregistered));
                }
                this.auth.getUserManager().remove(u);
                return ChatHelper.send(sender, this.auth.getConfiguration().unregistered);
            }
            case "changepassword":
            case "changepass": {
                if (args.length < 3) {
                    this.sendUsage(sender);
                    return true;
                }
                User u = this.auth.getUserManager().get(args[1]);
                if (u == null) {
                    return ChatHelper.send(sender, this.auth.getConfiguration().not_registered);
                }
                if (!u.isRegistered()) {
                    return ChatHelper.send(sender, this.auth.getConfiguration().not_registered);
                }
                u.setPassword(Hashing.md5().hashBytes(args[2].getBytes(StandardCharsets.UTF_8)).toString());
                u.setLogged(false);
                return ChatHelper.send(sender, this.auth.getConfiguration().changed);
            }
            default: {
                this.sendUsage(sender);
                return true;
            }
        }
    }

    private void sendUsage(CommandSender sender) {
        ChatHelper.send(sender, "&8&m-------[--&r &6&l" + this.auth.getDescription().getFullName() + "&r &8&m--]-------&r");
        ChatHelper.send(sender, "&8>> &6/auth register <nick> <password> &8- &7register player");
        ChatHelper.send(sender, "&8>> &6/auth unregister <nick> &8- &7unregister player");
        ChatHelper.send(sender, "&8>> &6/auth changepassword <nick> <password> &8- &7change player password");
        ChatHelper.send(sender, "&8&m-------[--&r &6&l" + this.auth.getDescription().getFullName() + "&r &8&m--]-------&r");
    }
}
