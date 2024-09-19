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
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (!pattern.matcher(player.getName()).find()) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatHelper.color(auth.getAuthConfig().getMsgInvalidCharacters()));
            return;
        }
        User user = auth.getUserManager().getIgnoreCase(player.getName());
        if (user != null && !user.getName().equals(player.getName())) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatHelper.color(auth.getAuthConfig().getMsgCorrectUsername().replace("{NAME}", user.getName())));
            return;
        }
        if (auth.getAuthConfig().getMaxAccounts() != 0 && (user == null || !user.isRegistered()) && auth.getUserManager().getByIp(event.getAddress().getHostAddress()) >= auth.getAuthConfig().getMaxAccounts()) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatHelper.color(auth.getAuthConfig().getMsgMaxAccounts()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        User user = auth.getUserManager().get(player.getName());
        if (user == null) {
            user = auth.getUserManager().create(player);
        }

        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.setLevel(0);
        player.setExp(0.0F);

        user.setLastJoin(Timestamp.from(Instant.now()));

        if (auth.getAuthConfig().isPremiumAuth())
            return;

        if (auth.getAuthConfig().isSessionsEnabled() && user.shouldAutoLogin(player)) {
            user.setLogged(true);
            user.updateLastLogin(player);
            ChatHelper.send(player, auth.getAuthConfig().getMsgSession());
            return;
        }

        new LoginTask(auth, player, user).runTaskTimer(auth, 5L, 20L);
    }
}
