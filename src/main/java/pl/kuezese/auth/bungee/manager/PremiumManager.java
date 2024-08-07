package pl.kuezese.auth.bungee.manager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.kuezese.auth.bungee.BungeePlugin;
import pl.kuezese.auth.bungee.data.ResultData;
import pl.kuezese.auth.bungee.type.ResultType;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@Getter @RequiredArgsConstructor
public class PremiumManager {

    private final static String API_URL = "https://api.mojang.com/users/profiles/minecraft/%s";

    private final BungeePlugin auth;
    private final Map<String, ResultData> cache = new ConcurrentHashMap<>();

    public ResultType check(String name) {
        String urlString = String.format(API_URL, name);
        
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            return responseCode == 200 ? ResultType.PREMIUM : ResultType.NON_PREMIUM;
        } catch (Exception ex) {
            auth.getLogger().log(Level.SEVERE, "Failed to verify player " + name, ex);
            return ResultType.ERROR;
        }
    }

    public void cache(ResultData data) {
        if (!cache.containsKey(data.getName())) {
            cache.put(data.getName(), data);
            if (data.getExpireDate() != null) {
                auth.getSql().updateAsync("INSERT INTO `premium_cache` (`name`, `result`, `expire_date`) VALUES (?, ?, ?)", data.getName(), data.getResult().getName(), data.getExpireDate());
            }
        }
    }

    public void load() {
        auth.getSql().query("SELECT * FROM `premium_cache`", rs -> {
            try {
                while (rs.next()) {
                    ResultData data = new ResultData(rs);
                    cache.put(data.getName(), data);
                }
                auth.getLogger().info("Loaded " + cache.size() + " cached data.");
            } catch (Exception ex) {
                auth.getLogger().log(Level.SEVERE, "Failed to load cached data", ex);
            }
        });
    }
}
