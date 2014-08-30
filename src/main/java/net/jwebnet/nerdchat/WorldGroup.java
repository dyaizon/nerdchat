/*
 * Copyright (C) 2014 Matthew Green <dyaizon@gmail.com>
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

import java.util.LinkedList;
import java.util.List;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author mgree_000
 */
public class WorldGroup {
    
    private List<World> worldList;
    private List<Player> players;
    
    public WorldGroup() {
        worldList = new LinkedList<World>();
        players = new LinkedList<Player>();
    }
    
    public WorldGroup(World world) {
        this();
        this.worldList.add(world);
        this.players.addAll(world.getPlayers());
    }
    
    public WorldGroup(List<World> worlds) {
        this();
        for (World world : worlds) {
            this.worldList.add(world);
            this.players.addAll(world.getPlayers());
        }
    }
    
    public boolean contains(World world) {             
        return this.worldList.contains(world);
    }
    
    public boolean contains(String worldName) {
        for (World world : this.worldList) {
            if (world.getName().equals(worldName)) {
                return true;
            }
        }
        
        return false;
    }
    
    public void addWorld(World world) {
        this.worldList.add(world);
        this.players.addAll(world.getPlayers());
    }
    
    public void removeWorld(World world) {
        this.worldList.remove(world);
        this.players.removeAll(world.getPlayers());
    }
    
    public void addPlayer(Player player) {
        this.players.add(player);
    }
    
    public void removePlayer(Player player) {
        this.players.remove(player);
    }
    
    public List<Player> getPlayers() {
        return this.players;
    }
}
