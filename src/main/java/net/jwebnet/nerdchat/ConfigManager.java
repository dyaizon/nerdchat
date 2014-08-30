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
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author Matthew Green
 */
public class ConfigManager {
    
    public final LinkedList<String[]> worldGroups;
    
    public ConfigManager (NerdChat plugin) {
        FileConfiguration config = plugin.getConfig();
        
        this.worldGroups = new LinkedList<String[]>();
        
        List<String> groups = config.getStringList("groups");
        if (groups != null) {
            for (String group : groups) {
                this.worldGroups.add(group.replaceAll("\\s","").split(","));
            }
        } else {
            plugin.getLogger().info("No group config found");
        }
    }
}
