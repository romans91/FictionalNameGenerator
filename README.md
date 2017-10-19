# Fictional Name Generator

Coming up with fictional names is hard. This is a tool that can make this task easier by randomly arranging English syllables into potentially meaningful words. In order to do that you first need a collection of syllables. This program can also scrape http://www.dictionary.com/ and its ~250000 pages for syllables from word entries, placing them in a comma separated .csv file. This file is not optional and can then be used indefinitely as a source of syllables.

This file is called:

```
SyllablesFromDictionary.csv
```
There are two more optional files:
```
SyllablesOccasional.csv
```
```
SyllablesMandatory.csv
```
SyllablesOccasional.csv is created by the user and contains syllables that can be randomly placed into a word instead of a syllable from SyllablesFromDictionary.csv. The chances of a syllable being replaces like this is variable. 

SyllablesMandatory.csv is also created by the user. One random syllable from this file must appear once somewhere in each word. The syllable that gets replaced is chosen randomly.