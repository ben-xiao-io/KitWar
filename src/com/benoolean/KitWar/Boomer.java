package com.benoolean.KitWar;

import java.util.Collection;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


public class Boomer implements Listener {

    private static String kitName = "Boomer";
    private KitWar plugin = KitWar.getInstance();
    private KitData kitData = KitWar.kitData;

    private KitData.Ability ability1 = KitWar.kitData.getKitAbility(kitName, 1);
    private KitData.Ability ability2 = KitWar.kitData.getKitAbility(kitName, 2);


    @EventHandler
    public void EggBombThrow(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (GameLogic.PlayerKitValidate(player, kitName)) {
            if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (player.getInventory().getItemInMainHand().getType() == Material.EGG) {

                    GameLogic.AttemptAbility(player, this.ability1, GameLogic.AbilityIsUnderCooldown(player, 1));
                    String test = GameLogic.AbilityIsUnderCooldown(player, 1) ? "true" : "false";
                    player.sendMessage(test);
                }
            }
        }
    }

    @EventHandler
    public void EggBombHit(ProjectileHitEvent event) {
        if(event.getEntity().getShooter() instanceof Player) {
            if (event.getEntity().getType().equals(EntityType.EGG)) {

                Player player = (Player) event.getEntity().getShooter();

                if (GameLogic.PlayerKitValidate(player, kitName)) {
                    Egg egg = (Egg) event.getEntity();
                    Location eggLoc = egg.getLocation();

                    player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, eggLoc, 0);
                    player.getWorld().playSound(eggLoc, Sound.ENTITY_GENERIC_EXPLODE, 3.0F, 0.533F);

                    Explode(player, egg, 3F, ability1.damage);
                }
            }
        }

        return;
    }

    @EventHandler
    public void TntPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (GameLogic.PlayerKitValidate(player, kitName)) {
            if (block.getType() == Material.TNT) {
                block.setType(Material.AIR);
                Location blockLoc = block.getLocation();

                SpawnTnt(player,blockLoc, 40, this.ability2.damage);
                GameLogic.AttemptAbility(player, this.ability2, GameLogic.AbilityIsUnderCooldown(player, 2));
            }
        }
    }

    public void SpawnTnt(Player player, Location placedLoc, int tntTicks, double tntDamage) {
        final Entity tnt = player.getWorld().spawn(placedLoc, TNTPrimed.class);

        tnt.setCustomNameVisible(true);

        new BukkitRunnable() {
            double ticks = (double) tntTicks;

            @Override
            public void run() {
                double nameDouble = ticks-- / 20F;
                String name = String.format("%.1f", nameDouble);

                tnt.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + name);

                if (ticks <= 0) {
                    if (!(tnt.isDead())) {
                        Explode(player, tnt, 5F, tntDamage);
                        tnt.remove();
                    }

                    cancel();
                }
            }

        }.runTaskTimer(KitWar.getInstance(), 0, 1);
    }

    public void SpawnTntZombiePassive(Player player, Player victim, int tntTicks, double tntDamage) {
        final Zombie zombie = (Zombie) victim.getWorld().spawnEntity(victim.getLocation(), EntityType.ZOMBIE);

        zombie.setTarget(victim);
        zombie.getEquipment().setHelmet(new ItemStack(Material.GOLDEN_HELMET, 1));
        zombie.getEquipment().setItemInMainHand(new ItemStack(Material.TNT, 1));
        zombie.setHealth(1F);
        zombie.setBaby(true);

        zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 8, 255));

        zombie.setCustomNameVisible(true);

        new BukkitRunnable() {
            double ticks = (double) tntTicks;

            @Override
            public void run() {
                double nameDouble = ticks-- / 20F;
                String name = String.format("%.1f", nameDouble);

                zombie.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + name);

                if (ticks <= 0) {
                    if (!(zombie.isDead())) {
                        Explode(player, zombie, 5F, tntDamage);
                        zombie.remove();
                    }

                    cancel();
                }
            }

        }.runTaskTimer(KitWar.getInstance(), 0, 1);
    }

    public void Explode(Player player, Entity explosiveEntity, double radius, double tntDamage) {

        Location loc = explosiveEntity.getLocation();

        if (radius <= 3) {
            player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 0);
        }
        else {
            player.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 0);
        }

        player.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 3.0F, 0.533F);

        int playerHit = 0;
        double velocityMultiplier = 1F;
        Collection<Entity> nearByEntities = explosiveEntity.getWorld().getNearbyEntities(explosiveEntity.getLocation(), radius, radius, radius);
        // nearByEntities.remove(player);
        for (Entity entity : nearByEntities) {
            if (entity instanceof Player) {
                Player victim = (Player) entity;

                Location victimLoc = victim.getLocation();
                double launchVelocityX = victimLoc.getX() - loc.getX();
                double launchVelocityZ = victimLoc.getZ() - loc.getZ();

                Vector launchVector = new Vector(launchVelocityX, 0, launchVelocityZ).normalize().multiply(velocityMultiplier);
                launchVector.normalize().multiply(velocityMultiplier);
                launchVector.setY(0.8F);
                victim.sendMessage(Double.toString(launchVelocityX) + "    " + Double.toString(launchVelocityZ));

                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10, 1));
                victim.damage(tntDamage, player);
                victim.setLastDamage(tntDamage);
                if (Double.isFinite(launchVector.getX()) && Double.isFinite(launchVector.getZ())) {
                    victim.setVelocity(launchVector);
                }

                 playerHit++;
                 if (playerHit >= 3 || true) {
                    SpawnTntZombiePassive(player, victim, 60, 3F);
                 }
            }
        }
    }

    @EventHandler
    public void EggSpawnDisable(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == SpawnReason.EGG) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void ZombieDamageDisable(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity victim = event.getEntity();

        if (attacker instanceof Zombie && victim instanceof Player) {
            event.setCancelled(true);
        }
    }
}
