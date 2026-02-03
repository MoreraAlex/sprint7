package ru.yandex.practicum;

import ru.yandex.practicum.dictionary.WordleDictionary;
import ru.yandex.practicum.dictionary.WordleDictionaryLoader;
import ru.yandex.practicum.dictionary.WordleGame;
import ru.yandex.practicum.exceptions.GameOverException;
import ru.yandex.practicum.exceptions.WordleDictionaryException;
import ru.yandex.practicum.exceptions.WordleGameException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/*
в главном классе нам нужно:
    создать лог-файл (он должен передаваться во все классы)
    создать загрузчик словарей WordleDictionaryLoader
    загрузить словарь WordleDictionary с помощью класса WordleDictionaryLoader
    затем создать игру WordleGame и передать ей словарь
    вызвать игровой метод в котором в цикле опрашивать пользователя и передавать информацию в игру
    вывести состояние игры и конечный результат
 */

public class Wordle {
    private static final int MAX_ATTEMPTS = 6;
    private static final int WORD_LENGTH = 5;

    public static void main(String[] args) {
        try (PrintWriter log = new PrintWriter(new FileWriter("wordle.log", StandardCharsets.UTF_8),
                true)) {
            WordleDictionaryLoader loader = new WordleDictionaryLoader();
            WordleDictionary dictionary = loader.load("words_ru.txt");

            WordleGame game = new WordleGame(dictionary, log);

            try (Scanner scanner = new Scanner(System.in, "UTF-8")) {
                System.out.println("Добро пожаловать в игру Wordle!");
                System.out.println("У вас есть " + MAX_ATTEMPTS + " попыток, чтобы угадать слово из " + WORD_LENGTH
                        + " букв.");
                System.out.println("Введите слово или нажмите Enter для подсказки.");

                boolean gameWon = false;

                while (!game.isGameOver()) {
                    System.out.print("> ");
                    String input = scanner.nextLine().trim();

                    if (input.isEmpty()) {
                        String hint = game.getHint();
                        if (hint != null) {
                            System.out.println("Подсказка: " + hint);
                        } else {
                            System.out.println("Подсказок больше нет.");
                        }
                        continue;
                    }

                    try {

                        boolean isCorrectGuess = game.makeGuess(input);

                        if (isCorrectGuess) {
                            System.out.println("Поздравляем! Вы угадали слово!");
                            System.out.println("Вы выиграли за " + (MAX_ATTEMPTS - game.getRemainingSteps()) + " попыток!");
                            gameWon = true;
                            break;
                        } else {
                            String analysis = game.getLastAnalysis();
                            System.out.println("> " + analysis);
                        }

                    } catch (GameOverException e) {
                        System.out.println(e.getMessage());
                        System.out.println("Загаданное слово: " + game.getAnswer());
                        break;
                    } catch (WordleGameException e) {
                        System.out.println("Ошибка: " + e.getMessage());
                    }
                }

                if (!gameWon && game.getRemainingSteps() == 0) {
                    System.out.println("К сожалению, вы проиграли.");
                    System.out.println("Загаданное слово: " + game.getAnswer());
                }

            } catch (Exception e) {
                System.err.println("Ошибка ввода: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Ошибка при работе с файлами: " + e.getMessage());
        } catch (WordleDictionaryException e) {
            System.err.println("Ошибка словаря: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Непредвиденная ошибка: " + e.getMessage());
        }
    }
}