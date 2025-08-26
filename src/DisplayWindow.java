import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.GraphicsEnvironment;

public class DisplayWindow extends JFrame {
    private static final int WINDOW_WIDTH = 256 * 2;  // 256 pixels * 2 for scaling
    private static final int WINDOW_HEIGHT = 240 * 2; // 240 pixels * 2 for scaling
    private static final int TARGET_FPS = 60;
    private static final long FRAME_TIME_NS = 1_000_000_000 / TARGET_FPS;
    
    private final JPanel displayPanel;
    private BufferedImage currentFrame;
    private boolean isRunning = false;
    private final Object frameLock = new Object();
    
    public DisplayWindow() {
        // Check if we're in a headless environment
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("⚠️  Running in headless environment - display window will not be shown");
            System.out.println("📱 Frames will still be generated and can be saved to files");
        }
        
        setTitle("NES PPU Display - 60FPS");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        
        // Create display panel
        displayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                synchronized (frameLock) {
                    if (currentFrame != null) {
                        // Scale the image to fit the window
                        g.drawImage(currentFrame, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, null);
                    } else {
                        // Draw a black background if no frame is available
                        g.setColor(Color.BLACK);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                }
            }
        };
        displayPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        displayPanel.setBackground(Color.BLACK);
        
        add(displayPanel);
        
        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isRunning = false;
            }
        });
        
        // Start the display loop
        startDisplayLoop();
    }
    
    private void startDisplayLoop() {
        isRunning = true;
        Thread displayThread = new Thread(() -> {
            long lastFrameTime = System.nanoTime();
            
            while (isRunning) {
                long currentTime = System.nanoTime();
                long elapsed = currentTime - lastFrameTime;
                
                if (elapsed >= FRAME_TIME_NS) {
                    // Repaint the display panel
                    SwingUtilities.invokeLater(() -> displayPanel.repaint());
                    
                    lastFrameTime = currentTime;
                    
                    // Calculate sleep time to maintain 60FPS
                    long sleepTime = FRAME_TIME_NS - elapsed;
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        });
        displayThread.setDaemon(true);
        displayThread.start();
    }
    
    public void updateFrame(BufferedImage newFrame) {
        synchronized (frameLock) {
            currentFrame = newFrame;
        }
    }
    
    public void stop() {
        isRunning = false;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
}