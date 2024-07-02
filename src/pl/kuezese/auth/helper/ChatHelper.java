package pl.kuezese.auth.helper;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class ChatHelper {

    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg).replace(">>", "»").replace("<<", "«");
    }

    public static void title(Player p, String up, String down, int i, int j, int k) {
        IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + color(up) + "\"}");
        IChatBaseComponent chatSubTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + color(down) + "\"}");
        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
        PacketPlayOutTitle subtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, chatSubTitle);
        PacketPlayOutTitle length = new PacketPlayOutTitle(i, j, k);
        ((CraftPlayer)p).getHandle().playerConnection.sendPacket(title);
        ((CraftPlayer)p).getHandle().playerConnection.sendPacket(subtitle);
        ((CraftPlayer)p).getHandle().playerConnection.sendPacket(length);
    }
    
    public static boolean send(CommandSender p, String msg) {
        p.sendMessage(color(msg));
        return true;
    }
}
