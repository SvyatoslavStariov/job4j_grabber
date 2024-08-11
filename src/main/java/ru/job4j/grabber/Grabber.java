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

    private final Parse parse;

    private final Store store;

    private final Scheduler scheduler;

    private final int time;

    public Grabber(Parse parse, Store store, Scheduler scheduler, int time) {
        this.parse = parse;
        this.store = store;
        this.scheduler = scheduler;
        this.time = time;
    }

    @Override
    public void init() throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
            .usingJobData(data)
            .build();
        SimpleScheduleBuilder times = simpleSchedule()
            .withIntervalInSeconds(time)
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

    public static void main(String[] args) throws Exception {
        var config = new Properties();
        try (InputStream input = Grabber.class.getClassLoader()
            .getResourceAsStream("rabbit.properties")) {
            config.load(input);
        }
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        var parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        var store = new PsqlStore(config);
        var time = Integer.parseInt(config.getProperty("rabbit.interval"));
        new Grabber(parse, store, scheduler, time).init();
    }
}