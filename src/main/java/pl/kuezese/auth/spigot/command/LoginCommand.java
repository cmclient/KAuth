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
        Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            return ChatHelper.send(sender, auth.getAuthConfig().getMsgCantUseInConsole());
        }
        if (args.length == 0) {
            return ChatHelper.send(player, auth.getAuthConfig().getMsgUsage().replace("{USAGE}", auth.getAuthConfig().getMsgLoginUsage()));
        }
        User user = auth.getUserManager().get(player.getName());
        if (user.isPremium())  {
            return ChatHelper.send(player, auth.getAuthConfig().getMsgCantUseAsPremium());
        }
        if (!user.isRegistered()) {
            return ChatHelper.send(player, auth.getAuthConfig().getMsgNotRegistered());
        }
        if (user.isLogged()) {
            return ChatHelper.send(player, auth.getAuthConfig().getMsgAlreadyLogged());
        }
        if (!user.getPassword().equals(Hashing.md5().hashBytes(args[0].getBytes(StandardCharsets.UTF_8)).toString())) {
            return ChatHelper.send(player, auth.getAuthConfig().getMsgWrongPassword());
        }
        user.setLogged(true);
        user.updateLastLogin(player);
        ChatHelper.send(player, auth.getAuthConfig().getMsgLogged());
        if (auth.getAuthConfig().isTitleEnabled()) {
            TitleHelper.title(player, "", auth.getAuthConfig().getTitleLogged(), 10, 30, 10);
        }
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.setLevel(0);
        player.setExp(0.0F);
        return true;
    }
}
