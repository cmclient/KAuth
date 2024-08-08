package pl.kuezese.auth.spigot.config;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.kuezese.auth.shared.database.SQL;
import pl.kuezese.auth.shared.type.DatabaseType;
import pl.kuezese.auth.spigot.SpigotPlugin;

import java.io.File;
import java.time.Duration;

@Getter
public class Config {

    /** Debug Connection **/
    private boolean debug;

    /** Database Connection **/
    private SQL.Credentials credentials;

    /** Auto Purge Configuration **/
    private int autoPurgeDays;

    /** Title Configuration **/
    private boolean titleEnabled;
    private String titleLogin;
    private String titleRegister;
    private String titleLogged;
    private String titleRegistered;

    /** Features Configuration **/
    private boolean experienceBarEnabled;
    private boolean pumpkinEnabled;

    /** Messages **/
    private String msgUsage;
    private String msgCantUseInConsole;
    private String msgLoginUsage;
    private String msgRegisterUsage;
    private String msgChangePasswordUsage;
    private String msgCantUseAsPremium;
    private String msgNotRegistered;
    private String msgNotLogged;
    private String msgAlreadyLogged;
    private String msgWrongPassword;
    private String msgLogged;
    private String msgLoggedPremium;
    private String msgNotSamePassword;
    private String msgRegistered;
    private String msgUnregistered;
    private String msgLogout;
    private String msgAlreadyRegistered;
    private String msgChangedPassword;
    private String msgLogin;
    private String msgSession;
    private String msgRegister;
    private String msgCorrectUsername;
    private String msgInvalidCharacters;
    private String msgInvalidUsername;
    private String msgTimeLeft;

    /** Auth Configuration **/
    private boolean premiumAuth;
    private int maxAccounts;

    /** Sessions Configuration **/
    private boolean sessionsEnabled;
    private Duration sessionsDuration;

    /** Permissions Configuration **/
    private String adminPermission;

    /** Accounts Configuration **/
    private String msgMaxAccounts;

    /** Listeners Configuration **/
    private boolean moveListener;

    public void load(SpigotPlugin auth) {
        if (!auth.getDataFolder().exists() && auth.getDataFolder().mkdirs()) {
            auth.getLogger().info("Created " + auth.getDataFolder().getAbsolutePath());
        }
        File file = new File(auth.getDataFolder(), "config.yml");
        if (!file.exists()) {
            auth.saveResource("config.yml", true);
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        debug = cfg.getBoolean("debug");

        credentials = new SQL.Credentials(
                DatabaseType.findByName(cfg.getString("database.type")), cfg.getString("database.host"),
                cfg.getInt("database.port"), cfg.getString("database.database"),
                cfg.getString("database.username"), cfg.getString("database.password"));

        autoPurgeDays = cfg.getInt("autopurge.days");

        titleEnabled = cfg.getBoolean("title.enabled");
        titleLogin = cfg.getString("title.messages.login");
        titleRegister = cfg.getString("title.messages.register");
        titleLogged = cfg.getString("title.messages.logged");
        titleRegistered = cfg.getString("title.messages.registered");

        experienceBarEnabled = cfg.getBoolean("features.experience-bar");
        pumpkinEnabled = cfg.getBoolean("features.pumpkin");

        msgUsage = cfg.getString("messages.usage");
        msgLoginUsage = cfg.getString("messages.login-usage");
        msgRegisterUsage = cfg.getString("messages.register-usage");
        msgChangePasswordUsage = cfg.getString("messages.change-password-usage");
        msgCantUseInConsole = cfg.getString("messages.cant-use-in-console");
        msgCantUseAsPremium = cfg.getString("messages.cant-use-as-premium");
        msgNotRegistered = cfg.getString("messages.not-registered");
        msgNotLogged = cfg.getString("messages.not-logged");
        msgAlreadyLogged = cfg.getString("messages.already-logged");
        msgWrongPassword = cfg.getString("messages.wrong-password");
        msgLogged = cfg.getString("messages.logged");
        msgLoggedPremium = cfg.getString("messages.logged-premium");
        msgNotSamePassword = cfg.getString("messages.not-same-password");
        msgRegistered = cfg.getString("messages.registered");
        msgUnregistered = cfg.getString("messages.unregistered");
        msgLogout = cfg.getString("messages.logout");
        msgAlreadyRegistered = cfg.getString("messages.already-registered");
        msgChangedPassword = cfg.getString("messages.changed");
        msgLogin = cfg.getString("messages.login");
        msgSession = cfg.getString("messages.session");
        msgRegister = cfg.getString("messages.register");
        msgCorrectUsername = cfg.getString("messages.correct-username");
        msgInvalidCharacters = cfg.getString("messages.invalid-characters");
        msgInvalidUsername = cfg.getString("messages.invalid-username");
        msgTimeLeft = cfg.getString("messages.time-left");
        msgMaxAccounts = cfg.getString("messages.max-accounts");

        premiumAuth = cfg.getBoolean("auth.premium");
        maxAccounts = cfg.getInt("auth.max-accounts");

        sessionsEnabled = cfg.getBoolean("sessions.enabled");
        sessionsDuration = Duration.ofMinutes(cfg.getInt("sessions.duration"));

        adminPermission = cfg.getString("permissions.admin");

        moveListener = cfg.getBoolean("listeners.move");
    }
}
