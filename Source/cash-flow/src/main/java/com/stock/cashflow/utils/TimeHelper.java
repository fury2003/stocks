package com.stock.cashflow.utils;

import java.util.Random;

public class TimeHelper {

    public static void randomSleep() {
        Random random = new Random();
        int minSleepTime = 1000; // 1 second in milliseconds
        int maxSleepTime = 2000; // 3 seconds in milliseconds

        try {
            // Generate a random sleep time between 1 and 3 seconds
            int sleepTime = random.nextInt(maxSleepTime - minSleepTime + 1) + minSleepTime;

            // Sleep for the randomly generated time
            Thread.sleep(sleepTime);

            System.out.println("Slept for " + sleepTime + " milliseconds");
        } catch (InterruptedException e) {
            // Handle interrupted exception if needed
            e.printStackTrace();
        }
    }
}
