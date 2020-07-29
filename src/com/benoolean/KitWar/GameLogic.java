package com.benoolean.KitWar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import org.bukkit.scheduler.BukkitRunnable;

public class GameLogic implements Listener {

    private KitWar plugin = KitWar.getInstance();

    public static HashMap<Player, Long> playerAbility1Cooldown = new HashMap<>();
    public static HashMap<Player, Long> playerAbility2Cooldown = new HashMap<>();
    public static HashMap<Player, KitData.Ability> playerLastAttemptedAbility = new HashMap<>();
    public static HashMap<Player, String> PlayerKitMap = new HashMap<>();

    public GameLogic() {
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


    /////////////////////////////////
    // 							   //
    //     Cooldown / Ability      //
    //							   //
    /////////////////////////////////

    public void CoolDownInit(KitWar plugin) {
        new BukkitRunnable() {
            // note that 2 ticks = 0.1 second = 100 miliseconds
            Set<Player> playerList = PlayerKitMap.keySet();

            @Override
            public void run() {
                for(Player player : playerList) {
                    int inactiveAbilityNum = player.getInventory().getHeldItemSlot();
                    if (playerLastAttemptedAbility.containsKey(player)) {
                        KitData.Ability ability = playerLastAttemptedAbility.get(player);
                        int abilityNum = ability.abilityNum;
                        if (abilityNum == 1 || abilityNum == 2) {
                            HashMap<Player, Long> playerAbilityCooldown = abilityNum == 1 ? playerAbility1Cooldown : playerAbility2Cooldown;

                            long timeDelta = 0;
                            timeDelta = (System.currentTimeMillis() - playerAbilityCooldown.get(player));

                            long cooldown = ability.cooldown;
                            SendAbilityCoolDownActionBar(player, ability, timeDelta, cooldown);
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0,2);
    }

    public static void AttemptAbility(Player player, KitData.Ability ability, boolean abilityIsUnderCoolDown) {
        playerLastAttemptedAbility.put(player, KitWar.kitData.getKitAbility(PlayerKitMap.get(player), ability.abilityNum));

        if (!abilityIsUnderCoolDown) {
            UseAbility(player, ability);
        }
    }

    public static void UseAbility(Player player, KitData.Ability ability) {
        HashMap<Player, Long> playerAbilityCooldown = ability.abilityNum == 1 ? playerAbility1Cooldown : playerAbility2Cooldown;
        playerAbilityCooldown.put(player, System.currentTimeMillis());
        player.sendMessage(ChatColor.GREEN + (ability.abilityNum == 1 ? "First" : "Second") + " activated!");
    }

    public static boolean AbilityIsUnderCooldown(Player player, int abilityNum) {
        String playerKitName = PlayerKitMap.get(player);
        KitData.Ability ability = KitWar.kitData.getKitAbility(playerKitName, abilityNum);
        long abilityCooldown = ability.cooldown;

        long timeLeft = -1;

        if (abilityNum == 1 && playerAbility1Cooldown.containsKey(player)) {
            timeLeft = ((playerAbility1Cooldown.get(player) - System.currentTimeMillis() + abilityCooldown));
        }
        else if (abilityNum == 2 && playerAbility2Cooldown.containsKey(player)) {
            timeLeft = ((playerAbility2Cooldown.get(player) - System.currentTimeMillis() + abilityCooldown));
        }

        if (timeLeft > 0) {
            player.sendMessage(ChatColor.GOLD + (abilityNum == 1 ? "First" : "Second") + " ability under cooldown.");
            player.sendMessage(ChatColor.RED + "" + "▶  Remaing time: " + ChatColor.GOLD + "" + ChatColor.BOLD + String.format("%.1f", ((double)timeLeft) / 1000) + " sec");
            return true;
        }

        return false;
    }

    public static boolean PlayerKitValidate(Player player, String kit) {
        return PlayerKitMap.get(player).equalsIgnoreCase(kit);
    }

    public static void EquipKit(Player player, String kitName) {
        // clear player inventory and put them in the player kit mapping
        player.getInventory().clear();
        KitData.Kit playerKit = KitWar.kitData.getKitByName(kitName);
        if (playerKit != null) {
            PlayerKitMap.put(player, kitName);
            // give player proper items
            List<ItemStack> equipment = Arrays.asList(
                    KitWar.kitData.getKitEquipmentByName(kitName, 1),
                    KitWar.kitData.getKitEquipmentByName(kitName, 2)
            );

            int invSlot = 0;
            for (ItemStack item : equipment) {
                System.out.println(item);
                player.getInventory().setItem(invSlot, item);
                invSlot++;
            }

            // send player kit information
            KitWar.PrintSeperatorLine(player, true);
            player.sendMessage("Current Kit: " + ChatColor.AQUA + "" + ChatColor.BOLD + PlayerKitMap.get(player).toUpperCase());
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Passive: ");
            player.sendMessage(playerKit.passiveDescription);
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Ability 1 (" + playerKit.getAbility(1).name + "): ");
            player.sendMessage(playerKit.getAbility(1).description);
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD +"Ability 2 (" + playerKit.getAbility(2).name + "): ");
            player.sendMessage(playerKit.getAbility(2).description);
            KitWar.PrintSeperatorLine(player, false);
            return;
        }

        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Error: Internal server error on choosing kit.");
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
