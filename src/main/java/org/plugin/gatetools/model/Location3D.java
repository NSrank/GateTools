package org.plugin.gatetools.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * 三维坐标位置类
 * 用于存储世界名称和坐标信息
 * 
 * @author NSrank, Augment
 */
public class Location3D {
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    /**
     * 构造函数
     * 
     * @param worldName 世界名称
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     */
    public Location3D(String worldName, double x, double y, double z) {
        this(worldName, x, y, z, 0.0f, 0.0f);
    }

    /**
     * 构造函数（包含朝向）
     * 
     * @param worldName 世界名称
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @param yaw 偏航角
     * @param pitch 俯仰角
     */
    public Location3D(String worldName, double x, double y, double z, float yaw, float pitch) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * 从Bukkit Location创建Location3D
     * 
     * @param location Bukkit Location对象
     * @return Location3D对象
     */
    public static Location3D fromBukkitLocation(Location location) {
        return new Location3D(
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getYaw(),
            location.getPitch()
        );
    }

    /**
     * 转换为Bukkit Location
     * 
     * @return Bukkit Location对象，如果世界不存在则返回null
     */
    public Location toBukkitLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * 从字符串解析Location3D
     * 格式: "worldName,x,y,z" 或 "worldName,x,y,z,yaw,pitch"
     * 
     * @param str 字符串
     * @return Location3D对象
     * @throws IllegalArgumentException 如果格式不正确
     */
    public static Location3D fromString(String str) {
        String[] parts = str.split(",");
        if (parts.length < 4 || parts.length > 6) {
            throw new IllegalArgumentException("Invalid location format: " + str);
        }
        
        try {
            String worldName = parts[0].trim();
            double x = Double.parseDouble(parts[1].trim());
            double y = Double.parseDouble(parts[2].trim());
            double z = Double.parseDouble(parts[3].trim());
            
            if (parts.length >= 6) {
                float yaw = Float.parseFloat(parts[4].trim());
                float pitch = Float.parseFloat(parts[5].trim());
                return new Location3D(worldName, x, y, z, yaw, pitch);
            } else {
                return new Location3D(worldName, x, y, z);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in location: " + str, e);
        }
    }

    /**
     * 转换为字符串
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        if (yaw == 0.0f && pitch == 0.0f) {
            return String.format("%s,%.2f,%.2f,%.2f", worldName, x, y, z);
        } else {
            return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f", worldName, x, y, z, yaw, pitch);
        }
    }

    // Getters
    public String getWorldName() { return worldName; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Location3D that = (Location3D) obj;
        return Double.compare(that.x, x) == 0 &&
               Double.compare(that.y, y) == 0 &&
               Double.compare(that.z, z) == 0 &&
               Float.compare(that.yaw, yaw) == 0 &&
               Float.compare(that.pitch, pitch) == 0 &&
               worldName.equals(that.worldName);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(worldName, x, y, z, yaw, pitch);
    }
}
