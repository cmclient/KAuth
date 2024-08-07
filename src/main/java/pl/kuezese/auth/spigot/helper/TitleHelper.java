package pl.kuezese.auth.spigot.helper;

import com.connorlinfoot.titleapi.TitleAPI;
import org.bukkit.entity.Player;

public class TitleHelper {

    public static void title(Player p, String up, String down, int i, int j, int k) {
        TitleAPI.sendTitle(p, i, j, k, up, down);
    }
}
