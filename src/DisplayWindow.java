import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.Vector;

public class DisplayWindow extends JFrame {
    private static final int WINDOW_WIDTH = 256 * 2;  // 256 pixels * 2 for scaling
    private static final int WINDOW_HEIGHT = 240 * 2; // 240 pixels * 2 for scaling
    private static final int TARGET_FPS = 60;
    private static final long FRAME_TIME_NS = 1_000_000_000 / TARGET_FPS;
    
    private final JPanel displayPanel;
    private JComboBox<String> romSelector;
    private JButton loadRomButton;
    private JButton browseButton;
    private JLabel statusLabel;
    private BufferedImage currentFrame;
    private BufferedImage backBuffer; // Double buffering for smooth rendering
    private boolean isRunning = false;
    private volatile boolean frameUpdated = false; // Volatile for better performance
    
    // ROM management
    private Vector<String> availableRoms = new Vector<>();
    private String currentRom = null;
    
    public DisplayWindow() {
        // Check if we're in a headless environment
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("⚠️  Running in headless environment - display window will not be shown");
            System.out.println("📱 Frames will still be generated and can be saved to files");
        }
        
        setTitle("NES PPU Display - 60FPS");
        setSize(WINDOW_WIDTH + 50, WINDOW_HEIGHT + 100); // Extra space for controls
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        
        // Create control panel
        JPanel controlPanel = createControlPanel();
        
