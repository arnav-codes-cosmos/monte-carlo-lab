package montecarlo;

import java.util.SplittableRandom;

/** Throw darts at the unit square; the share landing inside the quarter circle is π/4. */
public final class PiEstimator {
    private PiEstimator() {}

    public static double estimate(long trials, SplittableRandom rng) {
        long inside = 0;
        for (long i = 0; i < trials; i++) {
            double x = rng.nextDouble(), y = rng.nextDouble();
            if (x * x + y * y <= 1.0) inside++;
        }
        return 4.0 * inside / trials;
    }
}
