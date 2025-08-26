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

    public PPU(CPU6502 cpu) {
        this.cpu = cpu;
    }


    public PPU(CPU6502 cpu, Mode mode, Memory memory) {
        this.cpu = cpu;
        this.mode = mode;
        this.ppuMemory = memory;
    }


    public void clock() {
        cycle++;
        if (cycle >= CYCLES_PER_SCANLINE) {
            cycle = 0;
            scanline++;
            if (scanline == VBLANK_START_LINE) {
                try {
                    enterVBlank();
                } catch (IOException e) {
                    System.err.println("Error during VBlank: " + e.getMessage());
                }
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


    private void enterVBlank() throws IOException {
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

        if (renderOnNextVBlank) {
            renderBackgroundFrame("frame.png");
            renderOnNextVBlank = false; // reset
        }


    }


    public void mapPage(int pageIndex, MemoryPage page) {
        ppuPages[pageIndex] = page;
    }


    private int readCHR(int addr) {
        int pageIndex = (addr >> 10) & 0x0F; // 1 KB per page
        MemoryPage page = ppuPages[pageIndex];
        if (page == null) return 0;
        return page.read(addr);
    }

    public void renderBackgroundFrame(String filename) throws IOException {
        int tilesPerRow = 32;
        int tilesPerCol = 30;
        int tileSize = 8;
        int width = tilesPerRow * tileSize;
        int height = tilesPerCol * tileSize;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Color[] palette = {
                new Color(255, 255, 255), // 0
                new Color(170, 170, 170), // 1
                new Color(85, 85, 85),    // 2
                new Color(0, 0, 0)        // 3
        };

        for (int row = 0; row < tilesPerCol; row++) {
            for (int col = 0; col < tilesPerRow; col++) {
                int tileIndex = ppuMemory.read(0x2000 + row * 32 + col) & 0xFF;
                int base = tileIndex * 16;

                for (int y = 0; y < 8; y++) {
                    int b0 = readCHR(base + y);
                    int b1 = readCHR(base + y + 8);

                    for (int x = 0; x < 8; x++) {
                        int bit0 = (b0 >> (7 - x)) & 1;
                        int bit1 = (b1 >> (7 - x)) & 1;
                        int pixel = (bit1 << 1) | bit0;

                        image.setRGB(col * 8 + x, row * 8 + y, palette[pixel].getRGB());
                    }
                }
            }
        }

        ImageIO.write(image, "png", new File(filename));
        System.out.println("🖼️ Background rendered to: " + filename);
    }


    public int getScanline() {
        return scanline;
    }

    public int getCycle() {
        return cycle;
    }
}