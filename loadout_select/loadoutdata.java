public class loadoutdata {
    public static class basicdata {
		public final byte[] NAME, DESCRIPTION;
		public final byte ID, ICONSPRITE, TYPE;
		public boolean CANUSE;

		public basicdata(byte[] name, byte[] description, byte id, byte iconsprite, byte type, boolean canuse) {
			this.NAME = name;
			this.DESCRIPTION = description;
			this.ID = id;
			this.ICONSPRITE = iconsprite;
			this.TYPE = type;
			this.CANUSE = canuse;
		}
	}

	// Element / damage
	public static class wand extends basicdata {
		public final byte GAMESPRITE, DAMAGE;

		public wand(byte[] name, byte[] description, int id, byte gamesprite, byte iconsprite, byte type, byte damage, boolean canuse) {
			super(name, description, (byte)id, iconsprite, type, canuse);

			this.GAMESPRITE = gamesprite;
			this.DAMAGE = damage;
		}
	}

    // Starting wands
    public static final wand earth = new wand(loadouttextures.EARTH, loadouttextures.EARTHDESC, 1, (byte)1, (byte)1, (byte)0, (byte)1, true);
    public static final wand wind = new wand(loadouttextures.WIND, loadouttextures.WINDDESC, 2, (byte)1, (byte)1, (byte)1, (byte)1, true);
    public static final wand fire = new wand(loadouttextures.FIRE, loadouttextures.FIREDESC, 3, (byte)1, (byte)1, (byte)2, (byte)1, true);
    public static final wand water = new wand(loadouttextures.WATER, loadouttextures.WATERDESC, 4, (byte)1, (byte)1, (byte)3, (byte)1, true);

    // Unlockable wands
    public static final wand lightning = new wand(loadouttextures.LIGHTNING, loadouttextures.LIGHTNINGDESC, 5, (byte)1, (byte)1, (byte)4, (byte)1, false);
    public static final wand ice = new wand(loadouttextures.ICE, loadouttextures.ICEDESC, 6, (byte)1, (byte)1, (byte)5, (byte)1, false);
    public static final wand dark = new wand(loadouttextures.DARK_MAGIC, loadouttextures.DARK_MAGICDESC, 7, (byte)1, (byte)1, (byte)6, (byte)1, false);


    // Buffs / debuffs

	public static class pact extends basicdata {
		public final byte TEMP;

		public pact(byte[] name, byte[] description, byte id, byte iconsprite, byte type, byte temp, boolean canuse) {
			super(name, description, id, iconsprite, type, canuse);
			this.TEMP = temp;
		}
	}

    // Shot pattern / speed
	public static class sacrficialdagger extends basicdata {
		public final byte PATTERN, SPEED;

		public sacrficialdagger(byte[] name, byte[] description, byte id, byte iconsprite, byte type, byte pattern, byte speed, boolean canuse) {
			super(name, description, id, iconsprite, type, canuse);
			this.PATTERN = pattern;
			this.SPEED = speed;
		}
	}

    // Special effects
	public static class CHALICE extends basicdata {
		public final byte TEMP;

		public CHALICE(byte[] name, byte[] description, byte id, byte iconsprite, byte type, byte temp, boolean canuse) {
			super(name, description, id, iconsprite, type, canuse);
			this.TEMP = temp;
		}
	}
	
	public static final wand[] ALL_WANDS = {
		earth, wind, fire, water, lightning, ice, dark
	};

	public static wand[] getUnlockedWands() {
		int count = 0;
		// Count unlocked wands
		for (wand w : ALL_WANDS) {
			if (w.CANUSE)
				count++;
		}

		wand[] unlocked = new wand[count];

		int index = 0;

		// Fill array with unlocked wands
		for (wand w : ALL_WANDS) {
			if (w.CANUSE)
				unlocked[index++] = w;
		}

		return unlocked;
	}
}
