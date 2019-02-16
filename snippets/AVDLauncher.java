package Pages.GenericLib;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class AVDLauncher {
    //Update your android SDK path accordingly
    public static Process process;
    private static String sdkPath = "/Users/vinay/Library/Android/sdk/";
    private static String adbPath = sdkPath + "platform-tools" + File.separator + "adb";
    private static String emulatorPath = sdkPath + "tools" + File.separator + "emulator";

    //Launching an emulator. Pass your emulator name as a string when you call this method
    public static void launchEmulator(String nameOfAVD) {
        System.out.println("Starting emulator for '" + nameOfAVD + "' ...");
        String[] aCommand = new String[]{emulatorPath, "-avd", nameOfAVD};
        try {
            process = new ProcessBuilder(aCommand).start();
            Thread.sleep(10000);
            System.out.println("Emulator launched successfully!");
        } catch (Exception e) {

        }
    }
    //Closing an emulator
    public static void closeEmulator() {
        try {
            process.destroy();
            System.out.println("Emulator closed successfully");
        } catch (Exception e) {

        }
    }

}
