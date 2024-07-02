package pl.kuezese.auth.manager;

import org.bukkit.entity.Player;
import pl.kuezese.auth.AuthPlugin;
import pl.kuezese.auth.object.User;

import java.util.concurrent.ConcurrentHashMap;

public class UserManager {

    private final AuthPlugin auth;
    private final ConcurrentHashMap<String, User> users;

    public UserManager(AuthPlugin auth) {
        this.auth = auth;
        this.users = new ConcurrentHashMap<>();
    }

    public User get(String name) {
        return this.users.get(name);
    }

    public User getIgnoreCase(String name) {
        return this.users.values().stream().filter(user -> user.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public int getByIp(String ip) {
        return (int) this.users.values().stream().filter(User::isRegistered).filter(user -> user.getLastIp().equals(ip)).count();
    }

    public User create(Player player) {
        User u = new User(player);
        this.users.put(u.getName(), u);
        return u;
    }

    public User create(String name) {
        User u = new User(name);
        this.users.put(name, u);
        return u;
    }

    public void remove(User user) {
        this.auth.getSql().updateAwait("DELETE FROM `KAuth` WHERE `name` ='" + user.getName() + "'");
        this.users.remove(user.getName());
    }

    public void load(AuthPlugin auth) {
        auth.getSql().query("SELECT * FROM `KAuth`", rs -> {
            try {
                while (rs.next()) {
                    User u = new User(rs);
                    users.put(u.getName(), u);
                }
                auth.getLogger().info("Loaded " + users.size() + " players.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
