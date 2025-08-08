package com.example.demo;

import java.util.List;

public class PhysicsUtil {

    // Prevent instantiation
    private PhysicsUtil() {
    }

    public static void updateAllBodies(List<CelestialBody> bodies, double deltaTime) {
        // TODO: Add null checks for the input list of bodies and potentially for individual body objects
        if (bodies == null) {
            return; // Or throw an exception
        }
        // Barnes-Hut optimization: O(n log n) vs naive O(n²)
        SpatialPartitioningTree tree = new BarnesHutTree(bodies); // Assuming BarnesHutTree constructor builds the tree
        for (CelestialBody body : bodies) {
            if (body.isStatic) {
                continue;
            }
            Vector3D netForce = tree.calculateNetForce(body); // O(log n)
            body.applyForce(netForce, deltaTime);                          // O(1)
            body.updatePosition(deltaTime);                                     // O(1)
        }
    }
}