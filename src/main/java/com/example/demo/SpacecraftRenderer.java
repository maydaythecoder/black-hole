package com.example.demo;

public class SpacecraftRenderer implements BodyRenderer {

    @Override
    public void setupGL() {
        // Placeholder for OpenGL setup specific to Spacecraft rendering
        System.out.println("Setting up GL for Spacecraft rendering...");
    }

    @Override
    public void draw(CelestialBody body) {
        // Placeholder for drawing a Spacecraft using OpenGL
        if (body instanceof Spacecraft) {
            Spacecraft spacecraft = (Spacecraft) body;
            System.out.println("Drawing Spacecraft: " + spacecraft.getId() + " at position " + spacecraft.getPosition());
            // Actual OpenGL drawing calls will go here
        }
    }
}