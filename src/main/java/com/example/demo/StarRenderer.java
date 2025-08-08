package com.example.demo;

public class StarRenderer implements BodyRenderer {

    @Override
    public void setupGL() {
        // Basic OpenGL setup for rendering stars (placeholder)
        System.out.println("Setting up GL for Star rendering...");
    }

    @Override
    public void draw(CelestialBody body) {
        // Basic OpenGL drawing for a star (placeholder)
        if (!(body instanceof Star)) {
            System.err.println("StarRenderer received a non-Star body.");
            return;
        }
        Star star = (Star) body;
        System.out.println("Drawing Star: " + star.getId() + " at position " + star.getPosition());
        // Actual OpenGL drawing code will go here later
    }
}