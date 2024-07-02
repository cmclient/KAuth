package pl.kuezese.auth.command;

import com.google.common.hash.Hashing;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kuezese.auth.AuthPlugin;
import pl.kuezese.auth.helper.ChatHelper;
import pl.kuezese.auth.object.User;

import java.nio.charset.StandardCharsets;

public class ChangePasswordCommand implements CommandExecutor {

    private final AuthPlugin auth;
    
    public ChangePasswordCommand(AuthPlugin auth) {
        (this.auth = auth).getCommand("changepassword").setExecutor(this);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (args.length < 2) {
            return ChatHelper.send(p, this.auth.getConfiguration().usage.replace("{USAGE}", "/changepassword <stare_haslo> <nowe_haslo>"));
        }
        User u = this.auth.getUserManager().get(p.getName());
        if (!u.isRegistered()) {
            return ChatHelper.send(p, this.auth.getConfiguration().not_registered);
        }
        if (!u.getPassword().equals(Hashing.md5().hashBytes(args[0].getBytes(StandardCharsets.UTF_8)).toString())) {
            return ChatHelper.send(p, this.auth.getConfiguration().wrong_password);
        }
        u.setPassword(Hashing.md5().hashBytes(args[1].getBytes(StandardCharsets.UTF_8)).toString());
        ChatHelper.send(p, this.auth.getConfiguration().changed);
        return true;
    }
}
