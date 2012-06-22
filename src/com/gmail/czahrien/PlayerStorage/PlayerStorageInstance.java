/**
 * File: PlayerStorageInstance.java
 * Description: Contains a class which represents an instance of the
 * PlayerStorage plugin's information storage about Players.
 */
package com.gmail.czahrien.PlayerStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * A class representing a PlayerStorage instance storing the type it is templated
 * over in dat files for each player.
 * @author Czahrien
 */
public class PlayerStorageInstance<E extends Serializable> {
    /**
     * A HashMap containing all (name,instance) pairs that exist.
     */
    private static HashMap<String,PlayerStorageInstance<?>> instances = new HashMap<>();  
    /** 
     * The path to the directory containing sub-directories for each instance.
     */
    private static final String PATH = "plugins/PlayerStorage/";
    /**
     * The logger to which error messages are written.
     */
    private static final Logger log = Bukkit.getServer().getPluginManager().getPlugin("PlayerStorage").getLogger();
    
    /**
     * The complete path to this instance's sub-directory.
     */
    private String myPath;

    /**
     * A HashMap contianing all (name,E) pairs that are a part of this instance.
     */
    private HashMap<String,E> myMap;
    /**
     * A HashSet containing all entries that need to be saved.
     */
    private HashSet<String> myDirtyEntries;
    /**
     * The name of this PlayerStorageInstance
     */
    private String myName;
    
    /**
     * Creates a new PlayerStorageInstance with the specified name.
     * @param name The name of this instance.
     */
    private PlayerStorageInstance(String name) {
        myName = name;
        myPath = PATH + myName + "/";
        myDirtyEntries = new HashSet<>();
        myMap = new HashMap<>();
        File dir = new File(myPath);
        if(dir.isDirectory()) {
            // Iterate through all files in the directory.
            for(File f : dir.listFiles()) {
                // We only want files with names in the format "name.dat"
                String[] fileName = f.getName().split("\\.");
                if(fileName.length == 2 && fileName[1].equals("dat")) {
                    try {
                        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
                        myMap.put(fileName[0].toLowerCase(),(E)ois.readObject());
                        ois.close();
                    } catch(IOException | ClassNotFoundException e) {
                        //TODO: ERROR: Proper Exception handling...
                        e.printStackTrace();
                    }
                }
            }
        } else if(dir.isFile()) {
            log.info("ERROR: Expected directory for " + name + " but got a non-directory file.");
        } else {
            log.info("Creating directory structure for " + myPath + "...");
            if( !dir.mkdirs() ) {
                log.info("ERROR: Could not create the directory structure!");
            } 
        }
    }
    
    public static <E extends Serializable> PlayerStorageInstance<E> getPlayerStorage(String name) {
        PlayerStorageInstance<E> instance = null;
        try {
            instance = (PlayerStorageInstance<E>)instances.get(name);
            if(instance == null) {
                instance = new PlayerStorageInstance<>(name);
                instances.put(name,instance);
            }
        } catch(ClassCastException e) {
            System.err.println("ERROR: Multiple PlayerStorages of name " + name + ".");
        }
        
        return instance;
    }
    
    public void save() {
        for(String name : myDirtyEntries ) {
            try {
                // We will write to a temporary file in case if the server is
                // forcefully terminated or crashes during saving. 
                // In this case no player's data will be corrupt if something
                // goes wrong elsewhere but it might be old.
                File f = new File(myPath + name + ".dat.tmp");
                
                // TODO: Check if f's name is a legal file name!
                // TODO: It is conceivable that a malicious user on an offline
                // server could set their name to the relative path a file that
                // could cause damage elsewhere. 
                
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
                oos.writeObject(myMap.get(name));
                oos.close();
                
                // Delete the old file.
                File dat = new File(myPath + name + ".dat");
                dat.delete();
                // Rename the new file.
                File tmp = new File(myPath + name + ".dat.tmp");
                tmp.renameTo(dat);
            } catch(IOException e) {
                log.info("ERROR: Could not save " + myName + " for " + name + ". Did you delete a directory?");
            }
        }
        myDirtyEntries.clear();
    }
    
    public static void saveAll() {
        for(PlayerStorageInstance<?> psi : instances.values() ) {
            psi.save();
        }
    }
    
    public E getPlayer(Player p) {
        return getPlayer(p.getName());
    }
    
    public E getPlayer(String name) {
        return myMap.get(name.toLowerCase());
    }
    
    public void setPlayer(Player p, E entry) {
        setPlayer(p.getName(),entry);
    }
    
    public void setPlayer(String name, E entry) {
        myDirtyEntries.add(name.toLowerCase());
        myMap.put(name.toLowerCase(), entry);
    }
}