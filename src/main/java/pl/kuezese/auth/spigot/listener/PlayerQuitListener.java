package pl.kuezese.auth.spigot.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.kuezese.auth.spigot.SpigotPlugin;
import pl.kuezese.auth.spigot.object.User;

@RequiredArgsConstructor
public class PlayerQuitListener implements Listener {

    private final SpigotPlugin auth;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        User user = auth.getUserManager().get(player.getName());
        if (user != null) {
            user.setLogged(false);
            user.setPremium(false);
        }
    }
}
