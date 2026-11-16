public class loadoutselect {
    public static byte equipedWand;
    public static byte equipedPact;
    public static byte equipedSacrafice = 2;
    public static byte equipedRelic;

    public static Runnable currentFunction = bulletdata::machinegun;

    public static int damage;
    public static int speed;

    public static int getEquipment() {
        return (equipedWand & 63) | ((equipedPact & 63) << 6) | ((equipedSacrafice & 63) << 12) | ((equipedRelic & 63) << 18);
    }

    public static void updateLoadout() {
        // Base wand properties
        loadoutdata.wand wand = loadoutdata.ALL_WANDS[equipedWand & 0xFF];

        //damage = wand.DAMAGE;

        // Base firing pattern
        switch (equipedSacrafice) {
            case 0:
                currentFunction = bulletdata::shotgun;
                break;
            case 1:
                currentFunction = bulletdata::laserbeam;
                break;
            case 2:
                currentFunction = bulletdata::machinegun;
                break;
            case 3:
                currentFunction = bulletdata::singun;
                break;
            default:
                currentFunction = bulletdata::shotgun;
        }

        // Special combinations override normal behavior
        if (equipedWand == 2 && equipedSacrafice == 1) {
            //currentFunction = bulletdata::firelaser;
        }

        if (equipedWand == 4 && equipedSacrafice == 1) {
           // currentFunction = bulletdata::chainlightning;
        }

        // Pact and relic effects can be applied here
    }

    public static void fire() {
        currentFunction.run();
    }
}