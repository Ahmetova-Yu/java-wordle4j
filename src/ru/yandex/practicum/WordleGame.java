package ru.yandex.practicum;

import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

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
    private Map<Integer, Character> correctPosition = new HashMap<>();
    private Map<Integer, Set<Character>> wrongPosition = new HashMap<>();
    private Set<Character> containsLetterInWord = new HashSet<>();
    private Set<Character> notContainsLetterInWord = new HashSet<>();
    private Map<Character, Integer> minLetterCounts = new HashMap<>();
    private Map<Character, Integer> maxLetterCounts = new HashMap<>();

    public WordleGame(WordleDictionary dictionary, PrintWriter logFile) {
        this.dictionary = dictionary;
        this.secretWord = dictionary.getRandomWord();
        steps = 6;

        this.playerWords = new ArrayList<>();
        this.resultsPlay = new ArrayList<>();
        this.logFile = logFile;

        for (int i = 0; i < 5; i++) {
            wrongPosition.put(i, new HashSet<>());
        }

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

        updateLetterInfo(playerWord, pattern);

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

    public int getSteps() {
        return steps;
    }

    private void updateLetterInfo(String playerWord, String pattern) {
        Map<Character, Integer> countsLetterSecret = new HashMap<>();

        for (int i = 0; i < secretWord.length(); i++) {
            char letter = playerWord.charAt(i);
            char symbol = pattern.charAt(i);

            if (symbol == '+' || symbol == '^') {
                countsLetterSecret.put(letter, countsLetterSecret.getOrDefault(letter, 0) + 1);
            }
        }

        for (int i = 0; i < secretWord.length(); i++) {
            char letter = playerWord.charAt(i);
            char symbol = pattern.charAt(i);

            switch (symbol) {
                case '+':
                    correctPosition.put(i, letter);
                    containsLetterInWord.add(letter);

                    for (int j = 0; j < secretWord.length(); j++) {
                        if (j != i) {
                            wrongPosition.computeIfAbsent(j, k -> new HashSet<>()).add(letter);
                        }
                    }

                    break;

                case '^':
                    containsLetterInWord.add(letter);
                    wrongPosition.computeIfAbsent(i, k -> new HashSet<>()).add(letter);

                    break;

                case '-':
                    if (countsLetterSecret.containsKey(letter)) {
                        maxLetterCounts.put(letter, countsLetterSecret.get(letter));
                    } else {
                        notContainsLetterInWord.add(letter);
                    }

                    break;
            }
        }

        for (Map.Entry<Character, Integer> entry : countsLetterSecret.entrySet()) {
            char letter = entry.getKey();
            int k = entry.getValue();

            minLetterCounts.put(letter, Math.max(minLetterCounts.getOrDefault(letter, 0), k));
        }
    }

    private boolean wordMatchConditions(String word) {
        for (Map.Entry<Integer, Character> entry : correctPosition.entrySet()) {
            int pos = entry.getKey();
            char exp = entry.getValue();

            if (word.charAt(pos) != exp) {
                return false;
            }
        }

        // буквы не должно быть в слове
        for (char letter : notContainsLetterInWord) {
            if (word.indexOf(letter) != -1) {
                return false;
            }
        }

        // буква должна быть в слове
        for (char letter : containsLetterInWord) {
            if (word.indexOf(letter) == -1) {
                return false;
            }
        }

        // проверка позиций для букв ^
        for (Map.Entry<Integer, Set<Character>> entry : wrongPosition.entrySet()) {
            int pos = entry.getKey();
            Set<Character> wrongLetters = entry.getValue();

            if (wrongLetters.contains(word.charAt(pos))) {
                return false;
            }
        }

        for (char letter = 'а'; letter <= 'я'; letter++) {
            int countInWord = countLetter(word, letter);

            Integer minCount = minLetterCounts.get(letter);
            if (minCount != null && countInWord < minCount) {
                return false;
            }

            Integer maxCount = maxLetterCounts.get(letter);
            if (maxCount != null && countInWord > maxCount) {
                return false;
            }
        }

        return true;
    }

    private int countLetter(String word, char letter) {
        int k = 0;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == letter) {
                k++;
            }
        }

        return k;
    }

    public String getHint() {
        List<String> words = dictionary.getWords();

        List<String> possibleWords = new ArrayList<>();
        for (String word : words) {
            if (wordMatchConditions(word) && !useHints.contains(word)) {
                possibleWords.add(word);
            }
        }

        if (possibleWords.isEmpty()) {
            logFile.println("Нет подсказок");
            return null;
        }

        Random random = new Random();
        String hint = possibleWords.get(random.nextInt(possibleWords.size()));

        useHints.add(hint);

        logFile.println("Использована подсказка: " + hint);

        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }
}
