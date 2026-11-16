import java.awt.event.KeyEvent;

public class bulletdata {
	private static int[] bulletpos = new int[500];
	
	private static short positioninarray = 0;
	private static final int MAX_BULLETS = 100;
	private static short[] bullets = new short[MAX_BULLETS];
	private static int bulletcount = 0;
	private static int nextbullet = 0;
	private static int machinegunCooldown = 0;

	private static short xpos, ypos;
	public static boolean firstrun = true;
	
	private static int pack(int x, int y) {
		return (x << 16) | (y & 0xFFFF); 
	}
	
	private static short[] unpack(int bullet) {
		short y = (short)(bulletpos[bullet] & 0xFFFF);
		short x = (short)(bulletpos[bullet] >> 16);
		return new short[]{x, y};
	}

	private static void playershooter(int offsetx, int offsety) {
		bulletpos[positioninarray] = pack(playermove.xplayer + 16 + offsetx, playermove.yplayer + 32 + offsety);
		positioninarray++;
		if(positioninarray >= bulletpos.length)
			positioninarray = 0;
	}
	
	private static void movebullet(int bulletpostion, int xmove, int ymove) {
		int index = bulletpostion % bulletpos.length;
		int packed = bulletpos[index];
		///evil demon shit, instead of unpacking using my unpacker which creates a temp array this does the bare minumum, prob will delete unpack later
		int x = (packed >> 16) + xmove;
		int y = (short) packed + ymove;
		
		bulletpos[index] = pack(x, y);
	}
	
	private static final byte[] BULLETSPRITE = {
		0b0110,
		0b1111,
		0b1111,
		0b0110
	};

	public static void drawbullets(Window window, byte buffernum) {
		int[] buffer = window.getBuffer(buffernum);
		for (int i = 0; i < bulletpos.length; i++) {
			int packed = bulletpos[i];
			if (packed == 0)
				continue;
			int x = packed >> 16;
			int y = (short) packed;
			// Skip bullets outside the screen
			if (x <= -4 || x >= 320 || y <= -4 || y >= 240)
				continue;
			for (int py = 0; py < 4; py++) {
				int dy = y + py;
				if (dy < 0 || dy >= 240)
					continue;
				int row = BULLETSPRITE[py];
				for (int px = 0; px < 4; px++) {
					int dx = x + px;
					if (dx < 0 || dx >= 320)
						continue;
					if ((row & (1 << (3 - px))) != 0) {
						buffer[dy * 320 + dx] = 0xFFFF0000;
					}
				}
			}
		}
	}
	
	public static void shotgun() {
		bullets[nextbullet] = positioninarray;

		nextbullet++;
		if (nextbullet >= MAX_BULLETS)
			nextbullet = 0;

		if (bulletcount < MAX_BULLETS)
			bulletcount++;

		playershooter(0, 2);
		playershooter(0, 1);
		playershooter(0, 0);
		playershooter(0, -1);
		playershooter(0, -2);
	}
	
	public static void updateshotgun() {
		for (int i = 0; i < bulletcount; i++) {
			int start = bullets[i];

			movebullet(start,3, 2);
			movebullet(start + 1, 3, 1);
			movebullet(start + 2, 3, 0);
			movebullet(start + 3, 3, -1);
			movebullet(start + 4, 3, -2);
		}
	}
	
	public static void laserbeam() {
		if(xpos + playermove.xplayer <= 320)
			playershooter(xpos, 0);
		
		xpos++;
		if(xpos + playermove.xplayer > 320)
			xpos = 0;
	}

	public static void machinegun() {
		if (machinegunCooldown > 0) {
			machinegunCooldown--;
			return;
		}
		bullets[nextbullet] = positioninarray;
		nextbullet++;
		if (nextbullet >= MAX_BULLETS)
			nextbullet = 0;

		if (bulletcount < MAX_BULLETS)
			bulletcount++;

		playershooter(0, 0);
		machinegunCooldown = 5;
	}
	
	public static void updatemachinegun() {
		for (int i = 0; i < bulletcount; i++) {
			int start = bullets[i];

			movebullet(start,3, 0);
		}
	}
	
	public static void singun() {
		
	}
}
