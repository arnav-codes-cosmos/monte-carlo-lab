package montecarlo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SplittableRandom;

/** Repeats each experiment at 10, 100, ... 10^maxExp trials and records how far the estimates land from the truth. */
public final class ConvergenceStudy {
    private ConvergenceStudy() {}

    public record Row(String experiment, long trials, int runs, double meanAbsError, double rmse) {
        /** Stays roughly constant when error shrinks like 1/sqrt(n). */
        public double rmseTimesSqrtN() { return rmse * Math.sqrt(trials); }
    }

    public static List<Row> run(List<Target> targets, int maxExp, int runs, long seed) {
        SplittableRandom master = new SplittableRandom(seed);
        List<Row> rows = new ArrayList<>();
        for (Target t : targets) {
            for (int e = 1; e <= maxExp; e++) {
                long n = Math.round(Math.pow(10, e));
                double abs = 0, sq = 0;
                for (int r = 0; r < runs; r++) {
                    double err = t.experiment().run(n, master.split()) - t.truth();
                    abs += Math.abs(err);
                    sq += err * err;
                }
                Row row = new Row(t.name(), n, runs, abs / runs, Math.sqrt(sq / runs));
                rows.add(row);
                System.out.printf(Locale.ROOT, "%-18s n=%-9d mean|err|=%.6f  rmse=%.6f  rmse*sqrt(n)=%.4f%n",
                        row.experiment(), n, row.meanAbsError(), row.rmse(), row.rmseTimesSqrtN());
            }
        }
        return rows;
    }

    public static void writeCsv(Path file, List<Row> rows) throws IOException {
        StringBuilder sb = new StringBuilder("experiment,trials,runs,mean_abs_error,rmse,rmse_times_sqrt_n\n");
        for (Row r : rows) {
            sb.append(String.format(Locale.ROOT, "%s,%d,%d,%.8f,%.8f,%.6f%n",
                    r.experiment(), r.trials(), r.runs(), r.meanAbsError(), r.rmse(), r.rmseTimesSqrtN()));
        }
        Files.writeString(file, sb.toString());
    }
}
