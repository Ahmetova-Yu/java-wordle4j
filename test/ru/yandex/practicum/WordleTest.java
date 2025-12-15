package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {
    private WordleDictionary dictionary;
    private WordleGame game;
    private StringWriter logWriter;
    private PrintWriter log;

    @BeforeEach
    void setUp() {
        List<String> words = Arrays.asList("конец", "экран", "рояль", "слон", "кот", "", null, "привет", "тесто", "слово");
        dictionary = new WordleDictionary(words);
    }

    @Test
    void testContainsWord() {
        assertTrue(dictionary.contains("конец"));
        assertTrue(dictionary.contains("КОНЕЦ"));
        assertTrue(dictionary.contains("кОнЕц"));
        assertTrue(dictionary.contains("тесто"));
    }

    @Test
    void testContainsWordWithYo() {
        List<String> wordsWithYo = Arrays.asList("бельё", "актёр");
        WordleDictionary dictWithYo = new WordleDictionary(wordsWithYo);

        assertTrue(dictWithYo.contains("белье"));
        assertTrue(dictWithYo.contains("актер"));
    }

    @Test
    void testDoesNotContainInvalidWords() {
        assertFalse(dictionary.contains("привет"));
        assertFalse(dictionary.contains("кот"));
        assertFalse(dictionary.contains("слон"));
        assertFalse(dictionary.contains(""));
        assertFalse(dictionary.contains(null));
    }

    @Test
    void testEmptyWordListThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new WordleDictionary(Arrays.asList("", "привет", "кот"));
        });
    }

    @Test
    void testNullWordListThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new WordleDictionary(null);
        });
    }

    @BeforeEach
    void setUpWordleGameTest() {
        List<String> words = Arrays.asList("слово", "тесто", "треть", "пятно", "краска", "буква", "ответ", "загадка", "спина", "огонь");
        dictionary = new WordleDictionary(words);
        logWriter = new StringWriter();
        log = new PrintWriter(logWriter);

        try {
            game = new WordleGame(dictionary, log);
            java.lang.reflect.Field secretWordField = WordleGame.class.getDeclaredField("secretWord");
            secretWordField.setAccessible(true);
            secretWordField.set(game, "слово");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testMakeGuessCorrectWord() throws GameOverException, WordNotFoundInDictionary {
        String result = game.makeGuess("слово");
        assertEquals("+++++", result);
        assertTrue(game.isWin());
        assertTrue(game.isGameOver());
        assertEquals(5, game.getSteps());
    }

    @Test
    void testMakeGuessPartiallyCorrect() throws GameOverException, WordNotFoundInDictionary {
        String result = game.makeGuess("спина");
        assertEquals("+----", result);
        assertFalse(game.isWin());
        assertFalse(game.isGameOver());
        assertEquals(5, game.getSteps());
    }

    @Test
    void testMakeGuessWithWrongPositions() throws GameOverException, WordNotFoundInDictionary {
        String result = game.makeGuess("огонь");
        assertEquals("^-+--", result);
    }

    @Test
    void testMakeGuessWordNotInDictionary() {
        assertThrows(WordNotFoundInDictionary.class, () -> {
            game.makeGuess("олово");
        });
    }

    @Test
    void testMakeGuessAfterGameOver() throws GameOverException, WordNotFoundInDictionary {
        game.makeGuess("слово");

        assertThrows(GameOverException.class, () -> {
            game.makeGuess("тесто");
        });
    }

    @Test
    void testSixAttemptsGameOver() throws GameOverException, WordNotFoundInDictionary {
        for (int i = 0; i < 6; i++) {
            game.makeGuess("тесто");
        }

        assertTrue(game.isGameOver());
        assertFalse(game.isWin());
        assertEquals(0, game.getSteps());
    }

    @Test
    void testGetHintReturnsWordFromDictionary() {
        try {
            java.lang.reflect.Field secretWordField = WordleGame.class.getDeclaredField("secretWord");
            secretWordField.setAccessible(true);
            secretWordField.set(game, "слово");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String hint = game.getHint();
        assertNotNull(hint);
        assertEquals(5, hint.length());
        assertTrue(hint.matches("[а-я]+"));
    }

    @TempDir
    Path tempDir;

    @Test
    void testLoadDictionaryFromValidFile() throws IOException {
        File tempFile = tempDir.resolve("test_words.txt").toFile();
        try (PrintWriter writer = new PrintWriter(tempFile, StandardCharsets.UTF_8)) {
            writer.println("слово");
            writer.println("тесто");
            writer.println("треть");
            writer.println("пятно");
            writer.println("буква");
            writer.println("");
            writer.println("   ");
            writer.println("КРАСКА");
            writer.println("бельё");
        }

        StringWriter logWriter = new StringWriter();
        PrintWriter log = new PrintWriter(logWriter);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(tempFile.getAbsolutePath(), log);
        WordleDictionary dictionary = loader.loadDictionary();

        assertNotNull(dictionary);
        assertTrue(dictionary.contains("слово"));
        assertTrue(dictionary.contains("слово"));
        assertTrue(dictionary.contains("белье"));
        assertTrue(dictionary.contains("бельё"));
        assertFalse(dictionary.contains(""));
        assertFalse(dictionary.contains("   "));
    }

    @Test
    void testLoadDictionaryEmptyFile() throws IOException {
        File tempFile = tempDir.resolve("empty.txt").toFile();
        tempFile.createNewFile(); // Создаем пустой файл

        StringWriter logWriter = new StringWriter();
        PrintWriter log = new PrintWriter(logWriter);

        WordleDictionaryLoader loader = new WordleDictionaryLoader(tempFile.getAbsolutePath(), log);

        assertThrows(IOException.class, loader::loadDictionary);
    }
}
