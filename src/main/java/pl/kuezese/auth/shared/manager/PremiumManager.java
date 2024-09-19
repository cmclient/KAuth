package pl.kuezese.auth.shared.manager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.kuezese.auth.shared.data.ResultData;
import pl.kuezese.auth.shared.type.ResultType;
import pl.kuezese.auth.shared.database.SQL;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter @RequiredArgsConstructor
public class PremiumManager {

    private final static String API_URL = "https://api.mojang.com/users/profiles/minecraft/%s";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final SQL sql;
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
            logger.error("Failed to verify player {}", name, ex);
            return ResultType.ERROR;
        }
    }

    public void cache(ResultData data) {
        if (!cache.containsKey(data.getName())) {
            cache.put(data.getName(), data);
            if (data.getExpireDate() != null) {
                sql.updateAsync("INSERT INTO `premium_cache` (`name`, `result`, `expire_date`) VALUES (?, ?, ?)", data.getName(), data.getResult().getName(), data.getExpireDate());
            }
        }
    }

    public void load() {
        sql.queryAsync("SELECT * FROM `premium_cache`").thenAccept(rs -> {
            try {
                while (rs.next()) {
                    ResultData data = new ResultData(rs);
                    cache.put(data.getName(), data);
                }
                logger.info("Loaded {} cached data.", cache.size());
            } catch (Exception ex) {
                logger.info("Failed to load cached data", ex);
            }
        });
    }
}
