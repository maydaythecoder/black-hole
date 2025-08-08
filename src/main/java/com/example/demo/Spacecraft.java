package com.example.demo;

public class Spacecraft extends CelestialBody {

    private final double thrustPower;
    private double fuel;

    // Placeholder for the FUEL_EFFICIENCY constant
    private static final double FUEL_EFFICIENCY = 0.1; // Example value, adjust as needed

    public Spacecraft(String id, double mass, double radius, float[] color, boolean isStatic, Vector3D position, Vector3D velocity, double thrustPower, double fuel) {
        super(id, mass, radius, color, position, velocity);
        setStatic(isStatic);
        this.thrustPower = thrustPower;
        this.fuel = fuel;
    }

    public double getThrustPower() {
        return thrustPower;
    }

    public double getFuel() {
        return fuel;
    }

    public void setFuel(double fuel) {
        this.fuel = fuel;
    }

    public void applyThrust(Vector3D dir, double deltaTime) {
        if (fuel <= 0) {
            return;
        }
        Vector3D thrust = dir.normalize().scale(thrustPower * deltaTime);
        applyForce(thrust, deltaTime);
        fuel -= thrust.length() * FUEL_EFFICIENCY;
    }
}