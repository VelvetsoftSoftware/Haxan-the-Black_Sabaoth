public class loadoutdisplay {
	private static loadoutdata.wand[] unlocked = loadoutdata.getUnlockedWands();
	private static int count = unlocked.length;// "amount of new wands"
	public static int LocationIndicatorWands = 0;
	private static int x = 0, y = 0;
	private static int animationX = 0;
	private static int oldlocation = 0, direction = 0;
	private static boolean changing = false;
	
	private static final int[] maxY = {
		count - 1, // Inventory
		3,         // Four equipment categories
		1          // Finish: two options
	};
	
	public static int getLayer(int section) {
		return section == x ? 1 : 0;
	}

	public static int listener(Window window, int i, int buffer, byte[][] texture, int textureid) {
	   // Change active menu section
		x = utility.buttonupdown(x, 2);
		if (x == 0) {
			// Inventory navigation
			if (!changing) {
				oldlocation = i;
				i = utility.buttonleftright(i, maxY[0]);
				if (oldlocation != i) {
					direction = oldlocation > i ? 0 : 1;
					changing = true;
				}
				LocationIndicatorWands = i;
			}
			if (changing) {
				changing = drawcarousel(window, direction, buffer, texture, textureid);
			}
		} else {
			// Category or finish navigation
			y = utility.buttonleftright(y, maxY[x]);
		}
		return i;
	}
	
	private static Boolean drawcarousel(Window window, int leftright, int buffer, byte[][] texture, int textureid) {
		for (int i = 0; i < count; i++) {
			int xpis = 150 + (getPosition(i) * 16 + animationX);
			spriteLoader.drawtexture(window, buffer, texture, textureid, xpis, 112, 100);
		}
		if (leftright == 0)
			animationX++;
		else
			animationX--;

		if (animationX >= 16 || animationX <= -16) {
			animationX = 0;
			return false;
		}
		return true;
	}

	public static int getPosition(int i) {
		int caeouselItemCount = count;
		int startingPostion = (i + LocationIndicatorWands) % caeouselItemCount;
		//calculate the postion based on the starting pos and carousel rules
		int realPosition  = CalculateCarouselPosition(startingPostion);
		
		return realPosition;
	}
	
	private static int CalculateCarouselPosition(int currentIndex) {
		int halfIndex = currentIndex / 2;
		// Return the calculated position based on the index
		return currentIndex % 2 == 0 ? halfIndex : -halfIndex - 1;
	}
	
	public static void inventorydraw(Window window) {
		//listener(window, 0, 0, 0, 0, ,texid);
		
		
		
	}
}