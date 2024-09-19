package pl.kuezese.auth.spigot.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import pl.kuezese.auth.spigot.SpigotPlugin;
import pl.kuezese.auth.spigot.object.User;

@RequiredArgsConstructor
public class PlayerInventoryClickListener implements Listener {

    private final SpigotPlugin auth;

    @EventHandler(ignoreCancelled = true)
    public void onDrop(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        User u = auth.getUserManager().get(player.getName());
        if (!u.isPremium() && !u.isLogged()) {
            event.setCancelled(true);
        }
    }
}
