package com.benoolean.KitWar;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class CommandHandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("team")) {
            Player player = (Player) sender;
            if (sender instanceof Player) {
                if (args.length == 0) {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Choose a team by doing /team red or /team blue");
                }
                else if (args.length == 1) {
                    String teamArg = args[0];
                    TeamScoreLogic.JoinTeam(player, teamArg);
                }
            }
        }
        else if (label.equalsIgnoreCase("kit")) {
            if (sender instanceof Player) {

                Player player = (Player) sender;

                if (args.length == 0) {
                    KitWar.ChatClear(player);

                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Choose a kit!");
                    Set<String> kitNames = KitWar.kitData.kitMap.keySet();

                    KitWar.PrintSeperatorLine(player, true);
                    for (String kitName: kitNames) {
                        TextComponent messageKit = new TextComponent("▶  " + kitName + "  ◀");
                        messageKit.setColor(ChatColor.GOLD);
                        messageKit.setBold(true);
                        messageKit.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/kit " + kitName));
                        messageKit.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.YELLOW + "" + ChatColor.BOLD + "Click to get the " + kitName.toUpperCase() +" kit")));
                        player.spigot().sendMessage(messageKit);
                    }
                    KitWar.PrintSeperatorLine(player, false);
                }
                else if (args.length == 1) {
                    String kitArg = args[0];
                    GameLogic.EquipKit(player, kitArg);
                }
            }
            else {
                sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Console working!");
                return true;
            }
        }

        return false;
    }
}
