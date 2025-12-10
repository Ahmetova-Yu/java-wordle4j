package ru.yandex.practicum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {

    private String filename;
    private PrintWriter logFile;

    public WordleDictionaryLoader(PrintWriter logFile) throws IOException {
        this("words_ru.txt", logFile);
    }

    public WordleDictionaryLoader(String filename, PrintWriter logFile) {
        this.filename = filename;
        this.logFile = logFile;
    }

    public WordleDictionary loadDictionary() throws IOException {
        List<String> words = readWordsFromFile();

        logFile.println("Файл '" + filename + "' считан");
        logFile.flush();

        return new WordleDictionary(words);
    }

    private List<String> readWordsFromFile() throws IOException {
        List<String> words = new ArrayList<>();

        try (BufferedReader bf = new BufferedReader(
                new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {

            String line;
            while ((line = bf.readLine()) != null) {
                String word = line.trim();
                if (!word.isEmpty()) {
                    words.add(word);
                }
            }
        }

        if (words.isEmpty()) {
            throw new FileNotFoundException("Файл не найден: " + filename);
        }

        return words;
    }

//    private List<String> readWordsFromFile() throws IOException {
//        List<String> words = new ArrayList<>();
//
//        File file = new File(filename);
//        if (!file.exists()) {
//            throw new FileNotFoundException("Файл не найден: " + filename);
//        }
//
//        try (BufferedReader bf = new BufferedReader(
//                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
//
//            String line;
//            while ((line = bf.readLine()) != null) {
//                String word = rebuildWord(line);
//                if (isValid(word)) {
//                    words.add(word);
//                }
//            }
//        }
//
//        return words;
//    }

//    private String rebuildWord(String word) {
//        word = word.trim()
//                .replace("ё", "е")
//                .toLowerCase();
//
//        return word;
//    }
//
//    private boolean isValid(String word) {
//        return word.length() == 5 && word.matches("[а-я]+");
//    }
}
