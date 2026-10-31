public class intro {
    public static final int[] LETTERPALETTE = {
        0x0B0B10, // 0 - background
        0x4A0E17, // 1
        0x8B1E2F, // 2
        0xD4A359, // 3
        0xF4F4F9  // 4
    };

    public static final int[] PALETTE_MASCCOT = {
        0x0B0B10, // 0: Transparent/Background
        0xF4F4F9, // 1: Pure White
        0xD4A359, // 2: Bright Gold
        0x101018, // 3: Jet Black
        0x252535, // 4: Dark Slate
        0xFF2A4B, // 5: Crimson Red
        0x9E1B32, // 6: Deep Crimson
        0xFFE0BD, // 7: Pale Skin
        0xF5C29B, // 8: Skin Shadow
        0x9A9AB0, // 9: Mid-Tone Hair Shadow
        0x333348, // A: Secondary Lineart
        0x000000, // B: Pure Black Outline
        0xB8860B, // C: Dark Gold
        0xDCDCE5, // D: Soft White
        0x6E6E85, // E: Deep Hair Shadow
        0x1E1E2C  // F: Very Dark Slate
    };

    private static final int WIDTH_TILES = 20;
    private static final int HEIGHT_TILES = 15;

    private static final int[] LETTER_BUFFER = new int[WIDTH_TILES * HEIGHT_TILES];

    private static final int[] CHARACTER_BUFFER = new int[WIDTH_TILES * HEIGHT_TILES];

    private static boolean palettesInitialized = false;

    static {
        // --- 1. TEXT BUFFER ---
        int textStart = (7 * WIDTH_TILES) + 2;

        LETTER_BUFFER[textStart]     = 1;
        LETTER_BUFFER[textStart + 1] = 2;
        LETTER_BUFFER[textStart + 2] = 3;
        LETTER_BUFFER[textStart + 3] = 1;
        LETTER_BUFFER[textStart + 4] = 2;
        LETTER_BUFFER[textStart + 5] = 4;
        LETTER_BUFFER[textStart + 6] = 5;
        LETTER_BUFFER[textStart + 7] = 6;
        LETTER_BUFFER[textStart + 8] = 7;
        LETTER_BUFFER[textStart + 9] = 4;

        // --- 2. 4x4 CHARACTER BUFFER ---
        int charStartCol = 12;
        int charStartRow = 4;

        int[] tileLayout = {
            8, 9, 10, 11,
            12, 13, 14, 15,
            16, 17, 18, 19,
            20, 21, 22, 23
        };

        int layoutIdx = 0;

        for (int row = 0; row < 4; row++) {
            int rowOffset = ((charStartRow + row) * WIDTH_TILES) + charStartCol;
            for (int col = 0; col < 4; col++) {
                int texID = tileLayout[layoutIdx++];

                if (texID != -1 && texID < Textures.TEXTURES.length) 
                    CHARACTER_BUFFER[rowOffset + col] = texID;
                else 
                    CHARACTER_BUFFER[rowOffset + col] = 0;
            }
        }
    }

    static int xPOS = 0, yPOS = 0, scale = 1, x = 0;

    // Assign palettes once
    private static void initializePalettes() {
        // Background
        spriteLoader.setPalette(0, LETTERPALETTE);
        // Logo
        spriteLoader.setPalette(2, LETTERPALETTE);
        // Mascot
        spriteLoader.setPalette(3, PALETTE_MASCCOT);
        palettesInitialized = true;
    }

    public static void loadintro(Window window) {
         if (!palettesInitialized){
            initializePalettes();
		}
		
		
        if (scale != 100) {
            x = 0;
            yPOS = 0;
            while (yPOS < 240) {
                xPOS = 0;
                while (xPOS < 320) {
                    // Logo - Layer 2 (Mode 0)
                    if (LETTER_BUFFER[x] != 0) {
                        spriteLoader.drawtexture(window, 2, Textures.TEXTURES, LETTER_BUFFER[x], xPOS, yPOS, scale);
                    }
                    // Mascot - Layer 3 (Mode 0)
                    if (CHARACTER_BUFFER[x] != 0) {
						spriteLoader.drawtexture( window, 3, Textures.TEXTURES, CHARACTER_BUFFER[x], xPOS, yPOS, scale);
                    }
                    // Background - Layer 0 (Mode 0)
                    spriteLoader.drawtexture(window, 0, Textures.TEXTURES, 0, xPOS, yPOS, scale);
                    x++;
                    xPOS += 16;
                }
                yPOS += 16;
            }
            window.compose();
            window.repaint();

            scale++;
        } else {
            window.fadeOUT(window);
        }
    }
}