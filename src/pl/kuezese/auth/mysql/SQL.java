package pl.kuezese.auth.mysql;

import pl.kuezese.auth.AuthPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQL {

    private final AuthPlugin auth;
    private Connection conn;
    private final String host;
    private final int port;
    private final String pass;
    private final String base;
    private final String user;
    public final ExecutorService executor;

    public SQL(AuthPlugin auth) {
        this.auth = auth;
        this.host = auth.getConfiguration().mysql_host;
        this.port = auth.getConfiguration().mysql_port;
        this.pass = auth.getConfiguration().mysql_pass;
        this.base = auth.getConfiguration().mysql_base;
        this.user = auth.getConfiguration().mysql_user;
        this.executor = Executors.newScheduledThreadPool(10);
    }

    public boolean connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.conn = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.base, this.user, this.pass);
            this.auth.getServer().getScheduler().runTaskTimer(this.auth, () -> this.execute("SELECT CURTIME()"), 15000L, 15000L);
            this.auth.getLogger().info("Connected to database.");
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            this.auth.getLogger().warning("Failed to connect to database!");
            e.printStackTrace();
            return false;
        }
    }

    public void execute(String query) {
        try {
            this.conn.createStatement().execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(String update) {
        executor.submit(() -> {
            try {
                this.conn.createStatement().executeUpdate(update);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void updateAwait(String update) {
        try {
            this.conn.createStatement().executeUpdate(update);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void query(String query, QueryCallback callback) {
        executor.submit(() -> {
            try (ResultSet rs = this.conn.createStatement().executeQuery(query)) {
                callback.receivedResultSet(rs);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void disconnect() {
        if (this.conn != null) {
            try {
                this.conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        if (this.conn == null)
            return false;

        try {
            return !this.conn.isClosed();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public interface QueryCallback {
        void receivedResultSet(ResultSet rs);
    }
}
