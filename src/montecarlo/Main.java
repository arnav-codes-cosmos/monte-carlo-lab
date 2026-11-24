package montecarlo;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SplittableRandom;
import javax.imageio.ImageIO;

public final class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args[0].equals("help") || args[0].equals("--help")) { usage(); return; }
        String cmd = args[0];
        Map<String, String> o = parse(args);
        long trials = Long.parseLong(o.getOrDefault("trials", "1000000"));
        long seed = Long.parseLong(o.getOrDefault("seed", "42"));
        int people = Integer.parseInt(o.getOrDefault("people", "23"));
        int runs = Integer.parseInt(o.getOrDefault("runs", "20"));
        int maxExp = Integer.parseInt(o.getOrDefault("max-exp", "6"));

        List<Target> targets = List.of(
                new Target("pi", PiEstimator::estimate, Math.PI),
                new Target("birthday_" + people, (n, r) -> BirthdayProblem.simulate(people, n, r), BirthdayProblem.exact(people)),
                new Target("monty_hall_switch", (n, r) -> MontyHall.winRate(true, n, r), 2.0 / 3.0));
        SplittableRandom rng = new SplittableRandom(seed);

        switch (cmd) {
            case "pi" -> single(targets.get(0), trials, rng);
            case "birthday" -> single(targets.get(1), trials, rng);
            case "monty" -> {
                single(targets.get(2), trials, rng);
                double stay = MontyHall.winRate(false, trials, rng);
                System.out.printf(Locale.ROOT, "stay strategy win rate: %.6f (exact 0.333333)%n", stay);
            }
            case "study" -> study(targets, o, maxExp, runs, seed);
            default -> { System.err.println("Unknown command: " + cmd); usage(); System.exit(1); }
        }
    }

    private static void single(Target t, long trials, SplittableRandom rng) {
        double est = t.experiment().run(trials, rng);
        System.out.printf(Locale.ROOT, "%s  trials=%d%n  estimate = %.6f%n  exact    = %.6f%n  abs error = %.6f%n",
                t.name(), trials, est, t.truth(), Math.abs(est - t.truth()));
    }

    private static void study(List<Target> targets, Map<String, String> o, int maxExp, int runs, long seed) throws Exception {
        System.out.printf("Study: %d runs per size, sizes 10^1..10^%d, seed %d%n", runs, maxExp, seed);
        var rows = ConvergenceStudy.run(targets, maxExp, runs, seed);
        String csv = o.get("csv");
        if (csv == null && !o.containsKey("png") && !o.containsKey("chart")) csv = "results.csv";
        if (csv != null) { ConvergenceStudy.writeCsv(Path.of(csv), rows); System.out.println("Wrote " + csv); }
        BufferedImage img = Chart.render(rows, 820, 520);
        if (o.containsKey("png")) { ImageIO.write(img, "png", Path.of(o.get("png")).toFile()); System.out.println("Wrote " + o.get("png")); }
        if (o.containsKey("chart")) {
            if (GraphicsEnvironment.isHeadless()) System.err.println("No display available; use --png chart.png instead.");
            else Chart.show(img);
        }
    }

    private static Map<String, String> parse(String[] a) {
        Map<String, String> m = new HashMap<>();
        for (int i = 1; i < a.length; i++) {
            if (!a[i].startsWith("--")) continue;
            String key = a[i].substring(2);
            boolean hasValue = i + 1 < a.length && !a[i + 1].startsWith("--");
            m.put(key, hasValue ? a[++i] : "true");
        }
        return m;
    }

    private static void usage() {
        System.out.println("""
                Monte Carlo lab
                Usage: java -cp out montecarlo.Main <command> [options]

                Commands:
                  pi        estimate pi by random sampling
                  birthday  shared-birthday probability (--people, default 23)
                  monty     Monty Hall, switch vs stay
                  study     error vs trial count for all experiments (CSV and/or chart)

                Options:
                  --trials N     trials for a single run (default 1000000)
                  --seed S       random seed (default 42)
                  --people K     group size for birthday (default 23)
                  --runs R       repeats per size in study (default 20)
                  --max-exp E    study sizes 10^1..10^E (default 6)
                  --csv FILE     write study results as CSV (default results.csv)
                  --png FILE     write the chart as a PNG (works without a display)
                  --chart        open the chart in a Swing window""");
    }
}
