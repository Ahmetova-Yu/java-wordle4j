package ru.yandex.practicum;

class EmptyDictionaryException extends Exception {
    public EmptyDictionaryException(String message) {
        super(message);
    }

    public EmptyDictionaryException(String message, Throwable cause) {
        super(message, cause);
    }
}
