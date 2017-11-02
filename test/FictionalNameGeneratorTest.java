import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;

public class FictionalNameGeneratorTest {
    @Before
    public void setUp() {
        DictionaryScraper.generateSyllablesFromWebsiteFile(1000);
    }

    @Test
    public void testFileHasBeenCreated() {
        File file = new File(FictionalNameGenerator.SYLLABLES_FROM_WEBSITE_FILENAME);
        Assert.assertTrue(file.exists());
    }

    @Test
    public void testSyllablesContainNoUnwantedCharacters() {
        for (String s : getSyllablesFromCsv(FictionalNameGenerator.SYLLABLES_FROM_WEBSITE_FILENAME)) {
            for (char c : s.toCharArray()) {
                Assert.assertTrue(Character.isLetter(c));
            }
        }
    }

    @Test
    public void testOutputSizeIsCorrect() {
        Assert.assertTrue(FictionalNameGenerator.generateNames(1000, 1, 1, 0.0f).size() == 1000);
    }

    @Test
    public void testAllSyllablesFromCsvAppearInOutput() {
        List<String> syllables = getSyllablesFromCsv(FictionalNameGenerator.SYLLABLES_FROM_WEBSITE_FILENAME);
        List<String> generatedNames = FictionalNameGenerator.generateNames(syllables.size() * 1000, 3, 5, 0.0f);

        Map<String, Boolean> syllableHasAppeared = new HashMap<>();

        for (String s : syllables) {
            syllableHasAppeared.putIfAbsent(s, false);
        }

        for (String s : generatedNames) {
            for (String t : syllableHasAppeared.keySet()) {
                if (s.contains(t)) {
                    syllableHasAppeared.put(t, true);
                }
            }
        }

        String errorText = "";
        for (Map.Entry<String, Boolean> e : syllableHasAppeared.entrySet()) {
            if (!e.getValue()) {
                errorText += String.format("Syllable \"%s\" not used\n", e.getKey());
            }
        }

        if (errorText.length() > 0) {
            Assert.fail(errorText);
        }
    }

    @Test
    public void testOccasionalSyllables() {
        List<String> occasionalSyllables = new ArrayList<String>()  {{
            add("occasionalSyllableOne");
            add("occasionalSyllableTwo");
            add("occasionalSyllableThree");
        }};

        createCsv(FictionalNameGenerator.SYLLABLES_OCCASIONAL_FILENAME, occasionalSyllables);

        List<String> generatedNames = FictionalNameGenerator.generateNames(1000, 3, 5, 0.0f);

        for (String s : generatedNames) {
            for (String t : occasionalSyllables) {
                Assert.assertFalse(s.contains(t));
            }
        }

        generatedNames = FictionalNameGenerator.generateNames(1000, 3, 5, 1.0f);
        for (String s : generatedNames) {
            for (String t : occasionalSyllables) {
                s = s.replace(t, "");
            }
            Assert.assertTrue(s.length() == 0);
        }

        deleteFile(FictionalNameGenerator.SYLLABLES_OCCASIONAL_FILENAME);
    }

    @Test
    public void testMandatorySyllables() {
        List<String> mandatorySyllables = new ArrayList<String>()  {{
            add("mandatorySyllableOne");
            add("mandatorySyllableTwo");
            add("mandatorySyllableThree");
        }};

        List<String> generatedNames = FictionalNameGenerator.generateNames(1000, 3, 5, 0.0f);

        for (String s : generatedNames) {
            for (String t : mandatorySyllables) {
                Assert.assertFalse(s.contains(t));
            }
        }

        createCsv(FictionalNameGenerator.SYLLABLES_MANDATORY_FILENAME, mandatorySyllables);
        generatedNames = FictionalNameGenerator.generateNames(1000, 3, 5, 0.0f);

        boolean mandatorySyllableFound = false;

        for (String s : generatedNames) {
            for (String t : mandatorySyllables) {
                if (s.contains(t)) {
                    mandatorySyllableFound = true;
                }
            }
            Assert.assertTrue(mandatorySyllableFound);
            mandatorySyllableFound = false;
        }

        deleteFile(FictionalNameGenerator.SYLLABLES_MANDATORY_FILENAME);
    }

    @After
    public void tearDown() {
        cleanOutDirectory();
    }

    private List<String> getSyllablesFromCsv(String filename) {
        List<String> syllables = new ArrayList<String>();
        try {
            Scanner scanner = new Scanner(new File(filename));
            scanner.useDelimiter(",\n");

            while (scanner.hasNext()) {
                syllables.add(scanner.next());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return syllables;
    }

    private void cleanOutDirectory() {
        deleteFile(FictionalNameGenerator.SYLLABLES_FROM_WEBSITE_FILENAME);
        deleteFile(FictionalNameGenerator.SYLLABLES_OCCASIONAL_FILENAME);
        deleteFile(FictionalNameGenerator.SYLLABLES_MANDATORY_FILENAME);
    }

    private void deleteFile(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
    }

    private void createCsv(String filename, List<String> entries) {
        File syllableStore = new File(filename);
        if (syllableStore.exists()) {
            syllableStore.delete();
        }

        try {
            PrintWriter writer = new PrintWriter(syllableStore);
            boolean firstLineWritten = false;
            for (String s : entries) {
                if (!firstLineWritten) {
                    firstLineWritten = true;
                } else {
                    writer.write(",\n");
                }
                writer.write(s);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
