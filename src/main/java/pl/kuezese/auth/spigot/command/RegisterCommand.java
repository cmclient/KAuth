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
        Player p = sender instanceof Player ? (Player) sender : null;
        if (p == null) {
            return ChatHelper.send(sender, auth.getAuthConfig().getMsgCantUseInConsole());
        }
        if (args.length < 2) {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgUsage().replace("{USAGE}", auth.getAuthConfig().getMsgRegisterUsage()));
        }
        User u = auth.getUserManager().get(p.getName());
        if (u.isPremium())  {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgCantUseAsPremium());
        }
        if (u.isRegistered()) {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgAlreadyRegistered());
        }
        if (!passwordPattern.matcher(args[0]).matches()) {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgInvalidCharacters());
        }
        if (!args[0].equals(args[1])) {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgNotSamePassword());
        }
        if (auth.getAuthConfig().getMaxAccounts() != 0 && auth.getUserManager().getByIp(p.getAddress().getAddress().getHostAddress()) >= auth.getAuthConfig().getMaxAccounts()) {
            return ChatHelper.send(p, auth.getAuthConfig().getMsgMaxAccounts());
        }
        u.setRegisterDate(Timestamp.from(Instant.now()));
        u.setPassword(Hashing.md5().hashBytes(args[1].getBytes(StandardCharsets.UTF_8)).toString());
        u.setLogged(true);
        u.updateLastLogin(p);
        u.insert();
        ChatHelper.send(p, auth.getAuthConfig().getMsgRegistered());
        if (auth.getAuthConfig().isTitleEnabled()) {
            TitleHelper.title(p, "", auth.getAuthConfig().getTitleRegistered(), 10, 30, 10);
        }
        p.removePotionEffect(PotionEffectType.BLINDNESS);
        p.setLevel(0);
        p.setExp(0.0f);
        return true;
    }
}
