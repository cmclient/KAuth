package pl.kuezese.auth.spigot;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;
import pl.kuezese.auth.shared.database.SQL;
import pl.kuezese.auth.spigot.command.*;
import pl.kuezese.auth.spigot.config.Config;
import pl.kuezese.auth.spigot.listener.*;
import pl.kuezese.auth.spigot.manager.UserManager;
import pl.kuezese.auth.spigot.message.AuthMessageListener;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Getter
public class SpigotPlugin extends JavaPlugin {

    @Getter
    private static SpigotPlugin instance;

    private Config authConfig;
    private SQL sql;
    private UserManager userManager;

    public SpigotPlugin() {
        instance = this;
    }

    @SneakyThrows
    @Override
    public void onEnable() {
        (authConfig = new Config()).load(this);
        if ((sql = new SQL(getLogger(), authConfig.getCredentials())).connect(getDataFolder())) {
            switch (authConfig.getCredentials().getType()) {
                case MYSQL:
                    sql.updateAsync("CREATE TABLE IF NOT EXISTS `auth` (`id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT, `name` varchar(32) NOT NULL, `password` text NULL, `registered` int(1), `registerIp` text NOT NULL, `lastIp` text NOT NULL, `lastLogin` BIGINT(22) NOT NULL);").get();
                    break;
                case SQLITE:
                    sql.updateAsync("CREATE TABLE IF NOT EXISTS auth (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, password TEXT, registered INTEGER, registerIp TEXT NOT NULL, lastIp TEXT NOT NULL, lastLogin INTEGER NOT NULL);").get();
                    break;
            }
            (userManager = new UserManager(this)).load(this);
        } else {
            getServer().shutdown();
            return;
        }

        getLogger().info("Loading commands...");
        new LoginCommand(this);
        new RegisterCommand(this);
        new LogoutCommand(this);
        new ChangePasswordCommand(this);
        new AuthCommand(this);

        getLogger().info("Loading listeners...");
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerCommandListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        if (authConfig.isMoveListener()) {
            getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        }
        getServer().getPluginManager().registerEvents(new PlayerDropItemListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInventoryOpenListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInventoryClickListener(this), this);

        if (authConfig.isPremiumAuth()) {
            getServer().getMessenger().registerIncomingPluginChannel(this, "kauth:premiumlogin", new AuthMessageListener(this));
        }

        getLogger().info("Plugin " + getDescription().getFullName() + " by " + getDescription().getAuthors().get(0) + " has been loaded successfully.");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        if (sql.isConnected()) {
            sql.executor.shutdown();
            try {
                sql.executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                getLogger().log(Level.SEVERE, "SQL Error", ex);
            }
            sql.disconnect();
        }
    }
}
