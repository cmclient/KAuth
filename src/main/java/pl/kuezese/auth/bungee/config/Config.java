package pl.kuezese.auth.bungee.config;

import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import pl.kuezese.auth.bungee.BungeePlugin;
import pl.kuezese.auth.shared.database.SQL;
import pl.kuezese.auth.shared.helper.ChatHelper;
import pl.kuezese.auth.shared.type.DatabaseType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

@Getter
public class Config {

    private final BungeePlugin auth;
    private final File configFile;

    private boolean debug;

    private SQL.Credentials credentials;

    private boolean premiumAuth;

    private BaseComponent[] msgTooManyPlayers;
    private BaseComponent[] msgAlreadyChecking;
    private BaseComponent[] msgFailedToCheck;

    public Config(BungeePlugin auth) {
        this.auth = auth;
        configFile = new File(auth.getDataFolder(), "config.yml");
        try {
            if (!configFile.exists()) {
                Files.createDirectory(auth.getDataFolder().toPath());
                InputStream in = auth.getResourceAsStream("bungee/config.yml");
                Files.copy(in, configFile.toPath());
                in.close();
            }
        } catch (IOException ex) {
            auth.getLogger().log(Level.SEVERE, "Failed to load configuration!", ex);
        }
        reload();
    }

    public void reload() {
        try {
            Configuration conf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);

            debug = conf.getBoolean("debug");

            credentials = new SQL.Credentials(
                    DatabaseType.findByName(conf.getString("database.type")), conf.getString("database.host"),
                    conf.getInt("database.port"), conf.getString("database.database"),
                    conf.getString("database.username"), conf.getString("database.password"));

            premiumAuth = conf.getBoolean("auth.premium");

            msgTooManyPlayers = ChatHelper.colorComponent(conf.getString("messages.too-many-players"));
            msgAlreadyChecking = ChatHelper.colorComponent(conf.getString("messages.already-checking"));
            msgFailedToCheck = ChatHelper.colorComponent(conf.getString("messages.failed-to-check"));
        } catch (IOException ex) {
            auth.getLogger().log(Level.SEVERE, "Failed to load configuration!", ex);
        }
    }
}
