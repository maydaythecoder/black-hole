package com.example.demo;

import java.util.List;

public class PhysicsUtil {

    // Prevent instantiation
    private PhysicsUtil() {
    }

    public static void updateAllBodies(List<CelestialBody> bodies, double deltaTime) {
        // SAFETY: Null check for input list to prevent NullPointerException
        if (bodies == null) {
            return;
        }
        // SAFETY: Remove null elements from the list to prevent downstream NPEs
        // (Alternatively, skip nulls in the loop below for efficiency with large lists)
        SpatialPartitioningTree tree = new BarnesHutTree(bodies);
        for (CelestialBody body : bodies) {
            if (body == null) {
                continue; // SAFETY: Skip null body to prevent NullPointerException
            }
            if (body.isStatic) {
                continue;
            }
            Vector3D netForce = tree.calculateNetForce(body); // O(log n)
            body.applyForce(netForce, deltaTime);             // O(1)
            body.updatePosition(deltaTime);                   // O(1)
        }
    }
}