package com.iskcon.bvks.util;

import java.util.concurrent.TimeUnit;

public class LectureUtil {
    private LectureUtil() {
    }

    public static String getFormattedTime(long seconds) {
        long hours = TimeUnit.SECONDS.toHours(seconds);
        seconds -= TimeUnit.HOURS.toSeconds(hours);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        seconds -= TimeUnit.MINUTES.toSeconds(minutes);

        return hours > 0 ? String.format("%01d:%02d:%02d", hours, minutes, seconds) :
                String.format("%02d:%02d", minutes, seconds);
    }
}
