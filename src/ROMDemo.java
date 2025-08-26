import java.io.File;
import java.io.IOException;

public class ROMDemo {
    public static void main(String[] args) {
        System.out.println("🎮 NES ROM Loading Demo");
        System.out.println("========================");
        
        // Scan for available ROMs
        System.out.println("🔍 Scanning for available ROMs...");
        scanForRoms();
        
        // Test ROM loading functionality
        System.out.println("\n🧪 Testing ROM loading...");
        testRomLoading();
        
        System.out.println("\n✅ Demo completed!");
    }
    
    private static void scanForRoms() {
        File romsDir = new File("ROMs");
        if (romsDir.exists() && romsDir.isDirectory()) {
            System.out.println("📁 Found ROMs directory");
            
            // Scan root directory
            File[] rootFiles = romsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".nes"));
            if (rootFiles != null && rootFiles.length > 0) {
                System.out.println("📦 ROMs in root directory:");
                for (File file : rootFiles) {
                    System.out.println("   • " + file.getName() + " (" + file.length() + " bytes)");
                }
            }
            
            // Scan subdirectories
            File[] subdirs = romsDir.listFiles(File::isDirectory);
            if (subdirs != null) {
                for (File subdir : subdirs) {
                    System.out.println("📁 Scanning subdirectory: " + subdir.getName());
                    File[] subFiles = subdir.listFiles((dir, name) -> name.toLowerCase().endsWith(".nes"));
                    if (subFiles != null && subFiles.length > 0) {
                        for (File file : subFiles) {
                            System.out.println("   • " + subdir.getName() + "/" + file.getName() + " (" + file.length() + " bytes)");
                        }
                    }
                }
            }
        } else {
            System.out.println("⚠️  ROMs directory not found");
        }
    }
    
    private static void testRomLoading() {
        // Test with a sample ROM if available
        String[] testRoms = {
            "ROMs/1.Branch_Basics.nes",
            "ROMs/test_jsr_rts.nes",
            "ROMs/test_jsr_rts_with_header.nes"
        };
        
        for (String romPath : testRoms) {
            File romFile = new File(romPath);
            if (romFile.exists()) {
                System.out.println("🎮 Testing ROM: " + romPath);
                try {
                    // Test INES file parsing
                    INESFile inesFile = new INESFile(romPath);
                    System.out.println("   ✅ PRG ROM: " + inesFile.prgSize + " bytes");
                    System.out.println("   ✅ CHR ROM: " + inesFile.chrSize + " bytes");
                    System.out.println("   ✅ Mapper: " + inesFile.mapper);
                    System.out.println("   ✅ Trainer: " + inesFile.hasTrainer);
                } catch (IOException e) {
                    System.out.println("   ❌ Error loading: " + e.getMessage());
                }
            }
        }
    }
}