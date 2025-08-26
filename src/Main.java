import cpu6502.*;

import java.io.IOException;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {



        // Create a simple test setup instead of loading .nes file
        System.out.println("🎮 Creating test PPU display...");
        Memory memory = new Memory();
        Memory ppuMemory = new Memory();
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



        CPU6502 cpu = new CPU6502(memory, Mode.DEBUG);
        ClockController clock = new ClockController(1_789_773);
        //ClockController clock = new ClockController(Integer.MAX_VALUE);

        PPU ppu = new PPU(cpu, Mode.DEBUG, ppuMemory);
        
        // Create and show the display window
        DisplayWindow displayWindow = new DisplayWindow();
        displayWindow.setVisible(true);
        
        // Enable real-time display
        ppu.enableRealTimeDisplay(displayWindow);

        // Create a simple test pattern in VRAM
        for (int row = 0; row < 30; row++) {
            for (int col = 0; col < 32; col++) {
                int tileIndex = (row * 32 + col) & 0xFF; // wrap 0–255
                ppuMemory.write(0x2000 + row * 32 + col, tileIndex);
            }
        }


        for (int row = 0; row < 30; row++) {
            for (int col = 0; col < 32; col++) {
                int tileIndex = (row * 32 + col) & 0xFF; // wrap 0–255
                ppuMemory.write(0x2000 + row * 32 + col, tileIndex);
            }
        }


        cpu.reset();
        int totalCycles = 0;

        // Simple loop that just runs the PPU to generate frames
        long lastSaveTime = System.currentTimeMillis();
        int frameCount = 0;
        
        while (displayWindow.isRunning()) {
            // Simulate CPU cycles (simplified)
            totalCycles += 1;
            
            // Run PPU at 3x CPU speed (typical NES timing)
            for (int i = 0; i < 3; i++) {
                ppu.clock();
            }

            // Throttle to maintain timing
            if (totalCycles >= 29829) {
                clock.throttle(totalCycles);
                totalCycles = 0;
            }
            
            // Save a frame every 5 seconds for debugging
            long now = System.currentTimeMillis();
            if (now - lastSaveTime >= 5000) {
                try {
                    ppu.renderBackgroundFrame("frame_" + frameCount + ".png");
                    frameCount++;
                    lastSaveTime = now;
                } catch (IOException e) {
                    System.err.println("Error saving frame: " + e.getMessage());
                }
            }
            
            // Small delay to prevent overwhelming the system
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
        }



        if (cpu.getMode() == Mode.DEBUG) {
            cpu.debugState(memory);
        }
        
        // Cleanup
        ppu.disableRealTimeDisplay();
        displayWindow.stop();
        System.out.println("🎮 Emulator stopped.");



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
