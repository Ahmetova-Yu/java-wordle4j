package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.*;

public class WordleGame {

    private int steps;
    private WordleDictionary dictionary;
    private List<String> playerWords;
    private List<String> resultsPlay;
    private boolean isWin;
    private String secretWord;
    private PrintWriter logFile;

    public WordleGame(WordleDictionary dictionary, PrintWriter logFile) {
        this.dictionary = dictionary;
        this.secretWord = dictionary.getRandomWord();
        steps = 6;

        this.playerWords = new ArrayList<>();
        this.resultsPlay = new ArrayList<>();

        logFile.println("Игра началась. Загаданное слово: " + secretWord);
        logFile.flush();
    }

    public String makeGuess(String playerWord) throws InvalidWordException, GameOverException {
        logFile.println("Игрок ввел слово: " + playerWord);

        if (isGameOver()) {
            logFile.println("Попытка хода после окончания игры");
            throw new GameOverException("Игра окончена!");
        }

        if (!isValidWord(playerWord)) {
            logFile.println("Слово " + playerWord + " невалидно");
            throw new InvalidWordException("Слово загадано неверно!");
        }

        steps--;
        playerWords.add(playerWord);
        String pattern = compareWords(playerWord);
        resultsPlay.add(pattern);

        logFile.println("Результат: " + pattern + ". Количество попыток: " + steps);
        return pattern;
    }

    private String compareWords(String playerWord) {
        if (playerWord.equals(secretWord)) {
            isWin = true;
            return "+++++";
        }

        char[] res = new char[5];
        char[] secretChars = secretWord.toCharArray();
        char[] playerChars = playerWord.toCharArray();

        int len = playerWord.length();
        for (int i = 0; i < len; i++) {
            if (playerChars[i] == secretChars[i]) {
                res[i] = '+';
                secretChars[i] = ' ';
            }
        }

        for (int i = 0; i < len; i++) {
            if (res[i] == '+') continue;

            boolean isFound = false;

            for (int j = 0; j < len; j++) {
                if (secretChars[j] == playerChars[i]) {
                    res[i] = '^';
                    secretChars[j] = ' ';
                    isFound = true;
                    break;
                }
            }

            if (!isFound) {
                res[i] = '-';
            }
        }

        return new String(res);
    }

    private boolean isValidWord(String playerWord) {
        return playerWord != null
                && !playerWord.isBlank()
                && playerWord.length() == 5
                && dictionary.contains(playerWord);
    }

    public boolean isGameOver() {
        return steps == 0 || isWin;
    }

    public boolean isWin() {
        return isWin;
    }

    public List<String> getPlayerWords() {
        return new ArrayList<>(playerWords);
    }

    public List<String> getResultsPlay() {
        return new ArrayList<>(resultsPlay);
    }

    public String getSecretWord() {
        return secretWord;
    }

    public void logGameEnd() {
        if (isWin) {
            logFile.println("Игра окончена. Игрок выиграл!");
        } else if (steps == 0) {
            logFile.println("Игра окончена. Игрок проиграл.");
        }
        logFile.flush();
    }

    public int getStep() {
        return steps;
    }
}
