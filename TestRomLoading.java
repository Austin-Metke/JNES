import java.awt.image.BufferedImage;
import java.io.IOException;

public class TestRomLoading {
    public static void main(String[] args) {
        try {
            System.out.println("🧪 Testing ROM loading and frame display...");
            
            // Use command line argument or default ROM path
            String testRomPath = args.length > 0 ? args[0] : "ROMs/branch_timing_tests/1.Branch_Basics.nes";
            System.out.println("📁 Testing ROM path: " + testRomPath);
            
            // Check if ROM file exists
            java.io.File romFile = new java.io.File(testRomPath);
            if (romFile.exists()) {
                System.out.println("✅ ROM file exists");
                System.out.println("📊 ROM file size: " + romFile.length() + " bytes");
            } else {
                System.out.println("❌ ROM file not found");
                return;
            }
            
            // Test INES file parsing
            try {
                INESFile inesFile = new INESFile(testRomPath);
                System.out.println("✅ INES file parsed successfully");
                System.out.println("📦 PRG ROM size: " + inesFile.prgSize + " bytes");
                System.out.println("🎨 CHR ROM size: " + inesFile.chrSize + " bytes");
                System.out.println("🔧 Mapper: " + inesFile.mapper);
                System.out.println("📚 Has trainer: " + inesFile.hasTrainer);
                
                // Test PPU frame generation with ROM data
                System.out.println("\n🎬 Testing PPU frame generation with ROM data...");
                
                // Create mock components
                cpu6502.Memory ppuMemory = new cpu6502.Memory();
                cpu6502.CPU6502 mockCpu = new cpu6502.CPU6502(new cpu6502.Memory(), cpu6502.Mode.DEBUG);
                PPU ppu = new PPU(mockCpu, cpu6502.Mode.DEBUG, ppuMemory);
                
                // Load CHR ROM data into PPU memory (or initialize with default pattern if none)
                if (inesFile.chrRom != null && inesFile.chrRom.length > 0) {
                    System.out.println("🎨 Loading CHR ROM data into PPU memory...");
                    for (int i = 0; i < inesFile.chrRom.length && i < 0x2000; i++) {
                        ppuMemory.write(i, inesFile.chrRom[i] & 0xFF);
                    }
                } else {
                    System.out.println("🎨 No CHR ROM data - initializing with default pattern...");
                    for (int i = 0; i < 0x2000; i++) {
                        ppuMemory.write(i, (i & 0xFF));
                    }
                }
                
                // Initialize name table
                System.out.println("🎨 Initializing name table...");
                for (int row = 0; row < 30; row++) {
                    for (int col = 0; col < 32; col++) {
                        ppuMemory.write(0x2000 + row * 32 + col, (row + col) & 0xFF);
                    }
                }
                
                // Mark patterns as dirty and test frame generation
                ppu.markTilePatternsDirty();
                BufferedImage frame = ppu.generateFrameOptimized();
                
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
                System.out.println("❌ Error parsing INES file: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}