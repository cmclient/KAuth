package pl.kuezese.auth.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import pl.kuezese.auth.AuthPlugin;
import pl.kuezese.auth.object.User;

public class PlayerDropItemListener implements Listener {

    private final AuthPlugin auth;

    public PlayerDropItemListener(AuthPlugin auth) {
        this.auth = auth;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        User u = this.auth.getUserManager().get(p.getName());
        if (!u.isLogged()) {
            e.setCancelled(true);
        }
    }
}
