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
        Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            return ChatHelper.send(sender, auth.getAuthConfig().getMsgCantUseInConsole());
        }
        User user = auth.getUserManager().get(player.getName());
        if (user.isPremium())  {
            return ChatHelper.send(player, auth.getAuthConfig().getMsgCantUseAsPremium());
        }
        if (!user.isRegistered()) {
            return ChatHelper.send(player, auth.getAuthConfig().getMsgNotRegistered());
        }
        if (!user.isLogged()) {
            return ChatHelper.send(player, auth.getAuthConfig().getMsgNotLogged());
        }
        user.setLogged(false);
        user.removeLastLogin();
        player.kickPlayer(ChatHelper.color(auth.getAuthConfig().getMsgLogout()));
        return true;
    }
}
