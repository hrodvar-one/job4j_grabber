package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
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
            if (url == null || user == null || password == null) {
                throw new SQLException("Database connection properties are not set correctly.");
            }
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
            Properties properties = getProperties();
            String intervalStr = properties.getProperty("rabbit.interval");
            if (intervalStr == null) {
                throw new RuntimeException("Property 'time.interval' is not set in rabbit.properties.");
            }
            int interval = Integer.parseInt(intervalStr);
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            jobDataMap.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(jobDataMap)
                    .build();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (SchedulerException se) {
            se.printStackTrace();
        } catch (SQLException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            LocalDateTime currentTime = LocalDateTime.now();
            String sql = "INSERT INTO rabbit (created_date) VALUES (?)";
            Connection connection = null;
            try {
                Properties dbProps = (Properties) context.getJobDetail().getJobDataMap().get("dbProps");
                String url = dbProps.getProperty("jdbc.url");
                String user = dbProps.getProperty("jdbc.username");
                String password = dbProps.getProperty("jdbc.password");
                connection = DriverManager.getConnection(url, user, password);
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setObject(1, currentTime);
                    ps.executeUpdate();
                    System.out.println("Timestamp inserted successfully: " + currentTime);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
