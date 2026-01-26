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
    private String hint;
    private Set<String> useHints = new HashSet<>();

    private Map<Character, LetterInfo> letterInfo = new HashMap<>();
    private List<String> possibleWordsCache;
    private boolean cacheValid = false;
    private final Random random = new Random();

    public WordleGame(WordleDictionary dictionary, PrintWriter logFile) {
        this.dictionary = dictionary;
        this.secretWord = dictionary.getRandomWord();
        this.steps = 6;
        this.isWin = false;

        this.playerWords = new ArrayList<>();
        this.resultsPlay = new ArrayList<>();
        this.logFile = logFile;
    }

    public String makeGuess(String playerWord) throws GameOverException, WordNotFoundInDictionary {

        if (isGameOver()) {
            logFile.println("Попытка хода после окончания игры");
            logFile.flush();

            throw new GameOverException("Игра окончена!");
        }

        if (!isValidWord(playerWord)) {
            logFile.println("Слово " + playerWord + " невалидно");
            logFile.flush();

            throw new WordNotFoundInDictionary("Слово загадано неверно!");
        }

        steps--;
        playerWords.add(playerWord);

        String pattern = compareWords(playerWord);
        resultsPlay.add(pattern);

        updateLetterInfo(playerWord, pattern);
        cacheValid = false;

        if (playerWord.equals(secretWord)) {
            isWin = true;
            logFile.println("Игрок выиграл!");
            logFile.flush();

        }

        logFile.println("Результат: " + pattern + ". Количество попыток: " + steps);
        logFile.flush();

        return pattern;
    }

    private String compareWords(String playerWord) {
        if (playerWord.equals(secretWord)) {
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

    private boolean isValidWord(String playerWord) throws WordNotFoundInDictionary {
        if (playerWord == null || playerWord.isBlank() || playerWord.length() != 5) {
            return false;
        }

        if (!dictionary.contains(playerWord)) {
            throw new WordNotFoundInDictionary("Загаданное слово '" + playerWord + "' не найдено в словаре");
        }

        return true;
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

    public int getSteps() {
        return steps;
    }

    private void updateLetterInfo(String playerWord, String pattern) {
        Map<Character, Integer> greenYellowCount = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            char letter = playerWord.charAt(i);
            char symbol = pattern.charAt(i);

            if (symbol == '+' || symbol == '^') {
                greenYellowCount.put(letter, greenYellowCount.getOrDefault(letter, 0) + 1);
            }
        }

        for (int i = 0; i < 5; i++) {
            char letter = playerWord.charAt(i);
            char symbol = pattern.charAt(i);

            LetterInfo info = letterInfo.computeIfAbsent(letter, k -> new LetterInfo());

            switch (symbol) {
                case '+':
                    info.correctPositions.add(i);
                    info.minCount = Math.max(info.minCount, greenYellowCount.get(letter));
                    break;

                case '^':
                    info.wrongPositions.add(i);
                    info.minCount = Math.max(info.minCount, greenYellowCount.get(letter));
                    break;

                case '-':
                    if (greenYellowCount.containsKey(letter)) {
                        info.maxCount = Math.min(info.maxCount, greenYellowCount.get(letter));
                    } else {
                        info.maxCount = 0;
                    }
                    break;
            }
        }
    }

    private boolean wordMatchConditions(String word) {
        for (Map.Entry<Character, LetterInfo> entry : letterInfo.entrySet()) {
            char letter = entry.getKey();
            LetterInfo info = entry.getValue();

            int count = countLetter(word, letter);

            if (count < info.minCount) {
                return false;
            }
            if (info.maxCount >= 0 && count > info.maxCount) {
                return false;
            }

            for (int pos : info.correctPositions) {
                if (word.charAt(pos) != letter) {
                    return false;
                }
            }

            for (int pos : info.wrongPositions) {
                if (word.charAt(pos) == letter) {
                    return false;
                }
            }
        }

        return true;
    }

    private int countLetter(String word, char letter) {
        int count = 0;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == letter) {
                count++;
            }
        }
        return count;
    }

    public String getHint() {
        if (!cacheValid || possibleWordsCache == null) {
            possibleWordsCache = new ArrayList<>();
            List<String> allWords = dictionary.getWords();

            for (String word : allWords) {
                if (wordMatchConditions(word) && !useHints.contains(word)) {
                    possibleWordsCache.add(word);
                }
            }

            if (possibleWordsCache.isEmpty()) {
                for (String word : allWords) {
                    if (wordMatchConditions(word)) {
                        possibleWordsCache.add(word);
                    }
                }
            }

            cacheValid = true;
        }

        if (possibleWordsCache.isEmpty()) {
            logFile.println("Нет подходящих слов для подсказки");
            logFile.flush();

            return null;
        }

        String hint = possibleWordsCache.get(random.nextInt(possibleWordsCache.size()));
        useHints.add(hint);

        logFile.println("Дана подсказка: " + hint);
        logFile.flush();

        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }
}