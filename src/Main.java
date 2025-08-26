import cpu6502.*;

import java.io.IOException;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Main {
    private static CPU6502 cpu;
    private static PPU ppu;
    private static Memory memory;
    private static Memory ppuMemory;
    private static DisplayWindow displayWindow;
    private static boolean emulatorRunning = false;
    
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("🎮 Starting NES Emulator with PPU Display...");
        
        // Initialize emulator components
        initializeEmulator();
        
        // Create and show the display window
        displayWindow = new DisplayWindow();
        displayWindow.setVisible(true);
        
        // Enable real-time display
        ppu.enableRealTimeDisplay(displayWindow);
        
        // Start emulator loop
        startEmulatorLoop();
        
        // Cleanup
        cleanup();
    }
    
    private static void initializeEmulator() {
        System.out.println("🔧 Initializing emulator components...");
        
        memory = new Memory();
        ppuMemory = new Memory();
        MemoryPage vram = new RamPage(0x800); // 2KB VRAM
        ppuMemory.mapRange(0x2000, 0x2FFF, vram); // mirrors every 0x1000

        MemoryPage ram = new RamPage(0x800); // 2KB RAM
        memory.mapRange(0x0000, 0x1FFF, ram); // mirror into full $0000–$1FFF

        // Set correct interrupt vectors manually
        memory.write(0xFFFA, 0x00); memory.write(0xFFFB, 0x80); // NMI → $8000
        memory.write(0xFFFC, 0x00); memory.write(0xFFFD, 0x80); // RESET → $8000
        memory.write(0xFFFE, 0x00); memory.write(0xFFFF, 0x80); // IRQ/BRK → $8000
        
        // Create a simple infinite loop program at $8000
        memory.write(0x8000, 0x4C); // JMP $8000
        memory.write(0x8001, 0x00); // low byte
        memory.write(0x8002, 0x80); // high byte

        System.out.printf("🧠 memory[0xFFFC] = %02X\n", memory.read(0xFFFC));
        System.out.printf("🧠 memory[0xFFFD] = %02X\n", memory.read(0xFFFD));
        System.out.printf("📍 CPU Reset Vector = %04X\n", memory.readWord(0xFFFC));

        cpu = new CPU6502(memory, Mode.DEBUG);
        ppu = new PPU(cpu, Mode.DEBUG, ppuMemory);
        
        // Create a simple test pattern in VRAM
        createTestPattern();
        
        cpu.reset();
        System.out.println("✅ Emulator initialized successfully");
    }
    
    private static void createTestPattern() {
        System.out.println("🎨 Creating test pattern in VRAM...");
        for (int row = 0; row < 30; row++) {
            for (int col = 0; col < 32; col++) {
                int tileIndex = (row * 32 + col) & 0xFF; // wrap 0–255
                ppuMemory.write(0x2000 + row * 32 + col, tileIndex);
            }
        }
    }
    
    public static void loadRom(String romPath) {
        try {
            System.out.println("🎮 Loading ROM: " + romPath);
            
            // Load the .nes file
            INESFile inesFile = new INESFile(romPath);
            
            // Load PRG ROM into CPU memory
            if (inesFile.prgRom != null) {
                System.out.println("📦 Loading PRG ROM (" + inesFile.prgSize + " bytes)");
                loadPrgRom(inesFile.prgRom);
            }
            
            // Load CHR ROM into PPU memory
            if (inesFile.chrRom != null) {
                System.out.println("🎨 Loading CHR ROM (" + inesFile.chrSize + " bytes)");
                loadChrRom(inesFile.chrRom);
                ppu.markTilePatternsDirty(); // Mark patterns for regeneration
            }
            
            // Reset the emulator with new ROM data
            resetEmulator();
            
            System.out.println("✅ ROM loaded successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error loading ROM: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void loadPrgRom(byte[] prgRom) {
        // Load PRG ROM starting at $8000
        int startAddress = 0x8000;
        for (int i = 0; i < prgRom.length && (startAddress + i) < 0x10000; i++) {
            memory.write(startAddress + i, prgRom[i] & 0xFF);
        }
        
        // If PRG ROM is 16KB, mirror it to $C000-$FFFF
        if (prgRom.length == 16 * 1024) {
            for (int i = 0; i < prgRom.length; i++) {
                memory.write(0xC000 + i, prgRom[i] & 0xFF);
            }
        }
    }
    
    private static void loadChrRom(byte[] chrRom) {
        // Load CHR ROM into PPU memory starting at $0000
        for (int i = 0; i < chrRom.length && i < 0x2000; i++) {
            ppuMemory.write(i, chrRom[i] & 0xFF);
        }
    }
    
    private static void resetEmulator() {
        System.out.println("🔄 Resetting emulator...");
        cpu.reset();
        ppu = new PPU(cpu, Mode.DEBUG, ppuMemory);
        ppu.enableRealTimeDisplay(displayWindow);
        emulatorRunning = true;
    }
    
    private static void startEmulatorLoop() {
        System.out.println("🚀 Starting emulator loop...");
        emulatorRunning = true;
        
        final int CPU_CYCLES_PER_FRAME = 29829; // Approximate NTSC CPU cycles per frame
        final int PPU_CYCLES_PER_CPU = 3;       // PPU runs at 3x CPU speed
        final long TARGET_FRAME_TIME_NS = 16_666_667L; // ~16.67ms
        
        while (displayWindow.isRunning() && emulatorRunning) {
            long frameStart = System.nanoTime();
            
            // Run one full frame worth of work
            for (int cpuCycle = 0; cpuCycle < CPU_CYCLES_PER_FRAME; cpuCycle++) {
                for (int p = 0; p < PPU_CYCLES_PER_CPU; p++) {
                    ppu.clock();
                }
            }
            
            // Sleep the remainder to maintain 60 FPS
            long frameElapsed = System.nanoTime() - frameStart;
            long sleepNs = TARGET_FRAME_TIME_NS - frameElapsed;
            if (sleepNs > 0) {
                try {
                    Thread.sleep(sleepNs / 1_000_000, (int) (sleepNs % 1_000_000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                // If we are behind, yield to avoid hogging the CPU
                Thread.yield();
            }
        }
        
        System.out.println("⏹️ Emulator loop stopped");
    }
    
    private static void cleanup() {
        System.out.println("🧹 Cleaning up...");
        if (ppu != null) {
            ppu.disableRealTimeDisplay();
        }
        if (displayWindow != null) {
            displayWindow.stop();
        }
        emulatorRunning = false;
        System.out.println("🎮 Emulator stopped.");
    }
    
    // Public method to load ROM from external sources
    public static void loadRomFromPath(String romPath) {
        if (emulatorRunning) {
            loadRom(romPath);
        }
    }
    
    // Public method to reset the emulator
    public static void resetEmulatorFromExternal() {
        if (emulatorRunning) {
            resetEmulator();
        }
    }

    public static void exportCHRToPNG(MemoryPage chrRom, String filename) throws IOException {
        int tilesPerRow = 16;
        int tileSize = 8;
        int tileCount = 256; // 8 KB / 16 bytes = 512, but NES CHR ROM typically uses 256 tiles

        int imageSize = tilesPerRow * tileSize;
        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);

        // Simple 2-bit grayscale palette
        Color[] palette = {
                new Color(255, 255, 255), // 0 = white
                new Color(170, 170, 170), // 1 = light gray
                new Color(85, 85, 85),    // 2 = dark gray
                new Color(0, 0, 0)        // 3 = black
        };

        for (int tile = 0; tile < tileCount; tile++) {
            int base = tile * 16;
            int tileX = (tile % tilesPerRow) * tileSize;
            int tileY = (tile / tilesPerRow) * tileSize;

            for (int y = 0; y < 8; y++) {
                int b0 = chrRom.read(base + y);
                int b1 = chrRom.read(base + y + 8);

                for (int x = 0; x < 8; x++) {
                    int bit0 = (b0 >> (7 - x)) & 1;
                    int bit1 = (b1 >> (7 - x)) & 1;
                    int pixel = (bit1 << 1) | bit0;

                    image.setRGB(tileX + x, tileY + y, palette[pixel].getRGB());
                }
            }
        }

        ImageIO.write(image, "png", new File(filename));
        System.out.println("🖼️ CHR ROM exported to " + filename);
    }
}
