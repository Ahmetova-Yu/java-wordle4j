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
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }
        if (logFile == null) {
            throw new IllegalArgumentException("Лог файл не может быть null");
        }

        this.filename = filename;
        this.logFile = logFile;
    }

    public WordleDictionary loadDictionary() throws IOException, EmptyDictionaryException {
        logFile.println("Начало загрузки словаря из файла: '" + filename + "'");

        List<String> words;
        try {
            words = readWordsFromFile();
        } catch (FileNotFoundException e) {
            String errorMessage = "Файл словаря не найден: " + filename;
            logFile.println("Проверьте, что файл находится в правильной директории");
            throw new FileNotFoundException(errorMessage);
        }

        if (words.isEmpty()) {
            String errorMessage = "Словарь пуст. Файл '" + filename + "' не содержит допустимых слов";
            throw new EmptyDictionaryException(errorMessage);
        }

        logFile.println("Файл '" + filename + "' успешно обработан. Загружено слов: " + words.size());
        logFile.flush();

        return new WordleDictionary(words);
    }

    private List<String> readWordsFromFile() throws IOException {
        List<String> words = new ArrayList<>();

        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotFoundException("Файл не найден: " + filename);
        }

        if (!file.canRead()) {
            throw new IOException("Нет прав на чтение файла: " + filename);
        }

        try (BufferedReader bf = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = bf.readLine()) != null) {
                String word = line.trim();
                if (!word.isEmpty()) {
                    word = word.toLowerCase().replace('ё', 'е');
                    if (word.length() == 5 && word.matches("[а-я]+")) {
                        words.add(word);
                    }
                }
            }
        }

        return words;
    }
}