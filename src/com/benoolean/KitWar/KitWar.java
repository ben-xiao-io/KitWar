package com.benoolean.KitWar;
import java.util.Set;

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

    public static KitData kitData;

    private static KitWar instance;

    public static KitWar getInstance() { return instance; }

    @Override
    public void onEnable() {
        kitData = new KitData(this);
        instance = this;

        this.getServer().getPluginManager().registerEvents(new GameLogic(), this);
        this.getServer().getPluginManager().registerEvents(new TeamScoreLogic(), this);
        this.getServer().getPluginManager().registerEvents(new Boomer(), this);
        this.getServer().getPluginManager().registerEvents(new Electro(), this);

        this.getCommand("kit").setExecutor(new CommandHandler());
        this.getCommand("kit").setTabCompleter(new TabCompleteHandler());
        this.getCommand("team").setExecutor(new CommandHandler());
        this.getCommand("team").setTabCompleter(new TabCompleteHandler());
    }

    @Override
    public void onDisable() {

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
