package com.example.demo;

import java.util.List;
import java.util.ArrayList;

public class BarnesHutTree extends SpatialPartitioningTree {

    private Node root;
    private static final double THETA = 0.5; // Barnes-Hut opening angle parameter

    // Placeholder for an internal node or leaf in the tree
    private static abstract class Node {
        // Represents the bounding box of this node's region
        protected Vector3D center;
        protected double size;

        abstract Vector3D calculateForce(CelestialBody body);
    }

    // Placeholder for a leaf node (contains a single body)
    private static class LeafNode extends Node {
        private CelestialBody body;

        LeafNode(CelestialBody body, Vector3D center, double size) {
            this.body = body;
            this.center = center;
            this.size = size;
        }

        @Override
        Vector3D calculateForce(CelestialBody targetBody) {
            // If the target body is the same as this body, no force
            if (this.body.equals(targetBody)) {
                return Vector3D.ZERO;
            }
            return CelestialBody.calculateGravity(targetBody, this.body);
        }
    }

    // Placeholder for an internal node (contains child nodes)
    private static class InternalNode extends Node {
        private List<Node> children;
        private double totalMass;
        private Vector3D centerOfMass;

        InternalNode(Vector3D center, double size) {
            this.center = center;
            this.size = size;
            this.children = new ArrayList<>();
            this.totalMass = 0;
            this.centerOfMass = Vector3D.ZERO;
        }

        public void addChild(Node child) {
            this.children.add(child);
        }

        @Override
        Vector3D calculateForce(CelestialBody targetBody) {
            // Use subtract() instead of sub()
            double distance = this.center.subtract(targetBody.position).length();
            if (this.size / distance < THETA) {
                // Create temporary body with valid radius (1e-5 instead of 0)
                CelestialBody equivalentBody = new CelestialBody(
                    "temp", 
                    this.totalMass, 
                    1e-5,  // Small positive radius
                    new float[]{0,0,0}, 
                    this.centerOfMass, 
                    Vector3D.ZERO
                );
                equivalentBody.setStatic(true);
                
                if (equivalentBody.equals(targetBody)) {
                    return Vector3D.ZERO;
                }
                return CelestialBody.calculateGravity(targetBody, equivalentBody);

            } else {
                // Recurse into children
                Vector3D netForce = Vector3D.ZERO;
                for (Node child : children) {
                    netForce = netForce.add(child.calculateForce(targetBody));
                }
                return netForce;
            }
        }
    }

    public BarnesHutTree(List<CelestialBody> bodies) {
        if (bodies == null || bodies.isEmpty()) {
            this.root = null;
            return;
        }
        
        // Use separate variables instead of modifying final Vector3D fields
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (CelestialBody body : bodies) {
            minX = Math.min(minX, body.position.x);
            minY = Math.min(minY, body.position.y);
            minZ = Math.min(minZ, body.position.z);
            maxX = Math.max(maxX, body.position.x);
            maxY = Math.max(maxY, body.position.y);
            maxZ = Math.max(maxZ, body.position.z);
        }

        double maxSize = Math.max(Math.max(maxX - minX, maxY - minY), maxZ - minZ);
        Vector3D center = Vector3D.obtain(
            (minX + maxX) / 2.0,
            (minY + maxY) / 2.0,
            (minZ + maxZ) / 2.0
        );

        this.root = buildTree(bodies, center, maxSize);
    }

