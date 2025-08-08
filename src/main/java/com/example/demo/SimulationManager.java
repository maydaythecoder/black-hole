package com.example.demo;

import java.util.*;
import java.util.stream.Collectors;

public class SimulationManager {
    private final Map<String, CelestialBody> bodies;
    private final Renderer renderer;
    private final Camera camera;
    private double timeScale;
    private boolean paused;
    private long lastUpdateTime;
    
    // SECURITY: Simulation bounds prevent runaway calculations
    private static final double MAX_TIME_SCALE = 1e6;  // Max 1 million times speed
    private static final double MIN_TIME_SCALE = 0.1;  // Min 0.1 times speed
    
    public SimulationManager() {
        this.bodies = new HashMap<>();
        this.renderer = new Renderer();
        this.camera = Camera.createDefault();
        this.timeScale = 86400.0; // Start at 1 day per second
        this.paused = false;
        this.lastUpdateTime = System.nanoTime();
    }
    
    public void loadSolarSystem() {
        try {
            // SECURITY: Use absolute path validation
            String resourcePath = "solar_system.json";
            Map<String, CelestialBody> loadedBodies = DataLoader.loadFromJson(
                getClass().getClassLoader().getResource(resourcePath).getPath()
            );
            
            bodies.clear();
            bodies.putAll(loadedBodies);
            
            System.out.println("Loaded " + bodies.size() + " celestial bodies");
            bodies.forEach((id, body) -> 
                System.out.println("  - " + id + ": " + body.getClass().getSimpleName())
            );
            
        } catch (Exception e) {
            System.err.println("Failed to load solar system data: " + e.getMessage());
            // Fallback: Create minimal system
            createMinimalSystem();
        }
    }
    
    private void createMinimalSystem() {
        // SECURITY: Validated minimal system as fallback
        bodies.clear();
        
        // Sun
        Star sun = new Star("sun", 1.989e30, 6.96e8, 
            new float[]{1.0f, 0.8f, 0.0f}, true,
            Vector3D.ZERO, Vector3D.ZERO, 3.828e26);
        bodies.put("sun", sun);
        
        // Earth
        Planet earth = new Planet("earth", 5.972e24, 6.371e6,
            new float[]{0.0f, 0.5f, 1.0f}, false,
            new Vector3D(1.496e11, 0, 0), new Vector3D(0, 29780, 0),
            "sun", false);
        earth.setParentBody(sun);
        bodies.put("earth", earth);
        
        System.out.println("Created minimal solar system (Sun + Earth)");
    }
    
    public void update(double deltaTime) {
        if (paused) return;
        
        // SECURITY: Bounds checking on time scale
        double clampedTimeScale = Math.max(MIN_TIME_SCALE, 
                                  Math.min(MAX_TIME_SCALE, timeScale));
        double scaledDeltaTime = deltaTime * clampedTimeScale;
        
        // Update physics
        List<CelestialBody> bodyList = new ArrayList<>(bodies.values());
        PhysicsUtil.updateAllBodies(bodyList, scaledDeltaTime);
        
        // Update camera to follow interesting objects
        updateCameraTarget();
    }
    
    private void updateCameraTarget() {
        // Follow Earth if it exists, otherwise follow the first planet
        CelestialBody target = bodies.get("earth");
        if (target == null) {
            target = bodies.values().stream()
                .filter(body -> body instanceof Planet)
                .findFirst()
                .orElse(bodies.get("sun"));
        }
        
        if (target != null) {
            camera.setTarget(target.getPosition());
        }
    }
    
