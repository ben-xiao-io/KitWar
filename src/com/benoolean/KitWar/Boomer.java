package com.benoolean.KitWar;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

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
import org.bukkit.inventory.Inventory;
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
    public void boomerEggBombThrow(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (GameLogic.playerKitValidate(player, kitName)) {
            if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (player.getInventory().getItemInMainHand().getType() == Material.EGG) {

                    if (GameLogic.abilityIsUnderCooldown(player, 1)) {
                        GameLogic.attemptAbility(player, this.ability1, false);
                        event.setCancelled(true);
                        return;
                    }

                    GameLogic.attemptAbility(player, this.ability1, true);
                }
            }
        }
    }

    @EventHandler
    public void boomerEggBombHit(ProjectileHitEvent event) {
        if(event.getEntity().getShooter() instanceof Player) {
            if (event.getEntity().getType().equals(EntityType.EGG)) {

                Player player = (Player) event.getEntity().getShooter();

                if (GameLogic.playerKitValidate(player, kitName)) {
                    Egg egg = (Egg) event.getEntity();
                    Location eggLoc = egg.getLocation();

                    player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, eggLoc, 0);
                    player.getWorld().playSound(eggLoc, Sound.ENTITY_GENERIC_EXPLODE, 3.0F, 0.533F);

                    Collection<Entity> nearByEntities = egg.getWorld().getNearbyEntities(eggLoc, 3, 3, 3);
                    nearByEntities.remove((Entity) player);

                    for (Entity entity : nearByEntities) {
                        if (entity instanceof Player) {
                            Player victim = (Player) entity;

                            Location victimLoc = victim.getLocation();

                            double launchVelocityX =  player.getLocation().getX() - victimLoc.getX();
                            double launchVelocityZ = player.getLocation().getZ() - victimLoc.getZ();

                            Vector launchVector = new Vector(launchVelocityX, 0, launchVelocityZ).normalize().multiply(2);
                            launchVector.setY(0.6F);

                            victim.setVelocity(launchVector);
                            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10, 1));
                            victim.damage(this.ability1.damage, player);
                            victim.setLastDamage(this.ability1.damage);

                            // spawn creeper
                            int randomNum = ThreadLocalRandom.current().nextInt(1, 10 + 1);
                            player.sendMessage(Integer.toString(randomNum));
                            if (randomNum <= 3 || true) {
                                spawnTntZombie(player, victim, eggLoc, 100, this.ability1.damage);
                            }
                        }
                    }
                }
            }
        }

        return;
    }

    @EventHandler
    public void boomerTntPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (GameLogic.playerKitValidate(player, kitName)) {
            if (block.getType() == Material.TNT) {

                if (GameLogic.abilityIsUnderCooldown(player, 2)) {
                    GameLogic.attemptAbility(player, this.ability2, false);
                    event.setCancelled(true);
                    return;
                }

                block.setType(Material.AIR);
                Location blockLoc = block.getLocation();

                spawnTnt(player,blockLoc, 40, this.ability2.damage);
                GameLogic.attemptAbility(player, this.ability2, true);
            }
        }
    }

    @EventHandler
    public void boomerEntityDamage(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity victim = event.getEntity();

        if (attacker instanceof Player && victim instanceof Player) {
            Player playerAttacker = (Player) attacker;
            Player playerVictim = (Player) victim;

            double damageDealt = playerVictim.getLastDamage();
            double victimHealth = playerVictim.getHealth();

            playerAttacker.sendMessage("HP:" + Double.toString(victimHealth) + " DMG:" + Double.toString(damageDealt));

            playerAttacker.sendMessage("You hurt a " + victim.getName());
            if (damageDealt >= victimHealth) {
                playerAttacker.sendMessage("You blown up a " + victim.getName());
            }
        }
        return;
    }

    @EventHandler
    public void chickenSpawnEgg(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == SpawnReason.EGG) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void disableZombieDamage(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity victim = event.getEntity();

        if (attacker instanceof Zombie && victim instanceof Player) {
            event.setCancelled(true);
        }
    }

    public void spawnTnt(Player player, Location placedLoc, int tntTicks, double tntDamage) {
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
                        tntExplode(player, tnt, tnt.getLocation(), tntDamage);
                        tnt.remove();
                    }

                    cancel();
                }
            }

        }.runTaskTimer(KitWar.getInstance(), 0, 1);
    }

    public void spawnTntZombie(Player player, Player victim, Location eggLoc, int tntTicks, double tntDamage) {
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
                        tntExplode(player, zombie, zombie.getLocation(), tntDamage);
                        zombie.remove();
                    }

                    cancel();
                }
            }

        }.runTaskTimer(KitWar.getInstance(), 0, 1);
    }

    public void tntExplode(Player player, Entity tnt, Location loc, double tntDamage) {

        player.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 0);
        player.getWorld().playSound(loc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 3.0F, 0.533F);

        Collection<Entity> nearByEntities = tnt.getWorld().getNearbyEntities(tnt.getLocation(), 5, 5, 5);
        nearByEntities.remove(player);
        for (Entity entity : nearByEntities) {
            if (entity instanceof Player) {
                Player victim = (Player) entity;

                Location victimLoc = victim.getLocation();
                double launchVelocityX = victimLoc.getX() - loc.getX();
                double launchVelocityZ = victimLoc.getZ() - loc.getZ();

                Vector launchVector = new Vector(launchVelocityX, 0, launchVelocityZ).normalize().multiply(2);
                launchVector.normalize().multiply(1.5F);
                launchVector.setY(0.6F);

                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 255));
                victim.damage(tntDamage, player);
                victim.setLastDamage(tntDamage);
            }
        }
    }
}
