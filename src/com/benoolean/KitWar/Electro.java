package com.benoolean.KitWar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

public class Electro implements Listener {

    private static String kitName = "Electro";
    private KitWar plugin = KitWar.getInstance();
    private KitData kitData = KitWar.kitData;

    private KitData.Ability ability1 = KitWar.kitData.getKitAbility(kitName, 1);
    private KitData.Ability ability2 = KitWar.kitData.getKitAbility(kitName, 2);

    @EventHandler
    public void zapClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (GameLogic.PlayerKitValidate(player, kitName)) {
            if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                // zap
                if (player.getInventory().getItemInMainHand().getType() == Material.STICK) {
                    player.getInventory().getItemInMainHand().setType(Material.RED_STAINED_GLASS_PANE);
                    List<Location> zapSpawnLoc = new ArrayList<Location>();
                    Location beamLoc = player.getLocation().add(0,1.5,0);
                    Vector beamVector = beamLoc.getDirection().normalize();

                    player.getWorld().playSound(beamLoc, Sound.ENTITY_ELDER_GUARDIAN_DEATH, 50.0F, 10.0F);
                    player.getWorld().playSound(beamLoc, Sound.ENTITY_ELDER_GUARDIAN_DEATH, 50.0F, 50.0F);
                    player.getWorld().playSound(beamLoc, Sound.ENTITY_ELDER_GUARDIAN_DEATH, 50.0F, 5.0F);
                    player.getWorld().playSound(beamLoc, Sound.ENTITY_ELDER_GUARDIAN_DEATH, 50.0F, 0.0F);

                    for (double len = 0; len < 40; len = len+0.3) {
                        double x = beamVector.getX() * len;
                        double y = beamVector.getY() * len;
                        double z = beamVector.getZ() * len;

                        beamLoc.add(x,y,z);

                        if (!(beamLoc.getBlock().getType() == Material.AIR)) {
                            zapSpawnLoc.add(beamLoc.clone());
                            break;
                        }

                        Collection<Entity> nearbyEntites = beamLoc.getWorld().getNearbyEntities(beamLoc, 1, 1, 1);
                        nearbyEntites.remove((Entity) player);
                        for (Entity entity : nearbyEntites) {
                            if (entity instanceof Player) {
                                Player victim = (Player) entity;
                                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 255));
                                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 255));
                                new BukkitRunnable() {
                                    int stuns = 10;

                                    @Override
                                    public void run() {
                                        if (stuns <= 0 ) {
                                            victim.damage(ability2.damage, player);
                                            cancel();
                                        }

                                        victim.damage(0, player);
                                        victim.setVelocity(new Vector(0,0,0));
                                        victim.getWorld().spawnParticle(Particle.SNOWBALL, victim.getLocation().add(0,2,0), 10);
                                        victim.playSound(victim.getLocation(), Sound.ENTITY_BLAZE_HURT, 3.0F, 0.533F);
                                        victim.setNoDamageTicks(0);
                                        victim.setLastDamage(0);


                                        stuns--;
                                    }

                                }.runTaskTimer(KitWar.getInstance(), 0, 1);
                            }
                            else if (entity instanceof LivingEntity) {
                                LivingEntity livingEntity = (LivingEntity) entity;
                                livingEntity.damage(900);
                            }
                        }

                        beamLoc.add(x,y,z);
                        player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, beamLoc, 0);
                        player.getWorld().spawnParticle(Particle.FLAME, beamLoc, 0);
                        beamLoc.subtract(x,y,z);
                    }
                }
            }
        }
    }

    @EventHandler
    public void turretPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (block.getType() == Material.END_ROD) {
            if (GameLogic.PlayerKitValidate(player, kitName)) {
                block.setType(Material.AIR);
                Location turretLoc = block.getLocation().add(0.5,-0.5,0.5);
                int armorticks = 80;

                ArmorStand turret_stack_1 = player.getWorld().spawn(turretLoc.clone().add(0.37,0.84,-0.43), ArmorStand.class);
                turret_stack_1.setVisible(false);
                turret_stack_1.setGravity(false);
                turret_stack_1.setInvulnerable(true);
                turret_stack_1.getEquipment().setItemInMainHand(new ItemStack(Material.BEACON));
                turret_stack_1.setRightArmPose(new EulerAngle(Math.toRadians(345), Math.toRadians(0), Math.toRadians(360)));

                ArmorStand turret_stack_2 = player.getWorld().spawn(turretLoc, ArmorStand.class);
                turret_stack_2.setVisible(false);
                turret_stack_2.setGravity(false);
                turret_stack_2.setInvulnerable(true);
                turret_stack_2.getEquipment().setHelmet(new ItemStack(Material.GLASS, 1));
                turret_stack_2.setRightArmPose(new EulerAngle(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)));
                turret_stack_2.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "Electric Turret");
                turret_stack_2.setCustomNameVisible(true);

                ArmorStand turret_stack_3 = player.getWorld().spawn(turretLoc.add(0,0.5,0), ArmorStand.class);
                turret_stack_3.setVisible(false);
                turret_stack_3.setGravity(false);
                turret_stack_3.setInvulnerable(true);
                turret_stack_3.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + Double.toString(armorticks/200 * 10));
                turret_stack_3.setCustomNameVisible(true);

                turretLoc.getWorld().playSound(turretLoc, Sound.ITEM_AXE_STRIP, 50.0F, 0.0F);

                new BukkitRunnable() {
                    double ticks = (double) armorticks;
                    @Override
                    public void run() {
                        double nameDouble = ticks-- / 20F;
                        String name = String.format("%.1f", nameDouble);

                        turret_stack_3.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + name);

                        if (ticks <= 0) {
                            Collection<Entity> nearbyEntitesFinal = turret_stack_2.getWorld().getNearbyEntities(turret_stack_3.getLocation(), 5, 5, 5);
                            nearbyEntitesFinal.remove((Entity) player);
                            for (Entity entity : nearbyEntitesFinal) {
                                if (entity instanceof Player) {
                                    Player victim = (Player) entity;
                                    victim.getWorld().strikeLightningEffect(victim.getLocation());
                                    victim.damage(3F, player);
                                    victim.setLastDamage(3F);
                                }
                            }
                            turret_stack_1.remove();
                            turret_stack_2.remove();
                            turret_stack_3.remove();
                            cancel();
                        }
                    }

                }.runTaskTimer(KitWar.getInstance(), 0, 1);

                new BukkitRunnable() {
                    double ticks = (double) armorticks;
                    @Override
                    public void run() {
                        ticks = ticks - 20;

                        Collection<Entity> nearbyEntites = turret_stack_2.getWorld().getNearbyEntities(turret_stack_3.getLocation(), 5, 5, 5);
                        nearbyEntites.remove((Entity) player);
                        for (Entity entity : nearbyEntites) {
                            if (entity instanceof Player) {
                                Player victim = (Player) entity;
                                victim.getWorld().strikeLightningEffect(victim.getLocation());
                                victim.damage(3F, player);
                                victim.setLastDamage(3F);
                            }
                        }

                        if (ticks <= 0) {
                            cancel();
                        }
                    }

                }.runTaskTimer(KitWar.getInstance(), 0, 20);
            }
        }
        else {
            event.setCancelled(true);
        }
    }
}
