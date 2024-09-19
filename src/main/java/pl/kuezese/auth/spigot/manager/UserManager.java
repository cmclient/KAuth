package pl.kuezese.auth.spigot.manager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import pl.kuezese.auth.spigot.SpigotPlugin;
import pl.kuezese.auth.spigot.object.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@RequiredArgsConstructor
public class UserManager {

    private final SpigotPlugin auth;
    @Getter
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    public User get(String name) {
        return users.get(name);
    }

    public User get(ResultSet rs) throws SQLException {
        String name = rs.getString("name");
        User user = users.get(name);
        if (user == null) {
            user =  new User(rs);
            users.put(user.getName(), user);
        }

        return user;
    }

    public User getIgnoreCase(String name) {
        return users.values()
                .stream()
                .filter(user -> user.getName().equalsIgnoreCase(name))
                .findAny()
                .orElse(null);
    }

    public int getByIp(String ip) {
        return (int) users.values()
                .stream()
                .filter(User::isRegistered)
                .filter(user -> user.getLastIp() != null)
                .filter(user -> user.getLastIp().equals(ip))
                .count();
    }

    public User create(Player player) {
        User user = new User(player);
        users.put(user.getName(), user);
        return user;
    }

    public User create(String name) {
        User user = new User(name);
        users.put(name, user);
        return user;
    }

    @SneakyThrows
    public void remove(User user) {
        auth.getSql().updateAsync("DELETE FROM `auth` WHERE `name` ='" + user.getName() + "'");
        users.remove(user.getName());
    }

    public void load(SpigotPlugin auth) {
        auth.getSql().queryAsync("SELECT name, password, registerDate, loginDate, registerIp, lastIp FROM `auth`").thenAccept(rs -> {
            try {
                while (rs.next()) {
                    User user = new User(rs);
                    users.put(user.getName(), user);
                }
                auth.getLogger().info("Loaded " + users.size() + " players.");
            } catch (Exception ex) {
                auth.getLogger().log(Level.SEVERE, "Failed to load users", ex);
            }
        });
    }
}
