package com.example.demo;

import java.util.*;
import java.util.stream.Collectors;

public class SimulationManager {
    private final Map<String, CelestialBody> bodies;
    private final Renderer renderer;
    private final Camera camera;
    private double timeScale;
    private boolean paused;
    
    // SECURITY: Simulation bounds prevent runaway calculations
    private static final double MAX_TIME_SCALE = 1e6;  // Max 1 million times speed
    private static final double MIN_TIME_SCALE = 0.1;  // Min 0.1 times speed
    
    public SimulationManager() {
        this.bodies = new HashMap<>();
        this.renderer = new Renderer();
        this.camera = Camera.createDefault();
        this.timeScale = 86400.0; // Start at 1 day per second
        this.paused = false;
    }
    
    public void loadSolarSystem() {
        // TEMPORARY: Force simple system for visibility testing
        System.out.println("Using simplified solar system for debugging...");
        createMinimalSystem();
        
        // TODO: Re-enable JSON loading after fixing visibility issues
        /*
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
        */
    }
    
    private void createMinimalSystem() {
        // SECURITY: Validated minimal system with proper positioning for visibility
        bodies.clear();
        
        // Sun at center - large and bright
        Star sun = new Star("sun", 1.989e30, 50, 
            new float[]{1.0f, 0.9f, 0.2f}, true,  // Bright yellow
            Vector3D.ZERO, Vector3D.ZERO, 3.828e26);
        bodies.put("sun", sun);
        
        // Mercury - closest to sun
        Planet mercury = new Planet("mercury", 3.301e23, 15,
            new float[]{0.7f, 0.7f, 0.7f}, false,  // Gray
            Vector3D.obtain(120, 0, 0), Vector3D.ZERO,
            "sun", false);
        mercury.setParentBody(sun);
        bodies.put("mercury", mercury);
        
        // Venus - second planet
        Planet venus = new Planet("venus", 4.867e24, 18,
            new float[]{1.0f, 0.8f, 0.4f}, false,  // Yellow-orange
            Vector3D.obtain(180, 0, 0), Vector3D.ZERO,
            "sun", false);
        venus.setParentBody(sun);
        bodies.put("venus", venus);
        
        // Earth - third planet (blue)
        Planet earth = new Planet("earth", 5.972e24, 20,
            new float[]{0.2f, 0.6f, 1.0f}, false,  // Blue
            Vector3D.obtain(250, 0, 0), Vector3D.ZERO,
            "sun", false);
        earth.setParentBody(sun);
        bodies.put("earth", earth);
        
        // Mars - fourth planet (red)
        Planet mars = new Planet("mars", 6.39e23, 16,
            new float[]{1.0f, 0.4f, 0.2f}, false,  // Red
            Vector3D.obtain(320, 0, 0), Vector3D.ZERO,
            "sun", false);
        mars.setParentBody(sun);
        bodies.put("mars", mars);
        
        // Jupiter - large gas giant
        Planet jupiter = new Planet("jupiter", 1.898e27, 35,
            new float[]{0.8f, 0.6f, 0.3f}, false,  // Orange-brown
            Vector3D.obtain(420, 0, 0), Vector3D.ZERO,
            "sun", true);  // Gas giant
        jupiter.setParentBody(sun);
        bodies.put("jupiter", jupiter);
        
        // Saturn - with rings (visually distinctive)
        Planet saturn = new Planet("saturn", 5.683e26, 30,
            new float[]{0.9f, 0.8f, 0.6f}, false,  // Pale yellow
            Vector3D.obtain(520, 0, 0), Vector3D.ZERO,
            "sun", true);  // Gas giant (will show rings)
        saturn.setParentBody(sun);
        bodies.put("saturn", saturn);
        
        System.out.println("Created complete solar system lineup - Sun to Saturn");
    }
    
    public void update(double deltaTime) {
        if (paused) return;
        
        // TEMPORARY: Disable physics to keep planets in lineup for visibility test
        // TODO: Re-enable physics after confirming visibility
        /*
        // SECURITY: Bounds checking on time scale
        double clampedTimeScale = Math.max(MIN_TIME_SCALE, 
                                  Math.min(MAX_TIME_SCALE, timeScale));
        double scaledDeltaTime = deltaTime * clampedTimeScale;
        
        // Update physics
        List<CelestialBody> bodyList = new ArrayList<>(bodies.values());
        PhysicsUtil.updateAllBodies(bodyList, scaledDeltaTime);
        */
        
        // Update camera to follow interesting objects
        updateCameraTarget();
    }
    
    private void updateCameraTarget() {
        // Always look at the sun (center of solar system) for proper view
        CelestialBody sun = bodies.get("sun");
        if (sun != null) {
            // Set camera target to sun position, but scaled for rendering
            Vector3D sunPos = scalePositionForRendering(sun.getPosition());
            camera.setTarget(sunPos);
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
        // No scaling needed - positions are already set for visibility
        return position; // Keep original positions as they're already sized for rendering
    }
    
    private double scaleRadiusForRendering(double radius) {
        // No scaling needed - radii are already set for visibility
        return radius; // Keep original radii as they're already sized for rendering
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
