package pl.kuezese.auth.spigot.object;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import pl.kuezese.auth.spigot.SpigotPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;

@Getter @Setter
public class User {

    private final String name;
    private boolean registered;
    private boolean logged;
    private String password;
    private final String registerIp;
    private String lastIp;
    private long lastJoin;
    private long lastLogin;
    private boolean premium;

    public User(String name) {
        this.name = name;
        registered = false;
        logged = false;
        password = null;
        registerIp = "127.0.0.1";
        lastIp = registerIp;
        lastJoin = 0L;
        lastLogin = 0L;
        insert();
    }

    public User(Player player) {
        name = player.getName();
        registered = false;
        logged = false;
        password = null;
        registerIp = player.getAddress().getAddress().getHostAddress();
        lastIp = registerIp;
        lastJoin = System.currentTimeMillis();
        lastLogin = System.currentTimeMillis();
        insert();
    }

    public User(ResultSet rs) throws SQLException {
        name = rs.getString("name");
        registered = (rs.getInt("registered") == 1);
        password = rs.getString("password");
        registerIp = rs.getString("registerIp");
        lastIp = rs.getString("lastIp");
        lastLogin = rs.getLong("lastLogin");
    }

    private void insert() {
        // Prepare SQL query with placeholders
        String sql = "INSERT INTO `auth`(`id`, `name`, `password`, `registered`, `registerIp`, `lastIp`, `lastLogin`) VALUES (NULL, ?, ?, ?, ?, ?, ?)";

        // Prepare parameters for the query
        Object[] params = new Object[]{
                getName(),
                getPassword() != null ? getPassword() : null,
                isRegistered() ? 1 : 0,
                registerIp,
                lastIp,
                lastLogin
        };

        SpigotPlugin.getInstance().getSql().updateAsync(sql, params);
    }

    public void updateLastLogin(Player player) {
        lastIp = player.getAddress().getAddress().getHostAddress();
        lastLogin = System.currentTimeMillis();
        SpigotPlugin.getInstance().getSql().updateAsync("UPDATE `auth` SET `lastIp` = ?, `lastLogin` = ? WHERE `name` = ?", lastIp, lastLogin, getName());
    }

    public void setLastLogin(long time) {
        this.lastLogin = time;
        SpigotPlugin.getInstance().getSql().updateAsync("UPDATE `auth` SET `lastLogin` = ? WHERE `name` = ?", lastLogin, getName());
    }

    public boolean shouldAutoLogin(Player player) {
        return !premium && registered && !logged && lastIp.equals(player.getAddress().getAddress().getHostAddress()) && (lastLogin + 86400000L) > System.currentTimeMillis();
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
        SpigotPlugin.getInstance().getSql().updateAsync("UPDATE `auth` SET `registered` = ? WHERE `name` = ?", registered ? 1 : 0, getName());
    }

    public void setPassword(String password) {
        this.password = password;
        SpigotPlugin.getInstance().getSql().updateAsync("UPDATE `auth` SET `password` = ? WHERE `name` = ?", password, getName());
    }
}
