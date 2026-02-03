package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.yandex.practicum.dictionary.WordleDictionary;
import ru.yandex.practicum.dictionary.WordleDictionaryLoader;
import ru.yandex.practicum.dictionary.WordleGame;
import ru.yandex.practicum.exceptions.GameOverException;
import ru.yandex.practicum.exceptions.WordleException;
import ru.yandex.practicum.exceptions.WordleGameException;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    private WordleDictionary dictionary;
    private PrintWriter log;

    @BeforeEach
    void init() {
        List<String> words = Arrays.asList(
                "берег", "ветка", "груша", "доска", "жираф",
                "зебра", "ириска", "каюта", "лампа", "маска"
        );
        dictionary = new WordleDictionary(words);

        try {
            log = new PrintWriter(new FileWriter("test.log", StandardCharsets.UTF_8), true);
        } catch (Exception ignored) {
        }
    }

    @Test
    void normalizeWordWorks() {
        assertEquals("ежик", dictionary.normalizeWord("ЁЖиК"));
        assertEquals("лампа", dictionary.normalizeWord("ЛАМПА"));
        assertEquals("берег", dictionary.normalizeWord("БеРеГ"));
    }

    @Test
    void containsFindsWords() {
        assertTrue(dictionary.contains("берег"));
        assertTrue(dictionary.contains("ветка"));
        assertFalse(dictionary.contains("слово"));
    }

    @Test
    void randomWordIsValid() {
        String word = dictionary.getRandomWord();
        assertNotNull(word);
        assertEquals(5, word.length());
        assertTrue(dictionary.getWords().contains(word));
    }

    @Test
    void loaderFiltersLength(@TempDir File tempDir) throws Exception {
        File file = new File(tempDir, "dict.txt");
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write("берег\n");
            writer.write("самолет\n");
            writer.write("лампа\n");
            writer.write("кот\n");
            writer.write("маска\n");
            writer.write("окно\n");
            writer.write("доска\n");
        }

        WordleDictionaryLoader loader = new WordleDictionaryLoader();
        WordleDictionary loaded = loader.load(file.getAbsolutePath());

        assertEquals(4, loaded.getWords().size());
        assertTrue(loaded.contains("берег"));
        assertTrue(loaded.contains("лампа"));
        assertTrue(loaded.contains("маска"));
        assertTrue(loaded.contains("доска"));
        assertFalse(loaded.contains("самолет"));
        assertFalse(loaded.contains("кот"));
    }

    @Test
    void loaderHandlesCase(@TempDir File tempDir) throws Exception {
        File file = new File(tempDir, "dict2.txt");
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write("БЕРЕГ\n");
            writer.write("лампа\n");
            writer.write("МаСкА\n");
        }

        WordleDictionaryLoader loader = new WordleDictionaryLoader();
        WordleDictionary loaded = loader.load(file.getAbsolutePath());

        assertTrue(loaded.contains("берег"));
        assertTrue(loaded.contains("лампа"));
        assertTrue(loaded.contains("маска"));
    }

    @Test
    void gameInitialState() {
        WordleGame game = new WordleGame(dictionary, log);

        assertNotNull(game.getAnswer());
        assertEquals(6, game.getRemainingSteps());
        assertFalse(game.isGameOver());
        assertTrue(game.getAttempts().isEmpty());
    }

    @Test
    void correctGuessEndsGame() {
        WordleGame game = new WordleGame(dictionary, log);
        String answer = game.getAnswer();

        try {
            boolean result = game.makeGuess(answer);
            assertTrue(result);
            assertTrue(game.isGameOver());
            assertEquals(5, game.getRemainingSteps());
        } catch (WordleException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void invalidLengthThrows() {
        WordleGame game = new WordleGame(dictionary, log);

        assertThrows(WordleGameException.class, () -> game.makeGuess("кот"));
        assertThrows(WordleGameException.class, () -> game.makeGuess("длинноеслово"));
    }

    @Test
    void wordNotInDictionaryThrows() {
        WordleGame game = new WordleGame(dictionary, log);
        assertThrows(WordleGameException.class, () -> game.makeGuess("арбуз"));
    }

    @Test
    void hintReturnsWord() {
        WordleGame game = new WordleGame(dictionary, log);
        String hint = game.getHint();
        assertNotNull(hint);
        assertEquals(5, hint.length());
        assertTrue(dictionary.contains(hint));
    }

    @Test
    void attemptsIncrease() {
        WordleGame game = new WordleGame(dictionary, log);
        String answer = game.getAnswer();

        List<String> guesses = dictionary.getWords().stream()
                .filter(w -> !w.equals(answer))
                .limit(2)
                .collect(Collectors.toList());

        try {
            game.makeGuess(guesses.get(0));
            assertEquals(1, game.getAttempts().size());

            game.makeGuess(guesses.get(1));
            assertEquals(2, game.getAttempts().size());
        } catch (WordleException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void analysisFormatIsCorrect() {
        WordleGame game = new WordleGame(dictionary, log);
        String answer = game.getAnswer();

        String guess = dictionary.getWords().stream()
                .filter(w -> !w.equals(answer))
                .findFirst()
                .orElse("лампа");

        try {
            game.makeGuess(guess);
            String analysis = game.getLastAnalysis();
            assertEquals(5, analysis.length());
            for (char c : analysis.toCharArray()) {
                assertTrue(c == '+' || c == '^' || c == '-');
            }
        } catch (WordleException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void gameOverAfterMaxAttempts() {
        WordleGame game = new WordleGame(dictionary, log);
        String answer = game.getAnswer();

        List<String> pool = dictionary.getWords().stream()
                .filter(w -> !w.equals(answer))
                .collect(Collectors.toList());

        assertTrue(pool.size() >= 6, "Недостаточно слов для теста");

        for (int i = 0; i < 6; i++) {
            try {
                boolean result = game.makeGuess(pool.get(i));
                assertFalse(result);
            } catch (WordleException e) {
                fail(e.getMessage());
            }
        }

        assertTrue(game.isGameOver());
        assertEquals(0, game.getRemainingSteps());

        assertThrows(GameOverException.class, () -> game.makeGuess(pool.get(0)));
    }


    @Test
    void lastAnalysisInitiallyNull() {
        WordleGame game = new WordleGame(dictionary, log);
        assertNull(game.getLastAnalysis());
    }
}

