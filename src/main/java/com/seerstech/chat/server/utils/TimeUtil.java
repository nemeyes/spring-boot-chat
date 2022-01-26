package com.seerstech.chat.server.utils;

import java.time.Instant;

public class TimeUtil {
    public static long unixTime() {
        return Instant.now().getEpochSecond() * 1000;
    }
}
