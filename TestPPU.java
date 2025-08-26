import java.awt.image.BufferedImage;
import java.io.IOException;

public class TestPPU {
    public static void main(String[] args) {
        try {
            System.out.println("🧪 Testing PPU frame generation...");
            
            // Create a mock CPU and memory
            cpu6502.Memory ppuMemory = new cpu6502.Memory();
            cpu6502.CPU6502 mockCpu = new cpu6502.CPU6502(new cpu6502.Memory(), cpu6502.Mode.DEBUG);
            
            // Create PPU
            PPU ppu = new PPU(mockCpu, cpu6502.Mode.DEBUG, ppuMemory);
            
            // Test frame generation without display window
            System.out.println("🎬 Testing frame generation...");
            BufferedImage frame = ppu.generateFrameOptimized();
            if (frame != null) {
                System.out.println("✅ Frame generated successfully: " + frame.getWidth() + "x" + frame.getHeight());
                
                // Check if frame has non-black pixels
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
                System.out.println("❌ Frame generation failed - null frame");
            }
            
            // Test with a simple ROM pattern
            System.out.println("\n🎮 Testing with ROM data...");
            
            // Load a simple test pattern into PPU memory
            for (int i = 0; i < 0x2000; i++) {
                ppuMemory.write(i, (i & 0xFF));
            }
            
            // Initialize name table with a simple pattern
            for (int row = 0; row < 30; row++) {
                for (int col = 0; col < 32; col++) {
                    ppuMemory.write(0x2000 + row * 32 + col, (row + col) & 0xFF);
                }
            }
            
            // Mark patterns as dirty and generate new frame
            ppu.markTilePatternsDirty();
            frame = ppu.generateFrameOptimized();
            
            if (frame != null) {
                System.out.println("✅ ROM frame generated: " + frame.getWidth() + "x" + frame.getHeight());
                
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
                System.out.println("🎨 ROM frame non-black pixels: " + nonBlackPixels + "/" + totalPixels + 
                                 " (" + (nonBlackPixels * 100 / totalPixels) + "%)");
                
            } else {
                System.out.println("❌ ROM frame generation failed");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}