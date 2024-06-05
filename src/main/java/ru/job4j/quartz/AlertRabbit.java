package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;

public class AlertRabbit {

    public static Properties getProperties() throws IOException {
        Properties props = new Properties();
        try (var inputStream = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            if (inputStream == null) {
                throw new IOException("Resource not found: rabbit.properties");
            }
            props.load(inputStream);
        }
        return props;
    }

    public static Connection connect() throws SQLException {
        try {
            var url = getProperties().getProperty("jdbc.url");
            var user = getProperties().getProperty("jdbc.username");
            var password = getProperties().getProperty("jdbc.password");
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new SQLException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            Connection connection = connect();
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(jobDataMap)
                    .build();
            Trigger trigger = newTrigger()
                    .startNow()
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
            connection.close();
        } catch (SchedulerException se) {
            se.printStackTrace();
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            try (Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection")) {
                StringBuilder sql = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new FileReader("src/main/resources/db/scripts/001_ddl_create_rabbit_table.sql")))  {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sql.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try (Statement statement = connection.createStatement()) {
                    String[] sqlCommands = sql.toString().split(";");
                    for (String command : sqlCommands) {
                        if (!command.trim().isEmpty()) {
                            statement.execute(command);
                        }
                    }
                } catch  (SQLException e)  {
                    e.printStackTrace();
                }
            } catch  (SQLException e)  {
                e.printStackTrace();
            }
        }
    }
}
