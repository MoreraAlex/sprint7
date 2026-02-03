package ru.yandex.practicum.exceptions;

public class GameOverException extends WordleGameException {
    public GameOverException(String message) {
        super(message);
    }
}
