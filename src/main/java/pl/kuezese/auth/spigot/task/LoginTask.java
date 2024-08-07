package pl.kuezese.auth.spigot.task;

import lombok.RequiredArgsConstructor;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kuezese.auth.shared.helper.ChatHelper;
import pl.kuezese.auth.spigot.SpigotPlugin;
import pl.kuezese.auth.spigot.helper.TitleHelper;
import pl.kuezese.auth.spigot.object.User;

@RequiredArgsConstructor
public class LoginTask extends BukkitRunnable {

    private final SpigotPlugin auth;
    private final Player player;
    private final User user;
    private int lvl = 30;
    private float exp = 1.F;
    private int i;

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();
            return;
        }

        if (user.isLogged() || user.isPremium()) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            cancel();
            return;
        }

        if (i == 0) {
            if (user.shouldAutoLogin(player)) {
                user.setLogged(true);
                user.updateLastLogin(player);
                ChatHelper.send(player, auth.getAuthConfig().getMsgSession());
                return;
            } else {
                user.setLastJoin(System.currentTimeMillis());
            }
        }

        if (i++ != 1 && !user.isPremium()) {
            if (auth.getAuthConfig().isTitleEnabled() && user.getLastJoin() + 3000L < System.currentTimeMillis()) {
                if (!user.isRegistered()) {
                    TitleHelper.title(player, "", auth.getAuthConfig().getTitleRegister(), 0, 30, 10);
                } else {
                    TitleHelper.title(player, "", auth.getAuthConfig().getTitleLogin(), 0, 30, 10);
                }
            }

            if (lvl % 5 == 0) {
                if (!user.isRegistered()) {
                    ChatHelper.send(player, auth.getAuthConfig().getMsgRegister());
                } else if (!user.isLogged()) {
                    ChatHelper.send(player, auth.getAuthConfig().getMsgLogin());
                }
            }
        }

        if (auth.getAuthConfig().isPumpkinEnabled() && !player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1200, 0));
        }

        if (auth.getAuthConfig().isExperienceBarEnabled()) {
            player.setLevel(lvl);
            player.setExp(exp);
        }

        if (lvl % 2 == 0) {
            player.playNote(player.getLocation(), Instrument.STICKS, Note.flat(1, Note.Tone.A));
        } else {
            player.playNote(player.getLocation(), Instrument.STICKS, Note.flat(1, Note.Tone.B));
        }

        if (lvl == 0) {
            player.kickPlayer(ChatHelper.color(auth.getAuthConfig().getMsgTimeLeft()));
            cancel();
        }

        --lvl;
        exp -= 0.033f;
    }
}
