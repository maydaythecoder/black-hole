package com.example.demo;

public class CelestialBody {

    protected final String id;
    protected final double mass;
    protected final double radius;
    protected final float[] color;
    protected boolean isStatic;
    protected Vector3D position;
    protected Vector3D velocity;

    public CelestialBody(String id, double mass, double radius, float[] color, Vector3D position, Vector3D velocity) {
        if (mass <= 0 || radius <= 0) {
            throw new IllegalArgumentException("Mass and radius must be positive.");
        }
        if (color == null || color.length != 3) {
            throw new IllegalArgumentException("Color must be an RGB array of size 3.");
        }
        this.id = id;
        this.mass = mass;
        this.radius = radius;
        this.color = color;
        this.isStatic = false; // Default value
        this.position = position;
        this.velocity = velocity;
    }

    public void updatePosition(double deltaTime) {
        position = position.add(velocity.scale(deltaTime));
    }

    public void applyForce(Vector3D force, double deltaTime) {
        if (!isStatic) {
            velocity = velocity.add(force.scale(deltaTime / mass));
        }
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