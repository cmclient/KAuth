package pl.kuezese.auth.shared.task;

import lombok.RequiredArgsConstructor;
import pl.kuezese.auth.shared.database.SQL;
import pl.kuezese.auth.shared.manager.PremiumManager;

@RequiredArgsConstructor
public class RemoveExpiredTask implements Runnable {

    private final SQL sql;
    private final PremiumManager premiumManager;

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        sql.updateAsync("DELETE FROM `premium_cache` WHERE `expire_date` < NOW();");
        premiumManager.getCache().entrySet().removeIf(entry -> entry.getValue().getExpireDate().getTime() < now);
    }
}
