import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayDeque;

public class Window extends JPanel {

    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final int PIXELS = WIDTH * HEIGHT;
	
	
	private volatile long lastPaintTime = 0;
	private volatile long maxPaintTime = 0;

	public double getLastPaintTime() {
		return lastPaintTime / 1_000_000.0;
	}

	public double getMaxPaintTime() {
		return maxPaintTime / 1_000_000.0;
	}


    // Rendering buffers
    int[] background1;
    int[] background2;
    int[] playground;
    int[] foreground1;
    int[] foreground2;

    int[] screen;
    BufferedImage screenImage;

    int scale = 2;

    private JFrame frame;
    private JComboBox<String> resolutionBox;
    private JPanel menu;

    private volatile boolean fullscreen = false;
    private volatile boolean maximized = false;
    private boolean changingMode = false;

    private BufferedImage borderImage;

    private Timer resizeTimer;

    private GraphicsDevice device;

    private Point windowedLocation;

    // Border configuration
    private static final double BORDER_CROP = 0.16;
    private static final int BORDER_OVERLAP = 12;

    public Window() {

        // -------------------------------------
        // INITIALIZE FRAMEBUFFERS
        // -------------------------------------

        background1 = new int[PIXELS];
        background2 = new int[PIXELS];
        playground = new int[PIXELS];
        foreground1 = new int[PIXELS];
        foreground2 = new int[PIXELS];

        screen = new int[PIXELS];
        Arrays.fill(screen, 0xFF000000);

        // Direct framebuffer access
        DataBufferInt dataBuffer = new DataBufferInt(screen, screen.length);

        int[] masks = {
            0x00FF0000,
            0x0000FF00,
            0x000000FF,
            0xFF000000
        };

        SinglePixelPackedSampleModel sampleModel = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, WIDTH, HEIGHT, masks);

        WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);
        screenImage = new BufferedImage(ColorModel.getRGBdefault(), raster, false, null);
        // -------------------------------------
        // LOAD ORNAMENTAL BORDER
        // -------------------------------------

        try {
            BufferedImage original = ImageIO.read(new File("textures/border.png"));

            if (original != null) {
                borderImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);

                Graphics2D bg = borderImage.createGraphics();
                bg.drawImage(original, 0, 0, null);
                bg.dispose();
                // Remove exterior black background
                removeExteriorBlack(borderImage);

            }

        } catch (IOException e) {
            System.out.println("Could not load textures/border.png");
        }

        // -------------------------------------
        // WINDOW SETUP
        // -------------------------------------
        device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        frame = new JFrame("Haxan: The Black Sabaoth");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Required for Windows maximize button
        frame.setResizable(true);

        resolutionBox = new JComboBox<>(
            new String[] {
                "320 x 240 1x",
                "640 x 480 2x",
                "960 x 720 3x",
                "1280 x 960 4x",
                "1600 x 1200 5x"
            }
        );

        resolutionBox.setSelectedIndex(scale - 1);
        resolutionBox.setFocusable(false);

        resolutionBox.addActionListener(e -> {
            if (fullscreen || maximized) {
                return;
            }

            scale = resolutionBox.getSelectedIndex() + 1;
            resetWindowSize(true);
            requestFocusInWindow();
        });

        menu = new JPanel();
        menu.add(new JLabel("Resolution:"));
        menu.add(resolutionBox);

        JPanel container = new JPanel(new BorderLayout());
        container.add(menu, BorderLayout.NORTH);
        container.add(this, BorderLayout.CENTER);
        frame.setContentPane(container);
        // Window icon
        try {
            Image icon = ImageIO.read(new File("icon16.png"));
            frame.setIconImage(icon);
        } catch (IOException e) {
            System.out.println("Could not load icon16.png");
        }
        setFocusable(true);
        setBackground(Color.BLACK);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        // -------------------------------------
        // RESIZE TIMER
        // -------------------------------------

        resizeTimer = new Timer(250, e -> {
            if (fullscreen || maximized || changingMode || !frame.isVisible()) {
                return;
            }
            Dimension expected = getExpectedWindowSize();
            if (!frame.getSize().equals(expected)) {
                resetWindowSize(false);
            }
        });

        resizeTimer.setRepeats(false);

        frame.addComponentListener(
            new ComponentAdapter() {

                @Override
                public void componentResized(ComponentEvent e) {
                    if (!fullscreen && !maximized && !changingMode) {
                        resizeTimer.restart();
                    }
                }
            }
        );

        // -------------------------------------
        // MAXIMIZE / RESTORE
        // -------------------------------------
        frame.addWindowStateListener(e -> {
            if (fullscreen || changingMode) {
                return;
            }

            boolean nowMaximized = (e.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
            if (nowMaximized == maximized) {
                return;
            }
            maximized = nowMaximized;
            if (maximized) {
                resizeTimer.stop();
                menu.setVisible(false);
            } else {
                menu.setVisible(true);
                SwingUtilities.invokeLater(() -> {
                    if (!fullscreen && !maximized) {
                        resetWindowSize(false);
                    }
                });
            }
            repaint();
        });

        // -------------------------------------
        // SHOW WINDOW
        // -------------------------------------
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        windowedLocation = frame.getLocation();
        requestFocusInWindow();
    }

    // -----------------------------------------
    // REMOVE EXTERIOR BLACK BACKGROUND
    // -----------------------------------------
    private void removeExteriorBlack(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        boolean[] visited = new boolean[w * h];

        ArrayDeque<Integer> queue = new ArrayDeque<>();

        // Start from all black pixels
        // touching the image edges
        for (int x = 0; x < w; x++) {
            addBlackPixel(image, x, 0, visited, queue);
            addBlackPixel(image, x, h - 1, visited, queue);
        }
        for (int y = 0; y < h; y++) {
            addBlackPixel(image, 0, y, visited, queue);
            addBlackPixel(image, w - 1, y, visited, queue);
        }

        while (!queue.isEmpty()) {
            int index = queue.removeFirst();
            int x = index % w;
            int y = index / w;

            // Make pixel transparent
            image.setRGB(x, y, 0x00000000);

            if (x > 0) {
                addBlackPixel(
                    image, x - 1, y,
                    visited, queue
                );
            }
            if (x < w - 1) {
                addBlackPixel(
                    image, x + 1, y,
                    visited, queue
                );
            }
            if (y > 0) {
                addBlackPixel(
                    image, x, y - 1,
                    visited, queue
                );
            }
            if (y < h - 1) {
                addBlackPixel(
                    image, x, y + 1,
                    visited, queue
                );
            }
        }
    }

    private void addBlackPixel(BufferedImage image, int x, int y, boolean[] visited, ArrayDeque<Integer> queue) {

        int w = image.getWidth();
        int index = y * w + x;

        if (visited[index]) {
            return;
        }
        int color = image.getRGB(x, y);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // Only remove nearly-black pixels
        if (r <= 8 && g <= 8 && b <= 8) {
            visited[index] = true;
            queue.add(index);
        }
    }

    // -----------------------------------------
    // WINDOW DIMENSIONS
    // -----------------------------------------

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH * scale, HEIGHT * scale );
    }

    private Dimension getExpectedWindowSize() {
        Insets insets = frame.getInsets();
        int width = WIDTH * scale + insets.left + insets.right;
        int height = HEIGHT * scale + menu.getPreferredSize().height + insets.top + insets.bottom;

        return new Dimension(width, height);
    }

    private void resetWindowSize(boolean center) {

        if (fullscreen || maximized) {
            return;
        }
        changingMode = true;
        resizeTimer.stop();
        frame.pack();

        if (center) {
            frame.setLocationRelativeTo(null);
        }
        changingMode = false;
        repaint();
    }

    // -----------------------------------------
    // FULLSCREEN TOGGLE
    // -----------------------------------------
    public void toggleFullscreen() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> toggleFullscreen());
            return;
        }

        if (changingMode) {
            return;
        }

        changingMode = true;
        resizeTimer.stop();

        if (!fullscreen) {
            // Save windowed position
            if (!maximized) {
                windowedLocation = frame.getLocation();
            }

            fullscreen = true;
            maximized = false;
            frame.dispose();
            frame.setExtendedState(JFrame.NORMAL);
            frame.setUndecorated(true);
            frame.setResizable(false);
			
            menu.setVisible(false);

            Rectangle bounds = device.getDefaultConfiguration().getBounds();

            frame.setBounds(bounds);
            frame.setVisible(true);

        } else {
            fullscreen = false;
            maximized = false;

            frame.dispose();
            frame.setUndecorated(false);
            frame.setResizable(true);
            frame.setExtendedState(JFrame.NORMAL);
            menu.setVisible(true);
            frame.pack();

            if (windowedLocation != null) {
                frame.setLocation(windowedLocation);
            } else {
                frame.setLocationRelativeTo(null);
            }

            frame.setVisible(true);
        }

        changingMode = false;

        SwingUtilities.invokeLater(() -> {
            requestFocusInWindow();
            repaint();
        });
    }

    // -----------------------------------------
    // DISPLAY RENDERING
    // -----------------------------------------
	@Override
	protected void paintComponent(Graphics g) {
		long paintStart = System.nanoTime();

		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g.create();

		try {
			int displayWidth = getWidth();
			int displayHeight = getHeight();

			g2.setColor(Color.BLACK);
			g2.fillRect(0, 0, displayWidth, displayHeight);

			g2.setRenderingHint(
				RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
			);

			int gameScale = scale;

			if (fullscreen || maximized) {
				gameScale = Math.max(
					1,
					Math.min(
						displayWidth / WIDTH,
						displayHeight / HEIGHT
					)
				);
			}

			int gameWidth = WIDTH * gameScale;
			int gameHeight = HEIGHT * gameScale;

			int gameX = (displayWidth - gameWidth) / 2;
			int gameY = (displayHeight - gameHeight) / 2;

			// Draw game framebuffer
			g2.drawImage( screenImage, gameX, gameY, gameWidth, gameHeight, null );

			// Draw ornamental border in fullscreen/maximized mode
			if ((fullscreen || maximized) && borderImage != null) {
				g2.drawImage( borderImage, 0, 0, displayWidth, displayHeight, null );

				// Centered title
				String title = "Haxan: The Black Sabaoth";

				int fontSize = Math.max(12, displayWidth / 55);

				g2.setFont(new Font( Font.MONOSPACED, Font.BOLD, fontSize));

				FontMetrics metrics = g2.getFontMetrics();

				int titleX = (displayWidth - metrics.stringWidth(title)) / 2;

				int titleY = Math.max(fontSize + 8, displayHeight / 24);

				// Black shadow
				g2.setColor(new Color(35, 12, 30));
				g2.drawString(title, titleX + 2, titleY + 2);

				// Gold title
				g2.setColor(new Color(235, 196, 116));
				g2.drawString(title, titleX, titleY);
			}

		} finally {
			g2.dispose();

			long elapsed = System.nanoTime() - paintStart;

			lastPaintTime = elapsed;

			if (elapsed > maxPaintTime) {
				maxPaintTime = elapsed;
			}
		}
	}

    // -----------------------------------------
    // SET PIXEL
    // -----------------------------------------
    public int[] getBuffer(int buffer) {
		switch(buffer) {
			case 0: return background1;
			case 1: return background2;
			case 2: return playground;
			case 3: return foreground1;
			case 4: return foreground2;
			default: throw new IllegalArgumentException("Invalid buffer");
		}
	}

    // -----------------------------------------
    // COMPOSE
    // -----------------------------------------

	public void compose() {
		for(int i = 0; i < PIXELS; i++) {
			int color = foreground2[i];

			if(color == 0)
				color = foreground1[i];

			if(color == 0)
				color = playground[i];

			if(color == 0)
				color = background2[i];

			if(color == 0)
				color = background1[i];

			screen[i] = color;
		}
	}

    // -----------------------------------------
    // CLEAR BUFFERS
    // -----------------------------------------

    public void clearBuffers() {
        Arrays.fill(background1, 0);
        Arrays.fill(background2, 0);
        Arrays.fill(playground, 0);
        Arrays.fill(foreground1, 0);
        Arrays.fill(foreground2, 0);
    }

    // -----------------------------------------
    // FADE
    // -----------------------------------------
    private void fade(int alpha) {
        int amount = 255 - alpha;

        for (int i = 0; i < screen.length; i++) {
            int pixel = screen[i];
            int r = ((pixel >> 16) & 0xFF) * amount / 255;
            int g = ((pixel >> 8) & 0xFF) * amount / 255;
            int b = (pixel & 0xFF) * amount / 255;
			
            screen[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
        }
    }

    // -----------------------------------------
    // FADE OUT
    // -----------------------------------------
    static int alpha = 0;

    public void fadeOUT(Window window) {
        if (alpha <= 255) {
            fade(alpha);
            window.repaint();
            alpha += 5;
        }

        if (alpha >= 255) {
            alpha = 0;
            main.firstrun = 0;
            main.gameState++;
        }
    }

    // -----------------------------------------
    // FADE IN
    // -----------------------------------------
    static int alpha1 = 255;

    public void fadeIN(Window window) {
        if (alpha1 >= 0) {
            compose();
            fade(alpha1);
            window.repaint();
            alpha1 -= 5;
        }
        if (alpha1 <= 0) {
            alpha1 = 255;
            main.firstrun = 1;
        }
    }

    // -----------------------------------------
    // OBJECT FADE OUT
    // -----------------------------------------
    static int alpha2 = 0;

    public int fadeOutOBJ(Window window) {
        if (alpha2 <= 255) {
            fade(alpha2);
            window.repaint();
            alpha2 += 5;
        }
        if (alpha2 >= 255) {
            alpha2 = 0;
            return 1;
        }

        return 0;
    }

    // -----------------------------------------
    // OBJECT FADE IN
    // -----------------------------------------
    static int alpha3 = 255;

    public int fadeInOBJ(Window window) {
        if (alpha3 >= 0) {
            compose();
            fade(alpha3);
            window.repaint();
            alpha3 -= 5;
        }
        if (alpha3 <= 0) {
            alpha3 = 255;
            return 1;
        }
		
        return 0;
    }
}
