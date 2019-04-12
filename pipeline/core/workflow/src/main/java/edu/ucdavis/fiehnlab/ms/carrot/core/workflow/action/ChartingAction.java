package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.action.PostActionWrapper;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.MassAccuracy;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.QuantifiedSample;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.QuantifiedTarget;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import scala.Option;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Profile("charting")
@Component
public class ChartingAction<T> extends PostActionWrapper {

    private SampleLoader loader;

    @Override
    public ApplicationContext applicationContext() {
        return null;
    }

    @Override
    public int priority() {
        return 0;
    }

    @Autowired
    public ChartingAction(SampleLoader loader) {
        this.loader = loader;
    }

    @Override
    public void run(Sample ssample, ExperimentClass experimentClass, Experiment experiment) {
        int halfDelta = 8;
        File folder = new File("./charts");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (ssample instanceof QuantifiedSample) {
            List<QuantifiedTarget<T>> targets = scala.collection.JavaConversions.seqAsJavaList(((QuantifiedSample) ssample).quantifiedTargets());

            Sample rawData = this.loader.loadSample(ssample.fileName()).get();
            List<? extends Feature> rawSpectra = scala.collection.JavaConversions.seqAsJavaList(rawData.spectra());


            targets.forEach(target -> {
                BinBaseXYDataSet dataset = new BinBaseXYDataSet();
                String tgtId = cleanName(target.name().get()).concat(String.format("_%.4f", (double) target.accurateMass().get()));
                System.out.println(tgtId);

                double start = target.retentionIndex() - halfDelta;
                double end = target.retentionIndex() + halfDelta;

                List<Double> rts = new ArrayList<>();
                List<Double> ints = new ArrayList<>();

                rawSpectra.forEach(it -> {
                    if (it.retentionTimeInSeconds() >= start && it.retentionTimeInSeconds() < end) {
                        Option<Ion> ion = MassAccuracy.findClosestIon(it, (Double) target.accurateMass().get(), target);
                        if (ion.isDefined()) {
                            rts.add(it.retentionTimeInMinutes());
                            ints.add((double) ion.get().intensity());
                        }
                    }
                });

                double[] xs = new double[rts.size()];
                double[] ys = new double[ints.size()];
                assert rts.size() == ints.size();

                for (int i = 0; i < rts.size(); i++) {
                    xs[i] = rts.get(i);
                    ys[i] = ints.get(i);
                }
                dataset.addDataSet(xs, ys, tgtId);


                // create images -- preferably pdf (iText or PdfBox)
                File png = new File(String.format("./charts/%s.png", tgtId));
                try {
                    ChartUtilities.saveChartAsPNG(png, createLineChart(dataset, tgtId), 1024, 768);
                    System.out.println(String.format("%s saved", png.getAbsolutePath()));
                } catch (IOException ex) {
                    System.err.println(String.format("Error saving chart %s: %s", png.getAbsolutePath(), ex.getMessage()));
                }
            });
        }

    }

    private String cleanName(String dirty) {
        return dirty.replaceAll("_[A-Z]{14}-[A-Z]{10}-[A-Z]|/|\\*\\\\|\\|", "")
              .replaceAll("\\(", "[")
              .replaceAll("\\)", "]")
              .replaceAll(":", "_");
    }

    public static JFreeChart createLineChart(BinBaseXYDataSet data, String title) {
        XYLineAndShapeRenderer rendererShift = new XYLineAndShapeRenderer(true, false);

        NumberAxis x = new NumberAxis("retention time");
        x.setAutoRangeIncludesZero(false);

        ValueAxis y = new NumberAxis("intensity");
        XYPlot plot = new XYPlot(data, x, y, rendererShift);

        JFreeChart chart = new JFreeChart(title, plot);

        chart.setAntiAlias(false);

        return chart;
    }
}


class BinBaseXYDataSet extends AbstractXYDataset implements XYDataset, IntervalXYDataset, AbstractDataset {
    private static final long serialVersionUID = 2L;

    List<Map> dataset = new Vector<>();

    public Number getEndX(int arg0, int arg1) {
        return this.getXValue(arg0, arg1);
    }

    public Number getEndY(int arg0, int arg1) {
        return this.getYValue(arg0, arg1);
    }

    /**
     * gibt den namen f?r dieses wert zur?ck oder schmeisst eine nullpointerexception
     *
     * @param name
     * @return
     */
    public int getIndex(String name) {
        for (int i = 0; i < this.dataset.size(); i++) {
            if (getName(i).equals(name)) {
                return i;
            }
        }

        return -1;
    }

