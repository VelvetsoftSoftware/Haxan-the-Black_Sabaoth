import java.awt.event.KeyEvent;

public class playermove {
	
	public static int xplayer = 0, yplayer = 0;
	
	public static int[] listener(int x, int y) {
		if (Input.down(KeyEvent.VK_UP) ||  Input.down(KeyEvent.VK_W))
				y -= 2;
		if (Input.down(KeyEvent.VK_DOWN) || Input.down(KeyEvent.VK_S))
				y += 2;
		if (Input.down(KeyEvent.VK_LEFT) ||  Input.down(KeyEvent.VK_A))
				x -= 2;
		if (Input.down(KeyEvent.VK_RIGHT) || Input.down(KeyEvent.VK_D))
				x += 2;
		
		return new int[] {x, y};
	}
	
	public static void checkbounds() {
		if(xplayer < 0)
			xplayer = 0;
		if(xplayer >= 288)
			xplayer = 288;
		if(yplayer < 0)
			yplayer = 0;
		if(yplayer >= 192)
			yplayer = 192;
	}
	
	private static boolean fWasDown = false, isfullscreen = false, escWasDown = false;

	public static void fullscreen(Window window) {
		boolean fDown = Input.down(KeyEvent.VK_F);
		boolean escDown = Input.down(KeyEvent.VK_ESCAPE);
		if (fDown && !fWasDown) {
			window.toggleFullscreen();
			isfullscreen = !isfullscreen;
			
		}
		if(!escWasDown && escDown && isfullscreen) {
			window.toggleFullscreen();
			isfullscreen = !isfullscreen;
		}
		fWasDown = fDown;
		escWasDown = escDown;
	}
}