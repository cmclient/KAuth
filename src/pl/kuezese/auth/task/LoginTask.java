package pl.kuezese.auth.task;

import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kuezese.auth.AuthPlugin;
import pl.kuezese.auth.helper.ChatHelper;
import pl.kuezese.auth.object.User;

public class LoginTask extends BukkitRunnable {

    private final AuthPlugin auth;
    private final Player player;
    private final User user;
    private int lvl;
    private float exp;

    public LoginTask(AuthPlugin auth, Player player, User user) {
        this.auth = auth;
        this.player = player;
        this.user = user;
        this.lvl = 30;
        this.exp = 1.F;
    }

    @Override
    public void run() {
        if (!this.player.isOnline()) {
            this.cancel();
            return;
        }

        if (this.user.isLogged()) {
            this.player.removePotionEffect(PotionEffectType.BLINDNESS);
            this.cancel();
            return;
        }

        if (this.auth.getConfiguration().pumpkinEnabled && !this.player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            this.player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1200, 0));
        }

        if (this.auth.getConfiguration().titleEnabled && this.user.getLastJoin() + 3000L < System.currentTimeMillis()) {
            if (!this.user.isRegistered()) {
                ChatHelper.title(this.player, "", this.auth.getConfiguration().titleRegister, 0, 30, 10);
            } else {
                ChatHelper.title(this.player, "", this.auth.getConfiguration().titleLogin, 0, 30, 10);
            }
        }

        if (this.auth.getConfiguration().experienceBarEnabled) {
            this.player.setLevel(this.lvl);
            this.player.setExp(this.exp);
        }

        if (this.lvl % 5 == 0) {
            if (!this.user.isRegistered()) {
                ChatHelper.send(this.player, this.auth.getConfiguration().register);
            } else if (!user.isLogged()) {
                ChatHelper.send(this.player, this.auth.getConfiguration().login);
            }
        }

        if (this.lvl % 2 == 0) {
            this.player.playNote(player.getLocation(), Instrument.STICKS, Note.flat(1, Note.Tone.A));
        } else {
            this.player.playNote(player.getLocation(), Instrument.STICKS, Note.flat(1, Note.Tone.B));
        }

        if (this.lvl == 0) {
            this.player.kickPlayer(ChatHelper.color(this.auth.getConfiguration().time_left));
            this.cancel();
        }

        --this.lvl;
        this.exp -= 0.033f;
    }
}
