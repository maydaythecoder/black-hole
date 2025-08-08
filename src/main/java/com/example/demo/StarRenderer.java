package com.example.demo;

import org.lwjgl.opengl.GL11;

public class StarRenderer implements BodyRenderer {

    @Override
    public void setupGL() {
        // SECURITY: Enable blending for glowing effect with safe parameters
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        // Enable lighting system
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_LIGHT0);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
        
        // Set up strong sun lighting
        float[] lightAmbient = {0.3f, 0.3f, 0.3f, 1.0f};  // Stronger ambient
        float[] lightDiffuse = {1.0f, 0.9f, 0.7f, 1.0f};  // Warm sunlight
        float[] lightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};
        
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_AMBIENT, lightAmbient);
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, lightDiffuse);
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_SPECULAR, lightSpecular);
        
        // Set light attenuation for realistic falloff
        GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_CONSTANT_ATTENUATION, 1.0f);
        GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_LINEAR_ATTENUATION, 0.0f);
        GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_QUADRATIC_ATTENUATION, 0.0f);
    }

    @Override
    public void draw(CelestialBody body) {
        if (!(body instanceof Star)) {
            System.err.println("StarRenderer received a non-Star body.");
            return;
        }
        
        Star star = (Star) body;
        Vector3D pos = star.getPosition();
        float[] color = star.getColor();
        double radius = star.getRadius();
        
        // SECURITY: Bounds checking on color values
        float r = Math.max(0.0f, Math.min(1.0f, color[0]));
        float g = Math.max(0.0f, Math.min(1.0f, color[1]));
        float b = Math.max(0.0f, Math.min(1.0f, color[2]));
        
        // Set light position to star position
        float[] lightPos = {(float)pos.x, (float)pos.y, (float)pos.z, 1.0f};
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPos);
        
        // Save current matrix
        GL11.glPushMatrix();
        
        // Move to star position
        GL11.glTranslated(pos.x, pos.y, pos.z);
        
        // Set star color with emissive properties
        float[] emissive = {r * 0.5f, g * 0.5f, b * 0.5f, 1.0f};
        float[] diffuse = {r, g, b, 1.0f};
        
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_EMISSION, emissive);
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_DIFFUSE, diffuse);
        
        // Draw sphere using quad approximation
        drawSphere(radius, 16, 16);
        
        // Debug: Print when actually drawing
        if (System.currentTimeMillis() % 10000 < 100) {
            System.out.printf("Drawing Star %s at (%.1f, %.1f, %.1f) with radius %.1f%n",
                star.getId(), pos.x, pos.y, pos.z, radius);
        }
        
        // Reset material properties
        float[] noEmission = {0.0f, 0.0f, 0.0f, 1.0f};
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_EMISSION, noEmission);
        
        // Restore matrix
        GL11.glPopMatrix();
    }
    
    private void drawSphere(double radius, int slices, int stacks) {
        // SECURITY: Validate parameters to prevent excessive geometry
        int safeSlices = Math.max(4, Math.min(32, slices));
        int safeStacks = Math.max(4, Math.min(32, stacks));
        
        GL11.glBegin(GL11.GL_TRIANGLES);
        
        for (int i = 0; i < safeStacks; i++) {
            double lat0 = Math.PI * (-0.5 + (double) i / safeStacks);
            double z0 = Math.sin(lat0);
            double zr0 = Math.cos(lat0);
            
            double lat1 = Math.PI * (-0.5 + (double) (i + 1) / safeStacks);
            double z1 = Math.sin(lat1);
            double zr1 = Math.cos(lat1);
            
            for (int j = 0; j < safeSlices; j++) {
                double lng0 = 2 * Math.PI * (double) j / safeSlices;
                double lng1 = 2 * Math.PI * (double) (j + 1) / safeSlices;
                
                double x0 = Math.cos(lng0);
                double y0 = Math.sin(lng0);
                double x1 = Math.cos(lng1);
                double y1 = Math.sin(lng1);
                
                // First triangle
                GL11.glNormal3d(x0 * zr0, y0 * zr0, z0);
                GL11.glVertex3d(radius * x0 * zr0, radius * y0 * zr0, radius * z0);
                
                GL11.glNormal3d(x1 * zr0, y1 * zr0, z0);
                GL11.glVertex3d(radius * x1 * zr0, radius * y1 * zr0, radius * z0);
                
                GL11.glNormal3d(x0 * zr1, y0 * zr1, z1);
                GL11.glVertex3d(radius * x0 * zr1, radius * y0 * zr1, radius * z1);
                
                // Second triangle
                GL11.glNormal3d(x1 * zr0, y1 * zr0, z0);
                GL11.glVertex3d(radius * x1 * zr0, radius * y1 * zr0, radius * z0);
                
                GL11.glNormal3d(x1 * zr1, y1 * zr1, z1);
                GL11.glVertex3d(radius * x1 * zr1, radius * y1 * zr1, radius * z1);
                
                GL11.glNormal3d(x0 * zr1, y0 * zr1, z1);
                GL11.glVertex3d(radius * x0 * zr1, radius * y0 * zr1, radius * z1);
            }
        }
        
        GL11.glEnd();
    }
}