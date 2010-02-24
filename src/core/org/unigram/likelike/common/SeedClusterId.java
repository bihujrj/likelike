package org.unigram.likelike.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public final class SeedClusterId 
    implements WritableComparable<SeedClusterId> {

    public SeedClusterId() {}

    public SeedClusterId(long seed, long clusterId) {
        this.hashSeed = seed;
        this.clusterId = clusterId;
    }

    public long getClusterId() {
        return clusterId;
    }
  
    public long getSeed() {
        return hashSeed;
    }
  
    @Override
    public void write(DataOutput out) 
    throws IOException {
        out.writeLong(hashSeed);
        out.writeLong(clusterId);
    }

    @Override
    public void readFields(DataInput in) 
        throws IOException {
        hashSeed = in.readLong();
        clusterId= in.readLong();
    }

    @Override
    public int compareTo(SeedClusterId that) {
        int seedResult = compareLongs(this.hashSeed, 
                that.getSeed());
        return seedResult == 0 ? compareLongs(clusterId, 
                that.getClusterId()) : seedResult;
    }

    @Override
    public int hashCode() {
        return (int) ((int) hashSeed + 53 * clusterId);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SeedClusterId) {
            SeedClusterId that = (SeedClusterId) o;
            return (hashSeed == that.getSeed()) 
            && (clusterId == that.getClusterId());
        }
        return false;
    }

    @Override
    public String toString() {
        return hashSeed + ":" + clusterId;
    }
 
    private static int compareLongs(long a, long b) {
        return a < b ? -1 : a > b ? 1 : 0;
    }
  
    private long hashSeed;
  
    private long clusterId; 
  
}