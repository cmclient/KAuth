package pl.kuezese.auth.spigot.command;

import com.google.common.hash.Hashing;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import pl.kuezese.auth.spigot.SpigotPlugin;
import pl.kuezese.auth.shared.helper.ChatHelper;
import pl.kuezese.auth.spigot.helper.TitleHelper;
import pl.kuezese.auth.spigot.object.User;

import java.nio.charset.StandardCharsets;

public class LoginCommand implements CommandExecutor {

    private final SpigotPlugin auth;

    public LoginCommand(SpigotPlugin auth) {
        (this.auth = auth).getCommand("login").setExecutor(this);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (args.length == 0) {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgUsage().replace("{USAGE}", auth.getAuthConfig().getMsgLoginUsage()));
        }
        User u = auth.getUserManager().get(p.getName());
        if (u.isPremium())  {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgCantUseAsPremium());
        }
        if (!u.isRegistered()) {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgNotRegistered());
        }
        if (u.isLogged()) {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgAlreadyLogged());
        }
        if (!u.getPassword().equals(Hashing.md5().hashBytes(args[0].getBytes(StandardCharsets.UTF_8)).toString())) {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgWrongPassword());
        }
        u.setLogged(true);
        u.updateLastLogin(p);
        ChatHelper.send(p, auth.getAuthConfig().getMsgLogged());
        if (auth.getAuthConfig().isTitleEnabled()) {
            TitleHelper.title(p, "", auth.getAuthConfig().getTitleLogged(), 10, 30, 10);
        }
        p.removePotionEffect(PotionEffectType.BLINDNESS);
        p.setLevel(0);
        p.setExp(0.0f);
        return true;
    }
}
