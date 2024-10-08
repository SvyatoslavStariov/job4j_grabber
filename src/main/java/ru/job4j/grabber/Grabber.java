package ru.job4j.grabber;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;
import ru.job4j.model.Post;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static ru.job4j.grabber.utils.ConstantHabrCareer.PAGE_NUMBER;
import static ru.job4j.grabber.utils.ConstantHabrCareer.PREFIX;
import static ru.job4j.grabber.utils.ConstantHabrCareer.SOURCE_LINK;
import static ru.job4j.grabber.utils.ConstantHabrCareer.SUFFIX;

public class Grabber implements Grab {

    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
            .usingJobData(data)
            .build();
        SimpleScheduleBuilder times = simpleSchedule()
            .withIntervalInSeconds(Integer.parseInt(cfg().getProperty("rabbit.interval")))
            .repeatForever();
        Trigger trigger = newTrigger()
            .startNow()
            .withSchedule(times)
            .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            for (int number = 0; number <= PAGE_NUMBER; number++) {
                String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, number, SUFFIX);
                try {
                    List<Post> posts = parse.list(fullLink);
                    for (Post post : posts) {
                        store.save(post);
                    }
                } catch (IOException | SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Integer.parseInt(cfg().getProperty("port")))) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : store.getAll()) {
                            out.write(post.toString().getBytes(Charset.forName("Windows-1251")));
                            out.write(System.lineSeparator().getBytes());
                            out.write(System.lineSeparator().getBytes());
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Properties cfg() {
        var config = new Properties();
        try (InputStream input = Grabber.class.getClassLoader()
            .getResourceAsStream("rabbit.properties")) {
            config.load(input);
            return config;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    private Store store() throws SQLException {
        return new PsqlStore(cfg());
    }

    public static void main(String[] args) throws Exception {
        Grabber grab = new Grabber();
        grab.cfg();
        Scheduler scheduler = grab.scheduler();
        Store store = grab.store();
        grab.init(new HabrCareerParse(new HabrCareerDateTimeParser()), store, scheduler);
        grab.web(store);
    }
}