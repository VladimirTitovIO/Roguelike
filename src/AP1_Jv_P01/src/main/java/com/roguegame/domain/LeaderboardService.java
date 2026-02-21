package com.roguegame.domain;

import java.util.List;

public interface LeaderboardService {
    List<ScoreEntry> loadLeaderboard();
    void saveScore(ScoreEntry score);

    /** optional helper for debug/tests */
    default void clear() {}
}