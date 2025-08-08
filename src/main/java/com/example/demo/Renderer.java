package com.example.demo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

public class Renderer {
    
    private final GridRenderer gridRenderer;
    
    public Renderer() {
        // Grid spans 20x20 with 50 unit spacing (covers solar system)
        this.gridRenderer = new GridRenderer(20, 50);
    }

    public void render(List<CelestialBody> bodies) {
        // Draw grid first (background)
        gridRenderer.setupGL();
        gridRenderer.draw();
        
        // Use strategy pattern for rendering celestial bodies
        Map<Class<?>, BodyRenderer> renderers = Map.of(
            Star.class, new StarRenderer(),
            Planet.class, new PlanetRenderer(),
            Spacecraft.class, new SpacecraftRenderer()
        );

        // Batch by render type to minimize state changes
        bodies.stream()
            .collect(Collectors.groupingBy(
                body -> body.getClass(),
                LinkedHashMap::new,
                Collectors.toList()
            ))
            .forEach((type, group) -> {
                // Check if a renderer exists for this type before attempting to use it
                if (renderers.containsKey(type)) {
                    renderers.get(type).setupGL();
                    group.forEach(body ->
                        renderers.get(type).draw(body)
                    );
                }
            });
    }
}