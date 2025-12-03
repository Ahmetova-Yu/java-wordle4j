package ru.yandex.practicum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/*
этот класс содержит в себе всю рутину по работе с файлами словарей и с кодировками
    ему нужны методы по загрузке списка слов из файла по имени файла
    на выходе должен быть класс WordleDictionary
 */
public class WordleDictionaryLoader {
    private String filename;

    public WordleDictionaryLoader() throws IOException {
        this("words_ru.txt");
    }

    public WordleDictionaryLoader(String filename) {
        this.filename = filename;
    }

    public WordleDictionary loadDictionary() throws IOException {
        List<String> words = readWordsFromFile();
        return new WordleDictionary(words);
    }

    private List<String> readWordsFromFile() throws IOException {
        List<String> words = new ArrayList<>();

        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotFoundException("Файл не найден: " + filename);
        }

        try (BufferedReader bf = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = bf.readLine()) != null) {
                String word = rebuildWord(line);
                if (isValid(word)) {
                    words.add(word);
                }
            }
        }

        return words;
    }

    private String rebuildWord(String word) {
        word = word.trim()
                .replace("ё", "е")
                .toLowerCase();

        return word;
    }

    private boolean isValid(String word) {
        return word.length() == 5 && word.matches("[а-я]+");
    }
}
