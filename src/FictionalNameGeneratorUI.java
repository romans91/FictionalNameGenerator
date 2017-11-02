import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

/*
        User interface for the name generator. Checks if the syllable store files are present,
    allows scraping of as many pages of dictionary.com as desired as well as for scraping to be safely ended at any time.
    Allows user to tinker with syllable frequency in the output words, generate as many as desired and to copy
    the results onto the clipboard.
*/

public class FictionalNameGeneratorUI extends JFrame {
    private int minSyllables = 3, maxSyllables = 5;
    private final int wordsToGenerate = 10, significantFigures = 6;
    private float customSyllableFrequency = 0.0f;

    private JPanel mainPanel = new JPanel(new GridBagLayout(), false);
    GridBagConstraints constraints = new GridBagConstraints();

    private JLabel lSyllablesFromWebsiteFileFound = new JLabel();
    private JLabel lSyllablesOccasionalFileFound = new JLabel();
    private JLabel lSyllablesMandatoryFileFound = new JLabel();
    private JButton bRefresh = new JButton("Refresh");

    private JButton bScrape = new JButton("Scrape");
    private JLabel lScrapeLimit = new JLabel("Max. pages to scrape: ");
    private JTextField tfScrapeLimit = new JTextField();
    private JLabel lScrapeProgress = new JLabel("");

    private JButton bGenerate = new JButton("Generate");
    private JButton bExit = new JButton("Exit");
    private JSlider sMinSyllables = new JSlider(1, 10, 3);
    private JSlider sMaxSyllables = new JSlider(1, 10, 5);
    private JSlider sCustomSyllableFrequency = new JSlider(0, Integer.MAX_VALUE, Integer.MAX_VALUE / 2);
    private JTextArea resultsText = new JTextArea();
    private JLabel lMinSyllables = new JLabel();
    private JLabel lMaxSyllables = new JLabel();
    private JLabel lCustomSyllableFrequency = new JLabel();
    private JLabel lDictionarySyllableFrequency = new JLabel();

