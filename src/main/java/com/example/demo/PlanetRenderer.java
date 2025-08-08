package com.example.demo;

public class PlanetRenderer implements BodyRenderer {

    @Override
    public void setupGL() {
        // Placeholder for OpenGL setup specific to planets
        System.out.println("Setting up GL for PlanetRenderer");
    }

    @Override
    public void draw(CelestialBody body) {
        // Placeholder for drawing a planet using OpenGL
        if (body instanceof Planet) {
            Planet planet = (Planet) body;
            System.out.println("Drawing Planet: " + planet.getId() + " at position " + planet.getPosition());
        } else {
            System.out.println("Attempted to draw non-Planet object with PlanetRenderer");
        }
    }
}