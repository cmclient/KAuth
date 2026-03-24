package pl.kuezese.auth.spigot.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.potion.PotionEffectType;
import pl.kuezese.auth.shared.helper.ChatHelper;
import pl.kuezese.auth.spigot.SpigotPlugin;
import pl.kuezese.auth.spigot.config.Config;
import pl.kuezese.auth.spigot.object.User;
import pl.kuezese.auth.spigot.task.LoginTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {

    private final Pattern pattern = Pattern.compile("^[0-9a-zA-Z-_]+$");
    private static final Pattern AS_NAME_PATTERN = Pattern.compile("\"as_name\"\\s*:\\s*\"([^\"]*)\"");
    private static final long PROVIDER_CACHE_TTL_MS = 24L * 60L * 60L * 1000L;

    private final Map<String, ProviderCacheEntry> providerCache = new HashMap<>();
    private final SpigotPlugin auth;

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (!pattern.matcher(player.getName()).find()) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatHelper.color(auth.getAuthConfig().getMsgInvalidCharacters()));
            return;
        }
        User user = auth.getUserManager().getIgnoreCase(player.getName());
        if (user != null && !user.getName().equals(player.getName())) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatHelper.color(auth.getAuthConfig().getMsgCorrectUsername().replace("{NAME}", user.getName())));
            return;
        }

        if (auth.getAuthConfig().isStaffProtectionEnabled()) {
            Config.StaffProtectionUser protection = auth.getAuthConfig()
                    .getStaffProtectionUsers()
                    .get(player.getName().toLowerCase(Locale.ROOT));

            if (protection != null) {
                String ip = event.getAddress() == null ? "" : event.getAddress().getHostAddress();
                StaffAccessResult result = evaluateStaffAccess(ip, protection);
                if (result != StaffAccessResult.ALLOW) {
                    String message = result == StaffAccessResult.DENY_LOOKUP_FAILED
                            ? auth.getAuthConfig().getMsgStaffProtectionLookupFailed()
                            : auth.getAuthConfig().getMsgStaffProtectionDenied();
                    event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatHelper.color(message));
                    return;
                }
            }
        }

        if (auth.getAuthConfig().getMaxAccounts() != 0 && (user == null || !user.isRegistered()) && auth.getUserManager().getByIp(event.getAddress().getHostAddress()) >= auth.getAuthConfig().getMaxAccounts()) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatHelper.color(auth.getAuthConfig().getMsgMaxAccounts()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        User user = auth.getUserManager().get(player.getName());
        if (user == null) {
            user = auth.getUserManager().create(player);
        }

        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.setLevel(0);
        player.setExp(0.0F);

        user.setLastJoin(Timestamp.from(Instant.now()));

        if (auth.getAuthConfig().isPremiumAuth())
            return;

        if (auth.getAuthConfig().isSessionsEnabled() && user.shouldAutoLogin(player)) {
            user.setLogged(true);
            user.updateLastLogin(player);
            ChatHelper.send(player, auth.getAuthConfig().getMsgSession());
            return;
        }

        new LoginTask(auth, player, user).runTaskTimer(auth, 5L, 20L);
    }

    private StaffAccessResult evaluateStaffAccess(String ip, Config.StaffProtectionUser protection) {
        List<String> allowedIps = protection.getAllowedIpAddresses();
        List<String> allowedProviders = protection.getAllowedInternetProviders();

        boolean hasIpRules = allowedIps != null && !allowedIps.isEmpty();
        boolean hasProviderRules = allowedProviders != null && !allowedProviders.isEmpty();

        if (!hasIpRules && !hasProviderRules) {
            return StaffAccessResult.DENY;
        }

        boolean ipMatch = hasIpRules && isAllowedIp(ip, allowedIps);
        boolean providerMatch = false;

        if (hasProviderRules) {
            ProviderLookupResult lookup = resolveProvider(ip);
            if (!lookup.resolved) {
                return StaffAccessResult.DENY_LOOKUP_FAILED; // failed to resolve ipinfo => disallow
            }
            providerMatch = isProviderAllowed(lookup.provider, allowedProviders);
        }

        return (ipMatch || providerMatch) ? StaffAccessResult.ALLOW : StaffAccessResult.DENY;
    }

    private boolean isAllowedIp(String ip, List<String> allowedIps) {
        if (ip == null || ip.isEmpty()) return false;
        for (String allowedIp : allowedIps) {
            if (allowedIp != null && ip.equalsIgnoreCase(allowedIp.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean isProviderAllowed(String provider, List<String> allowedProviders) {
        if (provider == null || provider.isEmpty()) return false;
        for (String allowedProvider : allowedProviders) {
            if (allowedProvider != null && provider.equalsIgnoreCase(allowedProvider.trim())) {
                return true;
            }
        }
        return false;
    }

    private ProviderLookupResult resolveProvider(String ip) {
        if (ip == null || ip.isEmpty()) return ProviderLookupResult.unresolved();

        long now = System.currentTimeMillis();
        ProviderCacheEntry cached = providerCache.get(ip);
        if (cached != null && cached.expiresAt > now) {
            return ProviderLookupResult.resolved(cached.provider);
        }

        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://ipinfo.spacehost.ovh/api/v2/lookup?ip=" + ip);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() != 200) return ProviderLookupResult.unresolved();

            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }

            Matcher matcher = AS_NAME_PATTERN.matcher(body.toString());
            String provider = matcher.find() ? matcher.group(1) : null;

            providerCache.put(ip, new ProviderCacheEntry(provider, now + PROVIDER_CACHE_TTL_MS));
            return ProviderLookupResult.resolved(provider);
        } catch (Exception ignored) {
            return ProviderLookupResult.unresolved();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private enum StaffAccessResult {
        ALLOW,
        DENY,
        DENY_LOOKUP_FAILED
    }

    private static class ProviderLookupResult {
        private final boolean resolved;
        private final String provider;

        private ProviderLookupResult(boolean resolved, String provider) {
            this.resolved = resolved;
            this.provider = provider;
        }

        private static ProviderLookupResult resolved(String provider) {
            return new ProviderLookupResult(true, provider);
        }

        private static ProviderLookupResult unresolved() {
            return new ProviderLookupResult(false, null);
        }
    }

    private static class ProviderCacheEntry {
        private final String provider;
        private final long expiresAt;

        private ProviderCacheEntry(String provider, long expiresAt) {
            this.provider = provider;
            this.expiresAt = expiresAt;
        }
    }
}
