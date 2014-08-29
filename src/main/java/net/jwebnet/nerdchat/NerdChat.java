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

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

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

        Player senderPlayer = evt.getPlayer();

        // If player has chat.admin, they are chatting with all worlds
        if (senderPlayer.hasPermission("chat.admin")) {
            return;
        }

        // Get the sender's world
        World senderWorld;
        senderWorld = senderPlayer.getWorld();

        // Clear the list of players who will see the message
        evt.getRecipients().clear();

        /*
         * Get a list of all players in the sender's world
         */
        List<Player> senderWorldPlayers;
        senderWorldPlayers = senderWorld.getPlayers();

        /*
         * Add the players in the sender's world to the list
         */
        evt.getRecipients().addAll(senderWorldPlayers);

        /*
         * Check if sender is in a linked world
         */
        if ((senderWorld.getName().equals("Mineworld".toLowerCase()))
                || (senderWorld.getName().equals("vip".toLowerCase()))) {
            /*
             * sender is in a linked world
             */
            if (senderWorld.getName().equals("Mineworld")) {
                /* If sender is in either "Mineworld" or "vip"
                 * this check if the player is also in either world, and add.
                 */
                evt.getRecipients().addAll(Bukkit.getWorld("Mineworld").getPlayers());
            }

        } else if (senderWorld.getName().equals("vip")) {
            evt.getRecipients().addAll(Bukkit.getWorld("Mineworld").getPlayers());

        }
    }
}
