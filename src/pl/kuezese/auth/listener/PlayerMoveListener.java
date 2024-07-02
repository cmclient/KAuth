package pl.kuezese.auth.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import pl.kuezese.auth.AuthPlugin;
import pl.kuezese.auth.object.User;

public class PlayerMoveListener implements Listener {

    private final AuthPlugin auth;

    public PlayerMoveListener(AuthPlugin auth) {
        this.auth = auth;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
            Player p = e.getPlayer();
            User u = this.auth.getUserManager().get(p.getName());
            if (!u.isLogged()) {
                e.setTo(e.getFrom());
            }
        }
    }
}
