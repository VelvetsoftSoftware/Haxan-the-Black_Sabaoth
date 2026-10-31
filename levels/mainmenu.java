public class mainmenu {
    // Background tilemap
    public static final int[][] BACKGROUND_BUFFER = new int[17][22];
    // Scrollwork tilemap
    public static final int[][] BUFFER3 = new int[15][20];
    // Mode 1 background (256x256 pixels)
    private static final byte[] MODE1_MAP = new byte[256 * 256];
    // Mode 1 animation
    private static boolean startingFadeOut = false;
    private static boolean palettesInitialized = false;
    static int camX = 0, camY = 0;
	
	//mushit
	private static boolean musicrunning = false;

    // Mode 1 camera settings
    static final float CAMERA_HEIGHT = 80.0f;
    static final float PERSPECTIVE = 160.0f;
    static final int HORIZON = 40;

    // Pillar world position
    static float pillarX = 128.0f;
    static float pillarY = 180.0f;

    static {
        // Initialize background tiles
        for (int y = 0; y < 17; y++) {
            for (int x = 0; x < 22; x++) {
                BACKGROUND_BUFFER[y][x] = mainMenuTextures.BACKGROUND_TILES[(y & 1) * 2 + (x & 1)];
            }
        }
        // Initialize scrollwork
        for (int y = 0; y < 15; y++) {
            for (int x = 0; x < 20; x++) {
                if (y < 4) {
                    int tx = x & 1;
                    int ty = y & 1;
                    BUFFER3[y][x] = mainMenuTextures.SCROLLWORK_TILES[ty * 2 + tx];
                } else if (y == 4) {
                    BUFFER3[y][x] = 28;
                } else if (y == 12) {
                    BUFFER3[y][x] = 29;
                } else if (y >= 13) {
                    int tx = x & 1;
                    int ty = (y - 13) & 1;
                    BUFFER3[y][x] = mainMenuTextures.SCROLLWORK_TILES[ty * 2 + tx];
                } else {
                    BUFFER3[y][x] = 27;
                }
            }
        }
        // Build Mode 1 pixel map from background tiles
        buildMode1Map();
    }

    // Converts existing packed 4-bit tiles into a Mode 1 map
    private static void buildMode1Map() {
        for (int ty = 0; ty < 16; ty++) {
            for (int tx = 0; tx < 16; tx++) {
                int tileID = BACKGROUND_BUFFER[ty][tx];
                byte[] texture = mainMenuTextures.MAINMENUTEXTURES[tileID];

                for (int py = 0; py < 16; py++) {
                    for (int px = 0; px < 16; px++) {
                        int pixel = py * 16 + px;
                        int data = texture[pixel >> 1] & 0xFF;
                        int colorIndex;

                        if ((pixel & 1) == 0)
                            colorIndex = data >> 4;
                        else
                            colorIndex = data & 0x0F;

                        int destX = tx * 16 + px;
                        int destY = ty * 16 + py;

                        MODE1_MAP[destY * 256 + destX] = (byte) colorIndex;
                    }
                }
            }
        }
    }

    // Assign palettes once
	private static void initializePalettes() {

		spriteLoader.setPalette(0, mainMenuTextures.BACKGROUNDPALETTEMAINMENU);
		spriteLoader.setPalette(1, mainMenuTextures.BUTTONUNSELECTEDPALETTEMAINMENU);
		spriteLoader.setPalette(2, mainMenuTextures.SCROLLWORK);
		spriteLoader.setPalette(3, mainMenuTextures.SCROLLWORK);
		spriteLoader.setPalette(4, mainMenuTextures.TITLECARDAOFGAME);

		palettesInitialized = true;
	}

    // Draw pillar using Mode 1 world coordinates
    public static void drawPillar(Window window) {
        float distance = pillarY - camY;

        if (distance <= 0)
            return;

        // Project pillar onto the ground
        float screenY = HORIZON + (CAMERA_HEIGHT * PERSPECTIVE) / distance;
        float screenX = 160.0f + (pillarX - camX) * PERSPECTIVE / distance;

        // Don't draw pillars outside the visible ground
        if (screenY < HORIZON || screenY >= 240)
            return;

        spriteLoader.mode1pillar(window, 2, (int)screenX, (int)screenY, 32, 64, distance, mainMenuTextures.MAINMENUTEXTURES[23], mainMenuTextures.MAINMENUTEXTURES[24], mainMenuTextures.MAINMENUTEXTURES[25]);
    }

    public static void drawmainmenu(Window window) {	
        if (!palettesInitialized){
            initializePalettes();
		}
		
		if(musicrunning == false) {
			new Thread(() -> {
				main.music.playMidi(0);
			}, "MIDI-Transition").start();
			
			musicrunning = true;
		}

        // Input
        if (Input.pressed(java.awt.event.KeyEvent.VK_SPACE) || Input.pressed(java.awt.event.KeyEvent.VK_ENTER)) {
            startingFadeOut = true;
        }

        // Diagonal scrolling
        camX++;
        camY++;

        // Draw Mode 1 perspective background
        spriteLoader.drawMode1Perspective(window, 0, MODE1_MAP, camX, camY, CAMERA_HEIGHT, PERSPECTIVE, HORIZON, false);

        // Draw pillar on Mode 1 ground
        drawPillar(window);

        spriteLoader.drawtexture(window, 1, mainMenuTextures.MAINMENUTEXTURES, 4, 144, 120, 100);
        spriteLoader.drawtexture(window, 1, mainMenuTextures.MAINMENUTEXTURES, 5, 160, 120, 100);

        // Main title
        int temp = 0;

        for (int x = 16; temp < mainMenuTextures.MAIN_TITLE_TILES.length; x += 16) {
            spriteLoader.drawtexture(window, 4, mainMenuTextures.MAINMENUTEXTURES, mainMenuTextures.MAIN_TITLE_TILES[temp], x, 3, 100);
            temp++;
        }
        // Subtitle
        temp = 0;
        int y = 19;

        for (int x = 16; temp < mainMenuTextures.SUB_TITLE_TILES.length; x += 16) {
            if (temp == 9) {
                x = 16;
                y = 35;
            }
            spriteLoader.drawtexture(window, 4, mainMenuTextures.MAINMENUTEXTURES, mainMenuTextures.SUB_TITLE_TILES[temp], x, y, 100);
            temp++;
        }

        // Scrollwork
        for (int ty = 0; ty < BUFFER3.length; ty++) {
            for (int tx = 0; tx < BUFFER3[ty].length; tx++) {
                spriteLoader.drawtexture(window, 3, mainMenuTextures.MAINMENUTEXTURES, BUFFER3[ty][tx], tx * 16, ty * 16, 100);
            }
        }

        // Screen state
        if (startingFadeOut) {
            window.fadeOUT(window);

        } else if (main.firstrun != 1) {
            window.fadeIN(window);

        } else {
            window.compose();
            window.repaint();
        }
    }
}