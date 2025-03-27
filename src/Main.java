import cpu6502.*;

import java.io.IOException;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {



        INESFile ines = new INESFile("smb.nes");
        System.out.printf("📦 PRG ROM: %d bytes, CHR ROM: %d bytes, Mapper: %d\n", ines.prgSize, ines.chrSize, ines.mapper);
        Memory memory = new Memory();
        Memory ppuMemory = new Memory();
        MemoryPage prgPage = new RomPage(ines.prgRom, 0x8000);
        MemoryPage vram = new RamPage(0x800); // 2KB VRAM
        ppuMemory.mapRange(0x2000, 0x2FFF, vram); // mirrors every 0x1000


        MemoryPage ram = new RamPage(0x800); // 2KB RAM
        memory.mapRange(0x0000, 0x1FFF, ram); // mirror into full $0000–$1FFF

        if (ines.prgRom.length == 0x4000) {
            memory.mapRange(0x8000, 0xBFFF, prgPage);
            memory.mapRange(0xC000, 0xFFFF, prgPage); // mirror
        } else if (ines.prgRom.length == 0x8000) {
            memory.mapRange(0x8000, 0xFFFF, prgPage);
        } else {
            System.err.printf("❌ Unsupported PRG ROM size: %d\n", ines.prgRom.length);
            return;
        }



// Set correct interrupt vectors manually
        memory.write(0xFFFA, 0x00); memory.write(0xFFFB, 0xA0); // NMI → $A000
        memory.write(0xFFFC, 0x00); memory.write(0xFFFD, 0x80); // RESET → $8000
        memory.write(0xFFFE, 0x00); memory.write(0xFFFF, 0xA0); // IRQ/BRK → $A000



        System.out.printf("🧠 memory[0xFFFC] = %02X\n", memory.read(0xFFFC));
        System.out.printf("🧠 memory[0xFFFD] = %02X\n", memory.read(0xFFFD));
        System.out.printf("📍 CPU Reset Vector = %04X\n", memory.readWord(0xFFFC));



        CPU6502 cpu = new CPU6502(memory, Mode.DEBUG);
        ClockController clock = new ClockController(1_789_773);
        //ClockController clock = new ClockController(Integer.MAX_VALUE);

        PPU ppu = new PPU(cpu, Mode.DEBUG, memory);

        // CHR ROM goes into PPU space: $0000–$1FFF = pages 0–7
        MemoryPage chrRomPage = new RomPage(ines.chrRom, 0x0000);
        for (int i = 0; i < 8; i++) {
            ppu.mapPage(i, chrRomPage); // page 0 = $0000–$03FF, page 1 = $0400–$07FF, etc.
        }


        for (int row = 0; row < 30; row++) {
            for (int col = 0; col < 32; col++) {
                int tileIndex = (row * 32 + col) & 0xFF; // wrap 0–255
                ppuMemory.write(0x2000 + row * 32 + col, tileIndex);
            }
        }


        cpu.reset();
        int totalCycles = 0;
        int frameTrigger = 60; // after 60 frames

        while (!cpu.halted) {
            int cycles = cpu.clock();
            totalCycles += cycles;

            for (int i = 0; i < cycles * 3; i++) {
                ppu.clock();
            }

            if (ppu.getFrameCounter() == frameTrigger) {
                ppu.requestRenderOnNextVBlank();
                break; // Optionally stop here
            }

            if (totalCycles >= 29829) {
                clock.throttle(totalCycles);
                totalCycles = 0;
            }
        }



        if (cpu.getMode() == Mode.DEBUG) {
            cpu.debugState(memory);
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
