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

public class ChangePasswordCommand implements CommandExecutor {

    private final SpigotPlugin auth;
    
    public ChangePasswordCommand(SpigotPlugin auth) {
        (this.auth = auth).getCommand("changepassword").setExecutor(this);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = sender instanceof Player ? (Player) sender : null;
        if (p == null) {
            return ChatHelper.send(sender, auth.getAuthConfig().getMsgCantUseInConsole());
        }
        if (args.length < 2) {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgUsage().replace("{USAGE}", auth.getAuthConfig().getMsgChangePasswordUsage()));
        }
        User u = auth.getUserManager().get(p.getName());
        if (u.isPremium())  {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgCantUseAsPremium());
        }
        if (!u.isRegistered()) {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgNotRegistered());
        }
        if (!u.getPassword().equals(Hashing.md5().hashBytes(args[0].getBytes(StandardCharsets.UTF_8)).toString())) {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgWrongPassword());
        }
        u.setPassword(Hashing.md5().hashBytes(args[1].getBytes(StandardCharsets.UTF_8)).toString(), true);
        ChatHelper.send(p, auth.getAuthConfig().getMsgChangedPassword());
        return true;
    }
}
