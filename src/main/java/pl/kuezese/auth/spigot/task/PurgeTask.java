package pl.kuezese.auth.spigot.task;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kuezese.auth.spigot.SpigotPlugin;
import pl.kuezese.auth.spigot.object.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PurgeTask extends BukkitRunnable {

    private final SpigotPlugin auth;

    // Gson instance
    private final Gson gson = new Gson();

    @SneakyThrows
    @Override
    public void run() {
        int days = auth.getAuthConfig().getAutoPurgeDays();

        String query = "SELECT *, DATEDIFF(NOW(), loginDate) AS daysAgo FROM auth WHERE loginDate <= DATE_SUB(NOW(), INTERVAL ? DAY) AND loginDate IS NOT NULL";
        CompletableFuture<ResultSet> future = auth.getSql().queryAsync(query, days);

        future.thenAccept(rs -> {
            try {
                Map<String, Integer> users = new HashMap<>();
                while (rs.next()) {
                    int daysAgo = rs.getInt("daysAgo");
                    User user = auth.getUserManager().get(rs);
                    users.put(user.getName(), daysAgo);
                }

                if (!users.isEmpty()) {
                    if (auth.getAuthConfig().isDebug()) {
                        auth.getLogger().info("Found " + users.size() + " players to purge");
                        auth.getLogger().info(gson.toJson(users));
                    }

                    users.keySet().forEach(username -> auth.getUserManager().getUsers().remove(username));
                    String names = users.keySet().stream()
                            .map(name -> "'" + name + "'")
                            .collect(Collectors.joining(", "));
                    auth.getSql().updateAsync("DELETE FROM `auth` WHERE `name` IN (" + names + ")");
                }
            } catch (SQLException ex) {
                auth.getLogger().log(Level.SEVERE, "Error processing ResultSet", ex);
            } finally {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    auth.getLogger().log(Level.SEVERE, "Error closing ResultSet", ex);
                }
            }
        }).exceptionally(ex -> {
            auth.getLogger().log(Level.SEVERE, "SQL Query Error", ex);
            return null;
        });
    }
}