    public FictionalNameGeneratorUI() {
        super("Fictional Name Generator");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        constraints.insets = new Insets(2,5,2,5);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = 0;

        addFilesFoundLabels();
        addDivider();
        addScrapeMenu();
        addDivider();
        addSliders();
        addOutputSection();

        mainPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder()));

        Dimension d = new Dimension(600,700);
        getContentPane().setPreferredSize(d);

        add(mainPanel);

        pack();
        setLocationRelativeTo(null);
    }

    private void printGeneratedNames() {
        String text = "";

        for (String s : FictionalNameGenerator.generateNames(wordsToGenerate, minSyllables, maxSyllables, customSyllableFrequency)) {
            s = s.substring(0, 1).toUpperCase() + s.substring(1, s.length());
            text += s + '\n';
        }
        resultsText.setText(text.substring(0, text.length() - 1));
    }

    private void addDivider() {
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 0;
        constraints.gridy++;

        try {
            BufferedImage dividerImg = ImageIO.read(getClass().getResourceAsStream("img/Divider.png"));
            ImageIcon image = new ImageIcon(dividerImg);
            JLabel divider = new JLabel("", image, JLabel.CENTER);
            mainPanel.add( divider, constraints );

        } catch (IOException e) {
            e.printStackTrace();
        }

        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridwidth = 1;
    }

    private void addFilesFoundLabels() {
        constraints.gridwidth = 2;
        mainPanel.add(lSyllablesFromWebsiteFileFound, constraints);
        constraints.gridy++;
        mainPanel.add(lSyllablesOccasionalFileFound, constraints);
        constraints.gridy++;
        mainPanel.add(lSyllablesMandatoryFileFound, constraints);
        constraints.gridy++;
        mainPanel.add(bRefresh, constraints);

        bRefresh.addActionListener((ActionEvent event) -> {
            updateFilesFoundLabels();
        });
        constraints.gridwidth = 1;
        updateFilesFoundLabels();
    }

    private void addScrapeMenu() {
        constraints.gridy++;

        mainPanel.add(lScrapeLimit, constraints);
        constraints.gridx++;
        tfScrapeLimit.setPreferredSize(new Dimension(150, 30));
        mainPanel.add(tfScrapeLimit, constraints);

        constraints.gridy++;
        constraints.gridx = 0;

        bScrape.addActionListener((ActionEvent event) -> {
            if (!DictionaryScraper.isWorking()) {
                Thread tTextGenerator = new Thread(new TextGenerator(), "t1");
                tTextGenerator.start();
            } else {
                DictionaryScraper.stopWorking();
            }
        });

        constraints.gridwidth = 2;
        mainPanel.add(bScrape, constraints);
        mainPanel.add(lScrapeProgress, constraints);
        constraints.gridwidth = 1;
    }

    private void updateSyllableChanceLabels() {
        customSyllableFrequency = (float)sCustomSyllableFrequency.getValue() / (float)sCustomSyllableFrequency.getMaximum();
        lCustomSyllableFrequency.setText("Occasional syllable chance: " + String.format("%0$-" + significantFigures + "s", Float.toString(customSyllableFrequency)).replace(' ', '0').substring(0, significantFigures));
        lDictionarySyllableFrequency.setText("Syllable from dictionary chance: " + String.format("%0$-" + significantFigures + "s", Float.toString(1.0f - customSyllableFrequency)).replace(' ', '0').substring(0, significantFigures));
    }

    private void updateScrapeProgressLabels() {
        lScrapeProgress.setText("                              "
                + String.valueOf(DictionaryScraper.getPagesRead()) + "/" + String.valueOf(DictionaryScraper.getTotalPages()) + " pages scraped, "
                + String.valueOf(DictionaryScraper.getSyllablesFoundSoFar()) + " syllables found, "
                + String.valueOf(DictionaryScraper.getTotalPagesInDictionary() + " pages found in dictionary."));
    }

    private void updateSliderLabels() {
        lMinSyllables.setText("Min. syllables: " + String.valueOf(minSyllables));
        lMaxSyllables.setText("Max. syllables: " + String.valueOf(maxSyllables));
    }

    private void updateFilesFoundLabels() {
        if (new File(FictionalNameGenerator.SYLLABLES_FROM_WEBSITE_FILENAME).exists()) {
            lSyllablesFromWebsiteFileFound.setText("<html>" + FictionalNameGenerator.SYLLABLES_FROM_WEBSITE_FILENAME
                    + " found. <br/>Ready to generate words.</html>");
            bGenerate.setEnabled(true);
            bScrape.setText("Re-scrape");
        } else {
            lSyllablesFromWebsiteFileFound.setText("<html><font color=\"red\">" + FictionalNameGenerator.SYLLABLES_FROM_WEBSITE_FILENAME
                    + " not found. <br/>Need to scrape before continuing!</font></html>");
            bGenerate.setEnabled(false);
            bScrape.setText("Scrape");
        }

        if (new File(FictionalNameGenerator.SYLLABLES_OCCASIONAL_FILENAME).exists()) {
            lSyllablesOccasionalFileFound.setText("<html>" + FictionalNameGenerator.SYLLABLES_OCCASIONAL_FILENAME
                    + " found. <br/>Frequency of these syllables can be changed. </html>");
        } else {
            lSyllablesOccasionalFileFound.setText("<html>" + FictionalNameGenerator.SYLLABLES_OCCASIONAL_FILENAME
                    + " not found.<br/>Frequency of these syllables can be changed. </html>");
        }

        if (new File(FictionalNameGenerator.SYLLABLES_MANDATORY_FILENAME).exists()) {
            lSyllablesMandatoryFileFound.setText("<html>" + FictionalNameGenerator.SYLLABLES_MANDATORY_FILENAME
                    + " found. <br/>One of these syllables will appear in every word.</html>");
        } else {
            lSyllablesMandatoryFileFound.setText("<html>" + FictionalNameGenerator.SYLLABLES_MANDATORY_FILENAME
                    + " not found. <br/>One of these syllables would appear in every word.</html>");
        }
    }

    private void addSliders() {
        constraints.anchor = GridBagConstraints.NORTH;

        sMinSyllables.setMinorTickSpacing(1);
        sMinSyllables.setMajorTickSpacing(5);
        sMinSyllables.setPaintTicks(true);
        sMinSyllables.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (sMinSyllables.getValue() > sMaxSyllables.getValue()) {
                    sMinSyllables.setValue(sMaxSyllables.getValue());
                } else {
                    minSyllables = sMinSyllables.getValue();
                }
                updateSliderLabels();
            }
        });

        Dictionary<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
        table.put(1, new JLabel("1"));
        table.put(5, new JLabel("5"));
        table.put(10, new JLabel("10"));

        sMinSyllables.setLabelTable(table);
        sMinSyllables.setPaintLabels(true);
        sMinSyllables.setSnapToTicks(true);
        constraints.gridx = 0;
        constraints.gridy++;
        mainPanel.add(lMinSyllables, constraints);
        constraints.gridx = 0;
        constraints.gridy++;
        mainPanel.add(sMinSyllables, constraints);
        constraints.gridy = constraints.gridy - 2;

        sMaxSyllables.setMinorTickSpacing(1);
        sMaxSyllables.setMajorTickSpacing(5);
        sMaxSyllables.setPaintTicks(true);
        sMaxSyllables.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (sMaxSyllables.getValue() < sMinSyllables.getValue()) {
                    sMaxSyllables.setValue(sMinSyllables.getValue());
                } else {
                    maxSyllables = sMaxSyllables.getValue();
                }
                updateSliderLabels();

            }
        });

        sMaxSyllables.setLabelTable(table);
        sMaxSyllables.setPaintLabels(true);
        sMaxSyllables.setSnapToTicks(true);
        constraints.gridx = 1;
        constraints.gridy++;
        mainPanel.add(lMaxSyllables, constraints);
        constraints.gridy++;
        mainPanel.add(sMaxSyllables, constraints);
        constraints.gridx = 0;

        sCustomSyllableFrequency.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateSyllableChanceLabels();
            }
        });

        sCustomSyllableFrequency.setMajorTickSpacing(Integer.MAX_VALUE / 10);
        sCustomSyllableFrequency.setMinorTickSpacing(Integer.MAX_VALUE / 100);
        sCustomSyllableFrequency.setPaintTicks(true);

        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy++;
        mainPanel.add(lCustomSyllableFrequency, constraints);
        constraints.gridx++;
        mainPanel.add(lDictionarySyllableFrequency, constraints);
        constraints.gridx = 0;
        constraints.gridy++;
        mainPanel.add(sCustomSyllableFrequency, constraints);
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;

        updateSyllableChanceLabels();
        updateSliderLabels();
    }

    private void addOutputSection() {
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy++;
        mainPanel.add(bGenerate, constraints);

        bGenerate.addActionListener((ActionEvent event) -> {
            printGeneratedNames();
        });

        for (int i = 0; i < wordsToGenerate - 1; i++) {
            resultsText.append("\n");
        }

        constraints.gridy++;
        constraints.gridwidth = 2;
        mainPanel.add(resultsText, constraints);
        constraints.gridwidth = 1;

        constraints.gridy++;
        mainPanel.add(bExit, constraints);

        bExit.addActionListener((ActionEvent event) -> {
            if (DictionaryScraper.isWorking()) {
                DictionaryScraper.stopWorking();
            }
            System.exit(0);
        });

        constraints.fill = GridBagConstraints.NONE;
    }

    public class LabelScrapeProgressTextSetter implements Runnable {
        public void run() {
            while (DictionaryScraper.isWorking()) {
                try {
                    updateScrapeProgressLabels();
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e) {
                    tfScrapeLimit.setText("InterruptedException; try again");
                }
            }
            updateFilesFoundLabels();
            bGenerate.setEnabled(true);
        }
    }

    public class TextGenerator implements Runnable {
        public void run() {
            Thread tTextSetter = new Thread(new LabelScrapeProgressTextSetter(), "t1");
            try {
                bScrape.setText("Stop now");
                bGenerate.setEnabled(false);
                tTextSetter.start();
                DictionaryScraper.generateSyllablesFromWebsiteFile(Integer.parseInt(tfScrapeLimit.getText()));
                updateFilesFoundLabels();
                updateScrapeProgressLabels();
            } catch (NumberFormatException e) {
                tfScrapeLimit.setText("Enter a number!");
                updateFilesFoundLabels();
                bGenerate.setEnabled(true);
            }
        }
    }
}

