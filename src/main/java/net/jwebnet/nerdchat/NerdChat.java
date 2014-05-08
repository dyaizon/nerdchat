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

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class NerdChat
        extends JavaPlugin
        implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent evt) {
        // If event is canceled, abort
        if (evt.isCancelled()) {
            return;
        }
        // If player has chat.admin, they are chatting with all worlds
        if (evt.getPlayer().hasPermission("chat.admin")) {
            return;
        }

        // Set the sender's world
        World senderWorld = evt.getPlayer().getWorld();

        // Clear the list of players who will see the message
        evt.getRecipients().clear();

        // Create a new list to hold message recipients
        List<Player> recipients = new LinkedList<Player>();

//        for (World world : Bukkit.getWorlds()) {
            // Create a list of players for this world
            Player[] players;
            // Fill the list with the players from this world
            // Testing https://forums.bukkit.org/threads/java-util-concurrentmodificationexception-error.92020/
            players = getServer().getOnlinePlayers();
            // Loop though the players
            for (Player p : players) {
                try {
                    // If player has chat.admin, or is in the server's world, add them
                    if ((p.hasPermission("chat.admin")) || (p.getWorld() == senderWorld)) {
                        recipients.add(p);
                    }
                } catch (ConcurrentModificationException localConcurrentModificationException) {
                }

            }
//        }
        // Add the list of recipients back to the event
        evt.getRecipients().addAll(recipients);

    }
}
