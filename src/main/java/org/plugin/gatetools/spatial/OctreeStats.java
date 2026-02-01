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

/**
 * 八叉树统计信息
 * 
 * @author NSrank, Augment
 */
public class OctreeStats {
    private final int totalNodes;
    private final int totalItems;
    private final int maxDepth;
    
    public OctreeStats(int totalNodes, int totalItems, int maxDepth) {
        this.totalNodes = totalNodes;
        this.totalItems = totalItems;
        this.maxDepth = maxDepth;
    }
    
    public int getTotalNodes() {
        return totalNodes;
    }
    
    public int getTotalItems() {
        return totalItems;
    }
    
    public int getMaxDepth() {
        return maxDepth;
    }
    
    @Override
    public String toString() {
        return "OctreeStats{" +
                "totalNodes=" + totalNodes +
                ", totalItems=" + totalItems +
                ", maxDepth=" + maxDepth +
                '}';
    }
}
