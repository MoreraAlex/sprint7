package ru.yandex.practicum.dictionary;

import ru.yandex.practicum.exceptions.GameOverException;
import ru.yandex.practicum.exceptions.WordleGameException;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/*
в этом классе хранится словарь и состояние игры
    текущий шаг
    всё что пользователь вводил
    правильный ответ

в этом классе нужны методы, которые
    проанализируют совпадение слова с ответом
    предложат слово-подсказку с учётом всего, что вводил пользователь ранее

не забудьте про специальные типы исключений для игровых и неигровых ошибок
 */
public class WordleGame {
    private static final int MAX_ATTEMPTS = 6;
    private static final int WORD_LENGTH = 5;

    private final String answer;
    private int steps;
    private final WordleDictionary dictionary;
    private final PrintWriter log;
    private final List<Character> correctPositions;
    private final List<Character> wrongPositions;
    private final List<Character> absentLetters;
    private final List<String> attempts;
    private String lastAnalysis;

    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this.dictionary = dictionary;
        this.log = log;
        this.answer = dictionary.getRandomWord();
        this.steps = MAX_ATTEMPTS;
        this.correctPositions = new ArrayList<>();
        this.wrongPositions = new ArrayList<>();
        this.absentLetters = new ArrayList<>();
        this.attempts = new ArrayList<>();
        this.lastAnalysis = null;

        for (int i = 0; i < WORD_LENGTH; i++) {
            correctPositions.add(' ');
            wrongPositions.add(' ');
        }
    }

    public boolean makeGuess(String guess) throws WordleGameException, GameOverException {
        if (steps <= 0) {
            throw new GameOverException("Игра окончена. Закончились попытки.");
        }

        String normalizedGuess = dictionary.normalizeWord(guess);

        if (normalizedGuess.length() != WORD_LENGTH) {
            throw new WordleGameException("Слово должно состоять из " + WORD_LENGTH + " букв");
        }

        if (!dictionary.contains(normalizedGuess)) {
            throw new WordleGameException("Слово не найдено в словаре");
        }

        attempts.add(normalizedGuess);
        steps--;

        lastAnalysis = analyzeGuess(normalizedGuess);

        if (normalizedGuess.equals(answer)) {
            log.println("Попытка: " + normalizedGuess + " -> " + lastAnalysis + " [УГАДАНО!]");
            return true;
        }

        updateKnowledge(normalizedGuess, lastAnalysis);
        log.println("Попытка: " + normalizedGuess + " -> " + lastAnalysis);

        return false;
    }

    private String analyzeGuess(String guess) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < WORD_LENGTH; i++) {
            char guessChar = guess.charAt(i);

            if (guessChar == answer.charAt(i)) {
                result.append('+');
            } else if (answer.indexOf(guessChar) != -1) {
                result.append('^');
            } else {
                result.append('-');
            }
        }

        return result.toString();
    }

    private void updateKnowledge(String guess, String analysis) {
        for (int i = 0; i < WORD_LENGTH; i++) {
            char guessChar = guess.charAt(i);
            char analysisChar = analysis.charAt(i);

            if (analysisChar == '+') {
                correctPositions.set(i, guessChar);
            } else if (analysisChar == '^') {
                wrongPositions.set(i, guessChar);
                if (absentLetters.contains(guessChar)) {
                    absentLetters.remove((Character) guessChar);
                }
            } else if (analysisChar == '-') {
                if (!correctPositions.contains(guessChar) && !wrongPositions.contains(guessChar)) {
                    if (!absentLetters.contains(guessChar)) {
                        absentLetters.add(guessChar);
                    }
                }
            }
        }
    }

    public String getLastAnalysis() {
        return lastAnalysis;
    }

    public String getHint() {
        return dictionary.getHint(correctPositions, wrongPositions, absentLetters);
    }

    public int getRemainingSteps() {
        return steps;
    }

    public String getAnswer() {
        return answer;
    }

    public List<String> getAttempts() {
        return attempts;
    }

    public boolean isGameOver() {
        return steps <= 0 || attempts.contains(answer);
    }
}
