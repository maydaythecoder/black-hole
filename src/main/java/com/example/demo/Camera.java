package com.example.demo;

import org.lwjgl.opengl.GL11;

public class Camera {
    private Vector3D position;
    private Vector3D forward;  // Direction camera is looking (replaces target-based system)
    private Vector3D up;
    private double zoom;
    
    // Free-move camera rotation angles
    private double yaw;   // Left/right rotation
    private double pitch; // Up/down rotation
    
    // SECURITY: Input validation prevents malformed camera states
    public Camera(Vector3D position, Vector3D target, Vector3D up, double zoom) {
        if (position == null || target == null || up == null) {
            throw new IllegalArgumentException("Camera vectors cannot be null");
        }
        if (zoom <= 0) {
            throw new IllegalArgumentException("Camera zoom must be positive");
        }
        
        this.position = position;
        this.forward = target.subtract(position).normalize(); // Convert target to forward direction
        this.up = up.normalize();
        this.zoom = zoom;
        
        // Calculate initial yaw and pitch from forward vector
        this.yaw = Math.atan2(forward.x, forward.z);
        this.pitch = Math.asin(-forward.y);
    }
    
    public static Camera createDefault() {
        return new Camera(
            Vector3D.obtain(0, 50, 200),    // Start above and behind the solar system
            Vector3D.obtain(0, 0, 0),       // Look at center (sun)
            Vector3D.obtain(0, 1, 0),       // Y-axis up
            1.0                             // Default zoom
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
        double near = 1.0;        // 1 unit close
        double far = 2000.0;      // 2000 units (covers grid and solar system)
        
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
        
        // Update forward vector from yaw and pitch (free-look camera)
        updateForwardVector();
        
        // Calculate right and up vectors for the camera basis
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
    
    /**
     * Update the forward vector based on yaw and pitch angles
     * SECURITY: Clamps pitch to prevent gimbal lock
     */
    private void updateForwardVector() {
        // SECURITY: Clamp pitch to prevent looking straight up/down (gimbal lock)
        double clampedPitch = Math.max(-Math.PI/2 + 0.1, Math.min(Math.PI/2 - 0.1, pitch));
        
        // Calculate forward vector from spherical coordinates
        this.forward = Vector3D.obtain(
            Math.sin(yaw) * Math.cos(clampedPitch),   // X
            -Math.sin(clampedPitch),                  // Y (negative for correct up/down)
            Math.cos(yaw) * Math.cos(clampedPitch)    // Z
        ).normalize();
    }
    
    /**
     * Move camera in world coordinates
     */
    public void moveCamera(Vector3D offset) {
        this.position = this.position.add(offset);
    }
    
    /**
     * Move camera relative to its current orientation
     * SECURITY: Bounds checking on movement speed
     */
    public void moveRelative(double forward, double right, double up) {
        // SECURITY: Clamp movement values to prevent extreme speeds
        forward = Math.max(-100, Math.min(100, forward));
        right = Math.max(-100, Math.min(100, right));
        up = Math.max(-100, Math.min(100, up));
        
        updateForwardVector(); // Ensure forward vector is current
        
        Vector3D rightVec = this.forward.cross(this.up).normalize();
        Vector3D upVec = rightVec.cross(this.forward).normalize();
        
        Vector3D movement = Vector3D.obtain(0, 0, 0);
        if (forward != 0) movement = movement.add(this.forward.scale(forward));
        if (right != 0) movement = movement.add(rightVec.scale(right));
        if (up != 0) movement = movement.add(upVec.scale(up));
        
        this.position = this.position.add(movement);
    }
    
    /**
     * Rotate camera view (mouse look)
     * SECURITY: Input validation prevents extreme rotation values
     */
    public void rotate(double deltaYaw, double deltaPitch) {
        // SECURITY: Clamp rotation deltas to reasonable values
        deltaYaw = Math.max(-0.1, Math.min(0.1, deltaYaw));
        deltaPitch = Math.max(-0.1, Math.min(0.1, deltaPitch));
        
        this.yaw += deltaYaw;
        this.pitch += deltaPitch;
        
        // Keep yaw in range [0, 2π]
        while (this.yaw > Math.PI * 2) this.yaw -= Math.PI * 2;
        while (this.yaw < 0) this.yaw += Math.PI * 2;
        
        // Keep pitch in reasonable range (prevent flipping)
        this.pitch = Math.max(-Math.PI/2 + 0.1, Math.min(Math.PI/2 - 0.1, this.pitch));
    }
    
    public void zoomCamera(double factor) {
        // SECURITY: Bounds checking prevents extreme zoom values
        double newZoom = this.zoom * factor;
        if (newZoom > 0.1 && newZoom < 10.0) {
            this.zoom = newZoom;
        }
    }
    
    // Getters
    public Vector3D getPosition() { return position; }
    public Vector3D getForward() { return forward; }
    public double getZoom() { return zoom; }
    public double getYaw() { return yaw; }
    public double getPitch() { return pitch; }
    
    // Calculate target point for debug/display purposes
    public Vector3D getTarget() {
        updateForwardVector();
        return position.add(forward.scale(100)); // Target 100 units ahead
    }
}
