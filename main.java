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

        while (true) {	
			long frameStart = System.nanoTime();
			
            Input.update();
            window.clearBuffers();
			playermove.fullscreen(window);

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
				case 3:
                    level1.drawmainmenu(window);
					break;
           }

		   
		   long beforeSleep = System.nanoTime();

           utility.sleep60fps();
		   
		   long frameEnd = System.nanoTime();

			double workMs = (beforeSleep - frameStart) / 1_000_000.0;
			double sleepMs = (frameEnd - beforeSleep) / 1_000_000.0;
			double totalMs = (frameEnd - frameStart) / 1_000_000.0;

			if (totalMs > 25.0) {
				System.out.printf("SLOW FRAME | Work: %.3f ms | Limiter: %.3f ms | Total: %.3f ms%n", workMs, sleepMs, totalMs);
			}
		   
		   utility.garbagetester();
		   utility.fpscounter();
        }
    }
}   