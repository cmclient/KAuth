package pl.kuezese.auth.bungee.task;

import lombok.RequiredArgsConstructor;
import pl.kuezese.auth.bungee.BungeePlugin;

@RequiredArgsConstructor
public class RemoveExpiredTask implements Runnable {

    private final BungeePlugin auth;

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        auth.getPremiumManager().getCache().entrySet().removeIf(entry -> entry.getValue().getExpireDate().getTime() < now);
        auth.getSql().updateAsync("DELETE FROM `premium_cache` WHERE `expire_date` < NOW();");
    }
}
