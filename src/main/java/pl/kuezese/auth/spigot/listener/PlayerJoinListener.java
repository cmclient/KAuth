package pl.kuezese.auth.spigot.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.potion.PotionEffectType;
import pl.kuezese.auth.shared.helper.ChatHelper;
import pl.kuezese.auth.spigot.SpigotPlugin;
import pl.kuezese.auth.spigot.object.User;
import pl.kuezese.auth.spigot.task.LoginTask;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {

    private final Pattern pattern = Pattern.compile("^[0-9a-zA-Z-_]+$");
    private final SpigotPlugin auth;

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        if (!pattern.matcher(p.getName()).find()) {
            e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatHelper.color(auth.getAuthConfig().getMsgInvalidCharacters()));
            return;
        }
        User u = auth.getUserManager().getIgnoreCase(p.getName());
        if (u != null && !u.getName().equals(p.getName())) {
            e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatHelper.color(auth.getAuthConfig().getMsgCorrectUsername().replace("{NAME}", u.getName())));
            return;
        }
        if (auth.getAuthConfig().getMaxAccounts() != 0 && (u == null || !u.isRegistered()) && auth.getUserManager().getByIp(e.getAddress().getHostAddress()) >= auth.getAuthConfig().getMaxAccounts()) {
            e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatHelper.color(auth.getAuthConfig().getMsgMaxAccounts()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        User u = auth.getUserManager().get(p.getName());
        if (u == null) {
            u = auth.getUserManager().create(p);
        }

        p.removePotionEffect(PotionEffectType.BLINDNESS);
        p.setLevel(0);
        p.setExp(0.0F);

        u.setLastJoin(Timestamp.from(Instant.now()));

        if (auth.getAuthConfig().isPremiumAuth())
            return;

        if (auth.getAuthConfig().isSessionsEnabled() && u.shouldAutoLogin(p)) {
            u.setLogged(true);
            u.updateLastLogin(p);
            ChatHelper.send(p, auth.getAuthConfig().getMsgSession());
            return;
        }

        new LoginTask(auth, p, u).runTaskTimer(auth, 5L, 20L);
    }
}
