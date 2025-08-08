package com.example.demo;

public class Planet extends CelestialBody {

    private final String parentId;
    private final boolean isGasGiant;
    private transient CelestialBody parentBody;

    public Planet(String id, double mass, double radius, float[] color, boolean isStatic, Vector3D position, Vector3D velocity, String parentId, boolean isGasGiant) {
        super(id, mass, radius, color, position, velocity);
        setStatic(isStatic);
        validateCelestialBody(mass, radius);

        this.parentId = parentId;
        this.isGasGiant = isGasGiant;
    }

    public String getParentId() {
        return parentId;
    }

    public boolean isGasGiant() {
        return isGasGiant;
    }

    public CelestialBody getParentBody() {
        return parentBody;
    }

    public void setParentBody(CelestialBody parentBody) {
        this.parentBody = parentBody;
    }
}