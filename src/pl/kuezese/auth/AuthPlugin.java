package pl.kuezese.auth;

import org.bukkit.plugin.java.JavaPlugin;
import pl.kuezese.auth.command.*;
import pl.kuezese.auth.config.Config;
import pl.kuezese.auth.listener.*;
import pl.kuezese.auth.manager.UserManager;
import pl.kuezese.auth.mysql.SQL;

import java.util.concurrent.TimeUnit;

public class AuthPlugin extends JavaPlugin {

    private static AuthPlugin auth;

    public static AuthPlugin getAuth() {
        return auth;
    }

    private Config configuration;
    private SQL sql;
    private UserManager userManager;

    public Config getConfiguration() {
        return configuration;
    }

    public SQL getSql() {
        return this.sql;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public AuthPlugin() {
        auth = this;
    }

    @Override
    public void onEnable() {
        (this.configuration = new Config()).load(this);
        if ((this.sql = new SQL(this)).connect()) {
            this.sql.update("CREATE TABLE IF NOT EXISTS `KAuth` (`id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT, `name` varchar(32) NOT NULL, `password` text NULL, `registered` int(1), `registerIp` text NOT NULL, `lastIp` text NOT NULL, `lastLogin` BIGINT(22) NOT NULL);");
            (this.userManager = new UserManager(this)).load(this);
        } else {
            this.getServer().shutdown();
        }

        this.getLogger().info("Loading commands...");
        new LoginCommand(this);
        new RegisterCommand(this);
        new LogoutCommand(this);
        new ChangePasswordCommand(this);
        new AuthCommand(this);

        this.getLogger().info("Loading listeners...");
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerCommandListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        if (!this.configuration.block_move) {
            this.getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        }
        this.getServer().getPluginManager().registerEvents(new PlayerDropItemListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerInventoryOpenListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerInventoryClickListener(this), this);

        this.getLogger().info("Plugin " + getDescription().getFullName() + " by " + getDescription().getAuthors().get(0) + " has been loaded successfully.");
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        if (sql.isConnected()) {
            sql.executor.shutdown();
            try {
                sql.executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sql.disconnect();
        }
    }
}
