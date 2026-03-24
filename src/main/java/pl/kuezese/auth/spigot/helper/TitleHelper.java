package pl.kuezese.auth.spigot.helper;

import com.connorlinfoot.titleapi.TitleAPI;
import org.bukkit.entity.Player;
import pl.kuezese.auth.shared.helper.ChatHelper;

public class TitleHelper {

    public static void title(Player player, String up, String down, int fadeIn, int stay, int fadeOut) {
        TitleAPI.sendTitle(player, fadeIn, stay, fadeOut, ChatHelper.color(up), ChatHelper.color(down));
    }
}
