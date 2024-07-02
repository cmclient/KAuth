package pl.kuezese.auth.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import pl.kuezese.auth.AuthPlugin;
import pl.kuezese.auth.object.User;

import java.util.Arrays;

public class PlayerCommandListener implements Listener {

    private final AuthPlugin auth;
    private final String[] allowed;

    public PlayerCommandListener(AuthPlugin auth) {
        this.auth = auth;
        this.allowed = new String[]{"/login", "/log", "/l", "/register", "/reg", "/r", "/changepassword", "/changepass"};
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        User u = this.auth.getUserManager().get(p.getName());
        if (!u.isLogged()) {
            String command = e.getMessage().split(" ")[0];
            if (!match(this.allowed, command)) {
                e.setCancelled(true);
            }
        }
    }

    private boolean match(String[] array, String string) {
        return Arrays.stream(array).anyMatch(s -> s.equalsIgnoreCase(string));
    }
}
