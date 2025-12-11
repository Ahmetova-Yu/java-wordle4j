package ru.yandex.practicum;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Scanner;

public class Wordle {
    public static void main(String[] args) throws IOException {

        System.out.println("=== Игра Wordle ===");
        System.out.println("Правила:");
        System.out.println("- Угадайте слово из 5 русских букв");
        System.out.println("- У вас 6 попыток");
        System.out.println("- + буква на правильном месте");
        System.out.println("- ^ буква есть в слове, но не на этом месте");
        System.out.println("- - буквы нет в слове");
        System.out.println("- Для подсказки нажмите Enter");
        System.out.println("===================\n");

        try (PrintWriter log = new PrintWriter(new FileWriter("wordle.log", StandardCharsets.UTF_8))) {
            log.println("Игра началась! " + new Date());
            Scanner sc = new Scanner(System.in);

            try {
                log.println("Загрузка словаря");
                WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
                WordleDictionary dictionary = loader.loadDictionary();
                log.println("Словарь загружен. Слов: " + dictionary.getWords().size());

                WordleGame game = new WordleGame(dictionary, log);
                log.println("Игра создана. Загаданное слово: " + game.getSecretWord());

                System.out.println("Игра началась!");

                while (!game.isGameOver()) {
                    System.out.print("-- ");
                    String word = sc.nextLine();

                    String normalizeWord = normalizeWord(word);

                    if (normalizeWord.isEmpty()) {
                        log.println("Игрок запросил подсказку");
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
                        log.println("Некорректный ввод");
                        continue;
                    }

                    try {
                        log.println("Игрок ввел: " + normalizeWord);
                        String result = game.makeGuess(normalizeWord);

                        System.out.println(result);

                        if (game.isWin()) {
                            System.out.println("Вы угадали слово!");
                            log.println("Игрок выиграл! Слово: " + normalizeWord);
                            break;
                        }

                        int steps = game.getSteps();
                        System.out.println("Осталось " + steps + " попыток");

                    } catch (GameOverException e) {
                        System.out.println(e.getMessage());
                        System.out.println("Попытка хода после завершения игры");
                        break;

                    } catch (WordNotFoundInDictionary e) {
                        System.out.println(e.getMessage());
                        System.out.println("Попробуйте другое слово");
                        log.println("Слова нет в словаре: " + normalizeWord);

                    }
                }

                System.out.println("\n====================");
                if (game.isWin()) {
                    System.out.println("Вы выиграли!");
                    System.out.println("Слово угадано с " + (6 - game.getSteps()) + " попыток");
                } else {
                    System.out.println("Вы проиграли");
                }

                System.out.println("Загаданное слово: " + game.getSecretWord());

//                game.logGameEnd();

                sc.close();
                log.println("Результат: " + (game.isWin() ? "победа" : "поражение"));
                log.println("Загаданное слово: " + game.getSecretWord());
                log.flush();
            } finally {
                if (sc != null) {
                    sc.close();
                }
                if (log != null) {
                    log.close();
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка создания лог файла " + e.getMessage());
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

//    private static void printGameHistory(WordleGame game) {
//        var playerWords = game.getPlayerWords();
//        var results = game.getResultsPlay();
//
//        if (playerWords.isEmpty()) {
//            System.out.println("Ходов не было");
//            return;
//        }
//
//        for (int i = 0; i < playerWords.size(); i++) {
//            System.out.printf("%d.%s → %s%n", i + 1, playerWords.get(i), results.get(i));
//        }
//    }
}
