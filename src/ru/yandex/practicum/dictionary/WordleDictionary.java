package ru.yandex.practicum.dictionary;

import java.util.List;
import java.util.Random;

public class WordleDictionary {
    private static final int WORD_LENGTH = 5;
    private final List<String> words;
    private final Random random;

    public WordleDictionary(List<String> words) {
        this.words = words;
        this.random = new Random();
    }

    public String getRandomWord() {
        if (words.isEmpty()) {
            throw new IllegalStateException("Словарь пуст");
        }

        return words.get(random.nextInt(words.size()));
    }

    public boolean contains(String word) {
        return words.contains(word);
    }

    public List<String> getWords() {
        return words;
    }

    public String normalizeWord(String word) {
        return word.toLowerCase().replace('ё', 'е');
    }

    public String getHint(List<Character> correctPositions,
                          List<Character> wrongPositions,
                          List<Character> absentLetters) {
        for (String word : words) {
            boolean matches = true;

            for (int i = 0; i < WORD_LENGTH; i++) {
                char correctChar = correctPositions.get(i);
                if (correctChar != ' ' && word.charAt(i) != correctChar) {
                    matches = false;
                    break;
                }
            }

            if (!matches) continue;

            for (int i = 0; i < WORD_LENGTH; i++) {
                char wrongChar = wrongPositions.get(i);
                if (wrongChar != ' ') {
                    if (word.indexOf(wrongChar) == -1) {
                        matches = false;
                        break;
                    }
                    if (word.charAt(i) == wrongChar) {
                        matches = false;
                        break;
                    }
                }
            }

            if (!matches) continue;

            for (char absent : absentLetters) {
                if (word.indexOf(absent) != -1) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                return word;
            }
        }

        return null;
    }
}
