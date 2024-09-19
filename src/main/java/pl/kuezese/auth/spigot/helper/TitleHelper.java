package pl.kuezese.auth.spigot.helper;

import com.connorlinfoot.titleapi.TitleAPI;
import org.bukkit.entity.Player;

public class TitleHelper {

    public static void title(Player player, String up, String down, int fadeIn, int stay, int fadeOut) {
        TitleAPI.sendTitle(player, fadeIn, stay, fadeOut, up, down);
    }
}
