package pl.kuezese.auth.velocity.listener;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.InboundConnection;
import lombok.RequiredArgsConstructor;
import pl.kuezese.auth.shared.data.ResultData;
import pl.kuezese.auth.shared.type.ResultType;
import pl.kuezese.auth.velocity.VelocityPlugin;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class LoginListener {

    private final VelocityPlugin plugin;
    private final Cache<String, Boolean> loginCache = Caffeine.newBuilder()
            .expireAfterWrite(9, TimeUnit.SECONDS)
            .build();

    @Subscribe
    public void onLogin(PreLoginEvent event) {
        InboundConnection connection = event.getConnection();
        String username = event.getUsername();

        if (loginCache.estimatedSize() > 9) {
            plugin.getLogger().warn("Mojang API rate limit exceeded, kicking player " + username);
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(plugin.getAuthConfig().getMsgTooManyPlayers()));
            return;
        }

        ResultData data = plugin.getPremiumManager().getCache().get(username);
        if (data != null) {
            handleConnection(event, connection, data, true);
            return;
        }

        Boolean cached = loginCache.getIfPresent(username);
        if (cached != null) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(plugin.getAuthConfig().getMsgAlreadyChecking()));
            plugin.getLogger().info("PreLoginEvent completed for " + username + " (already checking)");
            return;
        }

        loginCache.put(username, true);
        ResultType result = plugin.getPremiumManager().check(username);
        data = new ResultData(username, result,
                result == ResultType.PREMIUM ? getTimestamp(7, TimeUnit.DAYS) :
                result == ResultType.NON_PREMIUM ? getTimestamp(30, TimeUnit.MINUTES) :
                getTimestamp(10, TimeUnit.SECONDS));

        handleConnection(event, connection, data, false);
        plugin.debugLog("PreLoginEvent completed for " + username);
    }

    private void handleConnection(PreLoginEvent event, InboundConnection connection, ResultData data, boolean cache) {
        ResultType result = data.getResult();
        plugin.debugLog("Got result " + result.getName() + " for player " + event.getUsername() + (cache ? " (cached)" : ""));
        if (!cache) {
            plugin.getPremiumManager().cache(data);
            loginCache.invalidate(event.getUsername());
        }
        if (result == ResultType.PREMIUM) {
            // TODO: In Velocity, there's no direct equivalent to setting online mode; this logic may need to be adjusted.
            plugin.debugLog("PreLoginEvent completed for " + event.getUsername() + " (premium)");
        } else if (result == ResultType.NON_PREMIUM) {
            // TODO: Similarly, this might need adjustment as well.
            plugin.debugLog("PreLoginEvent completed for " + event.getUsername() + " (non-premium)");
        } else if (result == ResultType.ERROR) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(plugin.getAuthConfig().getMsgFailedToCheck()));
            plugin.debugLog("PreLoginEvent completed for " + event.getUsername() + " (error)");
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
