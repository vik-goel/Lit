package mchacks.io.lit;

public class Util {

    public static String getTimeString(long timeStamp) {
        long dt = System.currentTimeMillis() - timeStamp;
        long seconds = dt / 10000;

        if(seconds < 0) {
            seconds = 0;
        }

        if(seconds < 60) {
            return seconds + "s";
        }

        long minutes = seconds / 60;

        if(minutes < 60) {
            return minutes + "m";
        }

        long hours = minutes / 60;

        if(hours < 24) {
            return hours + "h";
        }

        long days = hours / 24;
        return days + "d";
    }
}
