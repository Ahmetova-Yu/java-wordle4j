package ru.yandex.practicum;

import java.util.*;

public class WordleDictionary {

    private List<String> words;
    private Set<String> setWords;

    public WordleDictionary(List<String> words) {
        Set<String> uniqueWords = new HashSet<>();

        if (words == null) {
            throw new IllegalArgumentException("Список слов не должен быть пустым");
        }

        for (String word : words) {
            if (word == null) continue;

            String normWord = normalizeWord(word);

            if (isValid(normWord)) {
                uniqueWords.add(normWord);
            }
        }

        if (uniqueWords.isEmpty()) {
            throw new IllegalArgumentException("Нет валидных 5-х русских слов");
        }

        this.words = new ArrayList<>(uniqueWords);
        this.setWords = new HashSet<>(uniqueWords);
    }

    public boolean contains(String word) {
        if (word == null) return false;

        String normWord = normalizeWord(word);
        return setWords.contains(normWord);
    }

    public String getRandomWord() {
        Random random = new Random();
        return words.get(random.nextInt(words.size()));
    }

    public List<String> getWords() {
        return new ArrayList<>(words);
    }

    private String normalizeWord(String word) {
        word = word.trim()
                .replace("ё", "е")
                .toLowerCase();

        return word;
    }

    private boolean isValid(String word) {
        return word.length() == 5 && word.matches("[а-я]+");
    }
}
