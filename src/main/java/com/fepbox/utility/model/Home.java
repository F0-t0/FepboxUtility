package com.fepbox.utility.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public record Home(String name, String world, double x, double y, double z, float yaw, float pitch) {
    public Location toLocation() {
        World w = Bukkit.getWorld(world);
        if (w == null) return null;
        return new Location(w, x, y, z, yaw, pitch);
    }
}
