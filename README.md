# Solar System Simulation

A real-time 3D solar system simulation built with Java, Spring Boot, and OpenGL (LWJGL). Features accurate gravitational physics using the Barnes-Hut algorithm for O(n log n) performance.

## Features

### 🌟 **Physics Engine**

- **Barnes-Hut Spatial Partitioning**: O(n log n) gravitational calculations
- **Real Celestial Bodies**: Sun, planets (Mercury through Saturn), and spacecraft
- **Accurate Orbital Mechanics**: Based on real astronomical data
- **Dynamic Time Scaling**: Speed up or slow down time (0.1x to 1,000,000x)

### 🎮 **Interactive Controls**

- **Camera Controls**:
  - Mouse drag to orbit around celestial bodies
  - Scroll wheel to zoom in/out
  - WASD keys for free camera movement
- **Simulation Controls**:
  - SPACE: Pause/Resume simulation
  - +/-: Increase/Decrease time scale
  - R: Reset time scale to 1 day/second
  - ESC: Exit simulation

### 🎨 **3D Rendering**

- **OpenGL Lighting**: Dynamic lighting from the sun
- **Material Properties**: Different materials for stars, planets, and spacecraft
- **Visual Effects**: Saturn's rings, spacecraft thrust trails
- **Proper Scaling**: Astronomical distances scaled for visibility

## Getting Started

### Prerequisites

- **Java 17** (OpenJDK recommended)
- **Maven 3.6+**
- **macOS** (configured for Apple Silicon, but can be adapted)

### Installation & Running

1. **Install Dependencies** (if not already done):

   ```bash
   # Install Java and Maven via Homebrew
   brew install maven openjdk@17
   
   # Set up Java environment
   export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
   export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
   ```

2. **Clone and Build**:

   ```bash
   # Navigate to project directory
   cd black-hole
   
   # Install Maven dependencies
   mvn clean install
   ```

3. **Run the Simulation**:

   ```bash
   mvn spring-boot:run
   ```

## System Architecture

### Class Hierarchy

```txt
CelestialBody (Abstract Base)
├── Star (Sun with luminosity)
├── Planet (Orbital mechanics, gas giant detection)
└── Spacecraft (Thrust capabilities, fuel system)
```

### Core Components

- **PhysicsUtil**: Barnes-Hut algorithm implementation
- **SimulationManager**: Coordinates physics, rendering, and user input
- **Camera**: 3D perspective with orbital controls
- **Renderer System**: Strategy pattern for different celestial body types

### Performance Optimizations

- **Spatial Partitioning**: Barnes-Hut tree for efficient n-body calculations
- **Batch Rendering**: Objects grouped by type to minimize OpenGL state changes
- **Distance Culling**: Skip calculations for objects beyond interaction range
- **Adaptive Scaling**: Automatic radius scaling for visibility

## Solar System Data

The simulation includes:

- **Sun**: Central star with realistic mass and luminosity
- **Planets**: Mercury, Venus, Earth, Mars, Jupiter, Saturn
- **Spacecraft**: Demonstration object with thrust capabilities

All celestial bodies use real astronomical data for:

- Mass and radius
- Initial positions and velocities
- Orbital characteristics

## Security Features

- **Input Validation**: All user inputs bounds-checked
- **Resource Limits**: Time scale and camera movement limits
- **Safe Rendering**: Geometry complexity limits to prevent performance issues
- **Error Handling**: Graceful fallbacks for missing data or OpenGL errors

## Physics Accuracy

- **Gravitational Constant**: G = 6.674e-11 m³ kg⁻¹ s⁻²
- **Real Orbital Velocities**: Based on actual planetary data
- **Conservation Laws**: Energy and momentum properly conserved
- **Numerical Stability**: Adaptive time stepping prevents numerical drift

## Development

### Project Structure

```txt
src/main/java/com/example/demo/
├── CelestialBody.java          # Base class for all objects
├── Star.java, Planet.java      # Celestial body implementations
├── PhysicsUtil.java            # Barnes-Hut physics engine
├── SimulationManager.java      # Main simulation controller
├── Camera.java                 # 3D camera system
├── Renderer.java               # OpenGL rendering coordinator
├── *Renderer.java              # Specific renderers for each body type
└── DemoApplication.java        # Main application entry point

src/main/resources/
└── solar_system.json           # Celestial body data
```

### Building from Source

```bash
# Development build
mvn compile

# Run tests (when available)
mvn test

# Create executable JAR
mvn package

# Run packaged application
java -XstartOnFirstThread -jar target/demo-0.0.1-SNAPSHOT.jar
```

## Troubleshooting

### Common Issues

1. **Black Screen**:
   - Ensure OpenGL context is properly initialized
   - Check that celestial bodies are loaded from JSON
   - Verify camera is positioned correctly

2. **OpenGL Errors**:
   - Ensure `-XstartOnFirstThread` JVM argument is set (macOS requirement)
   - Update graphics drivers
   - Check OpenGL 2.1+ support

3. **Performance Issues**:
   - Reduce time scale for complex scenarios
   - Adjust camera distance for better performance
   - Check system OpenGL capabilities

### Platform Notes

- **macOS**: Requires `-XstartOnFirstThread` JVM argument (already configured)
- **Linux**: Change `lwjgl.natives` to `natives-linux` in `pom.xml`
- **Windows**: Change `lwjgl.natives` to `natives-windows` in `pom.xml`

## Contributing

This simulation demonstrates advanced Java development concepts:

- Object-oriented design with inheritance and polymorphism
- Strategy pattern for rendering systems
- OpenGL graphics programming
- Real-time physics simulation
- Spring Boot integration

## License

This project is educational and demonstrates advanced Java programming techniques.
