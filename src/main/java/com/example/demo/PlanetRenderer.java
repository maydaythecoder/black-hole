package com.example.demo;

import org.lwjgl.opengl.GL11;

public class PlanetRenderer implements BodyRenderer {

    @Override
    public void setupGL() {
        // SECURITY: Safe material setup for planets
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
        
        // Enable smooth shading and lighting
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_NORMALIZE); // Normalize normals after scaling
    }

    @Override
    public void draw(CelestialBody body) {
        if (!(body instanceof Planet)) {
            System.err.println("PlanetRenderer received a non-Planet body.");
            return;
        }
        
        Planet planet = (Planet) body;
        Vector3D pos = planet.getPosition();
        float[] color = planet.getColor();
        double radius = planet.getRadius();
        
        // SECURITY: Bounds checking on color and radius values
        float r = Math.max(0.0f, Math.min(1.0f, color[0]));
        float g = Math.max(0.0f, Math.min(1.0f, color[1]));
        float b = Math.max(0.0f, Math.min(1.0f, color[2]));
        double safeRadius = Math.max(100, radius); // Minimum radius for visibility
        
        // Save current matrix
        GL11.glPushMatrix();
        
        // Move to planet position
        GL11.glTranslated(pos.x, pos.y, pos.z);
        
        // Set planet color
        GL11.glColor3f(r, g, b);
        
        // Set material properties for lighting
        float[] ambient = {r * 0.3f, g * 0.3f, b * 0.3f, 1.0f};
        float[] diffuse = {r, g, b, 1.0f};
        float[] specular = {0.2f, 0.2f, 0.2f, 1.0f};
        float shininess = planet.isGasGiant() ? 5.0f : 30.0f;
        
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_AMBIENT, ambient);
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_DIFFUSE, diffuse);
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_SPECULAR, specular);
        GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, shininess);
        
        // Draw sphere
        drawSphere(safeRadius, 12, 12);
        
        // Draw rings for gas giants (simplified)
        if (planet.isGasGiant() && planet.getId().equals("saturn")) {
            drawRings(safeRadius, r, g, b);
        }
        
        // Restore matrix
        GL11.glPopMatrix();
    }
    
    private void drawSphere(double radius, int slices, int stacks) {
        // SECURITY: Validate parameters
        int safeSlices = Math.max(4, Math.min(24, slices));
        int safeStacks = Math.max(4, Math.min(24, stacks));
        
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
    
    private void drawRings(double planetRadius, float r, float g, float b) {
        // SECURITY: Simple ring system with bounds checking
        double innerRadius = planetRadius * 1.5;
        double outerRadius = planetRadius * 2.5;
        int segments = 32;
        
        // Semi-transparent rings
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(r * 0.7f, g * 0.7f, b * 0.7f, 0.3f);
        
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        for (int i = 0; i <= segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            double x = Math.cos(angle);
            double z = Math.sin(angle);
            
            GL11.glNormal3d(0, 1, 0);
            GL11.glVertex3d(innerRadius * x, 0, innerRadius * z);
            GL11.glVertex3d(outerRadius * x, 0, outerRadius * z);
        }
        GL11.glEnd();
        
        GL11.glDisable(GL11.GL_BLEND);
    }
}