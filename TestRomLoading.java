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