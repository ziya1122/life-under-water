import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.IOException;

public class WebScraper {
    static final String URL = "jdbc:mysql://localhost:3306/biodiversity";
    static final String USER = "root";
    static final String PASSWORD = "";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the database.");
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }

    public static void insertData(String headingText, String link) {
        String query = "INSERT INTO datasets (heading, link) VALUES (?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setString(1, headingText != null ? headingText : "");
            pstmt.setString(2, link != null ? link : "");

            int rowsAffected = pstmt.executeUpdate();
            System.out.println(rowsAffected + " row(s) inserted. Heading: " + headingText + ", Link: " + link);
        } catch (SQLException e) {
            System.out.println("Error inserting data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void scrapeData() {
        String url = "https://datasources.speciesmonitoring.org/";
        try {
            Document document = Jsoup.connect(url).get();
            System.out.println("Connected to the URL.");
            System.out.println("Page HTML:\n" + document.html()); // Check raw HTML structure

            Elements headings = document.select("h2, a, div"); // Broad selection for testing
            System.out.println("Headings/Elements found: " + headings.size());
            for (Element heading : headings) {
                System.out.println("Element: " + heading.tagName() + " | Text: " + heading.text());
                insertData(heading.text(), null); 
            }

            Elements links = document.select("a[href]");
            System.out.println("Links found: " + links.size());
            for (Element link : links) {
                System.out.println("Link tag: " + link.tagName() + " | URL: " + link.attr("href"));
                insertData(null, link.attr("href"));
            }

            System.out.println("Data has been stored in the database.");
        } catch (IOException e) {
            System.out.println("Error connecting to the URL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        scrapeData();
    }
}
