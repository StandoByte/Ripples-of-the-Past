package com.github.standobyte.jojo.util.mod;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;

public class JojoModVersion implements Comparable<JojoModVersion> {
    private final int release;
    private final int major;
    private final int minor;
    private final int patch;
    
    public JojoModVersion(int major, int minor, int patch) {
        this(0, major, minor, patch);
    }
    
    public JojoModVersion(int release, int major, int minor, int patch) {
        this.release = release;
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }
    
    @Override
    public int compareTo(JojoModVersion o) {
        return      this.patch - o.patch
                + ((this.minor - o.minor) << 8)
                + ((this.major - o.major) << 16)
                + ((this.release - o.release) << 24);
    }
    
    public int comparePatch(JojoModVersion other) {
        return patch - other.patch;
    }
    
    public int compareMinor(JojoModVersion other) {
        return minor - other.minor;
    }
    
    public int compareMajor(JojoModVersion other) {
        return major - other.major;
    }
    
//    public int compareRelease(ModVersion other) {
//        return release - other.release;
//    }
//    
//    public int getRelease() {
//        return release;
//    }
    
    public int getMajor() {
        return major;
    }
    
    public int getMinor() {
        return minor;
    }
    
    public int getPatch() {
        return patch;
    }
    
    public INBT toNBT() {
        IntArrayNBT nbt = new IntArrayNBT(new int[] {release, major, minor, patch});
        return nbt;
    }
    
    public static JojoModVersion fromNBT(INBT nbt) {
        int release = 0;
        int major = 2;
        int minor = 0;
        int patch = 0;
        if (nbt != null && nbt.getType() == IntArrayNBT.TYPE) {
            int[] arr = ((IntArrayNBT) nbt).getAsIntArray();
            if (arr.length > 0) {
                release = arr[0];
                if (arr.length > 1) {
                    major = arr[1];
                    if (arr.length > 2) {
                        minor = arr[2];
                        if (arr.length > 3) {
                            patch = arr[3];
                        }
                    }
                }
            }
        }
        return new JojoModVersion(release, major, minor, patch);
    }
    
    public static JojoModVersion getCurrentVersion() {
        return JojoMod.CURRENT_VERSION;
    }
}
