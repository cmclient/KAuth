package pl.kuezese.auth.spigot.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kuezese.auth.spigot.SpigotPlugin;
import pl.kuezese.auth.shared.helper.ChatHelper;
import pl.kuezese.auth.spigot.object.User;

public class LogoutCommand implements CommandExecutor {

    private final SpigotPlugin auth;

    public LogoutCommand(SpigotPlugin auth) {
        (this.auth = auth).getCommand("logout").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = sender instanceof Player ? (Player) sender : null;
        if (p == null) {
            return ChatHelper.send(sender, auth.getAuthConfig().getMsgCantUseInConsole());
        }
        User u = auth.getUserManager().get(p.getName());
        if (u.isPremium())  {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgCantUseAsPremium());
        }
        if (!u.isRegistered()) {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgNotRegistered());
        }
        if (!u.isLogged()) {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgNotLogged());
        }
        u.setLogged(false);
        u.removeLastLogin();
        p.kickPlayer(ChatHelper.color(auth.getAuthConfig().getMsgLogout()));
        return true;
    }
}
