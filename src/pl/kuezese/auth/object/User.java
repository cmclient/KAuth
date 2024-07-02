package pl.kuezese.auth.object;

import org.bukkit.entity.Player;
import pl.kuezese.auth.AuthPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User {

    private final String name;
    private boolean registered;
    private boolean logged;
    private String password;
    private final String registerIp;
    private String lastIp;
    private long lastJoin;
    private long lastLogin;

    public User(String name) {
        this.name = name;
        this.registered = false;
        this.logged = false;
        this.password = null;
        this.registerIp = "127.0.0.1";
        this.lastIp = this.registerIp;
        this.lastJoin = 0L;
        this.lastLogin = 0L;
        this.insert();
    }

    public User(Player player) {
        this.name = player.getName();
        this.registered = false;
        this.logged = false;
        this.password = null;
        this.registerIp = player.getAddress().getAddress().getHostAddress();
        this.lastIp = this.registerIp;
        this.lastJoin = System.currentTimeMillis();
        this.lastLogin = System.currentTimeMillis();
        this.insert();
    }

    public User(ResultSet rs) throws SQLException {
        this.name = rs.getString("name");
        this.registered = (rs.getInt("registered") == 1);
        this.password = rs.getString("password");
        this.registerIp = rs.getString("registerIp");
        this.lastIp = rs.getString("lastIp");
        this.lastLogin = rs.getLong("lastLogin");
    }

    private void insert() {
        AuthPlugin.getAuth().getSql().update("INSERT INTO `KAuth`(`id`, `name`, `password`, `registered`, `registerIp`, `lastIp`, `lastLogin`) VALUES (NULL, '" + this.getName() + "'," + (this.password == null ? "NULL" : "'" + this.getPassword() + "'") + ",'" + (this.isRegistered() ? 1 : 0) + "','" + this.registerIp + "','" + this.lastIp + "','" + this.lastLogin + "')");
    }

    public String getName() {
        return this.name;
    }

    public boolean isLogged() {
        return this.logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public long getLastJoin() {
        return lastJoin;
    }

    public void setLastJoin(long lastJoin) {
        this.lastJoin = lastJoin;
    }

    public void updateLastLogin(Player player) {
        this.lastIp = player.getAddress().getAddress().getHostAddress();
        this.lastLogin = System.currentTimeMillis();
        AuthPlugin.getAuth().getSql().update("UPDATE `KAuth` SET `lastIp` ='" + lastIp + "', `lastLogin` ='" + lastLogin + "' WHERE `name` ='" + this.getName() + "'");
    }

    public void setLastLogin(long time) {
        this.lastLogin = time;
        AuthPlugin.getAuth().getSql().update("UPDATE `KAuth` SET `lastLogin` ='" + lastLogin + "' WHERE `name` ='" + this.getName() + "'");
    }

    public boolean shouldAutoLogin(Player player) {
        return this.registered && !this.logged && this.lastIp.equals(player.getAddress().getAddress().getHostAddress()) && (this.lastLogin + 86400000L) > System.currentTimeMillis();
    }

    public boolean isRegistered() {
        return this.registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
        AuthPlugin.getAuth().getSql().update("UPDATE `KAuth` SET `registered` ='" + (registered ? 1 : 0) + "' WHERE `name` ='" + this.getName() + "'");
    }

    public String getPassword() {
        return this.password;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setPassword(String password) {
        this.password = password;
        AuthPlugin.getAuth().getSql().update("UPDATE `KAuth` SET `password` ='" + password + "' WHERE `name` ='" + this.getName() + "'");
    }
}
