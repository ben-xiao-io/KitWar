package com.benoolean.KitWar;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class TabCompleteHandler implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("team")) {
            if (args.length == 1) {
                List<String> teamNameList = new ArrayList<>();

                teamNameList.add("Red");
                teamNameList.add("Blue");
                return teamNameList;
            }
        }
        else if (label.equalsIgnoreCase("kit")) {
            if (args.length == 1) {
                Set<String> kitMapSet = KitWar.kitData.kitMap.keySet();

                List<String> kitNameList = new ArrayList<>();
                for (String kitName : kitMapSet) {
                    kitNameList.add(kitName);
                }
                return kitNameList;
            }
        }

        return null;
    }
}
