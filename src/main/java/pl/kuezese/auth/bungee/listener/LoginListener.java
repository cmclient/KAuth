package pl.kuezese.auth.bungee.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.kuezese.auth.bungee.BungeePlugin;
import pl.kuezese.auth.bungee.data.ResultData;
import pl.kuezese.auth.bungee.type.ResultType;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class LoginListener implements Listener {

    private final BungeePlugin auth;
    public final Cache<String, Boolean> loginCache = CacheBuilder.newBuilder().expireAfterWrite(9, TimeUnit.SECONDS).build();

    @EventHandler
    public void onLogin(PreLoginEvent event) {
        PendingConnection connection = event.getConnection();
        String username = connection.getName();

        if (loginCache.size() > 9) {
            auth.getLogger().warning("Mojang API rate limit exceeded, kicking player " + username);
            connection.disconnect(auth.getAuthConfig().getMsgTooManyPlayers());
            return;
        }

        ResultData data = auth.getPremiumManager().getCache().get(username);
        if (data != null) {
            handleConnection(connection, data.getResult(), true);
            return;
        }

        Boolean cached = loginCache.getIfPresent(username);
        if (cached != null) {
            connection.disconnect(auth.getAuthConfig().getMsgAlreadyChecking());
            return;
        }

        loginCache.put(username, true);
        ResultType result = auth.getPremiumManager().check(username);

        switch (result) {
            case PREMIUM:
                auth.getPremiumManager().cache(new ResultData(username, result, getTimestamp(7, TimeUnit.DAYS)));
                break;
            case NON_PREMIUM:
                auth.getPremiumManager().cache(new ResultData(username, result, getTimestamp(30, TimeUnit.MINUTES)));
                break;
            case ERROR:
                auth.getPremiumManager().cache(new ResultData(username, result, getTimestamp(10, TimeUnit.SECONDS)));
                break;
        }

        handleConnection(connection, result, false);
        loginCache.invalidate(username);
    }

    private void handleConnection(PendingConnection connection, ResultType result, boolean cache) {
        if (auth.getAuthConfig().isDebug()) {
            auth.getLogger().info("Got result " + result.getName() + " for player " + connection.getName() + (cache ? " (cached)" : ""));
        }
        if (result == ResultType.PREMIUM) {
            connection.setOnlineMode(true);
        } else if (result == ResultType.NON_PREMIUM) {
            connection.setOnlineMode(false);
        } else if (result == ResultType.ERROR) {
            connection.disconnect(auth.getAuthConfig().getMsgFailedToCheck());
        }
    }

    private Timestamp getTimestamp(int time, TimeUnit unit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime;

        switch (unit) {
            case DAYS:
                expireTime = now.plusDays(time);
                break;
            case HOURS:
                expireTime = now.plusHours(time);
                break;
            case MINUTES:
                expireTime = now.plusMinutes(time);
                break;
            case SECONDS:
                expireTime = now.plusSeconds(time);
                break;
            case MILLISECONDS:
                expireTime = now.plusNanos(time * 1_000_000L);
                break;
            case MICROSECONDS:
                expireTime = now.plusNanos(time * 1_000L);
                break;
            case NANOSECONDS:
                expireTime = now.plusNanos(time);
                break;
            default:
                throw new IllegalArgumentException("Unsupported TimeUnit: " + unit);
        }

        return Timestamp.valueOf(expireTime);
    }
}
