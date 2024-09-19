package pl.kuezese.auth.shared.helper;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

public final class ChatHelper {

    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg).replace(">>", "»").replace("<<", "«");
    }

    public static BaseComponent[] colorComponent(String msg) {
        return TextComponent.fromLegacyText(color(msg));
    }
    
    public static boolean send(CommandSender sender, String msg) {
        sender.sendMessage(color(msg));
        return true;
    }
}
