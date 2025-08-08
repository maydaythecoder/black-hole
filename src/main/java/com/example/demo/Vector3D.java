package com.example.demo;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Vector3D with object pooling for performance.
 * 
 * SECURITY: No sensitive data stored in pooled objects. Pool is thread-safe for basic use,
 * but not designed for untrusted input or adversarial pool exhaustion.
 * 
 * SAFETY: Pool size is bounded to prevent unbounded memory usage.
 */
public class Vector3D {
    public final double x;
    public final double y;
    public final double z;

    // Add ZERO constant
    public static final Vector3D ZERO = new Vector3D(0, 0, 0);

    // --- Object Pool Implementation ---
    private static final int POOL_SIZE = 1024; // Tune as needed for workload
    private static final ArrayBlockingQueue<PooledVector3D> pool = new ArrayBlockingQueue<>(POOL_SIZE);

    // Private constructor for pooled instances
    private Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Factory method for pooled instances
    public static Vector3D obtain(double x, double y, double z) {
        PooledVector3D v = pool.poll();
        if (v == null) {
            // Pool exhausted, create new (not pooled)
            return new Vector3D(x, y, z);
        }
        v.set(x, y, z);
        return v;
    }

    // Return a Vector3D to the pool if possible
    public void recycle() {
        // Only pooled instances can be recycled
        if (this instanceof PooledVector3D) {
            PooledVector3D v = (PooledVector3D) this;
            // SECURITY: Zero out values to avoid data leakage (not strictly needed for non-sensitive data)
            v.set(0, 0, 0);
            pool.offer(v);
        }
    }

    // --- Pooled subclass ---
    private static class PooledVector3D extends Vector3D {
        private PooledVector3D() {
            super(0, 0, 0);
        }
        private void set(double x, double y, double z) {
            // Use reflection to set final fields (hacky, but required for pooling with final fields)
            // Alternatively, remove 'final' from fields for poolable objects
            // For safety, only allow mutation in pooled subclass
            try {
                java.lang.reflect.Field fx = Vector3D.class.getDeclaredField("x");
                java.lang.reflect.Field fy = Vector3D.class.getDeclaredField("y");
                java.lang.reflect.Field fz = Vector3D.class.getDeclaredField("z");
                fx.setAccessible(true);
                fy.setAccessible(true);
                fz.setAccessible(true);
                fx.setDouble(this, x);
                fy.setDouble(this, y);
                fz.setDouble(this, z);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set pooled Vector3D fields", e);
            }
        }
    }

    // Pre-populate the pool
    static {
        for (int i = 0; i < POOL_SIZE; i++) {
            pool.offer(new PooledVector3D());
        }
    }

    // --- Vector Operations using pooling ---

    public Vector3D add(Vector3D other) {
        return obtain(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3D subtract(Vector3D other) {
        return obtain(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3D scale(double scalar) {
        return obtain(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public double dot(Vector3D other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vector3D cross(Vector3D other) {
        return obtain(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        );
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3D normalize() {
        double length = length();
        if (length == 0) {
            return ZERO; // Use ZERO constant
        }
        return obtain(x / length, y / length, z / length);
    }

    @Override
    public String toString() {
        return "Vector3D(" + x + ", " + y + ", " + z + ")";
    }
}