package pl.kuezese.auth.bungee.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pl.kuezese.auth.bungee.BungeePlugin;
import pl.kuezese.auth.shared.data.ResultData;
import pl.kuezese.auth.shared.type.ResultType;

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
            handleConnection(event, connection, data, true);
            return;
        }

        Boolean cached = loginCache.getIfPresent(username);
        if (cached != null) {
            event.setCancelled(true);
            event.setCancelReason(auth.getAuthConfig().getMsgAlreadyChecking());
            auth.getLogger().info("PreLoginEvent completed for " + username + " (already checking)");
            return;
        }

        loginCache.put(username, true);
        ResultType result = auth.getPremiumManager().check(username);
        data = new ResultData(username, result,
                result == ResultType.PREMIUM ? getTimestamp(7, TimeUnit.DAYS) :
                result == ResultType.NON_PREMIUM ? getTimestamp(30, TimeUnit.MINUTES) :
                getTimestamp(10, TimeUnit.SECONDS));

        handleConnection(event, connection, data, false);
        auth.debugLog("PreLoginEvent completed for " + username);
    }

    private void handleConnection(PreLoginEvent event, PendingConnection connection, ResultData data, boolean cache) {
        ResultType result = data.getResult();
        auth.debugLog("Got result " + result.getName() + " for player " + connection.getName() + (cache ? " (cached)" : ""));
        if (!cache) {
            auth.getPremiumManager().cache(data);
            loginCache.invalidate(connection.getName());
        }
        if (result == ResultType.PREMIUM) {
            connection.setOnlineMode(true);
            auth.debugLog("PreLoginEvent completed for " + connection.getName() + " (premium)");
        } else if (result == ResultType.NON_PREMIUM) {
            connection.setOnlineMode(false);
            auth.debugLog("PreLoginEvent completed for " + connection.getName() + " (non-premium)");
        } else if (result == ResultType.ERROR) {
            event.setCancelled(true);
            event.setCancelReason(auth.getAuthConfig().getMsgFailedToCheck());
            auth.debugLog("PreLoginEvent completed for " + connection.getName() + " (error)");
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
                throw new IllegalArgumentException("Unsupported unit: " + unit);
        }

        return Timestamp.valueOf(expireTime);
    }
}
