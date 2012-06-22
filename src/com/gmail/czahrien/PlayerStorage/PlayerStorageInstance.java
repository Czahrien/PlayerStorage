/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import org.bukkit.entity.Player;

/**
 *
 * @author Czahrien
 */
public class PlayerStorageInstance<E extends Serializable> {
    private static final String PATH = "plugins/PlayerStorage/";
    private String myPath;
    private static HashMap<String,PlayerStorageInstance<?>> instances = new HashMap<>(); 
    private HashMap<String,E> myMap;
    private HashSet<String> myDirtyEntries;
    private String myName;
    
    private PlayerStorageInstance(String name) {
        myName = name;
        myPath = PATH + myName + "/";
        myDirtyEntries = new HashSet<>();
        myMap = new HashMap<>();
        File dir = new File(myPath);
        if(dir.isDirectory()) {
            for(File f : dir.listFiles()) {
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
            // TODO: ERROR: Expected directory, got file.
            System.err.println("ERROR: Expected directory, got file.");
        } else {
            dir.mkdirs();
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
                // We write to a temporary file in case if the server is
                // forcefully terminated. In this case no player's data will
                // be corrupt, only old.
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(myPath + name + ".dat.tmp"));
                oos.writeObject(myMap.get(name));
                oos.close();
                File dat = new File(myPath + name + ".dat");
                dat.delete();
                File tmp = new File(myPath + name + ".dat.tmp");
                tmp.renameTo(dat);
            } catch(IOException e) {
                System.err.println("ERROR: Could not save " + myName + " for " + name + ". Did you delete a directory?");
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
