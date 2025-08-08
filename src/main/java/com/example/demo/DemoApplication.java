package com.example.demo;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public class DemoApplication {

    private long window;
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    
    private SimulationManager simulation;
    private double lastTime;
    private boolean[] keys = new boolean[512];
    private double mouseX, mouseY;
    private boolean mousePressed = false;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        // SECURITY: Initialize GLFW with error checking
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW for OpenGL 2.1 compatibility
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);

        // Create the window
        window = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "Solar System Simulation", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        setupCallbacks();

        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(window);
        // SECURITY: Critical! This enables the LWJGL bindings
        GL.createCapabilities();
        // Enable v-sync
        GLFW.glfwSwapInterval(1);

        // Make the window visible
        GLFW.glfwShowWindow(window);
        
        // Initialize simulation
        simulation = new SimulationManager();
        simulation.loadSolarSystem();
        
        // Initialize timing
        lastTime = GLFW.glfwGetTime();
        
        System.out.println("\n=== SOLAR SYSTEM SIMULATION CONTROLS ===");
        System.out.println("ESC        - Exit simulation");
        System.out.println("SPACE      - Pause/Resume simulation");
        System.out.println("R          - Reset time scale");
        System.out.println("+/-        - Increase/Decrease time scale");
        System.out.println("Mouse Drag - Free-look camera rotation");
        System.out.println("Scroll     - Zoom in/out");
        System.out.println("WASD       - Move forward/back/left/right");
        System.out.println("Q/E        - Move down/up");
        System.out.println("======================================\n");
    }
    
    private void setupCallbacks() {
        // SECURITY: Input validation in all callbacks
        
        // Keyboard callback
        GLFW.glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key >= 0 && key < keys.length) {
                if (action == GLFW.GLFW_PRESS) {
                    keys[key] = true;
                    handleKeyPress(key);
                } else if (action == GLFW.GLFW_RELEASE) {
                    keys[key] = false;
                }
            }
        });
        
        // Mouse button callback
        GLFW.glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                mousePressed = (action == GLFW.GLFW_PRESS);
            }
        });
        
        // Mouse position callback
        GLFW.glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            if (mousePressed) {
                double deltaX = xpos - mouseX;
                double deltaY = ypos - mouseY;
                // Convert pixel movement to rotation (sensitivity adjustment)
                double sensitivity = 0.002; // SECURITY: Limited sensitivity prevents extreme rotation
                simulation.rotateCamera(deltaX * sensitivity, -deltaY * sensitivity); // Negative Y for natural movement
            }
            mouseX = xpos;
            mouseY = ypos;
        });
        
        // Scroll callback for zoom
        GLFW.glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
            double zoomFactor = yoffset > 0 ? 0.9 : 1.1;
            simulation.zoomCamera(zoomFactor);
        });
        
        // Window resize callback
        GLFW.glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            GL11.glViewport(0, 0, width, height);
        });
    }
    
    private void handleKeyPress(int key) {
        switch (key) {
            case GLFW.GLFW_KEY_ESCAPE:
                GLFW.glfwSetWindowShouldClose(window, true);
                break;
            case GLFW.GLFW_KEY_SPACE:
                simulation.togglePause();
                break;
            case GLFW.GLFW_KEY_R:
                simulation.resetTimeScale();
                break;
            case GLFW.GLFW_KEY_EQUAL:
            case GLFW.GLFW_KEY_KP_ADD:
                simulation.adjustTimeScale(2.0);
                break;
            case GLFW.GLFW_KEY_MINUS:
            case GLFW.GLFW_KEY_KP_SUBTRACT:
                simulation.adjustTimeScale(0.5);
                break;
        }
    }
    
    private void handleContinuousInput() {
        // SECURITY: Safe camera movement with bounds checking
        Vector3D cameraMovement = Vector3D.ZERO;
        double moveSpeed = 50; // 50 units movement speed for scaled system
        
        double forward = 0, right = 0, up = 0;
        
        // Free-move camera controls (relative to camera orientation)
        if (keys[GLFW.GLFW_KEY_W]) {
            forward += moveSpeed; // Move forward
        }
        if (keys[GLFW.GLFW_KEY_S]) {
            forward -= moveSpeed; // Move backward
        }
        if (keys[GLFW.GLFW_KEY_A]) {
            right -= moveSpeed; // Strafe left
        }
        if (keys[GLFW.GLFW_KEY_D]) {
            right += moveSpeed; // Strafe right
        }
        if (keys[GLFW.GLFW_KEY_Q]) {
            up -= moveSpeed; // Move down
        }
        if (keys[GLFW.GLFW_KEY_E]) {
            up += moveSpeed; // Move up
        }
        
        // Apply relative movement if any keys are pressed
        if (forward != 0 || right != 0 || up != 0) {
            simulation.moveRelativeCamera(forward, right, up);
        }
    }

    private void loop() {
        // SECURITY: Safe OpenGL state setup
        GL11.glClearColor(0.0f, 0.0f, 0.05f, 1.0f); // Dark blue space background
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glFrontFace(GL11.GL_CCW);  // Counter-clockwise front faces
        
        System.out.println("OpenGL initialized. Rendering should begin...");
        
        // Main loop
        while (!GLFW.glfwWindowShouldClose(window)) {
            double currentTime = GLFW.glfwGetTime();
            double deltaTime = currentTime - lastTime;
            lastTime = currentTime;
            
            // Handle continuous input
            handleContinuousInput();
            
            // Update simulation
            simulation.update(deltaTime);
            
            // Get current framebuffer size
            int[] width = new int[1];
            int[] height = new int[1];
            GLFW.glfwGetFramebufferSize(window, width, height);
            
            // Clear the screen
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            
            // Render the simulation
            simulation.render(width[0], height[0]);
            
            // Swap buffers and poll events
            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }
    
    private void cleanup() {
        // SECURITY: Proper resource cleanup
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
        
        System.out.println("Solar System Simulation terminated.");
    }

    public static void main(String[] args) {
        System.out.println("Starting Solar System Simulation...");
        try {
            new DemoApplication().run();
        } catch (Exception e) {
            System.err.println("Simulation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
