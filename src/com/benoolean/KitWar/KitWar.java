package com.benoolean.KitWar;
import java.util.HashMap;
import java.util.Set;

import net.minecraft.server.v1_16_R1.ChatMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class KitWar extends JavaPlugin implements Listener {

    // player list
    public static HashMap<Player, Integer> PlayerScoreMap = new HashMap<Player, Integer>();
    public static HashMap<Player, String> TeamMap = new HashMap<Player, String>();
    public static HashMap<Player, Integer> TeamScoreMap = new HashMap<Player, Integer>();

    public static KitData kitData;

    private static KitWar instance;

    public static KitWar getInstance() { return instance; }

    @Override
    public void onEnable() {
        kitData = new KitData(this);
        instance = this;

        this.getServer().getPluginManager().registerEvents(new GameLogic(), this);
        this.getServer().getPluginManager().registerEvents(new Boomer(), this);
        this.getServer().getPluginManager().registerEvents(new Electro(), this);
    }

    @Override
    public void onDisable() {

    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("kit")) {
            if (sender instanceof Player) {

                Player player = (Player) sender;

                if (args.length == 0) {
                    ChatClear(player);

                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Choose a kit!");
                    Set<String> kitNames = kitData.kitMap.keySet();

                    PrintSeperatorLine(player, true);
                    for (String kitName: kitNames) {
                        TextComponent messageKit = new TextComponent("▶  " + kitName + "  ◀");
                        messageKit.setColor(ChatColor.GOLD);
                        messageKit.setBold(true);
                        messageKit.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/kit " + kitName));
                        messageKit.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.YELLOW + "" + ChatColor.BOLD + "Click to get the " + kitName.toUpperCase() +" kit")));
                        player.spigot().sendMessage(messageKit);
                    }
                    PrintSeperatorLine(player, false);
                }
                else if (args.length == 1) {
                    String kitArg = args[0];

                    if (kitData.kitNameExists(kitArg)) {
                        GameLogic.EquipKit(player, kitArg);
                    }
                }
            }
            else {
                sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Console working!");
                return true;
            }
        }

        return false;
    }



    /////////////////////////////////
    // 							   //
    //         Chat Control        //
    //							   //
    /////////////////////////////////


    public static void ChatClear(Player player) {
        for (int i = 0; i < 30; i++) {
            player.sendMessage("");
        }
    }

    public static void PrintSeperatorLine(Player player, boolean startHeader) {
        if (startHeader) {
            player.sendMessage("▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
            player.sendMessage("");
            return;
        }

        player.sendMessage("");
        player.sendMessage("▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
    }
}
