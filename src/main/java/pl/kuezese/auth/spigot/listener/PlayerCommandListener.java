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
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        User user = auth.getUserManager().get(player.getName());
        if (!user.isPremium() && !user.isLogged()) {
            String command = event.getMessage().split(" ")[0];
            if (!match(allowed, command)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean match(String[] array, String string) {
        return Arrays.stream(array).anyMatch(s -> s.equalsIgnoreCase(string));
    }
}
