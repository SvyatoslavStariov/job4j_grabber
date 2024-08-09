package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    public static final String PREFIX = "/vacancies?page=";

    public static final String SUFFIX = "page&q=Java%20developer&type=all";

    public static final int PAGE_NUMBER = 5;

    public static void main(String[] args) throws IOException {
        List<Elements> elements = new ArrayList<>();
        for (int pageNumber = 1; pageNumber <= PAGE_NUMBER; pageNumber++) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
            Connection connection = Jsoup.connect(fullLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            elements.add(rows);
        }
        for (Elements rows : elements) {
            System.out.println("Part records: " + rows.size());
            for (Element row : rows) {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element dateElement = row.select(".vacancy-card__date").first();
                Element linkElement = titleElement.child(0);
                String date = dateElement.child(0).attr("datetime");
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("date: [%s]; vacancy name: [%s]; link: [%s]%n", date, vacancyName, link);
                String string = retrieveDescription(link);
                System.out.printf("Description, %s%n%n", string);
            }
        }
    }

    private static String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-description__text");
        Element element = rows.get(0);
        return element.text();
    }
}