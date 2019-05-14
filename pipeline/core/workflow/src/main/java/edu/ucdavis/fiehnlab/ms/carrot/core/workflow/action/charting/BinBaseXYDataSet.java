package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action.charting;

import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class BinBaseXYDataSet extends AbstractXYDataset implements XYDataset, IntervalXYDataset, AbstractDataset {
    private static final long serialVersionUID = 2L;

    List<Map> dataset = new Vector<>();

    public Number getEndX(int arg0, int arg1) {
        return this.getXValue(arg0, arg1);
    }

    public Number getEndY(int arg0, int arg1) {
        return this.getYValue(arg0, arg1);
    }

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

    public double getMaxY(int i) {
        try {
            return (Double) this.dataset.get(i).get("yMax");
        } catch (Exception e) {
            e.printStackTrace();

            return 0;
        }
    }

    public String getName(int i) {
        return this.dataset.get(i).get("name").toString();
    }

    public int getSeriesCount() {
        return this.dataset.size();
    }

    public String getSeriesName(int arg0) {
        return this.getDataSet(arg0).get("name").toString();
    }

    public Number getStartX(int arg0, int arg1) {
        return this.getXValue(arg0, arg1);
    }

    public Number getStartY(int arg0, int arg1) {
        return this.getYValue(arg0, arg1);
    }

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

    private void addDataSet(double[] x, double[] y, String name,
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

    private void replaceDataSet(double[] x, double[] y, String name,
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
