package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
// import java.util.ArrayList;
// import java.util.List;

public class DataLoader {

    public static Map<String, CelestialBody> loadFromJson(String path) {
        Map<String, CelestialBody> bodies = new HashMap<>();
        Map<String, String> planetParentIds = new HashMap<>(); // Store planet-parent relationships
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(new File(path));
            if (rootNode.isArray()) {
                // Pass 1: Create all bodies
                for (JsonNode node : rootNode) {
                    String id = node.get("id").asText();
                    String type = node.get("type").asText();
                    double mass = node.get("mass").asDouble();
                    double radius = node.get("radius").asDouble();
                    float[] color = objectMapper.readValue(node.get("color").traverse(), float[].class);
                    boolean isStatic = node.has("isStatic") ? node.get("isStatic").asBoolean() : false;
                    Vector3D position = Vector3D.obtain(
                            node.get("position").get(0).asDouble(),
                            node.get("position").get(1).asDouble(),
                            node.get("position").get(2).asDouble()
                    );
                    Vector3D velocity = Vector3D.obtain(
                            node.get("velocity").get(0).asDouble(),
                            node.get("velocity").get(1).asDouble(),
                            node.get("velocity").get(2).asDouble()
                    );

                    switch (type) {
                        case "Star":
                            double luminosity = node.get("luminosity").asDouble();
                            bodies.put(id, new Star(id, mass, radius, color, isStatic, position, velocity, luminosity));
                            break;
                        case "Planet":
                            String parentId = node.get("parentId").asText();
                            boolean isGasGiant = node.get("isGasGiant").asBoolean();
                            Planet planet = new Planet(id, mass, radius, color, isStatic, position, velocity, parentId, isGasGiant);
                            bodies.put(id, planet);
                            // Store parent ID for later resolution
                            planetParentIds.put(id, parentId);
                            break;
                        case "Spacecraft":
                            double thrustPower = node.get("thrustPower").asDouble();
                            double fuel = node.get("fuel").asDouble();
                            bodies.put(id, new Spacecraft(id, mass, radius, color, isStatic, position, velocity, thrustPower, fuel));
                            break;
                        default:
                            System.err.println("Unknown celestial body type: " + type);
                    }
                }

                // Pass 2: Resolve parent references for Planets
                for (Map.Entry<String, String> entry : planetParentIds.entrySet()) {
                    String planetId = entry.getKey();
                    String parentId = entry.getValue();
                    
                    Planet planet = (Planet) bodies.get(planetId);
                    CelestialBody parentBody = bodies.get(parentId);
                    
                    if (parentBody != null) {
                        planet.setParentBody(parentBody); // Use setter method
                    } else {
                        System.err.println("Parent body with ID " + parentId + " not found for planet " + planetId);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception appropriately
        }

        return bodies;
    }
}