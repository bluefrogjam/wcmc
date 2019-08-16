package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.action.PostActionWrapper;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.MassAccuracy;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.Regression;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.*;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.ZeroReplacementProperties;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.ZeroreplacedTarget;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import scala.Option;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Profile("charting")
@Component
public class ChartingAction2<T> extends PostActionWrapper {

    private SampleLoader loader;
    private Double halfDelta;


    @Override
    public ApplicationContext applicationContext() {
        return null;
    }

    @Override
    public int priority() {
        return 0;
    }

    @Autowired
    public ChartingAction2(SampleLoader loader, ZeroReplacementProperties props) {
        this.loader = loader;
        this.halfDelta = props.retentionIndexWindowForPeakDetection();
    }

    @Override
    public void run(Sample ssample, ExperimentClass experimentClass, Experiment experiment) {
        File correctedFolder = new File(String.format("./charts2/corrected/%s", experiment.acquisitionMethod().chromatographicMethod().ionMode().get().mode()));

        if (!correctedFolder.exists()) {
            System.out.println("creating folder " + correctedFolder);
            correctedFolder.mkdirs();
        }

        List<? extends Target> targets = new ArrayList<>();
        List<? extends Feature> spectra = new ArrayList<>();

        if (ssample instanceof GapFilledSample) {
            targets = scala.collection.JavaConversions.seqAsJavaList(((GapFilledSample) ssample).quantifiedTargets());
            spectra = scala.collection.JavaConversions.seqAsJavaList(((GapFilledSample) ssample).spectra());
        } else if (ssample instanceof QuantifiedSample) {
            targets = scala.collection.JavaConversions.seqAsJavaList(((QuantifiedSample) ssample).quantifiedTargets());
            spectra = scala.collection.JavaConversions.seqAsJavaList(((QuantifiedSample) ssample).spectra());
        }

        Regression regression = ((QuantifiedSample) ssample).regressionCurve();

        Sample rawData = this.loader.loadSample(ssample.fileName()).get();
        List<? extends Feature> rawSpectra = scala.collection.JavaConversions.seqAsJavaList(rawData.spectra());

        XYSeriesCollection curves = new XYSeriesCollection();

        for (Target target : targets) {
            Feature annotation;

            String replaced = "";
            if (target instanceof ZeroreplacedTarget ||
                  target instanceof GapFilledTarget ||
                  target instanceof GapFilledSpectra) {
                replaced = "_replaced";
            }

            String tgtId = cleanName(target.name().get())
                  .concat(String.format("_%.4f", (double) target.accurateMass().get()))
                  .concat(replaced);

            curves.removeAllSeries();


            // create raw data series
            List<Point2D.Double> points = extractCloseIonsPoints(rawSpectra, (QuantifiedTarget<T>) target);
            XYSeries rawseries = new XYSeries("raw data");
            for (Point2D.Double point : points) {
                rawseries.add(point.x, point.y);
            }
            curves.addSeries(rawseries);


            // create corrected data series (the cheaty way)
            List<Point2D.Double> correctedPoints = correctData(points, regression);
            XYSeries corrseries = new XYSeries("corr data");
            for (Point2D.Double point : correctedPoints) {
                corrseries.add(new XYDataItem(point.x, point.y));
            }
            curves.addSeries(corrseries);


            // add target original rt marker
            XYSeries rtMarker = new XYSeries("Target RT");
            double intBarTgt = points.stream().map(it -> it.y).max(Comparator.comparing(it -> it)).orElse(10.0);
            rtMarker.add(target.retentionTimeInSeconds(), 0);
            rtMarker.add(target.retentionTimeInSeconds(), intBarTgt);
            rtMarker.add(target.retentionTimeInSeconds(), 0);
            curves.addSeries(rtMarker);


            // add target ri marker
            XYSeries riMarker = new XYSeries("Target RI");
            double intBarCorr = correctedPoints.stream().map(it -> it.y).max(Comparator.comparing(it -> it)).orElse(10.0);
            riMarker.add(target.retentionIndex(), 0);
            riMarker.add(target.retentionIndex(), intBarCorr);
            riMarker.add(target.retentionIndex(), 0);
            curves.addSeries(riMarker);


            // quant data EIC
            List<Point2D.Double> annotPoints = extractCloseIonsPoints(spectra, (QuantifiedTarget<T>) target);
            XYSeries annotFeatures = new XYSeries("Quantified Features", true);
            for (Point2D.Double apoint : annotPoints) {
                if (points.size() > 0 && !apoint.equals(points.get(0))) {
                    annotFeatures.add(apoint.x, 0);
                }
                annotFeatures.add(apoint.x, apoint.y);
                if (points.size() > 0 && !apoint.equals(points.get(points.size() - 1))) {
                    annotFeatures.add(apoint.x, 0);
                }
            }
            curves.addSeries(annotFeatures);


            // draw rt zone
            XYSeries rtZone = new XYSeries("rt zone");

            // define rt window for EIC from the target's RI point of view
            double start = target.retentionIndex() - halfDelta;
            double end = target.retentionIndex() + halfDelta;

            double maxint = points.stream().map(it -> it.y).max(Comparator.comparing(it -> it)).orElse(10.0);
            rtZone.add(start, maxint / 2);
            rtZone.add(end, maxint / 2);
            curves.addSeries(rtZone);


            // get replacement's intensity marker
            if (target instanceof ZeroreplacedTarget) {
                XYSeries replMarker = new XYSeries("Replacement Feature");
                XYSeries replMarker2 = new XYSeries("alt Replacement Feature");
                double rtRep = ((GapFilledSpectra) ((ZeroreplacedTarget) target).spectraUsedForReplacement()).retentionIndex();
                double intRep = ((ZeroreplacedTarget) target).spectraUsedForReplacement().massOfDetectedFeature().get().intensity();

                replMarker.add(rtRep, intRep);
                curves.addSeries(replMarker);
                curves.addSeries(replMarker2);
            } else {
                QuantifiedTarget<Double> tgt = (QuantifiedTarget<Double>) target;
                XYSeries replMarker = new XYSeries("Annotation Feature");
                double rtRep = tgt.retentionIndex();
                double intRep = -10.0;
                if (tgt.quantifiedValue().isDefined())
                    intRep = tgt.quantifiedValue().get();
                replMarker.add(rtRep, intRep);
                curves.addSeries(replMarker);
            }


            // create images -- preferably create pdf (iText or PdfBox)
            File png = new File(String.format("%s/%s.png", correctedFolder, tgtId));
            try {
                JFreeChart chart = createLineChart(curves, tgtId);
                ChartUtils.saveChartAsPNG(png, chart, 1024, 768);
            } catch (IOException ex) {
                System.err.println(String.format("Error saving chart %s: %s", png.getAbsolutePath(), ex.getMessage()));
            }
        }

        System.out.println("Images saved at " + correctedFolder.getAbsolutePath());
    }

