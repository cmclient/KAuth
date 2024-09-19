package pl.kuezese.auth.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import pl.kuezese.auth.velocity.VelocityPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;

@RequiredArgsConstructor
public class ConnectListener {

    private final VelocityPlugin plugin;

    @Subscribe
    public void onConnect(PostLoginEvent event) {
        Player player = event.getPlayer();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        plugin.debugLog("PostLoginEvent " + player.getUsername() + " " + (player.isOnlineMode() ? "online mode" : "offline mode"));

        try {
            out.writeBoolean(player.isOnlineMode());
            out.writeUTF(player.getUsername());
            out.writeUTF(player.getUniqueId().toString());
        } catch (Exception ex) {
            plugin.getLogger().info("Failed to create auth data", ex);
        }

        plugin.sendPluginMessageToBackend(VelocityPlugin.IDENTIFIER, stream.toByteArray());
        plugin.debugLog("Sent auth data " + Arrays.toString(new String[]{String.valueOf(player.isOnlineMode()), player.getUsername(), player.getUniqueId().toString()}));
    }
}
