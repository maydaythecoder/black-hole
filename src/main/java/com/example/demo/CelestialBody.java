package com.example.demo;

public class CelestialBody {

    static final double G = 6.674e-11;
    static final double MAX_DISTANCE = 1e16; // Optimization threshold

    protected final String id;
    protected final double mass;
    protected final double radius;
    protected final float[] color;
    protected boolean isStatic;
    protected Vector3D position;
    protected Vector3D velocity;

    public CelestialBody(String id, double mass, double radius, float[] color, Vector3D position, Vector3D velocity) {
        validateCelestialBody(mass, radius);  // Validate before assignment
        
        this.id = id;
        this.mass = mass;
        this.radius = radius;
        this.color = color != null && color.length == 3 ? color : new float[]{1.0f, 1.0f, 1.0f}; // Default to white if null or invalid
        this.isStatic = false; // Default value
        this.position = position;
        this.velocity = velocity;
    }

    protected static void validateCelestialBody(double mass, double radius) {
        if (mass <= 0 || radius <= 0) {
            throw new IllegalArgumentException("Mass and radius must be positive.");
        }
    }

    public void updatePosition(double deltaTime) {
        position = position.add(velocity.scale(deltaTime));
    }

    public void applyForce(Vector3D force, double deltaTime) {
        if (!isStatic) {
            velocity = velocity.add(force.scale(deltaTime / mass));
        }
    }

    public static Vector3D calculateGravity(CelestialBody a, CelestialBody b) {
        if (a.isStatic && b.isStatic) return Vector3D.ZERO;
        
        // Use subtract() instead of sub()
        Vector3D delta = b.position.subtract(a.position);
        
        double dist = delta.length();
        // Avoid division by zero and very large distances
        if (dist > MAX_DISTANCE || dist < 1e-5) {
            return Vector3D.ZERO;
        }
        double force = (G * a.mass * b.mass) / (dist * dist);
        return delta.normalize().scale(force);
    }

    // Getters (add more as needed)
    public String getId() {
        return id;
    }

    public double getMass() {
        return mass;
    }

    public double getRadius() {
        return radius;
    }

    public float[] getColor() {
        return color;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public Vector3D getPosition() {
        return position;
    }

    public Vector3D getVelocity() {
        return velocity;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public void setPosition(Vector3D position) {
        this.position = position;
    }

    public void setVelocity(Vector3D velocity) {
        this.velocity = velocity;
    }
}