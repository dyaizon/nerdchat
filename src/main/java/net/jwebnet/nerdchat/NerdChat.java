/*
 * Copyright (C) 2014 Joseph W Becher <jwbecher@jwebnet.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.jwebnet.nerdchat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class NerdChat extends JavaPlugin implements Listener {
    
    private ConfigManager config;
    private List<Player> staffPlayers;
    private List<WorldGroup> worldGroups;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.staffPlayers = new ArrayList<Player>();
        this.worldGroups = new ArrayList<WorldGroup>();
        
        // Parse config.
        this.config = new ConfigManager(this);
        if (this.config.debug) {
            enableDebug();
        }
        
        // Set up world groups.
        for (String[] group : this.config.worldGroups) {
            LinkedList<World> worldList = new LinkedList<World>();
            for (String worldName : group) {
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    getLogger().warning("World " + worldName + " not found!");
                } else {
                    worldList.add(world);
                }
            }
            
            if (!worldList.isEmpty()) {
                getLogger().fine("Creating WorldGroup for " + worldList);
                this.worldGroups.add(new WorldGroup(worldList));
            }
        }
        
        // Create a worldgroup for any worlds not currently in one.
        for (World world : Bukkit.getWorlds()) {
            WorldGroup wg = findWorldGroup(world);
            if (wg == null) {
                getLogger().fine("Creating WorldGroup for " + world.getName());
                this.worldGroups.add(new WorldGroup(world));
            }
        }
        
        // Register for events.
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    private void enableDebug() {
        File folder = getDataFolder();
        if (!folder.exists()) {
            folder.mkdir();
        }
        
        File debug = new File(folder, "debug.log");
        
        try {
            FileHandler fh = new FileHandler(debug.getPath());
            fh.setLevel(Level.FINER);
            getLogger().addHandler(fh);
            
            SimpleFormatter ft = new SimpleFormatter();
            fh.setFormatter(ft);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private WorldGroup findWorldGroup(World world) {
        for (WorldGroup group : worldGroups) {
            if (group.contains(world)) {
                return group;
            }
        }
        
        return null;
    }
    
    private WorldGroup findWorldGroup(String worldName) {
        for (WorldGroup group : worldGroups) {
            if (group.contains(worldName)) {
                return group;
            }
        }
        
        return null;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldLoad(WorldLoadEvent event) {
        getLogger().fine("Got world load event for " +
                event.getWorld().getName());
        String worldName = event.getWorld().getName();
        for (String[] group : this.config.worldGroups) {
            if (Arrays.asList(group).contains(worldName)) {
                for (String name : group) {
                    WorldGroup worldGroup = findWorldGroup(name);
                    if (worldGroup != null) {
                        getLogger().fine("Adding world " +
                            event.getWorld().getName() + " to existing group");
                        worldGroup.addWorld(event.getWorld());
                        return;
                    }
                }
            }
        }
        
        // No group found for this world - create own worldgroup.
        getLogger().fine("Creating world group for " +
                event.getWorld().getName());
        this.worldGroups.add(new WorldGroup(event.getWorld()));
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldUnload(WorldUnloadEvent event) {
        getLogger().fine("Got world unload event for " +
                event.getWorld().getName());
        WorldGroup worldGroup = findWorldGroup(event.getWorld());
        if (worldGroup != null) {
            getLogger().fine("Removing world " +
                event.getWorld().getName() + " from group");
            worldGroup.removeWorld(event.getWorld());
            if (worldGroup.size() == 0) {
                getLogger().fine("Deleting empty WorldGroup");
                worldGroups.remove(worldGroup);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        WorldGroup fromGroup = findWorldGroup(event.getFrom());
        WorldGroup toGroup = findWorldGroup(event.getPlayer().getWorld());
        
        if (fromGroup != null) {
            fromGroup.removePlayer(event.getPlayer());
        }
        
        if (toGroup != null) {
            toGroup.addPlayer(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // If player has chat.admin, they are chatting with all worlds
        if (event.getPlayer().hasPermission("chat.admin")) {
            staffPlayers.add(event.getPlayer());
        }
        
        WorldGroup toGroup = findWorldGroup(event.getPlayer().getWorld());
        if (toGroup != null) {
            toGroup.addPlayer(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // If player has chat.admin, they are chatting with all worlds
        if (event.getPlayer().hasPermission("chat.admin")) {
            staffPlayers.remove(event.getPlayer());
        }
        
        WorldGroup fromGroup = findWorldGroup(event.getPlayer().getWorld());
        if (fromGroup != null) {
            fromGroup.addPlayer(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        // If event is canceled, abort
        if (event.isCancelled()) {
            return;
        }

        Player senderPlayer = event.getPlayer();

        // If player has chat.admin, they are chatting with all worlds
        if (senderPlayer.hasPermission("chat.admin")) {
            return;
        }
        
        // Clear the list of players who will see the message
        event.getRecipients().clear();
        
        // Get the worldgroup of the sender and add all the players in that
        // worldgroup.
        World senderWorld = senderPlayer.getWorld();
        WorldGroup worldGroup = findWorldGroup(senderWorld);
        
        if (worldGroup != null) {
            event.getRecipients().addAll(worldGroup.getPlayers());
        } else {
            // The WorldGroup should never be null, so print a warning here.
            getLogger().warning("WorldGroup not found for world " + 
                    senderWorld.getName());
            event.getRecipients().addAll(senderWorld.getPlayers());
        }

        event.getRecipients().addAll(staffPlayers);
    }
}
