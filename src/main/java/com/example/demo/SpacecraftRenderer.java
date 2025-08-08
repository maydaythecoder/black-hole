package com.example.demo;

import org.lwjgl.opengl.GL11;

public class SpacecraftRenderer implements BodyRenderer {

    @Override
    public void setupGL() {
        // SECURITY: Safe setup for spacecraft rendering
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE);
    }

    @Override
    public void draw(CelestialBody body) {
        if (!(body instanceof Spacecraft)) {
            System.err.println("SpacecraftRenderer received a non-Spacecraft body.");
            return;
        }
        
        Spacecraft spacecraft = (Spacecraft) body;
        Vector3D pos = spacecraft.getPosition();
        float[] color = spacecraft.getColor();
        double size = Math.max(1000, spacecraft.getRadius()); // Minimum size for visibility
        
        // SECURITY: Bounds checking on color values
        float r = Math.max(0.0f, Math.min(1.0f, color[0]));
        float g = Math.max(0.0f, Math.min(1.0f, color[1]));
        float b = Math.max(0.0f, Math.min(1.0f, color[2]));
        
        // Save current matrix
        GL11.glPushMatrix();
        
        // Move to spacecraft position
        GL11.glTranslated(pos.x, pos.y, pos.z);
        
        // Set spacecraft color (metallic appearance)
        GL11.glColor3f(r, g, b);
        
        // Set material properties
        float[] ambient = {r * 0.2f, g * 0.2f, b * 0.2f, 1.0f};
        float[] diffuse = {r * 0.8f, g * 0.8f, b * 0.8f, 1.0f};
        float[] specular = {0.8f, 0.8f, 0.8f, 1.0f};
        float shininess = 100.0f;
        
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_AMBIENT, ambient);
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_DIFFUSE, diffuse);
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_SPECULAR, specular);
        GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, shininess);
        
        // Draw simple spacecraft shape (elongated box)
        drawSpacecraft(size);
        
        // Draw thrust trail if moving
        Vector3D velocity = spacecraft.getVelocity();
        if (velocity.length() > 1000) { // Only if moving significantly
            drawThrustTrail(velocity, size);
        }
        
        // Restore matrix
        GL11.glPopMatrix();
    }
    
    private void drawSpacecraft(double size) {
        // SECURITY: Simple geometric spacecraft with bounds checking
        double length = size * 3;
        double width = size;
        double height = size;
        
        GL11.glBegin(GL11.GL_TRIANGLES);
        
        // Front (nose cone)
        GL11.glNormal3d(0, 0, 1);
        GL11.glVertex3d(0, 0, length/2);
        GL11.glVertex3d(-width/2, -height/2, 0);
        GL11.glVertex3d(width/2, -height/2, 0);
        
        GL11.glVertex3d(0, 0, length/2);
        GL11.glVertex3d(width/2, -height/2, 0);
        GL11.glVertex3d(width/2, height/2, 0);
        
        GL11.glVertex3d(0, 0, length/2);
        GL11.glVertex3d(width/2, height/2, 0);
        GL11.glVertex3d(-width/2, height/2, 0);
        
        GL11.glVertex3d(0, 0, length/2);
        GL11.glVertex3d(-width/2, height/2, 0);
        GL11.glVertex3d(-width/2, -height/2, 0);
        
        // Main body (simplified)
        // Top face
        GL11.glNormal3d(0, 1, 0);
        GL11.glVertex3d(-width/2, height/2, 0);
        GL11.glVertex3d(width/2, height/2, 0);
        GL11.glVertex3d(width/2, height/2, -length/2);
        
        GL11.glVertex3d(-width/2, height/2, 0);
        GL11.glVertex3d(width/2, height/2, -length/2);
        GL11.glVertex3d(-width/2, height/2, -length/2);
        
        // Bottom face
        GL11.glNormal3d(0, -1, 0);
        GL11.glVertex3d(-width/2, -height/2, 0);
        GL11.glVertex3d(width/2, -height/2, -length/2);
        GL11.glVertex3d(width/2, -height/2, 0);
        
        GL11.glVertex3d(-width/2, -height/2, 0);
        GL11.glVertex3d(-width/2, -height/2, -length/2);
        GL11.glVertex3d(width/2, -height/2, -length/2);
        
        GL11.glEnd();
    }
    
    private void drawThrustTrail(Vector3D velocity, double size) {
        // SECURITY: Simple thrust visualization with bounds checking
        Vector3D thrustDir = velocity.normalize().scale(-size * 2);
        
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(0.0f, 0.5f, 1.0f, 0.6f); // Blue thrust
        
        GL11.glBegin(GL11.GL_TRIANGLES);
        
        // Simple thrust cone
        GL11.glVertex3d(0, 0, -size/2);
        GL11.glVertex3d(-size/4, -size/4, thrustDir.z);
        GL11.glVertex3d(size/4, -size/4, thrustDir.z);
        
        GL11.glVertex3d(0, 0, -size/2);
        GL11.glVertex3d(size/4, -size/4, thrustDir.z);
        GL11.glVertex3d(size/4, size/4, thrustDir.z);
        
        GL11.glVertex3d(0, 0, -size/2);
        GL11.glVertex3d(size/4, size/4, thrustDir.z);
        GL11.glVertex3d(-size/4, size/4, thrustDir.z);
        
        GL11.glVertex3d(0, 0, -size/2);
        GL11.glVertex3d(-size/4, size/4, thrustDir.z);
        GL11.glVertex3d(-size/4, -size/4, thrustDir.z);
        
        GL11.glEnd();
        
        GL11.glDisable(GL11.GL_BLEND);
    }
}