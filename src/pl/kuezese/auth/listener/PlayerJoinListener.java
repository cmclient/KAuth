package pl.kuezese.auth.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kuezese.auth.AuthPlugin;
import pl.kuezese.auth.helper.ChatHelper;
import pl.kuezese.auth.object.User;
import pl.kuezese.auth.task.LoginTask;

import java.util.regex.Pattern;

public class PlayerJoinListener implements Listener {

    private final AuthPlugin auth;
    private final Pattern pattern;

    public PlayerJoinListener(AuthPlugin auth) {
        this.auth = auth;
        this.pattern = Pattern.compile("^[0-9a-zA-Z-_]+$");
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        if (!this.pattern.matcher(p.getName()).find()) {
            e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatHelper.color(this.auth.getConfiguration().invalid_username));
            return;
        }
        User u = this.auth.getUserManager().getIgnoreCase(p.getName());
        if (u != null && !u.getName().equals(p.getName())) {
            e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatHelper.color(this.auth.getConfiguration().correct_username.replace("{NAME}", u.getName())));
            return;
        }
        if (this.auth.getConfiguration().max_reg_per_ip != 0 && (u == null || !u.isRegistered()) && this.auth.getUserManager().getByIp(e.getAddress().getHostAddress()) >= this.auth.getConfiguration().max_reg_per_ip) {
            e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatHelper.color(this.auth.getConfiguration().max_accounts));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        User u = this.auth.getUserManager().get(p.getName());
        if (u == null) {
            u = this.auth.getUserManager().create(p);
        } else if (u.shouldAutoLogin(p)) {
            u.setLogged(true);
            u.updateLastLogin(p);
            new BukkitRunnable() {
                @Override
                public void run() {
                    ChatHelper.send(p, auth.getConfiguration().session);
                }
            }.runTaskAsynchronously(this.auth);
            return;
        }
        u.setLastJoin(System.currentTimeMillis());
        new LoginTask(this.auth, p, u).runTaskTimer(this.auth, 0L, 20L);
    }
}
