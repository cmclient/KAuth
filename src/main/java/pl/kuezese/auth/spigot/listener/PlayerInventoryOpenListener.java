package pl.kuezese.auth.spigot.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import pl.kuezese.auth.spigot.SpigotPlugin;
import pl.kuezese.auth.spigot.object.User;

@RequiredArgsConstructor
public class PlayerInventoryOpenListener implements Listener {

    private final SpigotPlugin auth;

    @EventHandler
    public void onDrop(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        User user = auth.getUserManager().get(player.getName());
        if (!user.isPremium() && !user.isLogged()) {
            event.setCancelled(true);
        }
    }
}
