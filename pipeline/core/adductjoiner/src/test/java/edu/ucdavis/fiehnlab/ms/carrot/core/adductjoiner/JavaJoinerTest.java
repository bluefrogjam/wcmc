package edu.ucdavis.fiehnlab.ms.carrot.core.adductjoiner;

import joinery.DataFrame;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * Created by diego on 2/26/2018
 **/
@RunWith(SpringRunner.class)
public class JavaJoinerTest extends TestCase {

    Joiner joiner = new Joiner();

    @Test
    public void testJoinAdducts() throws IOException {

        DataFrame targets = DataFrame.readCsv(this.getClass().getResourceAsStream("/CSH_Positive_All-Targets_tabulated.csv")).reindex(0).transpose();

        System.out.println(targets.add("targets", targets.retain(0).index()).head(2));
//      System.out.println(targets.col("Weiss001_posHILIC_59094269_064.d.zip").toArray.mkString("\n"))
    }
}
