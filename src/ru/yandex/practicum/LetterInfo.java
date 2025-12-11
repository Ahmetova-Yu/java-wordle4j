package ru.yandex.practicum;

import java.util.HashSet;
import java.util.Set;

public class LetterInfo {
    int minCount = 0;
    int maxCount = 5;
    Set<Integer> correctPositions = new HashSet<>();
    Set<Integer> wrongPositions = new HashSet<>();

    @Override
    public String toString() {
        return "min=" + minCount + ", max=" + maxCount +
                ", correct=" + correctPositions + ", wrong=" + wrongPositions;
    }
}