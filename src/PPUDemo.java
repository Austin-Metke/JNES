import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;

public class PPUDemo {
    public static void main(String[] args) throws IOException {
        System.out.println("🎮 NES PPU Display Demo");
        System.out.println("=========================");
        
        // Create a simple animated pattern
        for (int frame = 0; frame < 10; frame++) {
            BufferedImage image = createAnimatedFrame(frame);
            
            // Save the frame
            String filename = "demo_frame_" + frame + ".png";
            ImageIO.write(image, "png", new File(filename));
            System.out.println("📸 Generated: " + filename);
            
            // Simulate 60FPS timing
            try {
                Thread.sleep(1000 / 60); // 16.67ms per frame
            } catch (InterruptedException e) {
                break;
            }
        }
        
        System.out.println("✅ Demo completed! Check the generated PNG files.");
    }
    
    private static BufferedImage createAnimatedFrame(int frameNumber) {
        int width = 256;
        int height = 240;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // Create a moving pattern
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Create a moving wave pattern
                int r = (x + frameNumber * 5) % 256;
                int g = (y + frameNumber * 3) % 256;
                int b = ((x + y) / 2 + frameNumber * 2) % 256;
                
                // Add some NES-style color constraints
                r = (r / 64) * 64; // Quantize to NES-like colors
                g = (g / 64) * 64;
                b = (b / 64) * 64;
                
                Color color = new Color(r, g, b);
                image.setRGB(x, y, color.getRGB());
            }
        }
        
        return image;
    }
}