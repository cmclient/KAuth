package pl.kuezese.auth.config;

import pl.kuezese.auth.AuthPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {

    /** Database Connection **/
    public String mysql_host;
    public int mysql_port;
    public String mysql_base;
    public String mysql_user;
    public String mysql_pass;

    /** Title Configuration **/
    public boolean titleEnabled;
    public String titleLogin;
    public String titleRegister;
    public String titleLogged;
    public String titleRegistered;

    /** Features Configuration **/
    public boolean experienceBarEnabled;
    public boolean pumpkinEnabled;

    /** Messages **/
    public String usage;
    public String not_registered;
    public String not_logged;
    public String already_logged;
    public String wrong_password;
    public String logged;
    public String not_same_password;
    public String registered;
    public String unregistered;
    public String logout;
    public String already_registered;
    public String changed;
    public String login;
    public String session;
    public String register;
    public String correct_username;
    public String invalid_characters;
    public String invalid_username;
    public String time_left;

    /** Accounts Configuration **/
    public String max_accounts;
    public int max_reg_per_ip;

    /** Listeners Configuration **/
    public boolean block_move;

    public void load(AuthPlugin auth) {
        if (!auth.getDataFolder().exists()) {
            auth.getDataFolder().mkdirs();
        }
        File file = new File(auth.getDataFolder(), "config.yml");
        if (!file.exists()) {
            auth.saveResource("config.yml", true);
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        mysql_host = cfg.getString("mysql.host");
        mysql_port = cfg.getInt("mysql.port");
        mysql_base = cfg.getString("mysql.base");
        mysql_user = cfg.getString("mysql.user");
        mysql_pass = cfg.getString("mysql.pass");

        titleEnabled = cfg.getBoolean("title.enabled");
        titleLogin = cfg.getString("title.messages.login");
        titleRegister = cfg.getString("title.messages.register");
        titleLogged = cfg.getString("title.messages.logged");
        titleRegistered = cfg.getString("title.messages.registered");

        experienceBarEnabled = cfg.getBoolean("features.experience-bar");
        pumpkinEnabled = cfg.getBoolean("features.pumpkin");

        usage = cfg.getString("messages.usage");
        not_registered = cfg.getString("messages.not_registered");
        not_logged = cfg.getString("messages.not_logged");
        already_logged = cfg.getString("messages.already_logged");
        wrong_password = cfg.getString("messages.wrong_password");
        logged = cfg.getString("messages.logged");
        not_same_password = cfg.getString("messages.not_same_password");
        registered = cfg.getString("messages.registered");
        unregistered = cfg.getString("messages.unregistered");
        logout = cfg.getString("messages.logout");
        already_registered = cfg.getString("messages.already_registered");
        changed = cfg.getString("messages.changed");
        login = cfg.getString("messages.login");
        session = cfg.getString("messages.session");
        register = cfg.getString("messages.register");
        correct_username = cfg.getString("messages.correct_username");
        invalid_characters = cfg.getString("messages.invalid_characters");
        invalid_username = cfg.getString("messages.invalid_username");
        time_left = cfg.getString("messages.time_left");

        max_accounts = cfg.getString("messages.max_accounts");
        max_reg_per_ip = cfg.getInt("accounts.max_reg_per_ip");

        block_move = cfg.getBoolean("listeners.move");
    }
}
