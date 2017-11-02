import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

/*
        Scrapes syllables and writes them to a comma separated output .csv file. Can be terminated and have it's
    write steam closed prematurely at any time.
*/

// http://www.dictionary.com/dictionary-sitemap/sitemap.xml
public class DictionaryScraper extends JFrame {
    public static final String DICTIONARY_URL = "http://www.dictionary.com";


    private static boolean working = false, shouldBeWorking = false;
    private static int pagesRead, totalPages, totalPagesInDictionary, syllablesFoundSoFar;

    private static File syllableStore;
    private static PrintWriter writer;

    public static void generateSyllablesFromWebsiteFile(int scrapePageLimit) {
        if (!working) {
            working = true;
            shouldBeWorking = true;
            syllablesFoundSoFar = 0;
            pagesRead = 0;
            totalPages = scrapePageLimit;
            try {
                syllableStore = new File(FictionalNameGenerator.SYLLABLES_FROM_WEBSITE_FILENAME);

                if (syllableStore.exists()) {
                    syllableStore.delete();
                }

                writer = new PrintWriter(FictionalNameGenerator.SYLLABLES_FROM_WEBSITE_FILENAME, "UTF-8");

                scrape(scrapePageLimit, 5);

                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            working = false;
        }
    }

    private static void scrape(int scrapePageLimit, int delayBetweenPagesMillis) {
        try {

            URLConnection robotsTxtConnection = new URL(String.format(DICTIONARY_URL + "/robots.txt")).openConnection();
            BufferedReader bin = new BufferedReader(new InputStreamReader(robotsTxtConnection.getInputStream()));

            String line, sitemapUrl = "";

            while ((line = bin.readLine()) != null) {
                if (line.contains("sitemap.xml")) {
                    sitemapUrl = line.substring(line.indexOf("http://"));
                }
            }

            URLConnection sitemapConnection = new URL(sitemapUrl).openConnection();

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            Document doc = dbFactory.newDocumentBuilder().parse(sitemapConnection.getInputStream());
            doc.getDocumentElement().normalize();

            totalPagesInDictionary = 0;

            NodeList sitemapPageUrlList = doc.getElementsByTagName("loc");

            boolean countingPages = true;

            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < sitemapPageUrlList.getLength(); j++) {
                    if (!shouldBeWorking) {
                        stopScraping();
                        return;
                    }
                    URLConnection sitemapPageConnection = new URL(String.format(sitemapPageUrlList.item(j).getTextContent())).openConnection();

                    Document sitemapPages = dbFactory.newDocumentBuilder().parse(new GZIPInputStream(sitemapPageConnection.getInputStream()));
                    sitemapPages.getDocumentElement().normalize();
                    NodeList zList = sitemapPages.getElementsByTagName("loc");

                    if (countingPages) {
                        totalPagesInDictionary += zList.getLength();
                    } else {
                        for (int k = 0; k < zList.getLength(); k++) {
                            if (!shouldBeWorking) {
                                stopScraping();
                                return;
                            }
                            pagesRead++;
                            scrapeSyllablesOutOfSinglePage(zList.item(k).getTextContent(), delayBetweenPagesMillis);
                            if (pagesRead >= scrapePageLimit) {
                                return;
                            }
                        }
                    }
                }
                countingPages = false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    private static void scrapeSyllablesOutOfSinglePage(String inURL, int delayBetweenPagesMillis) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(inURL).openConnection().getInputStream()));
            char[] unwantedChars = {'<', '>', '(', ')', '/'};

            Thread.sleep((long) (Math.random() * delayBetweenPagesMillis) + delayBetweenPagesMillis);

            String line;
            boolean foundSyllables = false;

            while (!foundSyllables && (line = in.readLine()) != null) {
                if (line.contains("·")) {
                    for (String s : line.split("\"")) {
                        if (s.contains("·")) {
                            boolean unwantedCharFound = false;

                            for (char c : unwantedChars) {
                                if (s.contains(String.valueOf(c))) {
                                    unwantedCharFound = true;
                                }
                            }

                            if (!unwantedCharFound) {
                                for (String t : s.split("·|\\ |\\-")) {
                                    for (char c : t.toCharArray()) {
                                        if (!Character.isLetter(c)) {
                                            unwantedCharFound = true;
                                        }
                                    }

                                    if (!unwantedCharFound) {
                                        t = sanitizeString(t);
                                        if (t.length() > 0) {
                                            if (syllablesFoundSoFar > 0) {
                                                writer.write(",\n");
                                            }
                                            writer.write(t);
                                            syllablesFoundSoFar++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    foundSyllables = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String sanitizeString(String in) {
        String out = "";
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                out += c;
            }
        }
        return out.toLowerCase().trim();
    }

    private static void stopScraping() {
        working = false;
        writer.close();
    }

    public static boolean isWorking() {
        return working;
    }

    public static void stopWorking() { shouldBeWorking = false; }

    public static int getPagesRead() {
        return pagesRead;
    }

    public static int getTotalPages() {
        return totalPages;
    }

    public static int getTotalPagesInDictionary() {
        return totalPagesInDictionary;
    }

    public static int getSyllablesFoundSoFar() { return syllablesFoundSoFar; }
}
