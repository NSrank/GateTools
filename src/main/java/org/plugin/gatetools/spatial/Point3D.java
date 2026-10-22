/*
 * MIT License
 * 
 * Copyright (c) 2024 NSrank, Augment
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.plugin.gatetools.spatial;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * 3D点坐标
 * 
 * 表示三维空间中的一个点，用于空间索引和碰撞检测
 * 
 * @author NSrank, Augment
 */
public class Point3D {
    private final int x;
    private final int y;
    private final int z;
    
    public Point3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * 从Bukkit Location创建Point3D
     */
    public Point3D(Location location) {
        this(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    
    /**
     * 转换为Bukkit Location
     */
    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }
    
    /**
     * 计算到另一个点的距离平方
     */
    public double distanceSquared(Point3D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }
    
    /**
     * 计算到另一个点的距离
     */
    public double distance(Point3D other) {
        return Math.sqrt(distanceSquared(other));
    }
    
    /**
     * 偏移点坐标
     */
    public Point3D offset(int dx, int dy, int dz) {
        return new Point3D(x + dx, y + dy, z + dz);
    }
    
    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Point3D point3D = (Point3D) obj;
        return x == point3D.x && y == point3D.y && z == point3D.z;
    }
    
    @Override
    public int hashCode() {
        return 31 * (31 * x + y) + z;
    }
    
    @Override
    public String toString() {
        return "Point3D(" + x + ", " + y + ", " + z + ")";
    }
}
