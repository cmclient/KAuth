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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.regex.Pattern;

public class RegisterCommand implements CommandExecutor {

    private final SpigotPlugin auth;
    private final Pattern passwordPattern;

    public RegisterCommand(SpigotPlugin auth) {
        (this.auth = auth).getCommand("register").setExecutor(this);
        passwordPattern = Pattern.compile("[!-~]*");
    }

    @SuppressWarnings("UnstableApiUsage")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            return ChatHelper.send(sender, auth.getAuthConfig().getMsgCantUseInConsole());
        }
        if (args.length < 2) {
            return ChatHelper.send(player, auth.getAuthConfig().getMsgUsage().replace("{USAGE}", auth.getAuthConfig().getMsgRegisterUsage()));
        }
        User user = auth.getUserManager().get(player.getName());
        if (user.isPremium())  {
            return ChatHelper.send(player, auth.getAuthConfig().getMsgCantUseAsPremium());
        }
        if (user.isRegistered()) {
            return ChatHelper.send(player, auth.getAuthConfig().getMsgAlreadyRegistered());
        }
        if (!passwordPattern.matcher(args[0]).matches()) {
            return ChatHelper.send(player, auth.getAuthConfig().getMsgInvalidCharacters());
        }
        if (!args[0].equals(args[1])) {
            return ChatHelper.send(player, auth.getAuthConfig().getMsgNotSamePassword());
        }
        if (auth.getAuthConfig().getMaxAccounts() != 0 && auth.getUserManager().getByIp(player.getAddress().getAddress().getHostAddress()) >= auth.getAuthConfig().getMaxAccounts()) {
            return ChatHelper.send(player, auth.getAuthConfig().getMsgMaxAccounts());
        }
        user.setRegisterDate(Timestamp.from(Instant.now()));
        user.setPassword(Hashing.md5().hashBytes(args[1].getBytes(StandardCharsets.UTF_8)).toString());
        user.setLogged(true);
        user.updateLastLogin(player);
        user.insert();
        ChatHelper.send(player, auth.getAuthConfig().getMsgRegistered());
        if (auth.getAuthConfig().isTitleEnabled()) {
            TitleHelper.title(player, "", auth.getAuthConfig().getTitleRegistered(), 10, 30, 10);
        }
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.setLevel(0);
        player.setExp(0.0f);
        return true;
    }
}