        // Create display panel with optimized painting
        displayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                // Use back buffer for smooth rendering
                if (backBuffer != null) {
                    g.drawImage(backBuffer, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, null);
                } else if (currentFrame != null) {
                    g.drawImage(currentFrame, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, null);
                } else {
                    // Draw a black background if no frame is available
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        displayPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        displayPanel.setBackground(Color.BLACK);
        
        // Enable double buffering for the panel
        displayPanel.setDoubleBuffered(true);
        
        // Layout setup
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(displayPanel, BorderLayout.CENTER);
        
        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isRunning = false;
            }
        });
        
        // Initialize ROM list
        scanForRoms();
        
        // Start the display loop
        startDisplayLoop();
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("ROM Controls"));
        
        // ROM selector dropdown
        romSelector = new JComboBox<>(availableRoms);
        romSelector.setPreferredSize(new Dimension(200, 25));
        romSelector.addActionListener(e -> {
            if (romSelector.getSelectedItem() != null) {
                currentRom = (String) romSelector.getSelectedItem();
                statusLabel.setText("Selected: " + currentRom);
            }
        });
        
        // Load ROM button
        loadRomButton = new JButton("Load ROM");
        loadRomButton.addActionListener(e -> loadSelectedRom());
        
        // Browse button
        browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browseForRom());
        
        // Reset button
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetEmulator());
        
        // Status label
        statusLabel = new JLabel("No ROM selected");
        statusLabel.setPreferredSize(new Dimension(150, 25));
        
        panel.add(new JLabel("ROM:"));
        panel.add(romSelector);
        panel.add(loadRomButton);
        panel.add(browseButton);
        panel.add(resetButton);
        panel.add(statusLabel);
        
        return panel;
    }
    
    private void scanForRoms() {
        availableRoms.clear();
        availableRoms.add("-- Select ROM --");
        
        // Scan ROMs directory
        File romsDir = new File("ROMs");
        if (romsDir.exists() && romsDir.isDirectory()) {
            scanDirectoryForRoms(romsDir);
        }
        
        // Scan subdirectories
        File[] subdirs = romsDir.listFiles(File::isDirectory);
        if (subdirs != null) {
            for (File subdir : subdirs) {
                scanDirectoryForRoms(subdir);
            }
        }
        
        romSelector.setModel(new DefaultComboBoxModel<>(availableRoms));
    }
    
    private void scanDirectoryForRoms(File directory) {
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".nes"));
        if (files != null) {
            for (File file : files) {
                availableRoms.add(directory.getName() + "/" + file.getName());
            }
        }
    }
    
    private void loadSelectedRom() {
        if (currentRom != null && !currentRom.equals("-- Select ROM --")) {
            String romPath = "ROMs/" + currentRom;
            File romFile = new File(romPath);
            
            if (romFile.exists()) {
                try {
                    // Load the ROM using the Main class
                    System.out.println("🎮 Loading ROM: " + romPath);
                    statusLabel.setText("Loading: " + currentRom);
                    
                    // Call the Main class method to load the ROM
                    Main.loadRomFromPath(romPath);
                    statusLabel.setText("Loaded: " + currentRom);
                    
                } catch (Exception e) {
                    statusLabel.setText("Error loading ROM");
                    System.err.println("Error loading ROM: " + e.getMessage());
                }
            } else {
                statusLabel.setText("ROM file not found");
            }
        }
    }
    
    private void browseForRom() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".nes");
            }
            public String getDescription() {
                return "NES ROM files (*.nes)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                try {
                    // Load the ROM directly from the selected file
                    System.out.println("🎮 Loading ROM from: " + selectedFile.getAbsolutePath());
                    statusLabel.setText("Loading: " + selectedFile.getName());
                    
                    // Call the Main class method to load the ROM
                    Main.loadRomFromPath(selectedFile.getAbsolutePath());
                    statusLabel.setText("Loaded: " + selectedFile.getName());
                    
                    // Add to available ROMs if not already present
                    String romName = selectedFile.getName();
                    if (!availableRoms.contains(romName)) {
                        availableRoms.add(romName);
                        romSelector.setModel(new DefaultComboBoxModel<>(availableRoms));
                    }
                    
                    // Select the new ROM
                    romSelector.setSelectedItem(romName);
                    currentRom = romName;
                    
                } catch (Exception e) {
                    statusLabel.setText("Error loading ROM");
                    System.err.println("Error loading ROM: " + e.getMessage());
                }
            }
        }
    }
    
    private void resetEmulator() {
        try {
            System.out.println("🔄 Resetting emulator...");
            statusLabel.setText("Resetting emulator...");
            
            // Call the Main class method to reset the emulator
            Main.resetEmulatorFromExternal();
            statusLabel.setText("Emulator reset");
            
        } catch (Exception e) {
            statusLabel.setText("Error resetting");
            System.err.println("Error resetting emulator: " + e.getMessage());
        }
    }
    
    private void startDisplayLoop() {
        isRunning = true;
        Thread displayThread = new Thread(() -> {
            long lastFrameTime = System.nanoTime();
            
            while (isRunning) {
                long currentTime = System.nanoTime();
                long elapsed = currentTime - lastFrameTime;
                
                if (elapsed >= FRAME_TIME_NS) {
                    // Only repaint if we have a new frame or need to maintain FPS
                    if (frameUpdated || elapsed >= FRAME_TIME_NS * 2) {
                        SwingUtilities.invokeLater(() -> {
                            displayPanel.repaint();
                            frameUpdated = false;
                        });
                    }
                    
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
        displayThread.setPriority(Thread.MAX_PRIORITY); // High priority for display thread
        displayThread.start();
    }
    
    public void updateFrame(BufferedImage newFrame) {
        if (newFrame != null) {
            // Create back buffer if it doesn't exist
            if (backBuffer == null) {
                backBuffer = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);
            }
            
            // Scale the new frame to the back buffer
            Graphics2D g2d = backBuffer.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            g2d.drawImage(newFrame, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, null);
            g2d.dispose();
            
            // Mark frame as updated for the display loop
            frameUpdated = true;
        }
    }
    
    public void stop() {
        isRunning = false;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    // Getter for the currently selected ROM
    public String getCurrentRom() {
        return currentRom;
    }
    
    // Method to refresh ROM list
    public void refreshRomList() {
        scanForRoms();
    }
}