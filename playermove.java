import java.awt.event.KeyEvent;

public class playermove {
	
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
	
	private static boolean fWasDown = false;

	public static void fullscreen(Window window) {
		boolean fDown = Input.down(KeyEvent.VK_F);
		if (fDown && !fWasDown) {
			window.toggleFullscreen();
		}
		fWasDown = fDown;
	}
}