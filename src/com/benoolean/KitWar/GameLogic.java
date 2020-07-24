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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import org.bukkit.scheduler.BukkitRunnable;

public class GameLogic implements Listener {

    private KitWar plugin = KitWar.getInstance();
    private KitData kitData = KitWar.kitData;

    public static HashMap<Player, Long> playerAbility1Cooldown = new HashMap<>();
    public static HashMap<Player, Long> playerAbility2Cooldown = new HashMap<>();
    public static HashMap<Player, Long> playerLastUsedAbilityCooldown = new HashMap<>();
    public static HashMap<Player, String> PlayerKitMap = new HashMap<>();

    public GameLogic() {
        coolDownInit(plugin);
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

    public void coolDownInit(KitWar plugin) {
        new BukkitRunnable() {
            // note that 2 ticks = 0.1 second = 100 miliseconds
            Set<Player> playerList = PlayerKitMap.keySet();

            @Override
            public void run() {
                for(Player player : playerList) {
                    int inactiveAbilityNum = player.getInventory().getHeldItemSlot();
                    HashMap<Player, Long> playerAbilityCooldown =
                            ((inactiveAbilityNum == 0) ? playerAbility1Cooldown :
                                    (inactiveAbilityNum == 1) ? playerAbility2Cooldown : playerLastUsedAbilityCooldown);

                    // action bar'
                    if (playerLastUsedAbilityCooldown.containsKey(player)) {
                        long timeDelta = 0;
                        timeDelta = (System.currentTimeMillis() - playerAbilityCooldown.get(player));

                        KitData.Kit kit = kitData.getPlayerKit(player);
                        long cooldown = kit.getAbility(1).cooldown; // find a way to make this dyanmic instead of just 1
                        sendAbilityCoolDownActionBar(player, timeDelta, cooldown);
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0,2);
    }

    public static void usedAbility(Player player, int abilityNum) {
        if (abilityNum == 1) {
            playerAbility1Cooldown.put(player, System.currentTimeMillis());
        }
        else if (abilityNum == 2) {
            playerAbility2Cooldown.put(player, System.currentTimeMillis());
        }
        playerLastUsedAbilityCooldown.put(player, System.currentTimeMillis());

        player.sendMessage(ChatColor.GREEN + (abilityNum == 1 ? "First" : "Second") + " activated!");
    }

    public static boolean abilityIsUnderCooldown(Player player, int abilityNum) {
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

    public static boolean playerKitValidate(Player player, String kit) {
        return PlayerKitMap.get(player).equalsIgnoreCase(kit);
    }

    public static void equipKit(Player player, String kitName) {
        // clear player inventory and put them in the player kit mapping
        player.getInventory().clear();

        if (PlayerKitMap.get(player) != null) {
            PlayerKitMap.remove(player);
        }

        PlayerKitMap.put(player, kitName);

        player.sendMessage("aight");

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

        player.sendMessage("Current Kit: " + ChatColor.AQUA + "" + ChatColor.BOLD + PlayerKitMap.get(player).toUpperCase());
    }

    /////////////////////////////////
    // 							   //
    //      Action Bar / Title     //
    //							   //
    /////////////////////////////////

    public void sendAbilityCoolDownActionBar(Player player, long timeDelta, long cooldown) {
        if (timeDelta >= cooldown) {
            String abilityReady = ChatColor.GREEN + "" + ChatColor.BOLD + "Ability READY";
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(abilityReady));
            return;
        }

        String bar = "█";
        String timeDeltaString = ChatColor.GREEN + "" + ChatColor.BOLD + String.format("%.1f", ((double) ((cooldown - timeDelta)/100)) / 10) + "s";

        int greenBarCount = Math.abs(Math.round((float) timeDelta / (float) cooldown * 10));
        int redBarCount = 10 - greenBarCount;

        String greenBar = ChatColor.GREEN + new String(new char[greenBarCount]).replace("\0", bar);
        String redBar = "";
        if (redBarCount > 0) {
            redBar = ChatColor.RED + new String(new char[redBarCount]).replace("\0", bar);
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(greenBar+redBar + "  " + timeDeltaString));
    }
}
