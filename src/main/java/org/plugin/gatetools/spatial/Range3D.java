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

/**
 * 3D范围
 *
 * 表示三维空间中的一个立方体区域，用于空间索引和碰撞检测
 *
 * @author NSrank, Augment
 */
public class Range3D {
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;
    
    public Range3D(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (minX > maxX) throw new IllegalArgumentException("minX must be <= maxX");
        if (minY > maxY) throw new IllegalArgumentException("minY must be <= maxY");
        if (minZ > maxZ) throw new IllegalArgumentException("minZ must be <= maxZ");
        
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }
    
    /**
     * 从两个点创建Range3D
     */
    public Range3D(Point3D point1, Point3D point2) {
        this(
            Math.min(point1.getX(), point2.getX()),
            Math.min(point1.getY(), point2.getY()),
            Math.min(point1.getZ(), point2.getZ()),
            Math.max(point1.getX(), point2.getX()),
            Math.max(point1.getY(), point2.getY()),
            Math.max(point1.getZ(), point2.getZ())
        );
    }
    
    /**
     * 从两个Location创建Range3D
     */
    public Range3D(Location loc1, Location loc2) {
        this(new Point3D(loc1), new Point3D(loc2));
    }
    
    /**
     * 获取范围的中心点
     */
    public Point3D getCenter() {
        return new Point3D(
            (minX + maxX) / 2,
            (minY + maxY) / 2,
            (minZ + maxZ) / 2
        );
    }
    
    /**
     * 获取范围的体积
     */
    public long getVolume() {
        return (long)(maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }
    
    /**
     * 检查点是否在范围内
     */
    public boolean contains(Point3D point) {
        return point.getX() >= minX && point.getX() <= maxX &&
               point.getY() >= minY && point.getY() <= maxY &&
               point.getZ() >= minZ && point.getZ() <= maxZ;
    }
    
    /**
     * 检查Location是否在范围内
     */
    public boolean contains(Location location) {
        return contains(new Point3D(location));
    }
    
    /**
     * 检查两个范围是否相交
     */
    public boolean intersects(Range3D other) {
        return !(maxX < other.minX || minX > other.maxX ||
                maxY < other.minY || minY > other.maxY ||
                maxZ < other.minZ || minZ > other.maxZ);
    }
    
    /**
     * 检查当前范围是否完全包含另一个范围
     */
    public boolean contains(Range3D other) {
        return minX <= other.minX && maxX >= other.maxX &&
               minY <= other.minY && maxY >= other.maxY &&
               minZ <= other.minZ && maxZ >= other.maxZ;
    }
    
    /**
     * 扩展范围
     */
    public Range3D expand(int amount) {
        return new Range3D(
            minX - amount, minY - amount, minZ - amount,
            maxX + amount, maxY + amount, maxZ + amount
        );
    }

    /**
     * 将范围分割为8个子范围（用于八叉树）
     */
    public Range3D[] subdivide() {
        Point3D center = getCenter();
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();

        return new Range3D[] {
            // 前下左
            new Range3D(minX, minY, minZ, centerX, centerY, centerZ),
            // 前下右
            new Range3D(centerX + 1, minY, minZ, maxX, centerY, centerZ),
            // 前上左
            new Range3D(minX, centerY + 1, minZ, centerX, maxY, centerZ),
            // 前上右
            new Range3D(centerX + 1, centerY + 1, minZ, maxX, maxY, centerZ),
            // 后下左
            new Range3D(minX, minY, centerZ + 1, centerX, centerY, maxZ),
            // 后下右
            new Range3D(centerX + 1, minY, centerZ + 1, maxX, centerY, maxZ),
            // 后上左
            new Range3D(minX, centerY + 1, centerZ + 1, centerX, maxY, maxZ),
            // 后上右
            new Range3D(centerX + 1, centerY + 1, centerZ + 1, maxX, maxY, maxZ)
        };
    }
    
    // Getters
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Range3D range3D = (Range3D) obj;
        return minX == range3D.minX && minY == range3D.minY && minZ == range3D.minZ &&
               maxX == range3D.maxX && maxY == range3D.maxY && maxZ == range3D.maxZ;
    }
    
    @Override
    public int hashCode() {
        int result = minX;
        result = 31 * result + minY;
        result = 31 * result + minZ;
        result = 31 * result + maxX;
        result = 31 * result + maxY;
        result = 31 * result + maxZ;
        return result;
    }
    
    @Override
    public String toString() {
        return "Range3D(" + minX + "," + minY + "," + minZ + " to " + maxX + "," + maxY + "," + maxZ + ")";
    }
}
