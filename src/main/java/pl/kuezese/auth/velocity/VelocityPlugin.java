package pl.kuezese.auth.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.Getter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import pl.kuezese.auth.shared.database.SQL;
import pl.kuezese.auth.shared.manager.PremiumManager;
import pl.kuezese.auth.shared.task.RemoveExpiredTask;
import pl.kuezese.auth.velocity.config.Config;
import pl.kuezese.auth.velocity.listener.ConnectListener;
import pl.kuezese.auth.velocity.listener.LoginListener;

import javax.inject.Inject;
import java.nio.file.Path;
import java.time.Duration;

@Getter
@Plugin(id = "kauth", name = "KAuth", version = "1.0.4-SNAPSHOT", description = "Authentication Plugin")
public class VelocityPlugin {

    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("kauth:premiumlogin");

    @Inject
    private ProxyServer proxyServer;

    @Inject
    private Logger logger;

    @Inject
    @DataDirectory
    private Path dataDirectory;

    private Config authConfig;
    private SQL sql;
    private PremiumManager premiumManager;

    @Inject
    public VelocityPlugin(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @SneakyThrows
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        authConfig = new Config(proxyServer, logger, dataDirectory);

        if ((sql = new SQL(authConfig.getCredentials())).connect(dataDirectory.toFile())) {
            sql.updateAsync("CREATE TABLE IF NOT EXISTS `premium_cache` (`id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT, `name` varchar(16) NOT NULL, `result` varchar(20), `expire_date` DATETIME NOT NULL);").get();
            sql.updateAsync("DELETE FROM `premium_cache` WHERE `expire_date` < NOW();").get();
        } else {
            proxyServer.shutdown();
            return;
        }

        premiumManager = new PremiumManager(sql);
        premiumManager.load();

        if (authConfig.isPremiumAuth()) {
            proxyServer.getScheduler().buildTask(this, new RemoveExpiredTask(sql, premiumManager))
                    .delay(Duration.ofMinutes(1))
                    .repeat(Duration.ofMinutes(1))
                    .schedule();
            proxyServer.getEventManager().register(this, new LoginListener(this));
            proxyServer.getEventManager().register(this, new ConnectListener(this));
            proxyServer.getChannelRegistrar().register(IDENTIFIER);
        }
    }

    public void sendPluginMessageToBackend(ChannelIdentifier identifier, byte[] data) {
        proxyServer.getAllServers().forEach(server -> server.sendPluginMessage(identifier, data));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        proxyServer.getChannelRegistrar().unregister(IDENTIFIER);
    }

    public void debugLog(String msg) {
        if (authConfig.isDebug()) {
            logger.info(msg);
        }
    }
}
