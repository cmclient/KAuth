package pl.kuezese.auth.spigot.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import pl.kuezese.auth.spigot.SpigotPlugin;
import pl.kuezese.auth.spigot.object.User;

import java.util.Arrays;

@RequiredArgsConstructor
public class PlayerCommandListener implements Listener {

    private final SpigotPlugin auth;
    private final String[] allowed = new String[]{"/login", "/log", "/l", "/register", "/reg", "/r", "/changepassword", "/changepass"};

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        User u = auth.getUserManager().get(p.getName());
        if (!u.isPremium() && !u.isLogged()) {
            String command = e.getMessage().split(" ")[0];
            if (!match(allowed, command)) {
                e.setCancelled(true);
            }
        }
    }

    private boolean match(String[] array, String string) {
        return Arrays.stream(array).anyMatch(s -> s.equalsIgnoreCase(string));
    }
}
