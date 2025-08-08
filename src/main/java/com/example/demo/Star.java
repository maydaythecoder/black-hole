package com.example.demo;

public class Star extends CelestialBody {
    private final double luminosity;

    public Star(String id, double mass, double radius, float[] color, 
                boolean isStatic, Vector3D position, Vector3D velocity, 
                double luminosity) {
        // Remove explicit validation call - super constructor already validates
        super(id, mass, radius, color, position, velocity);
        setStatic(isStatic);
        this.luminosity = luminosity;
    }

    public double getLuminosity() {
        return luminosity;
    }
}