    public int getItemCount(int arg0) {
        double[] d = (double[]) this.getDataSet(arg0).get("y");

        return d.length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param i DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public double getMaxY(int i) {
        try {
            return (Double) this.dataset.get(i).get("yMax");
        } catch (Exception e) {
            e.printStackTrace();

            return 0;
        }
    }

    /**
     * gibt den namen f?r dieses wert zur?ck oder schmeisst eine nullpointerexception
     *
     * @param i
     * @return
     */
    public String getName(int i) {
        return (String) this.dataset.get(i).get("name");
    }

    public int getSeriesCount() {
        return this.dataset.size();
    }

    public String getSeriesName(int arg0) {
        return (String) this.getDataSet(arg0).get("name");
    }

    public Number getStartX(int arg0, int arg1) {
        return this.getXValue(arg0, arg1);
    }

    public Number getStartY(int arg0, int arg1) {
        return this.getYValue(arg0, arg1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param i DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public double[] getX(int i) {
        return (double[]) this.dataset.get(i).get("x");
    }

    public Number getX(int arg0, int arg1) {
        double[] d = (double[]) this.getDataSet(arg0).get("x");

        return (d[arg1]);
    }

    public double[] getY(int i) {
        return (double[]) this.dataset.get(i).get("y");
    }

    public Number getY(int arg0, int arg1) {
        double[] d = (double[]) this.getDataSet(arg0).get("y");

        return (d[arg1]);
    }

    public void addDataSet(double[] x, double[] y, String name) {
        this.addDataSet(x, y, name, true);
    }

    public void addDataSet(double[] x, double[] y, String name,
                           boolean fireEvent) {
        if (x.length == y.length) {
            Map<String, Object> map = new HashMap<>();
            map.put("x", x);
            map.put("y", y);
            map.put("name", name);

            double xMax = 0;
            double yMax = 0;
            double xMin = 0;
            double yMin = 0;

            for (int i = 0; i < x.length; i++) {
                if (i == 0) {
                    xMax = x[i];
                    yMax = y[i];
                    xMin = x[i];
                    yMin = y[i];
                } else {
                    if (x[i] < xMin) {
                        xMin = x[i];
                    }

                    if (y[i] < yMin) {
                        yMin = y[i];
                    }

                    if (x[i] > xMax) {
                        xMax = x[i];
                    }

                    if (y[i] > yMax) {
                        yMax = y[i];
                    }
                }
            }

            map.put("xMin", xMin);
            map.put("yMin", yMin);
            map.put("xMax", xMax);
            map.put("yMax", yMax);

            int i = 0;

            if ((i = this.dataset.indexOf(map)) > -1) {
                this.dataset.set(i, map);
            } else {
                this.dataset.add(map);
            }
        } else {
            throw new RuntimeException("array must have the same size");
        }

        if (fireEvent) {
            this.fireDatasetChanged();
        }
    }

    public void clear() {
        this.dataset.clear();
        this.fireDatasetChanged();
    }

    public void clear(int index) {
        try {
            this.dataset.remove(index);
            this.fireDatasetChanged();
        } catch (Exception e) {
        }
    }

    public void refresh() {
        List list = new Vector();

        list.addAll(dataset);

        this.clear();

        for (Object aList : list) {
            Map map = (Map) aList;
            String name = (String) map.get("name");
            double[] x = (double[]) map.get("x");
            double[] y = (double[]) map.get("y");
            this.addDataSet(x, y, name, false);
        }

        this.fireDatasetChanged();
    }

    public void replaceDataSet(double[] x, double[] y, String name) {
        this.replaceDataSet(x, y, name, true);
    }

    public void replaceDataSet(double[] x, double[] y, String name,
                               boolean fireEvent) {
        if (x.length == y.length) {
            Map map = this.dataset.get(this.getIndex(name));
            map.put("x", x);
            map.put("y", y);
            map.put("name", name);

            double xMax = 0;
            double yMax = 0;
            double xMin = 0;
            double yMin = 0;

            for (int i = 0; i < x.length; i++) {
                if (i == 0) {
                    xMax = x[i];
                    yMax = y[i];
                    xMin = x[i];
                    yMin = y[i];
                } else {
                    if (x[i] < xMin) {
                        xMin = x[i];
                    }

                    if (y[i] < yMin) {
                        yMin = y[i];
                    }

                    if (x[i] > xMax) {
                        xMax = x[i];
                    }

                    if (y[i] > yMin) {
                        yMin = y[i];
                    }
                }
            }

            map.put("xMin", xMin);
            map.put("yMin", yMin);
            map.put("xMax", xMax);
            map.put("yMax", yMax);

            this.dataset.set(this.getIndex(name), map);
        } else {
            throw new RuntimeException("array must have the same size");
        }

        if (fireEvent) {
            this.fireDatasetChanged();
        }
    }

    /*
     * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.util.graph.dataset.AbstractDataset#update()
     */
    public void update() {
        this.fireDatasetChanged();
    }

    private Map getDataSet(int i) {
        try {
            Map map = this.dataset.get(i);

            if (map == null) {
                return new HashMap();
            }

            return map;
        } catch (Exception e) {
            return new HashMap();
        }
    }

    public double getXValue(int arg0, int arg1) {
        return getX(arg0, arg1).doubleValue();
    }

    public double getYValue(int arg0, int arg1) {
        return getY(arg0, arg1).doubleValue();
    }

    public double getStartXValue(int arg0, int arg1) {
        return getStartX(arg0, arg1).doubleValue();
    }

    public double getEndXValue(int arg0, int arg1) {
        return getEndX(arg0, arg1).doubleValue();
    }

    public double getStartYValue(int arg0, int arg1) {
        return getStartY(arg0, arg1).doubleValue();
    }

    public double getEndYValue(int arg0, int arg1) {
        return getEndY(arg0, arg1).doubleValue();
    }

    @Override
    public Comparable getSeriesKey(int arg0) {
        return arg0;
    }
}


/**
 * @author wohlgemuth
 */
interface AbstractDataset extends Dataset {
    public abstract void clear();

    public abstract void refresh();

    public abstract void update();
}
