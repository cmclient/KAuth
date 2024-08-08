package pl.kuezese.auth.shared.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import pl.kuezese.auth.shared.type.DatabaseType;

import java.io.File;
import java.sql.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class SQL {

    private final Logger logger;
    private Connection conn;
    private final Credentials credentials;
    public final ExecutorService executor = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Getter
    @RequiredArgsConstructor
    public static class Credentials {
        private final DatabaseType type;
        private final String host;
        private final int port;
        private final String database;
        private final String user;
        private final String password;
    }

    public boolean connect(File dataFolder) {
        logger.info("Connecting to " + credentials.getType() + " database...");

        try {
            switch (credentials.getType()) {
                case MYSQL:
                    Class.forName("com.mysql.jdbc.Driver");
                    conn = DriverManager.getConnection(
                            "jdbc:mysql://" + credentials.getHost() + ":" + credentials.getPort() + "/" + credentials.getDatabase(),
                            credentials.getUser(), credentials.getPassword()
                    );
                    scheduler.scheduleAtFixedRate(
                            () -> executeAsync("SELECT CURTIME()"), // Keepalive query
                            0, // Initial delay of 0 seconds
                            15, // Run every 15 seconds
                            TimeUnit.SECONDS // Time unit for the interval
                    );
                    break;
                case SQLITE:
                    Class.forName("org.sqlite.JDBC");
                    conn = DriverManager.getConnection("jdbc:sqlite:" + dataFolder + File.separatorChar + "minecraft.db");
                    break;
            }
            return true;
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "Failed to find driver for " + credentials.getType() + " database!");
            return false;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to connect to database!", ex);
            return false;
        }
    }

    public Future<Void> executeAsync(String query) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        executor.submit(() -> {
            try {
                conn.createStatement().execute(query);
                future.complete(null);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "SQL Execute Error", ex);
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    public Future<Void> updateAsync(String update, Object... params) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        executor.submit(() -> {
            try (PreparedStatement stmt = conn.prepareStatement(update)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }

                stmt.executeUpdate();
                future.complete(null);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "SQL Update Error", ex);
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    @SneakyThrows
    public CompletableFuture<Boolean> recordExistsAsync(String query, Object... params) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        queryAsync(query, params).thenAccept(rs -> {
            try {
                future.complete(rs.next());
            } catch (SQLException ex) {
                future.completeExceptionally(ex);
                logger.log(Level.SEVERE, "SQL Query Error", ex);
            }
        }).get();

        return future;
    }

    public CompletableFuture<ResultSet> queryAsync(String query, Object... params) {
        CompletableFuture<ResultSet> future = new CompletableFuture<>();

        executor.submit(() -> {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }

                ResultSet resultSet = stmt.executeQuery();
                future.complete(resultSet);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "SQL Query Error", ex);
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    public void disconnect() {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "SQL Disconnect Error", ex);
        }
    }

    public boolean isConnected() {
        if (conn == null) {
            return false;
        }
        try {
            return !conn.isClosed();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "SQL Closed Error", ex);
            return false;
        }
    }
}
