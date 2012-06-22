/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.czahrien.PlayerStorage;

import org.bukkit.plugin.java.JavaPlugin;

public class PlayerStorage extends JavaPlugin implements Runnable {
    
    /**
     * onEnable schedules a task which saves the PlayerStorage data.
     */
    @Override
    public void onEnable() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 900000, 900000);
    }
    
    /**
     * When the plugin is unloaded it saves all PlayerStorage data.
     */
    @Override
    public void onDisable() {
        saveInstances();
    }
    
    /**
     * The repeating task saves all PlayerStorage instances.
     */
    @Override
    public void run() {
        saveInstances();
    }
    
    /**
     * Save all instances of PlayerStorage.
     */
    public void saveInstances() {
        getLogger().info("Saving PlayerStorage data...");
        PlayerStorageInstance.saveAll();
        getLogger().info("Done saving PlayerStorage data!");
    }

}
