import java.awt.event.KeyEvent;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

public class utility {
	private static int y;
	
	private static final long FRAME_TIME = 1_000_000_000L / 60;
	private static long nextFrame = System.nanoTime();
	
	private static long fpsTimer = System.nanoTime();
	private static int frameCount = 0;
	private static long lastGCCount = -1;

	public static void sleep60fps() {
		nextFrame += FRAME_TIME;

		long remaining = nextFrame - System.nanoTime();

		while (remaining > 2_000_000L) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}

			remaining = nextFrame - System.nanoTime();
		}

		while (System.nanoTime() < nextFrame) {
			Thread.onSpinWait();
		}

		long now = System.nanoTime();

		if (now - nextFrame > FRAME_TIME) {
			nextFrame = now;
		}
	}
	
	public static void button(int maxbuttons) {
		int buttonpossition = 0;
		
		if (Input.down(KeyEvent.VK_UP))
			buttonpossition++;
		if (Input.down(KeyEvent.VK_DOWN))
			buttonpossition--;
		if (Input.down(KeyEvent.VK_W))
			buttonpossition++;
		if (Input.down(KeyEvent.VK_S))
			buttonpossition--;
		
		if(buttonpossition > maxbuttons)
			buttonpossition = 0;
		if(buttonpossition < 0)
			buttonpossition = maxbuttons;
	}
	 //hack rn fix later
	public static void scrollmap(int map[][], int cameraX, int cameraY, int scale, int layer, Window window) {
		for (int ty = 0; ty < map.length; ty++) {
			for (int tx = 0; tx < map[ty].length; tx++) {
				int x = tx * 16 - cameraX;
				int y = ty * 16 - cameraY;
				spriteLoader.drawtexture(window, layer, mainMenuTextures.MAINMENUTEXTURES, map[ty][tx], x, y, scale);
			}
		}
	}
	
	public static void drawText(byte[] text, Window window ) {
		int x = 2, y = 2, tracker = 0;
		while (tracker < text.length && y < 15) {
			int wordLength = 0;

			while (tracker + wordLength < text.length && text[tracker + wordLength] != 72) {
				wordLength++;
			}
			if (x + wordLength > 20) {
				x = 2;
				y++;
			}
			for (int i = 0; i < wordLength; i++) {
				spriteLoader.drawtexture(window, 1, systemTextures.ALPHABET_TEXTURES, text[tracker], x * 16, y * 18, 100);
				tracker++;
				x++;
			}
			if (tracker < text.length && text[tracker] == 72) {
				x++;
				tracker++;
			}
		}
	}
	
	public static void drawImage(Window window, byte[][] texture, int width, int x, int y, int scale, int layer) {
		final int TILE_SIZE = 16;

		int height = texture.length / width;
		int scaledTileSize = TILE_SIZE * scale / 100;

		for (int ty = 0; ty < height; ty++) {
			for (int tx = 0; tx < width; tx++) {

				int tile = ty * width + tx;

				spriteLoader.drawtexture(window, layer, texture, tile, x + tx * scaledTileSize, y + ty * scaledTileSize, scale);
			}
		}
	}
	
	
	public static void garbagetester() {
		   long totalCount = 0;
			long totalTime = 0;

			for (GarbageCollectorMXBean gc :
					ManagementFactory.getGarbageCollectorMXBeans()) {

				if (gc.getCollectionCount() >= 0)
					totalCount += gc.getCollectionCount();

				if (gc.getCollectionTime() >= 0)
					totalTime += gc.getCollectionTime();
			}

			if (totalCount != lastGCCount) {
				System.out.println("GC Collections: " + totalCount
						+ " | Total GC time: " + totalTime + " ms");

				lastGCCount = totalCount;
			}
	}
	
	public static void fpscounter() {
		frameCount++;
		long now = System.nanoTime();
		long elapsed = now - fpsTimer;

		if (elapsed >= 1_000_000_000L) {
			double fps = frameCount * 1_000_000_000.0 / elapsed;

			System.out.printf("FPS: %.2f%n", fps);

			frameCount = 0;
			fpsTimer = now;
		}
	}
}