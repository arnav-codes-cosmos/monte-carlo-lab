package montecarlo;

import java.util.SplittableRandom;

/** Three doors, one car. The host opens a goat door you did not pick; do you switch? */
public final class MontyHall {
    private MontyHall() {}

    public static boolean play(boolean switchDoor, SplittableRandom rng) {
        int car = rng.nextInt(3), pick = rng.nextInt(3), opened;
        do { opened = rng.nextInt(3); } while (opened == pick || opened == car);
        if (switchDoor) pick = 3 - pick - opened;   // the one door that is neither picked nor opened
        return pick == car;
    }

    public static double winRate(boolean switchDoor, long trials, SplittableRandom rng) {
        long wins = 0;
        for (long i = 0; i < trials; i++) if (play(switchDoor, rng)) wins++;
        return (double) wins / trials;
    }
}
