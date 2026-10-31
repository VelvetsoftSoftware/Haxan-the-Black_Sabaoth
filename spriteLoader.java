public class spriteLoader {

    /////////////////////
    // buffer
    // 0: background 1
    // 1: background 2
    // 2: playfield
    // 3: foreground 1
    // 4: foreground 2
    /////////////////////

	// to do, add mode 2 which uses 2 bit images, this would be for the text so an uncompressed image would be 32x32 bits
    // One palette per layer
    private static final int[][] PALETTES = new int[5][];

    // Assign palette to a layer
    public static void setPalette(int buffer, int[] palette) {
		long start = System.nanoTime();
		
        PALETTES[buffer] = palette;
		
		long elapsed = System.nanoTime() - start;

		System.out.printf("Actual palette assignment: %.6f ms%n", elapsed / 1_000_000.0);
    }

    public static void drawMode1(Window window, int buffer, byte[] map, double angle, double scale, int centerX, int centerY) {
        int[] palette = PALETTES[buffer];

        double cos = Math.cos(angle) / scale;
        double sin = Math.sin(angle) / scale;

        for (int y = 0; y < 240; y++) {
            for (int x = 0; x < 320; x++) {
                int dx = x - 160;
                int dy = y - 120;
                int srcX = (int)(dx * cos - dy * sin) + centerX;
                int srcY = (int)(dx * sin + dy * cos) + centerY;
                srcX &= 255;
                srcY &= 255;
                int colorIndex = map[srcY * 256 + srcX] & 0xFF;

                window.setPixel(buffer, x, y, palette[colorIndex]);
            }
        }
    }

    public static void drawMode1Perspective(Window window, int buffer, byte[] map, int camX, int camY, double cameraHeight, double perspective, int horizon, boolean ceiling) {
		int[] palette = PALETTES[buffer];
		int startY = ceiling ? 0 : horizon + 1;
		int endY = ceiling ? horizon : 240;

		for (int y = startY; y < endY; y++) {
			double screenY;

			if (ceiling)
				screenY = horizon - y;
			else
				screenY = y - horizon;

			if (screenY <= 0)
				continue;

			double distance = cameraHeight * perspective / screenY;
			double stepX = distance / perspective;

			for (int x = 0; x < 320; x++) {
				double worldX = camX + (x - 160) * stepX;
				double worldY = camY + distance;
				int srcX = ((int)Math.floor(worldX)) & 255;
				int srcY = ((int)Math.floor(worldY)) & 255;
				int colorIndex = map[srcY * 256 + srcX] & 0xFF;
				if (colorIndex >= palette.length)
					continue;

				window.setPixel(buffer, x, y, palette[colorIndex]);
			}
		}
	}
	
	    /////////////////////
    // Mode 1 Pillar
    // 3 textures construct the curved surface
    // Distance controls overall size
    // Dark edges and lighter center
    // Ground anchored
    /////////////////////
    public static void mode1pillar(Window window, int buffer, int x, int groundY, int width, int height, float depth, byte[] normtexture, byte[] smallertexture, byte[] smallesttexture) {
        int[] palette = PALETTES[buffer];

        if (depth <= 0 || width <= 0 || height <= 0)
            return;

        // Perspective scaling
        float perspective = 160.0f / depth;

        int drawwidth = Math.max(1, (int)(width * perspective));
        int drawheight = Math.max(1, (int)(height * perspective));

        // Center pillar horizontally
        int startx = x - drawwidth / 2;

        // Extend pillar to top of screen
        int starty = Math.min(0, groundY - drawheight);
        drawheight = groundY - starty;

        if (drawheight <= 0)
            return;

        // Render pillar
        for (int tileinrow = 0; tileinrow < drawwidth; tileinrow++) {

            // Calculate cylindrical curve
            float scale = (float)Math.sin((Math.PI / drawwidth) * (tileinrow + 0.5));

            byte[] texture;

            if (scale > 0.6f) {
                // Draw normal texture
                texture = normtexture;

            } else if (scale > 0.3f) {
                // Draw smaller texture
                texture = smallertexture;

            } else {
                // Draw smallest texture
                texture = smallesttexture;
            }

            int destX = startx + tileinrow;

            if (destX < 0 || destX >= 320)
                continue;

            // Texture coordinates
            int srcX = tileinrow * 16 / drawwidth;

            // Darker sides, brighter center
            float brightness = 0.25f + scale * 0.75f;

            for (int py = 0; py < drawheight; py++) {

                int destY = starty + py;

                if (destY < 0 || destY >= 240)
                    continue;

                int srcY = Math.min(15, py * 16 / drawheight);

                // Decode packed 4-bit texture
                int pixel = srcY * 16 + srcX;
                int data = texture[pixel >> 1] & 0xFF;
                int colorIndex;

                if ((pixel & 1) == 0)
                    colorIndex = data >> 4;
                else
                    colorIndex = data & 0x0F;

                // Transparent pixel or invalid palette index
                if (colorIndex >= palette.length || palette[colorIndex] == 0)
                    continue;

                int color = palette[colorIndex];

                // Apply cylindrical shading
                int r = (int)(((color >> 16) & 0xFF) * brightness);
                int g = (int)(((color >> 8) & 0xFF) * brightness);
                int b = (int)((color & 0xFF) * brightness);

                int finalColor = (r << 16) | (g << 8) | b;

                window.setPixel(buffer, destX, destY, finalColor);
            }
        }
    }

    public static void drawtexture(Window window, int buffer, byte[][] TEXTURES, int textureid, int x, int y, int scale) {
        byte[] texture = TEXTURES[textureid];
        int[] palette = PALETTES[buffer];

        int width = mathlib.Divider(16 * scale, 100);
        int height = mathlib.Divider(16 * scale, 100);

        if (width < 1 || height < 1)
            return;

        for (int py = 0; py < height; py++) {
            int srcY = (py * 100) / scale;
            for (int px = 0; px < width; px++) {
                int srcX = (px * 100) / scale;

                if (srcX >= 16 || srcY >= 16)
                    continue;

                int pixel = srcY * 16 + srcX;
                int data = texture[pixel >> 1] & 0xFF;
                int colorIndex;

                if ((pixel & 1) == 0)
                    colorIndex = data >> 4;
                else
                    colorIndex = data & 0x0F;

                if (palette[colorIndex] == 0)
                    continue;

                int color = palette[colorIndex];
                int destX = x + px;
                int destY = y + py;

                if (destX >= 0 && destX < 320 && destY >= 0 && destY < 240)
                    window.setPixel(buffer, destX, destY, color);
            }
        }
    }
}