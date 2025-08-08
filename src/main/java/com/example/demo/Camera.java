package com.example.demo;

import org.lwjgl.opengl.GL11;

public class Camera {
    private Vector3D position;
    private Vector3D target;
    private Vector3D up;
    private double zoom;
    
    // SECURITY: Input validation prevents malformed camera states
    public Camera(Vector3D position, Vector3D target, Vector3D up, double zoom) {
        if (position == null || target == null || up == null) {
            throw new IllegalArgumentException("Camera vectors cannot be null");
        }
        if (zoom <= 0) {
            throw new IllegalArgumentException("Camera zoom must be positive");
        }
        
        this.position = position;
        this.target = target;
        this.up = up;
        this.zoom = zoom;
    }
    
    public static Camera createDefault() {
        return new Camera(
            new Vector3D(0, 0, 500),   // Much closer - 500 scaled units from sun
            Vector3D.ZERO,              // Look at origin (sun)
            new Vector3D(0, 1, 0),      // Y-axis up
            1.0                         // Default zoom
        );
    }
    
    public void setupProjection(int width, int height) {
        // SECURITY: Bounds checking prevents division by zero
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Viewport dimensions must be positive");
        }
        
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        
        // Set perspective projection
        double aspectRatio = (double) width / height;
        double fov = 45.0 / zoom; // Field of view in degrees (inverted zoom logic)
        double near = 1.0;        // 1 scaled unit
        double far = 10000.0;     // 10000 scaled units
        
        // Convert to radians and calculate perspective
        double fovRad = Math.toRadians(fov);
        double f = Math.cos(fovRad / 2.0) / Math.sin(fovRad / 2.0);
        
        // Manual perspective matrix for better control
        GL11.glFrustum(
            -near * aspectRatio / f, near * aspectRatio / f,
            -near / f, near / f,
            near, far
        );
    }
    
    public void setupView() {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        
        // Look at transformation
        Vector3D forward = target.subtract(position).normalize();
        Vector3D right = forward.cross(up).normalize();
        Vector3D actualUp = right.cross(forward).normalize();
        
        // Create view matrix manually for better control
        double[] matrix = {
            right.x, actualUp.x, -forward.x, 0,
            right.y, actualUp.y, -forward.y, 0,
            right.z, actualUp.z, -forward.z, 0,
            -right.dot(position), -actualUp.dot(position), forward.dot(position), 1
        };
        
        GL11.glMultMatrixd(matrix);
    }
    
    public void moveCamera(Vector3D offset) {
        this.position = this.position.add(offset);
    }
    
    public void zoomCamera(double factor) {
        // SECURITY: Bounds checking prevents extreme zoom values
        double newZoom = this.zoom * factor;
        if (newZoom > 0.1 && newZoom < 10.0) {
            this.zoom = newZoom;
        }
    }
    
    public void orbitAroundTarget(double angleX, double angleY) {
        // Orbit camera around target point
        Vector3D offset = position.subtract(target);
        double distance = offset.length();
        
        // Convert to spherical coordinates
        double theta = Math.atan2(offset.z, offset.x) + angleX;
        double phi = Math.acos(offset.y / distance) + angleY;
        
        // SECURITY: Clamp phi to prevent gimbal lock
        phi = Math.max(0.1, Math.min(Math.PI - 0.1, phi));
        
        // Convert back to cartesian
        Vector3D newOffset = new Vector3D(
            distance * Math.sin(phi) * Math.cos(theta),
            distance * Math.cos(phi),
            distance * Math.sin(phi) * Math.sin(theta)
        );
        
        this.position = target.add(newOffset);
    }
    
    // Getters
    public Vector3D getPosition() { return position; }
    public Vector3D getTarget() { return target; }
    public double getZoom() { return zoom; }
    
    // Setters with validation
    public void setTarget(Vector3D target) {
        if (target != null) {
            this.target = target;
        }
    }
}