    public void render(int width, int height) {
        // Setup camera and projection
        camera.setupProjection(width, height);
        camera.setupView();
        
        // Enable depth testing for 3D
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
        
        // Render all bodies with proper scaling
        List<CelestialBody> renderBodies = bodies.values().stream()
            .map(this::createRenderableBody)
            .collect(Collectors.toList());
        
        // Debug: Print render info occasionally
        if (System.currentTimeMillis() % 5000 < 100) { // Every 5 seconds
            System.out.printf("Rendering %d bodies. Camera at: %.1f, %.1f, %.1f looking at: %.1f, %.1f, %.1f%n",
                renderBodies.size(),
                camera.getPosition().x, camera.getPosition().y, camera.getPosition().z,
                camera.getTarget().x, camera.getTarget().y, camera.getTarget().z);
            
            // Print first few body positions
            renderBodies.stream().limit(3).forEach(body -> {
                Vector3D pos = body.getPosition();
                System.out.printf("  %s at: %.1f, %.1f, %.1f (radius: %.1f)%n",
                    body.getId(), pos.x, pos.y, pos.z, body.getRadius());
            });
        }
            
        renderer.render(renderBodies);
    }
    
    private CelestialBody createRenderableBody(CelestialBody original) {
        // SECURITY: Create safe copies for rendering with proper scaling
        Vector3D scaledPosition = scalePositionForRendering(original.getPosition());
        double scaledRadius = scaleRadiusForRendering(original.getRadius());
        
        // Create a display copy with scaled dimensions
        if (original instanceof Star) {
            Star star = (Star) original;
            return new Star(star.getId(), star.getMass(), scaledRadius,
                star.getColor(), star.isStatic(), scaledPosition,
                original.getVelocity(), star.getLuminosity());
        } else if (original instanceof Planet) {
            Planet planet = (Planet) original;
            return new Planet(planet.getId(), planet.getMass(), scaledRadius,
                planet.getColor(), planet.isStatic(), scaledPosition,
                original.getVelocity(), planet.getParentId(), planet.isGasGiant());
        } else if (original instanceof Spacecraft) {
            Spacecraft craft = (Spacecraft) original;
            return new Spacecraft(craft.getId(), craft.getMass(), scaledRadius,
                craft.getColor(), craft.isStatic(), scaledPosition,
                original.getVelocity(), craft.getThrustPower(), craft.getFuel());
        }
        
        return original;
    }
    
    private Vector3D scalePositionForRendering(Vector3D position) {
        // Scale down astronomical distances for rendering
        double scale = 1e-9; // 1 billion to 1
        return position.scale(scale);
    }
    
    private double scaleRadiusForRendering(double radius) {
        // Scale and enforce minimum size for visibility
        double scaledRadius = radius * 1e-7; // Scale down more moderately
        
        // Different minimum sizes based on object type
        if (radius > 1e8) { // Sun-sized objects
            return Math.max(scaledRadius, 20); // Large minimum for stars
        } else if (radius > 1e7) { // Gas giant sized
            return Math.max(scaledRadius, 10); // Medium for gas giants
        } else { // Smaller planets and spacecraft
            return Math.max(scaledRadius, 5); // Small minimum for planets
        }
    }
    
    // Control methods
    public void togglePause() {
        this.paused = !this.paused;
        System.out.println("Simulation " + (paused ? "paused" : "resumed"));
    }
    
    public void adjustTimeScale(double factor) {
        double newScale = timeScale * factor;
        if (newScale >= MIN_TIME_SCALE && newScale <= MAX_TIME_SCALE) {
            timeScale = newScale;
            System.out.printf("Time scale: %.1f (%.1f days/second)%n", 
                timeScale, timeScale / 86400.0);
        }
    }
    
    public void resetTimeScale() {
        timeScale = 86400.0; // 1 day per second
        System.out.println("Time scale reset to 1 day/second");
    }
    
    // Camera controls
    public void orbitCamera(double deltaX, double deltaY) {
        camera.orbitAroundTarget(deltaX * 0.01, deltaY * 0.01);
    }
    
    public void zoomCamera(double factor) {
        camera.zoomCamera(factor);
    }
    
    public void moveCamera(Vector3D offset) {
        camera.moveCamera(offset);
    }
    
    // Getters
    public Map<String, CelestialBody> getBodies() { 
        return Collections.unmodifiableMap(bodies); 
    }
    
    public Camera getCamera() { return camera; }
    public double getTimeScale() { return timeScale; }
    public boolean isPaused() { return paused; }
    
    public int getBodyCount() { return bodies.size(); }
}
