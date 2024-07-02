package pl.kuezese.auth.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import pl.kuezese.auth.AuthPlugin;
import pl.kuezese.auth.object.User;

public class PlayerInventoryOpenListener implements Listener {

    private final AuthPlugin auth;

    public PlayerInventoryOpenListener(AuthPlugin auth) {
        this.auth = auth;
    }

    @EventHandler
    public void onDrop(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();
        User u = this.auth.getUserManager().get(p.getName());
        if (!u.isLogged()) {
            e.setCancelled(true);
        }
    }
}
