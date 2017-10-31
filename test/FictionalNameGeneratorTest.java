import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

public class FictionalNameGeneratorTest {

    @Before
    public void SetUp() {
        File file = new File(FictionalNameGenerator.SYLLABLES_FROM_WEBSITE_FILENAME);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void AssertFileExists() {
        File file = new File(FictionalNameGenerator.SYLLABLES_FROM_WEBSITE_FILENAME);
        Assert.assertFalse(file.exists());

        DictionaryScraper.generateSyllablesFromWebsiteFile(50);
        Assert.assertTrue(file.exists());
    }

    @After
    public void TearDown() {
        SetUp();
    }
}
