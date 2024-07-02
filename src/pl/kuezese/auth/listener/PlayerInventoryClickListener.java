package pl.kuezese.auth.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import pl.kuezese.auth.AuthPlugin;
import pl.kuezese.auth.object.User;

public class PlayerInventoryClickListener implements Listener {

    private final AuthPlugin auth;

    public PlayerInventoryClickListener(AuthPlugin auth) {
        this.auth = auth;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        User u = this.auth.getUserManager().get(p.getName());
        if (!u.isLogged()) {
            e.setCancelled(true);
        }
    }
}