    private List<Point2D.Double> extractCloseIonsPoints(List<? extends Feature> spectra, QuantifiedTarget<T> target) {
        double start = target.retentionIndex() - halfDelta;
        double end = target.retentionIndex() + halfDelta;

        List<Point2D.Double> points = new ArrayList<>();

        spectra.forEach(it -> {
            if (it instanceof QuantifiedSpectra) {
                QuantifiedSpectra<T> qspec = (QuantifiedSpectra<T>) it;
                if (qspec.retentionIndex() >= start && qspec.retentionIndex() < end) {
                    Option<Ion> ion = MassAccuracy.findClosestIon(it, (Double) target.accurateMass().get(), target, 0.01);
                    if (ion.isDefined()) {
                        points.add(new Point2D.Double(qspec.retentionIndex(), ion.get().intensity()));
                    }
                }
            } else {
                if (it.retentionTimeInSeconds() >= start && it.retentionTimeInSeconds() < end) {
                    Option<Ion> ion = MassAccuracy.findClosestIon(it, (Double) target.accurateMass().get(), target, 0.01);
                    if (ion.isDefined()) {
                        points.add(new Point2D.Double(it.retentionTimeInSeconds(), ion.get().intensity()));
                    }
                }
            }
        });
        return points.stream().sorted(Comparator.comparing(x -> x.x)).collect(Collectors.toList());
    }

    private String cleanName(String dirty) {
        return dirty.replaceAll("_[A-Z]{14}-[A-Z]{10}-[A-Z]|/|\\*\\\\|\\|", "")
              .replaceAll("\\(", "[")
              .replaceAll("\\)", "]")
              .replaceAll(":", "_");
    }

    private JFreeChart createLineChart(XYDataset curves, String title) {

        NumberAxis x = new NumberAxis("retention time");
        x.setAutoRangeIncludesZero(false);

        ValueAxis y = new NumberAxis("intensity");

        XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, true);

        XYPlot plot = new XYPlot(curves, x, y, lineRenderer);

        // chromatograms
        lineRenderer.setSeriesPaint(0, Color.black);                    // raw data
        lineRenderer.setSeriesPaint(1, Color.blue);                     // corrected data

        // markers
        lineRenderer.setSeriesPaint(2, new Color(40, 100, 40));   // rt
        lineRenderer.setSeriesPaint(3, new Color(80, 160, 80));   // ri
        lineRenderer.setSeriesPaint(4, new Color(150, 70, 20));   // annotation features

        // rt window for replacement feature search
        lineRenderer.setSeriesPaint(5, Color.magenta);                  // replacement search area
        lineRenderer.setSeriesShapesVisible(5, false);

        //replacement marker (single dot)
        lineRenderer.setSeriesPaint(6, Color.red);                      // replaced feature
        lineRenderer.setSeriesShape(6, ShapeUtils.createDiagonalCross(3, 3));
        lineRenderer.setSeriesPaint(7, Color.orange);                      // replaced feature
        lineRenderer.setSeriesShape(7, ShapeUtils.createRegularCross(4, 4));


        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);

        chart.setAntiAlias(false);

        return chart;
    }


    private List<Point2D.Double> correctData(List<Point.Double> points, Regression regression) {
        List<Point2D.Double> corrPoints = new ArrayList<>();
        for (Point2D.Double point : points) {
            corrPoints.add(new Point2D.Double(regression.computeY(point.x), point.y));
        }
        return corrPoints;
    }
}
