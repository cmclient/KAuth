package pl.kuezese.auth.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kuezese.auth.AuthPlugin;
import pl.kuezese.auth.helper.ChatHelper;
import pl.kuezese.auth.object.User;

public class LogoutCommand implements CommandExecutor {

    private final AuthPlugin auth;

    public LogoutCommand(AuthPlugin auth) {
        (this.auth = auth).getCommand("logout").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        User u = this.auth.getUserManager().get(p.getName());
        if (!u.isRegistered()) {
            return ChatHelper.send(p, this.auth.getConfiguration().not_registered);
        }
        if (!u.isLogged()) {
            return ChatHelper.send(p, this.auth.getConfiguration().not_logged);
        }
        u.setLogged(false);
        u.setLastLogin(0);
        p.kickPlayer(ChatHelper.color(this.auth.getConfiguration().logout));
        return true;
    }
}
