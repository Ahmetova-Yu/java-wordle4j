package ru.yandex.practicum;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Scanner;

public class Wordle {
    public static void main(String[] args) {
        System.out.println("=== Игра Wordle ===");
        System.out.println("Правила:");
        System.out.println("- Угадайте слово из 5 русских букв");
        System.out.println("- У вас 6 попыток");
        System.out.println("- + буква на правильном месте");
        System.out.println("- ^ буква есть в слове, но не на этом месте");
        System.out.println("- - буквы нет в слове");
        System.out.println("- Для подсказки нажмите Enter");
        System.out.println("===================\n");

        try (PrintWriter log = new PrintWriter(new FileWriter("wordle.log", StandardCharsets.UTF_8, true))) {
            log.println("Игра началась! " + new Date());

            WordleGame game = null;
            Scanner sc = null;

            try {
                sc = new Scanner(System.in, StandardCharsets.UTF_8);

                log.println("Загрузка словаря");
                log.flush();

                WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
                WordleDictionary dictionary = loader.loadDictionary();
                log.println("Словарь загружен. Слов: " + dictionary.getWords().size());
                log.flush();

                game = new WordleGame(dictionary, log);
                log.println("Игра создана. Загаданное слово: " + game.getSecretWord());
                log.flush();

                System.out.println("Игра началась!");

                while (!game.isGameOver()) {
                    System.out.print("-- ");
                    String word = sc.nextLine();

                    String normalizeWord = normalizeWord(word);

                    if (normalizeWord.isEmpty()) {
                        log.println("Игрок запросил подсказку");
                        log.flush();

                        String hint = game.getHint();
                        if (hint != null) {
                            System.out.println("Подсказка: " + hint);
                        } else {
                            System.out.println("Нет доступных подсказок");
                        }
                        continue;
                    }

                    if (!isValid(normalizeWord)) {
                        System.out.println("Слово должно быть из 5 русских букв");
                        log.println("Некорректный ввод: " + word);
                        log.flush();

                        continue;
                    }

                    try {
                        log.println("Игрок ввел: " + normalizeWord);
                        log.flush();

                        String result = game.makeGuess(normalizeWord);

                        System.out.println(result);

                        if (game.isWin()) {
                            System.out.println("Вы угадали слово!");
                            log.println("Игрок выиграл! Слово: " + normalizeWord);
                            log.flush();

                            break;
                        }

                        int steps = game.getSteps();
                        System.out.println("Осталось " + steps + " попыток");

                    } catch (GameOverException e) {
                        System.out.println(e.getMessage());
                        System.out.println("Попытка хода после завершения игры");
                        log.println("GameOverException: " + e.getMessage());
                        log.flush();

                        break;
                    } catch (WordNotFoundInDictionary e) {
                        System.out.println(e.getMessage());
                        System.out.println("Попробуйте другое слово");
                        log.println("Слова нет в словаре: " + normalizeWord);
                        log.flush();

                    }
                }

                System.out.println("\n====================");
                if (game != null && game.isWin()) {
                    System.out.println("Вы выиграли!");
                    System.out.println("Слово угадано с " + (6 - game.getSteps()) + " попыток");
                    log.println("Результат: победа");
                    log.flush();

                } else {
                    System.out.println("Вы проиграли");
                    log.println("Результат: поражение");
                    log.flush();

                }

                if (game != null) {
                    System.out.println("Загаданное слово: " + game.getSecretWord());
//                    log.println("Загаданное слово: " + game.getSecretWord());
//                    log.flush();

                }

            } catch (Exception e) {
                log.println("Произошла ошибка : " + e.getMessage());
                e.printStackTrace(log);
                log.flush();

                System.out.println("Произошла внутренняя ошибка. Игра будет завершена.");

            } finally {
                try {
                    if (sc != null) {
                        sc.close();
                    }
                } catch (Exception e) {
                    log.println("Ошибка при закрытии Scanner: " + e.getMessage());
                }

                log.println("Игра завершена: " + new Date());
                log.println("=====================================\n");
                log.flush();
            }

        } catch (IOException e) {
            System.out.println("Ошибка создания лог файла. Игра не может быть запущена.");
            System.out.println("Проверьте права на запись в текущей директории.");
        }

        System.out.println("Спасибо за игру!");
    }

    private static String normalizeWord(String word) {
        if (word == null) return "";

        return word.trim()
                .toLowerCase()
                .replace('ё', 'е');
    }

    private static boolean isValid(String word) {
        return word.length() == 5 && word.matches("[а-я]+");
    }
}