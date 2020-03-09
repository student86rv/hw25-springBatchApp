package ua.epam.springBatchApp.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import ua.epam.springBatchApp.model.Account;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableBatchProcessing
@PropertySource("classpath:/local.properties")
public class SpringBatchConfig {

    private static final double LOW_LIMIT = 10.0;
    private static final String SQL_QUERY = "SELECT * FROM accounts";
    private static final String MESSAGE_SUBJECT = "Low limit";
    private static final String MESSAGE_TEXT = "Dear %s, we remind you that your balance is lower than %f $";

    @Autowired
    private Environment environment;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(environment.getProperty("jdbc.driver"));
        dataSource.setUrl(environment.getProperty("jdbc.url"));
        dataSource.setUsername(environment.getProperty("jdbc.username"));
        dataSource.setPassword(environment.getProperty("jdbc.password"));
        return dataSource;
    }

    @Bean
    public RowMapper<Account> rowMapper() {
        return (resultSet, i) -> {
            long id = resultSet.getLong("id");
            String name = resultSet.getString("name");
            double balance = resultSet.getDouble("balance");
            String email = resultSet.getString("email");
            return new Account(id, name, balance, email);
        };
    }

    @Bean
    public ItemReader<Account> itemReader(DataSource dataSource, RowMapper<Account> rowMapper) {
        JdbcCursorItemReader<Account> itemReader = new JdbcCursorItemReader<>();
        itemReader.setDataSource(dataSource);
        itemReader.setSql(SQL_QUERY);
        itemReader.setRowMapper(rowMapper);
        return itemReader;
    }

    @Bean
    public ItemProcessor<Account, Account> itemProcessor() {
        return account -> account.getBalance() < LOW_LIMIT ? account : null;
    }

    @Bean
    public ItemWriter<Account> itemWriter(JavaMailSender javaMailSender) {

        return list -> {
            for (Account account : list) {
                SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
                simpleMailMessage.setTo(account.getEmail());
                simpleMailMessage.setSubject(MESSAGE_SUBJECT);
                simpleMailMessage.setText(String.format(MESSAGE_TEXT, account.getName(), LOW_LIMIT));

                javaMailSender.send(simpleMailMessage);
            }
        };
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(environment.getProperty("spring.mail.host"));
        mailSender.setPort(Integer.parseInt(environment.getProperty("spring.mail.port")));

        mailSender.setUsername(environment.getProperty("spring.mail.username"));
        mailSender.setPassword(environment.getProperty("spring.mail.password"));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<Account, Account>chunk(10)
                .reader(itemReader(dataSource(), rowMapper()))
                .processor(itemProcessor())
                .writer(itemWriter(javaMailSender()))
                .build();
    }

    @Bean
    public Job mailJob() {
        return jobBuilderFactory.get("mailJob")
                .incrementer(new RunIdIncrementer())
                .flow(step1())
                .end()
                .build();
    }
}
