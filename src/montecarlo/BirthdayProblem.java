package montecarlo;

import java.util.SplittableRandom;

/** Probability that at least two of n people share a birthday (365 equally likely days). */
public final class BirthdayProblem {
    private BirthdayProblem() {}

    public static double simulate(int people, long trials, SplittableRandom rng) {
        long[] seenInTrial = new long[365];   // stamp array avoids clearing every trial
        long hits = 0;
        for (long t = 1; t <= trials; t++) {
            for (int p = 0; p < people; p++) {
                int day = rng.nextInt(365);
                if (seenInTrial[day] == t) { hits++; break; }
                seenInTrial[day] = t;
            }
        }
        return (double) hits / trials;
    }

    /** Exact answer: 1 - (365/365)(364/365)...((365-n+1)/365). */
    public static double exact(int people) {
        double allDifferent = 1.0;
        for (int i = 0; i < people; i++) allDifferent *= (365.0 - i) / 365.0;
        return 1.0 - allDifferent;
    }
}
