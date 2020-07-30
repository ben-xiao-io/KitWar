package com.benoolean.KitWar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;


public class KitData {

    private FileConfiguration dataConfig = null;
    private File configFile = null;
    private final KitWar plugin;

    static class Ability {
        public String name;
        public int abilityNum;
        public String description;
        public long cooldown;
        public long damage;
        public ItemStack equipment;

        public Ability(String name, int abilityNum, String description, long cooldown, long damage, ItemStack equipment) {
            this.name = name;
            this.abilityNum=  abilityNum;
            this.description = description;
            this.cooldown = cooldown;
            this.damage = damage;
            this.equipment = equipment;

            if (name == null || (abilityNum != 1 && abilityNum != 2) || description == null || cooldown < 0 || damage < 0 || equipment == null) {
                throw new NullPointerException();
            }
        }
    }

    static class Kit {
        public String name;
        public String passiveDescription;

        public List<Ability> abilities;

        public Kit(String name, String passiveDescription) {
            this.name = name;
            this.passiveDescription = passiveDescription;
            this.abilities = new ArrayList<>();

            if (name == null || passiveDescription == null || abilities == null) {
                throw new NullPointerException();
            }
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

                    // configuring abilites and passive
                    ConfigurationSection abilities = this.getConfig().getConfigurationSection("kits." + kitName + ".abilities");
                    assert abilities != null;
                    String passive = abilities.getString("passive");

                    // initializing kit
                    Kit kit = new Kit(kitName, passive);

                    for (int abilityNum = 1; abilityNum<3; abilityNum++) {
                        ConfigurationSection abilityConfig = this.getConfig().getConfigurationSection(
                                "kits." + kitName + ".abilities.ability" + abilityNum);

                        assert abilityConfig != null;
                        String abilityName = abilityConfig.getString("name");
                        String description = abilityConfig.getString("description");
                        long cooldown = abilityConfig.getLong("cooldown");
                        long damage = abilityConfig.getLong("damage");

                        ConfigurationSection equipmentSection = abilityConfig.getConfigurationSection("equipment");
                        assert equipmentSection != null;
                        ItemStack equipment = ItemStack.deserialize(equipmentSection.getValues(true));

                        // initializing ability
                        Ability ability = new Ability(abilityName, abilityNum, description, cooldown, damage, equipment);

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

    public ItemStack getKitEquipmentByName(String name, int abilityNum) {
        Kit kit = getKitByName(name);
        if (kit == null) {
            return null;
        }

        return kit.getAbility(abilityNum).equipment;
    }

    public Ability getKitAbility(String name, int abilityNum) {
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
