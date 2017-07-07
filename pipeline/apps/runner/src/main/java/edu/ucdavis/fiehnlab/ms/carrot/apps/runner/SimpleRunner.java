package edu.ucdavis.fiehnlab.ms.carrot.apps.runner;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowConfig;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
/**
 * Created by wohlg on 7/14/2016.
 */
@SpringBootApplication
@Import(WorkflowConfig.class)
public class SimpleRunner {

  public static void main(String args[]){

        try{
            SpringApplication application = new SpringApplication(SimpleRunner.class);
            application.setAddCommandLineProperties(true);
            application.setWebEnvironment(false);
            application.run(args);
        }
        catch (BeanCreationException e){
            if(e.getMostSpecificCause() instanceof BindException){

                BindException b = (BindException) e.getMostSpecificCause();
                System.out.println();
                System.out.println("Usage:");
                System.out.println();
                System.out.println("\tdear user, we need you to provide the following additional parameters");
                System.out.println();

                for(ObjectError error : b.getAllErrors()){
                    System.out.println("\t\t" + error.getDefaultMessage());
                }
                System.out.println();
                System.exit(1) ;
            }
        }

    }
}
