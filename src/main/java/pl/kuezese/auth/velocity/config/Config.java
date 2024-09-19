package pl.kuezese.auth.velocity.config;

import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import pl.kuezese.auth.shared.database.SQL;
import pl.kuezese.auth.shared.helper.ChatHelper;
import pl.kuezese.auth.shared.type.DatabaseType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Getter
public class Config {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final File configFile;

    private boolean debug;
    private SQL.Credentials credentials;
    private boolean premiumAuth;
    private Component msgTooManyPlayers;
    private Component msgAlreadyChecking;
    private Component msgFailedToCheck;

    public Config(ProxyServer proxyServer, Logger logger, Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.configFile = dataDirectory.resolve("config.yml").toFile();

        try {
            if (!configFile.exists()) {
                Files.createDirectories(dataDirectory);
                try (InputStream in = getClass().getResourceAsStream("bungee/config.yml")) {
                    if (in != null) {
                        Files.copy(in, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        logger.warn("Resource 'bungee/config.yml' not found in the JAR");
                    }
                }
            }
        } catch (IOException ex) {
            logger.error("Failed to load configuration!", ex);
        }
        reload();
    }

    public void reload() {
        try {
            Yaml yaml = new Yaml();
            try (InputStream in = Files.newInputStream(configFile.toPath())) {
                Map<String, Object> config = yaml.load(in);
                debug = (Boolean) config.getOrDefault("debug", false);

                Map<String, Object> database = (Map<String, Object>) config.get("database");
                credentials = new SQL.Credentials(
                        DatabaseType.findByName((String) database.get("type")),
                        (String) database.get("host"),
                        (Integer) database.get("port"),
                        (String) database.get("database"),
                        (String) database.get("username"),
                        (String) database.get("password")
                );

                premiumAuth = (Boolean) config.getOrDefault("auth.premium", false);

                msgTooManyPlayers = Component.text(ChatHelper.color((String) config.get("messages.too-many-players")));
                msgAlreadyChecking = Component.text(ChatHelper.color((String) config.get("messages.already-checking")));
                msgFailedToCheck = Component.text(ChatHelper.color((String) config.get("messages.failed-to-check")));
            }
        } catch (IOException ex) {
            logger.error("Failed to reload configuration!", ex);
        }
    }
}
