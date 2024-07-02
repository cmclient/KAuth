package pl.kuezese.auth.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.kuezese.auth.AuthPlugin;
import pl.kuezese.auth.object.User;

public class PlayerQuitListener implements Listener {

    private final AuthPlugin auth;

    public PlayerQuitListener(AuthPlugin auth) {
        this.auth = auth;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        User u = this.auth.getUserManager().get(p.getName());
        u.setLogged(false);
    }
}
