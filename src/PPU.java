import cpu6502.CPU6502;
import cpu6502.Memory;
import cpu6502.MemoryPage;
import cpu6502.Mode;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class PPU {
    private static final int SCANLINES_PER_FRAME = 262;
    private static final int CYCLES_PER_SCANLINE = 341;
    private static final int VBLANK_START_LINE = 241;
    
    // Performance optimization constants
    private static final int TILES_PER_ROW = 32;
    private static final int TILES_PER_COL = 30;
    private static final int TILE_SIZE = 8;
    private static final int FRAME_WIDTH = TILES_PER_ROW * TILE_SIZE;
    private static final int FRAME_HEIGHT = TILES_PER_COL * TILE_SIZE;
    
    // Pre-computed color palette for better performance
    private static final int[] PALETTE_RGB = {
        0xFFFFFF, // 0 - White
        0xAAAAAA, // 1 - Light Gray
        0x555555, // 2 - Dark Gray
        0x000000  // 3 - Black
    };

    public int getFrameCounter() {
        return frameCounter;
    }

    private int frameCounter = 0;
    private int cycle = 0;
    private int scanline = 0;
    private CPU6502 cpu;
    private int frameCount = 0;
    private long lastFpsTime = System.currentTimeMillis();
    private Mode mode;
    private final MemoryPage[] ppuPages = new MemoryPage[16]; // 16 x 1KB = 16KB PPU address space
    private Memory ppuMemory;
    private boolean renderOnNextVBlank = false;
    private DisplayWindow displayWindow;
    private boolean realTimeDisplay = false;
    
    // Performance optimization: Frame buffer pooling
    private BufferedImage[] frameBuffers;
    private int currentBufferIndex = 0;
    private int nextBufferIndex = 1;
    private final Object frameBufferLock = new Object();
    
    // Performance optimization: Pre-computed tile patterns
    private int[][] tilePatterns;
    private boolean tilePatternsDirty = true;

    public PPU(CPU6502 cpu) {
        this.cpu = cpu;
        initializeFrameBuffers();
        initializeTilePatterns();
    }

    public PPU(CPU6502 cpu, Mode mode, Memory memory) {
        this.cpu = cpu;
        this.mode = mode;
        this.ppuMemory = memory;
        initializeFrameBuffers();
        initializeTilePatterns();
    }
    
    private void initializeFrameBuffers() {
        frameBuffers = new BufferedImage[2];
        for (int i = 0; i < 2; i++) {
            frameBuffers[i] = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_RGB);
        }
    }
    
    private void initializeTilePatterns() {
        tilePatterns = new int[256][64]; // 256 tiles, 64 pixels per tile
        updateTilePatterns();
    }
    
    private void updateTilePatterns() {
        if (!tilePatternsDirty) return;
        
        for (int tileIndex = 0; tileIndex < 256; tileIndex++) {
            int base = tileIndex * 16;
            
            for (int y = 0; y < 8; y++) {
                int b0 = readCHR(base + y);
                int b1 = readCHR(base + y + 8);
                
                for (int x = 0; x < 8; x++) {
                    int bit0 = (b0 >> (7 - x)) & 1;
                    int bit1 = (b1 >> (7 - x)) & 1;
                    int pixel = (bit1 << 1) | bit0;
                    
                    tilePatterns[tileIndex][y * 8 + x] = pixel;
                }
            }
        }
        tilePatternsDirty = false;
    }

    public void clock() {
        cycle++;
        if (cycle >= CYCLES_PER_SCANLINE) {
            cycle = 0;
            scanline++;
            if (scanline == VBLANK_START_LINE) {
                enterVBlank();
            }
            if (scanline >= SCANLINES_PER_FRAME) {
                scanline = 0;
                frameCounter++;
            }
        }
    }

    public void requestRenderOnNextVBlank() {
        renderOnNextVBlank = true;
    }
    
    public void enableRealTimeDisplay(DisplayWindow window) {
        this.displayWindow = window;
        this.realTimeDisplay = true;
        if (mode == Mode.DEBUG) {
            System.out.println("🔗 PPU real-time display enabled with window: " + 
                             (window != null ? "connected" : "null"));
        }
    }
    
    public void disableRealTimeDisplay() {
        this.realTimeDisplay = false;
        this.displayWindow = null;
    }
    
    // Force immediate frame update (useful for ROM loading)
    public void forceFrameUpdate() {
        if (realTimeDisplay && displayWindow != null) {
            BufferedImage frame = generateFrameOptimized();
            if (frame != null) {
                displayWindow.updateFrame(frame);
                if (mode == Mode.DEBUG) {
                    System.out.printf("🎬 Forced frame update - frame %d sent to display\n", frameCounter);
                }
            } else {
                if (mode == Mode.DEBUG) {
                    System.out.printf("⚠️ Forced frame update failed - null frame\n", frameCounter);
                }
            }
        } else {
            if (mode == Mode.DEBUG) {
                System.out.printf("⚠️ Cannot force frame update - realTimeDisplay: %s, displayWindow: %s\n", 
                                realTimeDisplay, (displayWindow != null ? "not null" : "null"));
            }
        }
    }
    
    public void reset() {
        // Reset PPU internal state
        frameCounter = 0;
        cycle = 0;
        scanline = 0;
        frameCount = 0;
        lastFpsTime = System.currentTimeMillis();
        renderOnNextVBlank = false;
        
        // Mark tile patterns as dirty to force regeneration
        tilePatternsDirty = true;
        
        // Reset frame buffer indices
        currentBufferIndex = 0;
        nextBufferIndex = 1;
        
        // Preserve display window connection and real-time display settings
        // (don't reset realTimeDisplay and displayWindow)
        if (mode == Mode.DEBUG) {
            System.out.println("🔄 PPU reset - realTimeDisplay: " + realTimeDisplay + 
                             ", displayWindow: " + (displayWindow != null ? "connected" : "null"));
        }
    }

    private void enterVBlank() {
        cpu.requestNMI(); // Trigger NMI
        if (mode == Mode.DEBUG) {
            System.out.printf("🌀 VBlank — frame %d\n", frameCounter);
        }

        // Only print FPS in debug mode
        if (mode == Mode.DEBUG) {
            frameCount++;
            long now = System.currentTimeMillis();
            if (now - lastFpsTime >= 1000) {
                System.out.printf("🎮 FPS: %d\n", frameCount);
                frameCount = 0;
                lastFpsTime = now;
            }
        }

        // Previously optionally saved a frame here; disabled to prevent disk writes
        if (renderOnNextVBlank) {
            renderOnNextVBlank = false; // reset without saving
        }
        
        // Update real-time display if enabled
        if (realTimeDisplay && displayWindow != null) {
            BufferedImage frame = generateFrameOptimized();
            if (frame != null) {
                displayWindow.updateFrame(frame);
                
                // Show frame generation in debug mode
                if (mode == Mode.DEBUG) {
                    System.out.printf("🎬 Frame %d generated and sent to display (size: %dx%d)\n", 
                                    frameCounter, frame.getWidth(), frame.getHeight());
                }
            } else {
                if (mode == Mode.DEBUG) {
                    System.out.printf("⚠️ Frame %d generation failed - null frame\n", frameCounter);
                }
            }
        } else {
            if (mode == Mode.DEBUG) {
                System.out.printf("⚠️ Frame %d not sent to display - realTimeDisplay: %s, displayWindow: %s\n", 
                                frameCounter, realTimeDisplay, (displayWindow != null ? "not null" : "null"));
            }
        }
    }

    public void mapPage(int pageIndex, MemoryPage page) {
        ppuPages[pageIndex] = page;
    }

    private int readCHR(int addr) {
        // Read from PPU memory where CHR ROM is loaded
        if (ppuMemory != null && addr >= 0x0000 && addr < 0x2000) {
            return ppuMemory.read(addr) & 0xFF;
        }
        
        // Fallback to test pattern if no PPU memory is available
        int tileIndex = (addr >> 4) & 0xFF; // 16 bytes per tile
        int tileOffset = addr & 0x0F;
        
        if (tileOffset < 8) {
            // First 8 bytes: pattern for bit 0
            return (tileIndex + tileOffset) & 0xFF;
        } else {
            // Last 8 bytes: pattern for bit 1
            return ((tileIndex + tileOffset) << 1) & 0xFF;
        }
    }
    
    // Performance optimized frame generation using pre-computed patterns
    public BufferedImage generateFrameOptimized() {
        synchronized (frameBufferLock) {
            // Get the next available buffer
            BufferedImage frameBuffer = frameBuffers[nextBufferIndex];
            
            // Update tile patterns if needed
            if (tilePatternsDirty) {
                updateTilePatterns();
            }
            
            // Get the frame buffer's raster data for direct pixel manipulation
            int[] pixels = new int[FRAME_WIDTH * FRAME_HEIGHT];
            
            // Generate frame using pre-computed tile patterns
            for (int row = 0; row < TILES_PER_COL; row++) {
                for (int col = 0; col < TILES_PER_ROW; col++) {
                    // Read tile index from name table (0x2000-0x23FF)
                    int nameTableAddr = 0x2000 + row * 32 + col;
                    int tileIndex = 0;
                    
                    if (ppuMemory != null) {
                        tileIndex = ppuMemory.read(nameTableAddr) & 0xFF;
                    } else {
                        // Fallback to test pattern if no PPU memory
                        tileIndex = (row * 32 + col) & 0xFF;
                    }
                    
                    // Copy pre-computed tile pattern to frame buffer
                    for (int y = 0; y < 8; y++) {
                        for (int x = 0; x < 8; x++) {
                            int pixelIndex = (row * 8 + y) * FRAME_WIDTH + (col * 8 + x);
                            int patternIndex = y * 8 + x;
                            int pixel = tilePatterns[tileIndex][patternIndex];
                            pixels[pixelIndex] = PALETTE_RGB[pixel];
                        }
                    }
                }
            }
            
            // Set all pixels at once for better performance
            frameBuffer.setRGB(0, 0, FRAME_WIDTH, FRAME_HEIGHT, pixels, 0, FRAME_WIDTH);
            
            // Swap buffer indices
            currentBufferIndex = nextBufferIndex;
            nextBufferIndex = (nextBufferIndex + 1) % 2;
            
            return frameBuffer;
        }
    }

    // Legacy method for backward compatibility
    public BufferedImage generateFrame() {
        return generateFrameOptimized();
    }

    public void renderBackgroundFrame(String filename) {
        // Disabled: no file output in real-time mode
    }

    public int getScanline() {
        return scanline;
    }

    public int getCycle() {
        return cycle;
    }
    
    // Method to mark tile patterns as dirty when CHR data changes
    public void markTilePatternsDirty() {
        tilePatternsDirty = true;
    }
}