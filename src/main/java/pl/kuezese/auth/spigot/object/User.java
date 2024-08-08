package pl.kuezese.auth.spigot.object;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import pl.kuezese.auth.spigot.SpigotPlugin;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;

@Getter @Setter
public class User {

    private final String name;
    private boolean logged;
    private String password;
    @Nullable
    private Timestamp registerDate;
    @Nullable
    private Timestamp loginDate;
    private Timestamp lastJoin;
    @Nullable
    private final String registerIp;
    @Nullable
    private String lastIp;
    private boolean premium;

    public User(String name) {
        this.name = name;
        logged = false;
        password = null;
        registerIp = null;
        lastIp = null;
        lastJoin = Timestamp.from(Instant.now());
    }

    public User(Player player) {
        name = player.getName();
        logged = false;
        password = null;
        registerIp = player.getAddress().getAddress().getHostAddress();
        lastIp = registerIp;
        lastJoin = Timestamp.from(Instant.now());
    }

    public User(ResultSet rs) throws SQLException {
        name = rs.getString("name");
        password = rs.getString("password");
        registerDate = rs.getTimestamp("registerDate");
        loginDate = rs.getTimestamp("loginDate");
        registerIp = rs.getString("registerIp");
        lastIp = rs.getString("lastIp");
    }

    public void insert() {
        String checkSql = "SELECT 1 FROM `auth` WHERE `name` = ?";
        String insertSql = "INSERT INTO `auth`(`id`, `name`, `password`, `registerDate`, `loginDate`, `registerIp`, `lastIp`) VALUES (NULL, ?, ?, ?, ?, ?, ?)";

        Object[] checkParams = new Object[]{name};
        Object[] insertParams = new Object[]{
                name,
                password,
                registerDate,
                loginDate,
                registerIp,
                lastIp
        };

        SpigotPlugin.getInstance().getSql().recordExistsAsync(checkSql, checkParams).thenAccept(exists -> {
            if (!exists) {
                SpigotPlugin.getInstance().getSql().updateAsync(insertSql, insertParams);
            }
        }).exceptionally(ex -> {
            SpigotPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to insert user " + name, ex);
            return null;
        });
    }

    public void updateLastLogin(Player player) {
        lastIp = player.getAddress().getAddress().getHostAddress();
        loginDate = Timestamp.from(Instant.now());
        SpigotPlugin.getInstance().getSql().updateAsync("UPDATE `auth` SET `lastIp` = ?, `loginDate` = ? WHERE `name` = ?", lastIp, loginDate, name);
    }

    public void removeLastLogin() {
        loginDate = null;
        SpigotPlugin.getInstance().getSql().updateAsync("UPDATE `auth` SET `loginDate` = ? WHERE `name` = ?", null, name);
    }

    public boolean isRegistered() {
        return password != null;
    }

    public boolean shouldAutoLogin(Player player) {
        if (!isRegistered() || premium || logged || lastIp == null || loginDate == null) {
            return false;
        }

        String currentPlayerIp = player.getAddress().getAddress().getHostAddress();
        if (!lastIp.equals(currentPlayerIp)) {
            return false;
        }

        Duration sessionsDuration = SpigotPlugin.getInstance().getAuthConfig().getSessionsDuration();
        Instant sessionEnd = loginDate.toInstant().plus(sessionsDuration);
        return !Instant.now().isAfter(sessionEnd);
    }

    public void setPassword(String password, boolean update) {
        this.password = password;
        if (update) {
            SpigotPlugin.getInstance().getSql().updateAsync("UPDATE `auth` SET `password` = ? WHERE `name` = ?", password, name);
        }
    }
}
