# NES PPU Display System

This project implements a real-time display window for the NES PPU (Picture Processing Unit) that can render frames at 60FPS.

## Features

- **Real-time Display**: Shows PPU frames in a dedicated window at 60FPS
- **Swing-based UI**: Cross-platform Java Swing interface
- **Headless Support**: Works in environments without display (saves frames to files)
- **NES Timing**: Maintains accurate NES timing (3 PPU cycles per CPU cycle)
- **Frame Generation**: Generates visual output from PPU memory and CHR data
- **Debug Output**: Console logging for frame generation and timing

## Architecture

### Core Components

1. **DisplayWindow.java** - Main display interface using Java Swing
2. **PPU.java** - Enhanced PPU with real-time display support
3. **Main.java** - Updated main emulator loop with display integration

### Key Features

- **60FPS Display Loop**: Maintains consistent frame rate using precise timing
- **Thread-safe Frame Updates**: Synchronized frame buffer updates
- **Automatic Scaling**: 2x scaling for better visibility (256x240 → 512x480)
- **Memory Integration**: Reads from PPU memory and CHR data for frame generation

## Usage

### Running the Emulator with Display

```bash
# Compile all source files
javac -cp src src/*.java

# Run the main emulator
java -cp src Main
```

### Running the Demo

```bash
# Run the PPU demo (generates sample frames)
java -cp src PPUDemo
```

## Display Window Features

### Window Properties
- **Size**: 512x480 pixels (2x scaled from NES native 256x240)
- **Title**: "NES PPU Display - 60FPS"
- **Resizable**: No (maintains aspect ratio)
- **Close Action**: Exits the emulator

### Frame Generation
- **Resolution**: 256x240 pixels (NES native)
- **Color Depth**: 24-bit RGB
- **Palette**: 4-color NES-style palette system
- **Tile System**: 32x30 tile grid (8x8 pixel tiles)

## PPU Integration

### Real-time Display Methods

```java
// Enable real-time display
ppu.enableRealTimeDisplay(displayWindow);

// Disable real-time display
ppu.disableRealTimeDisplay();

// Generate a frame as BufferedImage
BufferedImage frame = ppu.generateFrame();
```

### Frame Update Cycle

1. **VBlank Detection**: PPU detects vertical blank period
2. **Frame Generation**: Creates new frame from current PPU state
3. **Display Update**: Sends frame to DisplayWindow
4. **60FPS Rendering**: DisplayWindow renders at consistent frame rate

## Headless Environment Support

When running in environments without display capabilities:

- **Automatic Detection**: Detects headless environment
- **Console Output**: Shows frame generation status
- **File Output**: Saves frames to PNG files for verification
- **Performance**: Continues running without display overhead

## Performance Characteristics

### Timing
- **Target FPS**: 60 frames per second
- **Frame Time**: ~16.67ms per frame
- **PPU Clock**: 3x CPU clock speed (typical NES timing)
- **Throttling**: Automatic CPU throttling to maintain timing

### Memory Usage
- **Frame Buffer**: 256x240x3 bytes per frame
- **Display Window**: ~2MB for Swing components
- **PPU Memory**: 16KB address space

## File Output

### Frame Files
- **Format**: PNG images
- **Naming**: `frame_0.png`, `frame_1.png`, etc.
- **Size**: ~14KB per frame (compressed PNG)
- **Frequency**: Every 5 seconds during emulation

### Demo Files
- **Format**: PNG images with animated patterns
- **Naming**: `demo_frame_0.png` through `demo_frame_9.png`
- **Content**: Moving wave patterns demonstrating capabilities

## Technical Details

### Swing Implementation
- **Event Dispatch Thread**: Proper Swing threading model
- **Double Buffering**: Smooth frame updates
- **Custom Painting**: Optimized paintComponent implementation
- **Window Management**: Proper lifecycle management

### Threading Model
- **Main Thread**: Emulator logic and PPU simulation
- **Display Thread**: Dedicated 60FPS rendering loop
- **Synchronization**: Thread-safe frame buffer updates
- **Interruption**: Graceful shutdown handling

## Troubleshooting

### Common Issues

1. **No Display Window**: Check if running in headless environment
2. **Low Frame Rate**: Verify system performance and timing
3. **Memory Issues**: Monitor heap usage for long-running sessions
4. **Compilation Errors**: Ensure all dependencies are compiled

### Debug Output

Enable debug mode to see:
- Frame generation status
- FPS monitoring
- VBlank detection
- Memory access patterns

## Future Enhancements

### Planned Features
- **Multiple Display Modes**: Windowed, fullscreen, custom sizes
- **Frame Recording**: Save frame sequences as video
- **Performance Metrics**: Real-time FPS and timing display
- **Custom Palettes**: User-defined color schemes
- **Input Handling**: Keyboard/mouse input for testing

### Optimization Opportunities
- **Hardware Acceleration**: OpenGL/DirectX rendering
- **Frame Skipping**: Adaptive frame rate for performance
- **Memory Pooling**: Reusable frame buffers
- **Compression**: Efficient frame storage and transmission

## License

This project is part of a NES emulator implementation. Please refer to the main project license for usage terms.

## Contributing

To contribute to the PPU display system:

1. **Fork the repository**
2. **Create a feature branch**
3. **Implement your changes**
4. **Test thoroughly**
5. **Submit a pull request**

## Support

For issues or questions about the PPU display system:

1. **Check the console output** for error messages
2. **Verify file generation** in headless environments
3. **Review timing settings** for performance issues
4. **Check system requirements** for display capabilities