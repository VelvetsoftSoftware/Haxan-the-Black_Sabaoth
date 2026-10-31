public class loudoutselect {
	public byte equipedWand, equipedPact, equipedSacrafice, equipedRelic;
	
	//for wand
	switch(equipedWand) {
		case 0:
				break;
		case 1:
				break;
		case 2:
				break;
		case 3:
				break;
		default: 
				break;
	}
	
	//set them first
	int effect = (equipedWand & 63) | ((equipedPact & 63) << 6) | ((equipedSacrafice & 63) << 12) | ((equipedRelic & 63) << 18);
	
	
	
	
	
	
}