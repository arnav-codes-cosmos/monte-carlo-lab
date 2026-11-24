package montecarlo;

/** An experiment paired with the exact answer it should converge to. */
public record Target(String name, Experiment experiment, double truth) {}
