package com.benoolean.KitWar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;

public class GameLogic implements Listener {

    public static HashMap<Player, Long> playerAbility1Cooldown = new HashMap<>();
    public static HashMap<Player, Long> playerAbility2Cooldown = new HashMap<>();
    public static HashMap<Player, KitData.Ability> playerLastAttemptedAbility = new HashMap<>();
    public static HashMap<Player, KitData.Kit> PlayerKitMap = new HashMap<>();

    public GameLogic() {
        KitWar plugin = KitWar.getInstance();
        CoolDownInit(plugin);
    }

    /////////////////////////////////
    // 							   //
    //           Events            //
    //							   //
    /////////////////////////////////

    @EventHandler
    public void cancelPlayerDropItemEvent(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void cancelOpenInventoryEvent(InventoryOpenEvent event) { event.setCancelled(true); }

    @EventHandler
    public void cancelClickInventoryEvent(InventoryClickEvent event) { event.setCancelled(true); }

    @EventHandler
    public void cancelMoveInventoryEvent(InventoryDragEvent event) { event.setCancelled(true); }

    @EventHandler
    public void cancelPickupItemEvent(EntityPickupItemEvent event) { event.setCancelled(true); }


    /////////////////////////////////
    // 							   //
    //     Cooldown / Ability      //
    //							   //
    /////////////////////////////////

    public void CoolDownInit(KitWar plugin) {
        new BukkitRunnable() {
            // note that 2 ticks = 0.1 second = 100 miliseconds
            final Set<Player> playerList = PlayerKitMap.keySet();

            @Override
            public void run() {
                for(Player player : playerList) {
                    if (playerLastAttemptedAbility.containsKey(player)) {
                        KitData.Ability ability = playerLastAttemptedAbility.get(player);
                        int abilityNum = ability.abilityNum;
                        if (abilityNum == 1 || abilityNum == 2) {
                            HashMap<Player, Long> playerAbilityCooldown = abilityNum == 1 ? playerAbility1Cooldown : playerAbility2Cooldown;

                            long timeDelta = (System.currentTimeMillis() - playerAbilityCooldown.get(player));

                            long cooldown = ability.cooldown;
                            SendAbilityCoolDownActionBar(player, ability, timeDelta, cooldown);
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0,2);
    }

    public static boolean AttemptAbility(Player player, KitData.Ability ability) {
        TeamScoreLogic.ScoreboardSet(player);
        playerLastAttemptedAbility.put(player, ability);

        if (!AbilityIsUnderCooldown(player, ability)) {
            return UseAbility(player, ability);
        }

        return false;
    }

    public static boolean UseAbility(Player player, KitData.Ability ability) {
        HashMap<Player, Long> playerAbilityCooldown = ability.abilityNum == 1 ? playerAbility1Cooldown : playerAbility2Cooldown;
        playerAbilityCooldown.put(player, System.currentTimeMillis());
        player.sendMessage(ChatColor.GREEN + ability.name + " activated!");

        return true;
    }

    public static boolean AbilityIsUnderCooldown(Player player, KitData.Ability ability) {
        HashMap<Player, Long> playerAbilityCooldown = (ability.abilityNum == 1 ? playerAbility1Cooldown : playerAbility2Cooldown);

        if (playerAbilityCooldown == null) {
            return true;
        }

        if (playerAbilityCooldown.get(player) == null) {
            return false;
        }

        long timeLeft = ((playerAbilityCooldown.get(player) - System.currentTimeMillis() + ability.cooldown));
        if (timeLeft > 0) {
            player.sendMessage(ChatColor.GOLD + ability.name + " under cooldown.");
            player.sendMessage(ChatColor.RED + "" + "▶  Remaing time: " + ChatColor.GOLD + "" + ChatColor.BOLD + String.format("%.1f", ((double)timeLeft) / 1000) + " sec");
            return true;
        }

        return false;
    }

    public static boolean PlayerKitValidate(Player player, String kitName) {
        KitData.Kit kit = KitWar.kitData.getKitByName(kitName);
        return PlayerKitMap.get(player) == kit;
    }

    public static void EquipKit(Player player, String kitName) {
        if (KitWar.kitData.kitNameExists(kitName)) {


            // clear player inventory and put them in the player kit mapping
            player.getInventory().clear();
            KitData.Kit playerKit = KitWar.kitData.getKitByName(kitName);
            if (playerKit != null) {
                PlayerKitMap.put(player, playerKit);
                System.out.println("=====" + playerKit);
                // give player proper items
                List<ItemStack> equipment = Arrays.asList(
                        KitWar.kitData.getKitEquipmentByName(kitName, 1),
                        KitWar.kitData.getKitEquipmentByName(kitName, 2)
                );

                int invSlot = 0;
                for (ItemStack item : equipment) {
                    player.getInventory().setItem(invSlot, item);
                    invSlot++;
                }

                // send player kit information
                KitWar.PrintSeperatorLine(player, true);
                player.sendMessage("Current Kit: " + ChatColor.AQUA + "" + ChatColor.BOLD + PlayerKitMap.get(player).name.toUpperCase());
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Passive: ");
                player.sendMessage(playerKit.passiveDescription);
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Ability 1 (" + playerKit.getAbility(1).name + "): ");
                player.sendMessage(playerKit.getAbility(1).description);
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Ability 2 (" + playerKit.getAbility(2).name + "): ");
                player.sendMessage(playerKit.getAbility(2).description);
                KitWar.PrintSeperatorLine(player, false);
                return;
            }

            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Error: Internal server error on choosing kit.");
            return;
        }

        kitName = ChatColor.RESET + "" + ChatColor.GOLD + kitName + ChatColor.RED + "" + ChatColor.BOLD;
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Error: Kit " + kitName + " doesn't exist!");
    }

    /////////////////////////////////
    // 							   //
    //      Action Bar / Title     //
    //							   //
    /////////////////////////////////

    public void SendAbilityCoolDownActionBar(Player player, KitData.Ability ability, long timeDelta, long cooldown) {
        if (timeDelta >= cooldown) {
            String abilityReady = ChatColor.GREEN + "" + ChatColor.BOLD + ability.name + " READY";
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(abilityReady));
            return;
        }

        String bar = "▋";
        String timeDeltaString = ChatColor.GREEN + "" + ChatColor.BOLD + String.format("%.1f", ((double) ((cooldown - timeDelta)/100)) / 10) + "s";
        int totalBarCount = 20;

        int greenBarCount = Math.abs(Math.round((float) timeDelta / (float) cooldown * totalBarCount));
        int redBarCount = totalBarCount - greenBarCount;

        String greenBar = ChatColor.GREEN + new String(new char[greenBarCount]).replace("\0", bar);
        String redBar = "";
        if (redBarCount > 0) {
            redBar = ChatColor.RED + new String(new char[redBarCount]).replace("\0", bar);
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                ability.name + " " + greenBar + redBar + "  " + timeDeltaString));
    }
}
