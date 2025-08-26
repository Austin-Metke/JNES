import java.awt.image.BufferedImage;

public class TestEmulatorLoop {
    public static void main(String[] args) {
        try {
            System.out.println("🧪 Testing emulator loop and PPU VBLANK...");
            
            // Create mock components
            cpu6502.Memory ppuMemory = new cpu6502.Memory();
            cpu6502.CPU6502 mockCpu = new cpu6502.CPU6502(new cpu6502.Memory(), cpu6502.Mode.DEBUG);
            PPU ppu = new PPU(mockCpu, cpu6502.Mode.DEBUG, ppuMemory);
            
            // Initialize with test pattern
            for (int i = 0; i < 0x2000; i++) {
                ppuMemory.write(i, (i & 0xFF));
            }
            
            // Initialize name table
            for (int row = 0; row < 30; row++) {
                for (int col = 0; col < 32; col++) {
                    ppuMemory.write(0x2000 + row * 32 + col, (row + col) & 0xFF);
                }
            }
            
            ppu.markTilePatternsDirty();
            
            System.out.println("🎬 Testing PPU clocking to reach VBLANK...");
            
            // Clock the PPU enough times to reach VBLANK
            // VBLANK starts at scanline 241, each scanline has 341 cycles
            int cyclesToVBlank = 241 * 341;
            System.out.println("⏱️ Clocking PPU " + cyclesToVBlank + " cycles to reach VBLANK...");
            
            for (int i = 0; i < cyclesToVBlank; i++) {
                ppu.clock();
            }
            
            System.out.println("✅ PPU clocked to VBLANK");
            System.out.println("📊 Current scanline: " + ppu.getScanline());
            System.out.println("📊 Current cycle: " + ppu.getCycle());
            System.out.println("📊 Frame counter: " + ppu.getFrameCounter());
            
            // Test frame generation
            System.out.println("\n🎬 Testing frame generation...");
            BufferedImage frame = ppu.generateFrameOptimized();
            if (frame != null) {
                System.out.println("✅ Frame generated: " + frame.getWidth() + "x" + frame.getHeight());
                
                // Check pixel content
                int nonBlackPixels = 0;
                int totalPixels = frame.getWidth() * frame.getHeight();
                for (int y = 0; y < frame.getHeight(); y++) {
                    for (int x = 0; x < frame.getWidth(); x++) {
                        int rgb = frame.getRGB(x, y);
                        if (rgb != 0x000000) { // Not black
                            nonBlackPixels++;
                        }
                    }
                }
                System.out.println("🎨 Non-black pixels: " + nonBlackPixels + "/" + totalPixels + 
                                 " (" + (nonBlackPixels * 100 / totalPixels) + "%)");
            } else {
                System.out.println("❌ Frame generation failed");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}