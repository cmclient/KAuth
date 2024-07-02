package pl.kuezese.auth.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.kuezese.auth.AuthPlugin;
import pl.kuezese.auth.object.User;

public class PlayerInteractListener implements Listener {

    private final AuthPlugin auth;

    public PlayerInteractListener(AuthPlugin auth) {
        this.auth = auth;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        User u = this.auth.getUserManager().get(p.getName());
        if (!u.isLogged()) {
            e.setCancelled(true);
        }
    }
}
