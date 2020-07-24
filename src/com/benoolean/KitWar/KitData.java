package com.benoolean.KitWar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class KitData {

    private FileConfiguration dataConfig = null;
    private File configFile = null;
    private KitWar plugin;

    class Ability {
        public String name;
        public String description;
        public long cooldown;
        public long damage;
        public ItemStack equipment;

        public Ability(String name, String description, long cooldown, long damage, ItemStack equipment) {
            this.name = name;
            this.description = description;
            this.cooldown = cooldown;
            this.damage = damage;
            this.equipment = equipment;
        }
    }

    class Kit {
        public String name;
        public String passiveDescription;

        public List<Ability> abilities;

        public Kit(String name, String passiveDescription) {
            this.name = name;
            this.passiveDescription = passiveDescription;
            this.abilities = new ArrayList<Ability>();
        }

        public Ability getAbility(int abilityNum) {
            return abilities.get(abilityNum - 1);
        }
    }

    public HashMap<String, Kit> kitMap = new HashMap<>();

    public KitData(KitWar plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
        populateKits();
    }

    /////////////////////////////////
    // 							   //
    //           Config            //
    //							   //
    /////////////////////////////////

    public void populateKits() {
        if (this.getConfig().contains("kitNames")) {

            // getting KitNames
            List<String> kitNames = this.getConfig().getStringList("kitNames");
            for (String kitName : kitNames) {
                if (this.getConfig().contains("kits." + kitName)) {

                    // getting each kit configuration from yml

                    // initializing kit
                    Kit kit = new Kit(null, null);

                    // getting kit name
                    String name = this.getConfig().getString("kits." + kitName + ".name");

                    // configuring abilites and passive
                    ConfigurationSection abilities = this.getConfig().getConfigurationSection("kits." + kitName + ".abilities");
                    String passive = abilities.getString("passive");

                    for (int abilityNum = 1; abilityNum<3; abilityNum++) {
                        ConfigurationSection abilityConfig = this.getConfig().getConfigurationSection(
                                "kits." + kitName + ".abilities.ability" + Integer.toString(abilityNum));

                        String abilityName = abilityConfig.getString("name");
                        String description = abilityConfig.getString("description");
                        long cooldown = abilityConfig.getLong("cooldown");
                        long damage = abilityConfig.getLong("damage");

                        ConfigurationSection equipmentSection = abilityConfig.getConfigurationSection("equipment");
                        ItemStack equipment = ItemStack.deserialize(equipmentSection.getValues(true));

                        if (abilityName == null || description == null || cooldown < 0 || damage < 0 || equipment == null ) {
                            throw new NullPointerException();
                        }

                        // initializing ability
                        Ability ability = new Ability(abilityName, description, cooldown, damage, equipment);

                        // add ability to kit
                        kit.abilities.add(ability);
                    }

                    kitMap.put(kitName, kit);
                }
            }
        }
    }

    public boolean kitNameExists(String name) {
        Set<String> kitNames = kitMap.keySet();
        return kitNames.contains(name);
    }

    public Kit getKitByName(String name) {
        return kitMap.get(name);
    }

    public Kit getPlayerKit(Player player) {
        String kitName = GameLogic.PlayerKitMap.get(player);
        if (kitName == null) {
            return null;
        }

        return getKitByName(kitName);
    }

    public ItemStack getKitEquipmentByName(String name, int abilityNum) {
        Set<String> kitNames = kitMap.keySet();
        Kit kit = getKitByName(name);
        if (kit == null) {
            return null;
        }

        return kit.getAbility(abilityNum).equipment;
    }

    public Ability getKitAbility(String name, int abilityNum) {
        Set<String> kitNames = kitMap.keySet();
        Kit kit = getKitByName(name);
        if (kit == null) {
            return null;
        }
        return kit.abilities.get(abilityNum-1);
    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "data.yml");
        }

        dataConfig = YamlConfiguration.loadConfiguration(configFile);
        InputStream defaultStream = plugin.getResource("data.yml");

        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            dataConfig.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (dataConfig == null) {
            reloadConfig();
        }

        return dataConfig;
    }

    public void saveConfig() {
        if (dataConfig == null || configFile == null) {
            return;
        }

        try {
            getConfig().save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config file to: " + configFile, e);
        }
    }

    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "data.yml");
        }

        if (!configFile.exists()) {
            plugin.saveResource("data.yml", false);
        }
    }

}
