package pl.kuezese.auth.command;

import com.google.common.hash.Hashing;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import pl.kuezese.auth.AuthPlugin;
import pl.kuezese.auth.helper.ChatHelper;
import pl.kuezese.auth.object.User;

import java.nio.charset.StandardCharsets;

public class RememberCommand implements CommandExecutor {

    private final AuthPlugin auth;

    public RememberCommand(AuthPlugin auth) {
        (this.auth = auth).getCommand("login").setExecutor(this);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (args.length == 0) {
            return ChatHelper.send(p, this.auth.getConfiguration().usage.replace("{USAGE}", "/login <haslo>"));
        }
        User u = this.auth.getUserManager().get(p.getName());
        if (!u.isRegistered()) {
            return ChatHelper.send(p, this.auth.getConfiguration().not_registered);
        }
        if (u.isLogged()) {
            return ChatHelper.send(p, this.auth.getConfiguration().already_logged);
        }
        if (!u.getPassword().equals(Hashing.md5().hashBytes(args[0].getBytes(StandardCharsets.UTF_8)).toString())) {
            return ChatHelper.send(p, this.auth.getConfiguration().wrong_password);
        }
        u.setLogged(true);
        u.updateLastLogin(p);
        ChatHelper.send(p, this.auth.getConfiguration().logged);
        if (this.auth.getConfiguration().titleEnabled) {
            ChatHelper.title(p, "", this.auth.getConfiguration().titleLogged, 10, 30, 10);
        }
        p.removePotionEffect(PotionEffectType.BLINDNESS);
        p.setLevel(0);
        p.setExp(0.0f);
        return true;
    }
}
