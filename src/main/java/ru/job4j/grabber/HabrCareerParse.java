package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;
import ru.job4j.model.Post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    public static final String PREFIX = "/vacancies?page=";

    public static final String SUFFIX = "page&q=Java%20developer&type=all";

    public static final int PAGE_NUMBER = 5;

    private final DateTimeParser dateTimeParser;

    private int id = 0;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws IOException {
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        HabrCareerParse habrCareerParse = new HabrCareerParse(dateTimeParser);
        List<Post> posts = new ArrayList<>();
        for (int pageNumber = 1; pageNumber <= PAGE_NUMBER; pageNumber++) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
            posts.addAll(habrCareerParse.list(fullLink));
        }
        posts.forEach(System.out::println);
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> posts = new ArrayList<>();
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        for (Element row : rows) {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element dateElement = row.select(".vacancy-card__date").first();
            Element linkElement = titleElement.child(0);
            String date = dateElement.child(0).attr("datetime");
            String vacancyName = titleElement.text();
            String innerLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            String description = retrieveDescription(innerLink);
            Post post = new Post(id++, vacancyName, innerLink, description, dateTimeParser.parse(date));
            posts.add(post);
        }
        return posts;
    }

    private String retrieveDescription(String innerLink) throws IOException {
        Connection connection = Jsoup.connect(innerLink);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-description__text");
        Element element = rows.get(0);
        return element.text();
    }
}