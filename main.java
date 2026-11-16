import audio.munt;
import java.io.File;

public class main {
// i need to move alot of shit pver into a helper function to call so i dont repeat alot of junk
    public static int gameState = 0, firstrun = 0;
    public static munt music;

    public static void main(String[] args) {
		
        try {
            music = new munt();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        music.addFile(new File("audio/laura-s-theme.mid"));//0
        music.addFile(new File("audio/laura-s-theme-reprise-.mid"));
        music.addFile(new File("audio/overdose-delusion.mid"));//2
        music.addFile(new File("audio/promise.mid"));
        music.addFile(new File("audio/sh3-iwantlove.mid"));//4
        music.addFile(new File("audio/sh3-pleaselovemeoncemore.mid"));
        music.addFile(new File("audio/sh3-yourenothere.mid"));//6
        music.addFile(new File("audio/theme-song.mid"));
        music.addFile(new File("audio/theme-song-2-.mid"));//8
        music.addFile(new File("audio/alone-in-the-town.mid"));
		music.addFile(new File("audio/stage-2-inner-city.mid"));//10
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			music.shutdown();
		}));

        Window window = new Window();
		
		try {
			Class.forName("Textures");
			Class.forName("mainMenuTextures");
			Class.forName("level1textures");
			Class.forName("player");
			Class.forName("intro");
			Class.forName("mainmenu");
			Class.forName("cutScene1");
			Class.forName("level1");
			Class.forName("bulletdata");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	int previousState = gameState;

	while (true) {
		long frameStart = System.nanoTime();
		// INPUT
		Input.update();
		long afterInput = System.nanoTime();
		// CLEAR BUFFERS
		window.clearBuffers();
		long afterClear = System.nanoTime();
		// FULLSCREEN INPUT
		playermove.fullscreen(window);
		long afterFullscreen = System.nanoTime();
		// SCENE EXECUTION
		int stateAtStart = gameState;

	   switch (gameState) {
			case 0:
				intro.loadintro(window);
				break;
			case 1:
				mainmenu.drawmainmenu(window);
				break;
			case 2:
				cutScene1.drawcutscene1(window);
				break;
//			case 3:
//				loadoutdisplay.inventorydraw(window);
//				break;
			case 3:
				level1.drawmainmenu(window);
				break;
		}

		long afterScene = System.nanoTime();

		// FRAME WORK COMPLETE
		long workNs = afterScene - frameStart;
		double workMs = workNs / 1_000_000.0;

		// Only print diagnostics on slow frames
		if (workMs > 25.0) {
			System.out.println("========== SLOW FRAME ==========");

			System.out.printf("Input: %.3f ms%n", (afterInput - frameStart) / 1_000_000.0);
			System.out.printf("Clear: %.3f ms%n", (afterClear - afterInput) / 1_000_000.0);
			System.out.printf("Fullscreen: %.3f ms%n",(afterFullscreen - afterClear) / 1_000_000.0);
			System.out.printf("Scene %d: %.3f ms%n",stateAtStart,(afterScene - afterFullscreen) / 1_000_000.0);
			System.out.printf("Total Work: %.3f ms%n",workMs);
			System.out.printf("Swing Paint: %.3f ms | Max: %.3f ms%n",window.getLastPaintTime(),window.getMaxPaintTime());
			System.out.println("================================");
		}

		if (gameState != previousState) {
			System.out.printf(
				"SCENE CHANGE: %d -> %d%n",
				previousState,
				gameState
			);
			previousState = gameState;
		}

		utility.garbagetester();
		utility.fpscounter();

		long frameWorkEnd = System.nanoTime();
		double work2Ms = (frameWorkEnd - frameStart) / 1_000_000.0;

		if (workMs > 16.67) {
			System.out.printf("FRAME BUDGET EXCEEDED: %.3f ms%n", work2Ms);
		}

		// Keep disabled while benchmarking uncapped performance
		utility.sleep60fps();
	}
	}
}   