package co.ignitus.bungeelist.util;

public class HookUtil {

    private static boolean premiumVanish = false;

    public static void setPremiumVanish(boolean status) {
        premiumVanish = status;
    }

    public static boolean premiumVanishEnabled() {
        return premiumVanish;
    }
}
