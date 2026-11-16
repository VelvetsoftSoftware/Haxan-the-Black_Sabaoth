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
		int[] pixels = window.getBuffer(buffer);

		double cos = Math.cos(angle) / scale;
		double sin = Math.sin(angle) / scale;

		for(int y = 0; y < 240; y++) {
			int dy = y - 120;
			double srcX = -160 * cos - dy * sin + centerX;
			double srcY = -160 * sin + dy * cos + centerY;
			int dest = y * 320;

			for(int x = 0; x < 320; x++) {
				int mapX = ((int)srcX) & 255;
				int mapY = ((int)srcY) & 255;
				int colorIndex = map[(mapY << 8) + mapX] & 0xFF;

				pixels[dest++] = palette[colorIndex] | 0xFF000000;

				srcX += cos;
				srcY += sin;
			}
		}
	}

	public static void drawMode1Perspective(Window window, int buffer, byte[] map, int camX, int camY, double cameraHeight, double perspective, int horizon, boolean ceiling) {
		int[] palette = PALETTES[buffer];
		int[] pixels = window.getBuffer(buffer);

		int startY = Math.max(0, ceiling ? 0 : horizon + 1);
		int endY = Math.min(240, ceiling ? horizon : 240);

		for(int y = startY; y < endY; y++) {
			double screenY;

			if(ceiling)
				screenY = horizon - y;
			else
				screenY = y - horizon;

			if(screenY <= 0)
				continue;

			double distance = cameraHeight * perspective / screenY;
			double stepX = distance / perspective;
			double worldX = camX - 160 * stepX;
			double worldY = camY + distance;

			int srcY = ((int)Math.floor(worldY)) & 255;
			int mapRow = srcY << 8;
			int dest = y * 320;

			for(int x = 0; x < 320; x++) {
				int srcX = ((int)Math.floor(worldX)) & 255;
				int colorIndex = map[mapRow + srcX] & 0xFF;

				if(colorIndex < palette.length)
					pixels[dest] = palette[colorIndex] | 0xFF000000;

				dest++;
				worldX += stepX;
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
		int[] pixels = window.getBuffer(buffer);

		if(depth <= 0 || width <= 0 || height <= 0)
			return;

		float perspective = 160.0f / depth;
		int drawwidth = Math.max(1, (int)(width * perspective));
		int drawheight = Math.max(1, (int)(height * perspective));
		int startx = x - drawwidth / 2;
		int starty = Math.min(0, groundY - drawheight);
		drawheight = groundY - starty;

		if(drawheight <= 0)
			return;

		int startPy = Math.max(0, -starty);
		int endPy = Math.min(drawheight, 240 - starty);

		if(startPy >= endPy)
			return;

		for(int tileinrow = 0; tileinrow < drawwidth; tileinrow++) {
			int destX = startx + tileinrow;

			if(destX < 0 || destX >= 320)
				continue;

			float scale = (float)Math.sin((Math.PI / drawwidth) * (tileinrow + 0.5));

			byte[] texture;

			if(scale > 0.6f)
				texture = normtexture;
			else if(scale > 0.3f)
				texture = smallertexture;
			else
				texture = smallesttexture;

			int srcX = tileinrow * 16 / drawwidth;
			float brightness = 0.25f + scale * 0.75f;

			int dest = (starty + startPy) * 320 + destX;

			for(int py = startPy; py < endPy; py++) {
				int srcY = Math.min(15, py * 16 / drawheight);
				int pixel = srcY * 16 + srcX;
				int data = texture[pixel >> 1] & 0xFF;
				int colorIndex = (pixel & 1) == 0 ? data >> 4 : data & 0x0F;

				if(colorIndex < palette.length) {
					int color = palette[colorIndex];

					if(color != 0) {
						int r = (int)(((color >> 16) & 0xFF) * brightness);
						int g = (int)(((color >> 8) & 0xFF) * brightness);
						int b = (int)((color & 0xFF) * brightness);

						pixels[dest] = 0xFF000000 | (r << 16) | (g << 8) | b;
					}
				}

				dest += 320;
			}
		}
	}

	public static void drawtexture(Window window, int buffer, byte[][] TEXTURES, int textureid, int x, int y, int scale) {
		byte[] texture = TEXTURES[textureid];
		int[] palette = PALETTES[buffer];
		int[] pixels = window.getBuffer(buffer);

		if(scale == 100) {
			int startX = Math.max(0, -x);
			int startY = Math.max(0, -y);
			int endX = Math.min(16, 320 - x);
			int endY = Math.min(16, 240 - y);

			if(startX >= endX || startY >= endY)
				return;

			for(int py = startY; py < endY; py++) {
				int pixel = py * 16 + startX;
				int dest = (y + py) * 320 + x + startX;

				for(int px = startX; px < endX; px++) {
					int data = texture[pixel >> 1] & 0xFF;
					int colorIndex = (pixel & 1) == 0 ? data >> 4 : data & 0x0F;
					int color = palette[colorIndex];

					if(color != 0)
						pixels[dest] = color | 0xFF000000;

					pixel++;
					dest++;
				}
			}
			return;
		}

		if(scale <= 0)
			return;

		int width = mathlib.Divider(16 * scale, 100);
		int height = width;

		if(width < 1)
			return;

		int startX = Math.max(0, -x);
		int startY = Math.max(0, -y);
		int endX = Math.min(width, 320 - x);
		int endY = Math.min(height, 240 - y);

		if(startX >= endX || startY >= endY)
			return;

		for(int py = startY; py < endY; py++) {
			int srcY = (py * 100) / scale;

			if(srcY >= 16)
				continue;

			int dest = (y + py) * 320 + x + startX;

			for(int px = startX; px < endX; px++) {
				int srcX = (px * 100) / scale;

				if(srcX >= 16) {
					dest++;
					continue;
				}

				int pixel = srcY * 16 + srcX;
				int data = texture[pixel >> 1] & 0xFF;
				int colorIndex = (pixel & 1) == 0 ? data >> 4 : data & 0x0F;
				int color = palette[colorIndex];

				if(color != 0)
					pixels[dest] = color | 0xFF000000;

				dest++;
			}
		}
	}

}