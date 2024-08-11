package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.model.Post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ru.job4j.grabber.utils.ConstantHabrCareer.SOURCE_LINK;

public class HabrCareerParse implements Parse {

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
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
            Post post = new Post(vacancyName, innerLink, description, dateTimeParser.parse(date));
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