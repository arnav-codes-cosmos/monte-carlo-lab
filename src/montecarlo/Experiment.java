package montecarlo;

import java.util.SplittableRandom;

/** One Monte Carlo estimator: given a trial count and a random source, return an estimate. */
@FunctionalInterface
public interface Experiment {
    double run(long trials, SplittableRandom rng);
}
