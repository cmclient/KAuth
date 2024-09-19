package pl.kuezese.auth.bungee;

import lombok.Getter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.plugin.Plugin;
import pl.kuezese.auth.bungee.config.Config;
import pl.kuezese.auth.bungee.listener.ConnectListener;
import pl.kuezese.auth.bungee.listener.LoginListener;
import pl.kuezese.auth.shared.database.SQL;
import pl.kuezese.auth.shared.manager.PremiumManager;
import pl.kuezese.auth.shared.task.RemoveExpiredTask;

import java.util.concurrent.TimeUnit;

@Getter
public class BungeePlugin extends Plugin {

    @Getter
    public static BungeePlugin instance;

    private Config authConfig;
    private SQL sql;
    private PremiumManager premiumManager;

    public BungeePlugin() {
        instance = this;
    }

    @SneakyThrows
    @Override
    public void onEnable() {
        authConfig = new Config(this);

        if ((sql = new SQL(authConfig.getCredentials())).connect(getDataFolder())) {
            sql.updateAsync("CREATE TABLE IF NOT EXISTS `premium_cache` (`id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT, `name` varchar(16) NOT NULL, `result` varchar(20), `expire_date` DATETIME NOT NULL);").get();
            sql.updateAsync("DELETE FROM `premium_cache` WHERE `expire_date` < NOW();").get();
        } else {
            getProxy().stop();
            return;
        }

        (premiumManager = new PremiumManager(sql)).load();

        if (authConfig.isPremiumAuth()) {
            getProxy().getScheduler().schedule(this, new RemoveExpiredTask(sql, premiumManager), 1L, 1L, TimeUnit.MINUTES);
            getProxy().getPluginManager().registerListener(this, new LoginListener(this));
            getProxy().getPluginManager().registerListener(this, new ConnectListener(this));
            getProxy().registerChannel("kauth:premiumlogin");
        }
    }

    @Override
    public void onDisable() {
        getProxy().unregisterChannel("kauth:premiumlogin");
    }

    public void debugLog(String msg) {
        if (!getAuthConfig().isDebug()) return;

        getLogger().info(msg);
    }
}
