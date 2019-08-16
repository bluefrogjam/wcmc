package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action.charting;

import org.jfree.data.general.Dataset;

/**
 * @author wohlgemuth
 */
public interface AbstractDataset extends Dataset {
    public abstract void clear();

    public abstract void refresh();

    public abstract void update();
}
