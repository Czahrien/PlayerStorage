/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.czahrien.PlayerStorage;

import org.bukkit.plugin.java.JavaPlugin;

public class PlayerStorage extends JavaPlugin implements Runnable {
    
    @Override
    public void onEnable() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 900000, 900000);
    }
    
    @Override
    public void onDisable() {
        saveInstances();
    }
    
    @Override
    public void run() {
        saveInstances();
    }
    
    public void saveInstances() {
        getLogger().info("Saving PlayerStorage data...");
        PlayerStorageInstance.saveAll();
        getLogger().info("Done saving PlayerStorage data!");
    }

}
