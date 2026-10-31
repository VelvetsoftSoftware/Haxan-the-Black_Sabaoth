public class loudoutdata {
	public class basicdata {
		public final String NAME;
		public final byte ICONSPRITE, TYPE;
		Boolean CANUSE;
		
		public basicdata(String name, byte iconsprite, byte type, Boolean canuse) {
			this.NAME = name;
			this.ICONSPRITE = iconsprite;
			this.TYPE = type;
			this.CANUSE = canuse;
		}
	}
	
	//element/damage
	public class wand extends basicdata {
		public final byte  GAMESPRITE, DAMAGE, PATTERN;
		
		public wand(String name, byte gamesprite, byte iconsprite, byte type, byte damage, Boolean canuse) {
			super(name, iconsprite, type, cnause);
			this.GAMESPRITE = gamesprite;
			this.DAMAGE = damage;
			this.PATTERN = pattern;
		}
	}
	//starting wands
	public wand earth = new wand(Earth, 1, 1, 1, 1, 1, true);
	public wand wind = new wand(Wind, 1, 1, 1, 1, 1, true);
	public wand fire = new wand(Fire, 1, 1, 1, 1, 1, true);
	public wand water = new wand(Water, 1, 1, 1, 1, 1, true);
	//wands you can pick up
	public wand lighting = new wand(Lightning, 1, 1, 1, 1, 1, false);
	public wand ice = new wand(Ice, 1, 1, 1, 1, 1, false);
	public wand dark = new wand(Dark magic, 1, 1, 1, 1, 1, false);
	
	
	//buffs/debuffs
	public class pact extends basicdata {
		public final byte TEMP;
		
		public pact(String name, byte iconsprite, byte type, byte temp, Boolean canuse) {
			super(name, iconsprite, type, cnause);
			this.TEMP = temp;
			
		}
	}
	
	//Shot Pattern
	public class sacrficialdagger extends basicdata {
		public final byte PATTERN, SPEED;
		
		public sacrficialdagger(String name, byte iconsprite, byte type, byte pattern, byte speed, Boolean canuse) {
			super(name, iconsprite, type, cnause);
			this.PATTERN = pattern;
			this.SPEED = speed;
		}
	}
	
	//special
	public class CHALICE extends basicdata {
		public final byte TEMP;
		
		public CHALICE(String name, byte iconsprite, byte type, byte temp, Boolean canuse) {
			super(name, iconsprite, type, cnause);
			this.TEMP = temp;
		}
	}
}