package ua.epam.springBatchApp;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ua.epam.springBatchApp.config.SpringBatchConfig;

public class AppRunner {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(SpringBatchConfig.class);
        ctx.refresh();

        JobLauncher jobLauncher = (JobLauncher) ctx.getBean("jobLauncher");
        Job job = (Job) ctx.getBean("mailJob");

        try {
            JobExecution execution = jobLauncher.run(job, new JobParameters());
            System.out.println("Job status: " + execution.getStatus());
            System.out.println("Job completed");
        }catch (Exception e){
            System.out.println("Job failed");
        }
    }
}
