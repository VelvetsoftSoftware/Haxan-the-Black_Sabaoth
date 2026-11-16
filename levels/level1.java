public class level1 {
	//background layer will be present the moon on the right
	//clouds slowly paralaxing
	//player will fly from left to right
	//player flys back and down
	//as player goes down trees paralax up
	//player gains control
	
	private static int xscroll = 0, xpos0 = 0, xpos1 = 0, xpos2 = 0, xpos3 = 0, cutsceneStage = 0, bobCounter = 0, bobOffset = 0;
	static {
        playermove.xplayer = -32;
        playermove.yplayer = 240;
    }

	private static boolean startingFadeOut = false, palettesInitialized = false, introcutscene = false;
	
	public static final int[][] Background_BUFFER_1 = new int[15][20];
	
	//mushit
	private static boolean musicrunning = false;

	static {
		int tx = 0;
		for (int y = 0; y < 15; y++) {
			for (int x = 0; x < 20; x++) {
				Background_BUFFER_1[y][x] = tx;
				tx++;
			}
		}
	}
	
	// Assign palettes once
	private static void initializePalettes() {
		spriteLoader.setPalette(0, level1textures.PALETTELEVEL1TEXTURES);
		spriteLoader.setPalette(1, level1textures.MOONPALETTE);
		spriteLoader.setPalette(3, player.PLAYERPALETTE);
		spriteLoader.setPalette(2, level1textures.TREEPALETTE);
		spriteLoader.setPalette(4, level1textures.TREEPALETTE);

		palettesInitialized = true;
	}
	
	public static void drawmainmenu(Window window) {
		if(introcutscene) {
			int[] position = playermove.listener(playermove.xplayer, playermove.yplayer);
			playermove.xplayer = position[0];
			playermove.yplayer = position[1];
			utility.bulletcall(window, (byte)3);
			
			if(xscroll % 500 == 0)
				xpos0++;
			if(xscroll % 45 == 0 && xpos1 < 192)
				xpos1++;

			xpos2 += 2;
			xpos3++;
		} else {
			if(cutsceneStage == 0) {
				if(xscroll % 2 == 0){
					playermove.xplayer += 3;
					if(playermove.yplayer > 70)
						playermove.yplayer -= 2;
				}
				if(playermove.xplayer >= 220) {
					playermove.xplayer = 220;
					cutsceneStage = 1;
				}
				xpos2 += 4;
				xpos3 += 3;
			} else if(cutsceneStage == 1) {
				if(xscroll % 2 == 0)
					playermove.xplayer -= 2;
				if(playermove.xplayer <= 20) {
					playermove.xplayer = 20;
					cutsceneStage = 2;
				}

				xpos2 += 3;
				xpos3 += 2;
			} else if(cutsceneStage == 2) {
				if(playermove.yplayer < 70)
					playermove.yplayer--;
				else {
					introcutscene = true;
				}

				xpos2 += 3;
				xpos3 += 2;
				}
			}
		bobCounter++;
		switch((bobCounter / 12) % 4) {
			case 0:
				bobOffset = 0;
				break;
			case 1:
				bobOffset = -1;
				break;
			case 2:
				bobOffset = -2;
				break;
			case 3:
				bobOffset = -1;
				break;
		}
		
		if (!palettesInitialized){
			initializePalettes();
		}
		if(musicrunning == false) {
			new Thread(() -> {
				main.music.playMidi(10);
			}, "MIDI-Transition").start();
	
			musicrunning = true;
		}
		
		playermove.checkbounds();
		
		drawBackground(window, 0, level1textures.BACKGROUNDARABORA, 22, 15, xpos0, 0);
		utility.drawImage(window, level1textures.MOONTEXTURE, 8, 192 - xpos1, 36, 100, 1);
		drawBackground(window, 4, level1textures.TREES1TEXTURE, 45, 15, xpos2, 0);
		drawBackground(window, 2, level1textures.TREES2TEXTURE, 45, 15, xpos3, 0);
		utility.drawImage(window, player.PLAYERSPRITE, 2, playermove.xplayer, playermove.yplayer + bobOffset, 100, 3);
		
		window.compose();
		
		// Screen state
		if (startingFadeOut) {
			window.fadeOUT(window);
		} else if (main.firstrun != 1) {
			window.fadeIN(window);
		} else {
			window.compose();
			window.repaint();
		}
		
		xscroll++;
    }
	
	private static void drawBackground(Window window, int layer, byte[][] texture, int imageWidth, int imageHeight, int scrollX, int scrollY) {

		final int TILE_SIZE = 16;
		final int SCREEN_WIDTH = 320;
		final int SCREEN_HEIGHT = 240;

		// Dimensions are in tiles
		int backgroundWidth = imageWidth * TILE_SIZE;
		int backgroundHeight = imageHeight * TILE_SIZE;

		// Wrap scrolling in both directions
		int wrappedX = Math.floorMod(scrollX, backgroundWidth);
		int wrappedY = Math.floorMod(scrollY, backgroundHeight);

		// Find the first visible tile
		int firstTileX = wrappedX / TILE_SIZE;
		int firstTileY = wrappedY / TILE_SIZE;

		// Sub-tile pixel offsets
		int offsetX = wrappedX % TILE_SIZE;
		int offsetY = wrappedY % TILE_SIZE;

		// Only draw the visible tiles, plus partial edge tiles
		int columns = (SCREEN_WIDTH + offsetX + TILE_SIZE - 1) / TILE_SIZE;
		int rows = (SCREEN_HEIGHT + offsetY + TILE_SIZE - 1) / TILE_SIZE;

		for (int y = 0; y < rows; y++) {
			int tileY = (firstTileY + y) % imageHeight;

			for (int x = 0; x < columns; x++) {
				int tileX = (firstTileX + x) % imageWidth;
				int tile = tileY * imageWidth + tileX;

				spriteLoader.drawtexture(window, layer, texture, tile, x * TILE_SIZE - offsetX, y * TILE_SIZE - offsetY, 100);
			}
		}
	}
}
