package pl.kuezese.auth.spigot.message;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import pl.kuezese.auth.shared.helper.ChatHelper;
import pl.kuezese.auth.spigot.SpigotPlugin;
import pl.kuezese.auth.spigot.object.User;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Arrays;
import java.util.UUID;

@RequiredArgsConstructor
public class AuthMessageListener implements PluginMessageListener {

    private final SpigotPlugin auth;

    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
        try {
            String username = in.readUTF();
            UUID uuid = UUID.fromString(in.readUTF());
            if (auth.getAuthConfig().isDebug()) {
                auth.getLogger().info("Got auth data " + Arrays.toString(new String[]{username, uuid.toString()}));
            }
            if (!username.equals(player.getName())) {
                player.kickPlayer(ChatHelper.color("&cFailed to verify session."));
                return;
            }
            if (!uuid.equals(player.getUniqueId())) {
                player.kickPlayer(ChatHelper.color("&cFailed to verify session."));
                return;
            }
            User user = auth.getUserManager().get(username);
            if (user == null) {
                player.kickPlayer(ChatHelper.color("&cFailed to verify session."));
                return;
            }
            user.setPremium(true);
        } catch (Exception ex) {
            player.kickPlayer(ChatHelper.color("&cFailed to verify session."));
        }
    }
}
