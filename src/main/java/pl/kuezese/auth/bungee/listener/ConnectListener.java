package pl.kuezese.auth.bungee.listener;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.kuezese.auth.bungee.BungeePlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ConnectListener implements Listener {

    private final BungeePlugin auth;

    @EventHandler
    public void onConnect(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();
        auth.getLogger().info("ServerConnectedEvent " + player.getName() + " " + (player.getPendingConnection().isOnlineMode() ? "online mode" : "offline mode"));
        if (player.getPendingConnection().isOnlineMode()) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(stream);
            try {
                out.writeUTF(player.getName());
                out.writeUTF(player.getUniqueId().toString());
            } catch (Exception ex) {
                auth.getLogger().log(Level.SEVERE, "Failed to create auth data", ex);
            }
            if (auth.getAuthConfig().isDebug()) {
                auth.getLogger().info("Sending auth data " + Arrays.toString(new String[]{player.getName(), player.getUniqueId().toString()}));
            }
            event.getServer().sendData("kauth:premiumlogin", stream.toByteArray());
        }
    }
}
