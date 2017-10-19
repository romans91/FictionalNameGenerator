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
    private final static String DICTIONARY_URL = "http://www.dictionary.com";


    private static boolean working = false, shouldBeWorking = false;
    private static int pagesRead, totalPages, totalPagesInDictionary, syllablesFoundSoFar;

    private static File syllableStore;
    private static PrintWriter writer;



    public static void generateSyllablesFromWebsiteFile(int scrapeLimit) {
        if (!working) {
            working = true;
            shouldBeWorking = true;
            syllablesFoundSoFar = 0;
            pagesRead = 0;
            totalPages = scrapeLimit;
            try {
                syllableStore = new File(FictionalNameGenerator.SYLLABLES_FROM_WEBSITE_FILENAME);

                if (syllableStore.exists()) {
                    syllableStore.delete();
                }

                writer = new PrintWriter(FictionalNameGenerator.SYLLABLES_FROM_WEBSITE_FILENAME, "UTF-8");

                scrape(scrapeLimit, 5);

                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            working = false;
        }
    }

    private static void scrape(int pageLimit, int considerateDelay) {
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
                            scrapeSyllablesOutOfSinglePage(zList.item(k).getTextContent(), considerateDelay);
                            if (pagesRead >= pageLimit) {
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

    private static void scrapeSyllablesOutOfSinglePage(String inURL, int considerateDelay) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(inURL).openConnection().getInputStream()));

            for (int i = 0; i < 1263; i++) {
                in.readLine();
            }

            Thread.sleep((long) (Math.random() * considerateDelay) + considerateDelay);

            char[] forbiddenChars = {'<', '>', '(', ')', '/'};

            String line;
            boolean foundSyllables = false;

            while (!foundSyllables && (line = in.readLine()) != null) {
                if (line.contains("·")) {
                    for (String s : line.split("\"")) {
                        if (s.contains("·")) {
                            boolean forbiddenCharFound = false;

                            for (char c : forbiddenChars) {
                                if (s.contains(String.valueOf(c))) {
                                    forbiddenCharFound = true;
                                }
                            }

                            if (!forbiddenCharFound) {
                                for (String t : s.split("·|\\ |\\-")) {
                                    for (char c : t.toCharArray()) {
                                        if (!Character.isLetter(c)) {
                                            forbiddenCharFound = true;
                                        }
                                    }

                                    if (!forbiddenCharFound) {
                                        if (syllablesFoundSoFar > 0) {
                                            writer.write(",\n");
                                        }
                                        writer.write(sanitizeString(t));
                                        syllablesFoundSoFar++;
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
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
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
