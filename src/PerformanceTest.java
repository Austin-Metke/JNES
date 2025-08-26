import java.awt.image.BufferedImage;
import java.io.IOException;
import cpu6502.Memory;
import cpu6502.MemoryPage;
import cpu6502.RamPage;

public class PerformanceTest {
    public static void main(String[] args) throws IOException {
        System.out.println("🚀 Starting PPU Performance Test...");
        
        // Test parameters
        int testFrames = 1000;
        long startTime = System.currentTimeMillis();
        
        // Create a test PPU instance with proper memory setup
        Memory ppuMemory = new Memory();
        MemoryPage vram = new RamPage(0x800); // 2KB VRAM
        ppuMemory.mapRange(0x2000, 0x2FFF, vram); // mirrors every 0x1000
        
        // Create test pattern in VRAM
        for (int row = 0; row < 30; row++) {
            for (int col = 0; col < 32; col++) {
                int tileIndex = (row * 32 + col) & 0xFF; // wrap 0–255
                ppuMemory.write(0x2000 + row * 32 + col, tileIndex);
            }
        }
        
        PPU ppu = new PPU(null, null, ppuMemory);
        
        System.out.println("📊 Testing frame generation performance...");
        System.out.println("Generating " + testFrames + " frames...");
        
        // Generate test frames
        for (int i = 0; i < testFrames; i++) {
            BufferedImage frame = ppu.generateFrameOptimized();
            
            // Progress indicator every 100 frames
            if ((i + 1) % 100 == 0) {
                System.out.printf("Generated %d frames...\n", i + 1);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double fps = (double) testFrames / (totalTime / 1000.0);
        
        System.out.println("✅ Performance test completed!");
        System.out.printf("📈 Generated %d frames in %d ms\n", testFrames, totalTime);
        System.out.printf("⚡ Average FPS: %.2f\n", fps);
        System.out.printf("⏱️  Average frame time: %.2f ms\n", (double) totalTime / testFrames);
        
        // Performance benchmarks
        if (fps >= 55) {
            System.out.println("🎯 EXCELLENT: Performance meets 60 FPS target");
        } else if (fps >= 45) {
            System.out.println("👍 GOOD: Performance is acceptable");
        } else if (fps >= 30) {
            System.out.println("⚠️  FAIR: Performance could be improved");
        } else {
            System.out.println("❌ POOR: Performance needs optimization");
        }
        
        // Memory usage estimation
        long estimatedMemory = testFrames * 256 * 240 * 3; // 3 bytes per pixel
        System.out.printf("💾 Estimated memory processed: %.2f MB\n", estimatedMemory / (1024.0 * 1024.0));
    }
}