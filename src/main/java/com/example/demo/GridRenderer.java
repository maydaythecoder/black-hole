package com.example.demo;

import org.lwjgl.opengl.GL11;

/**
 * GridRenderer for spatial reference on the XZ plane.
 * 
 * SECURITY: Simple geometric rendering with bounds checking.
 */
public class GridRenderer {
    
    private final int gridSize;
    private final double gridSpacing;
    
    public GridRenderer(int gridSize, double gridSpacing) {
        this.gridSize = Math.max(10, Math.min(100, gridSize)); // SECURITY: Bounds checking
        this.gridSpacing = Math.max(1.0, gridSpacing);
    }
    
    public void setupGL() {
        // Grid doesn't need special lighting setup
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }
    
    public void draw() {
        GL11.glPushMatrix();
        
        // Set grid color (subtle gray)
        GL11.glColor4f(0.3f, 0.3f, 0.3f, 0.5f);
        GL11.glLineWidth(1.0f);
        
        GL11.glBegin(GL11.GL_LINES);
        
        // Draw lines parallel to X-axis (running in Z direction)
        for (int i = -gridSize; i <= gridSize; i++) {
            double x = i * gridSpacing;
            GL11.glVertex3d(x, 0, -gridSize * gridSpacing);
            GL11.glVertex3d(x, 0, gridSize * gridSpacing);
        }
        
        // Draw lines parallel to Z-axis (running in X direction)
        for (int i = -gridSize; i <= gridSize; i++) {
            double z = i * gridSpacing;
            GL11.glVertex3d(-gridSize * gridSpacing, 0, z);
            GL11.glVertex3d(gridSize * gridSpacing, 0, z);
        }
        
        GL11.glEnd();
        
        // Draw center axes in different colors
        GL11.glLineWidth(2.0f);
        GL11.glBegin(GL11.GL_LINES);
        
        // X-axis in red
        GL11.glColor4f(1.0f, 0.2f, 0.2f, 0.8f);
        GL11.glVertex3d(-gridSize * gridSpacing, 0, 0);
        GL11.glVertex3d(gridSize * gridSpacing, 0, 0);
        
        // Z-axis in blue
        GL11.glColor4f(0.2f, 0.2f, 1.0f, 0.8f);
        GL11.glVertex3d(0, 0, -gridSize * gridSpacing);
        GL11.glVertex3d(0, 0, gridSize * gridSpacing);
        
        GL11.glEnd();
        
        // Reset line width and color
        GL11.glLineWidth(1.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        
        GL11.glPopMatrix();
        
        // Re-enable lighting for other objects
        GL11.glEnable(GL11.GL_LIGHTING);
    }
}
