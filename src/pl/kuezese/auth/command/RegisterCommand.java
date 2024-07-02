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
import java.util.regex.Pattern;

public class RegisterCommand implements CommandExecutor {

    private final AuthPlugin auth;
    private final Pattern passwordPattern;

    public RegisterCommand(AuthPlugin auth) {
        (this.auth = auth).getCommand("register").setExecutor(this);
        this.passwordPattern = Pattern.compile("[!-~]*");
    }

    @SuppressWarnings("UnstableApiUsage")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (args.length < 2) {
            return ChatHelper.send(p, this.auth.getConfiguration().usage.replace("{USAGE}", "/register <haslo> <haslo>"));
        }
        User u = this.auth.getUserManager().get(p.getName());
        if (u.isRegistered()) {
            return ChatHelper.send(p, this.auth.getConfiguration().already_registered);
        }
        if (!passwordPattern.matcher(args[0]).matches()) {
            return ChatHelper.send(p, this.auth.getConfiguration().invalid_characters);
        }
        if (!args[0].equals(args[1])) {
            return ChatHelper.send(p, this.auth.getConfiguration().not_same_password);
        }
        if (this.auth.getConfiguration().max_reg_per_ip != 0 && this.auth.getUserManager().getByIp(p.getAddress().getAddress().getHostAddress()) >= this.auth.getConfiguration().max_reg_per_ip) {
            return ChatHelper.send(p, this.auth.getConfiguration().max_accounts);
        }
        u.setPassword(Hashing.md5().hashBytes(args[1].getBytes(StandardCharsets.UTF_8)).toString());
        u.setRegistered(true);
        u.setLogged(true);
        u.updateLastLogin(p);
        ChatHelper.send(p, this.auth.getConfiguration().registered);
        if (this.auth.getConfiguration().titleEnabled) {
            ChatHelper.title(p, "", this.auth.getConfiguration().titleRegistered, 10, 30, 10);
        }
        p.removePotionEffect(PotionEffectType.BLINDNESS);
        p.setLevel(0);
        p.setExp(0.0f);
        return true;
    }
}