    private Node buildTree(List<CelestialBody> bodies, Vector3D center, double size) {
        if (bodies.isEmpty()) {
            return null;
        }

        if (bodies.size() == 1) {
            return new LeafNode(bodies.get(0), center, size);
        }

        InternalNode node = new InternalNode(center, size);
        // Suppress warning for generic array creation
        @SuppressWarnings("unchecked")
        List<CelestialBody>[] octants = (List<CelestialBody>[]) new List[8];
        for (int i = 0; i < 8; i++) {
            octants[i] = new ArrayList<>();
        }

        double halfSize = size / 2.0;

        for (CelestialBody body : bodies) {
            int octantIndex = getOctantIndex(body.position, center);
            octants[octantIndex].add(body);
        }

        for (int i = 0; i < 8; i++) {
            if (octants[i].isEmpty()) continue;
            
            Vector3D octantCenter = getOctantCenter(center, halfSize, i);
            
            // SECURITY: Prevent infinite recursion when bodies are at same/similar positions
            // If subdivision becomes too small or we have multiple bodies in same location,
            // create a single leaf node with the first body (simplified approximation)
            if (halfSize < 1e-10 || (octants[i].size() > 1 && bodiesAreAtSameLocation(octants[i]))) {
                // Use first body as representative for this location
                CelestialBody representative = octants[i].get(0);
                double combinedMass = octants[i].stream().mapToDouble(b -> b.mass).sum();
                
                // Create a virtual body with combined mass at the same position
                CelestialBody virtualBody = new CelestialBody(
                    "virtual_" + i, combinedMass, representative.radius, 
                    representative.color, representative.position, Vector3D.ZERO
                ) {};
                virtualBody.isStatic = true; // Mark as static since it's virtual
                
                LeafNode leafChild = new LeafNode(virtualBody, octantCenter, halfSize);
                node.addChild(leafChild);
                node.totalMass += virtualBody.mass;
                node.centerOfMass = node.centerOfMass.add(
                    virtualBody.position.scale(virtualBody.mass)
                );
            } else {
                Node child = buildTree(octants[i], octantCenter, halfSize);
                if (child != null) {
                    node.addChild(child);
                    if (child instanceof LeafNode) {
                        CelestialBody childBody = ((LeafNode) child).body;
                        node.totalMass += childBody.mass;
                        node.centerOfMass = node.centerOfMass.add(
                            childBody.position.scale(childBody.mass)
                        );
                    } else if (child instanceof InternalNode) {
                        InternalNode internalChild = (InternalNode) child;
                        node.totalMass += internalChild.totalMass;
                        node.centerOfMass = node.centerOfMass.add(
                            internalChild.centerOfMass.scale(internalChild.totalMass)
                        );
                    }
                }
            }
        }
        if (node.totalMass > 0) {
            node.centerOfMass = node.centerOfMass.scale(1.0 / node.totalMass);
        }

        return node;
    }

    private int getOctantIndex(Vector3D position, Vector3D center) {
        int index = 0;
        if (position.x > center.x) index |= 4;
        if (position.y > center.y) index |= 2;
        if (position.z > center.z) index |= 1;
        return index;
    }

    private Vector3D getOctantCenter(Vector3D parentCenter, double halfSize, int octantIndex) {
        double offsetX = ((octantIndex & 4) != 0) ? halfSize : -halfSize;
        double offsetY = ((octantIndex & 2) != 0) ? halfSize : -halfSize;
        double offsetZ = ((octantIndex & 1) != 0) ? halfSize : -halfSize;
        return parentCenter.add(Vector3D.obtain(offsetX, offsetY, offsetZ));
    }
    
    /**
     * Check if multiple bodies are at the same or very similar location
     * SECURITY: Prevents infinite subdivision when bodies occupy same space
     */
    private boolean bodiesAreAtSameLocation(List<CelestialBody> bodies) {
        if (bodies.size() <= 1) return false;
        
        Vector3D firstPos = bodies.get(0).position;
        double tolerance = 1e-6; // 1 micrometer tolerance for "same" position
        
        for (int i = 1; i < bodies.size(); i++) {
            Vector3D pos = bodies.get(i).position;
            double distance = firstPos.subtract(pos).length();
            if (distance > tolerance) {
                return false; // Bodies are sufficiently separated
            }
        }
        return true; // All bodies are at essentially the same location
    }

    @Override
    public Vector3D calculateNetForce(CelestialBody body) {
        if (this.root == null) {
            return Vector3D.ZERO;
        }
        return this.root.calculateForce(body);
    }
}