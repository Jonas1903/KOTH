package com.koth.plugin.models;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class KOTHRegion {
    private final String worldName;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;

    public KOTHRegion(Location pos1, Location pos2) {
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            throw new IllegalArgumentException("Positions must be in the same world!");
        }
        
        this.worldName = pos1.getWorld().getName();
        this.minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        this.minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        this.maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        this.maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
    }

    public KOTHRegion(String worldName, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.worldName = worldName;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public boolean contains(Location location) {
        if (!location.getWorld().getName().equals(worldName)) {
            return false;
        }
        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }

    public void saveToConfig(ConfigurationSection section) {
        section.set("world", worldName);
        section.set("pos1.x", minX);
        section.set("pos1.y", minY);
        section.set("pos1.z", minZ);
        section.set("pos2.x", maxX);
        section.set("pos2.y", maxY);
        section.set("pos2.z", maxZ);
    }

    public static KOTHRegion loadFromConfig(ConfigurationSection section) {
        String world = section.getString("world");
        if (world == null || world.isEmpty()) {
            return null;
        }
        
        int minX = section.getInt("pos1.x");
        int minY = section.getInt("pos1.y");
        int minZ = section.getInt("pos1.z");
        int maxX = section.getInt("pos2.x");
        int maxY = section.getInt("pos2.y");
        int maxZ = section.getInt("pos2.z");
        
        return new KOTHRegion(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public String getWorldName() {
        return worldName;
    }
}
