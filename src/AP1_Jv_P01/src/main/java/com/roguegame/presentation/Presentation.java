package com.roguegame.presentation;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.roguegame.domain.*;
import com.roguegame.domain.Character;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

//import static com.roguegame.presentation.Controller.leaderboardService;

public class Presentation {

    public static final int TERMINAL_WIDTH = 140;
    public static final int TERMINAL_HEIGHT = 38;

    public static void main(String[] args) {
        Screen screen = null;
        SwingTerminalFrame terminal = null;

        try {
            // Инициализация ресурсов
            screen = initializeTerminal();
            ScreenManager.renderStartScreen(screen);

            // Запуск игрового цикла
            runGameLoop(screen);

        } catch (IOException e) {
            System.err.println("Ошибка инициализации терминала: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Непредвиденная ошибка в игре: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdownTerminal(screen, terminal);
        }
    }

    // Инициализация терминала и экрана
    private static Screen initializeTerminal() throws IOException {
        TerminalSize terminalSize = new TerminalSize(TERMINAL_WIDTH, TERMINAL_HEIGHT);
        Font font = new Font("Monospaced", Font.PLAIN, 17);
        SwingTerminalFontConfiguration fontConfig =
                SwingTerminalFontConfiguration.newInstance(font);

        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory()
                .setInitialTerminalSize(terminalSize)
                .setTerminalEmulatorFontConfiguration(fontConfig);

        SwingTerminalFrame terminal = (SwingTerminalFrame) terminalFactory.createTerminal();
        terminal.setResizable(true);

        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();

        return screen; // Возвращаем экран
    }

    // Главный игровой цикл
    private static void runGameLoop(Screen screen) throws IOException {
        Controller controller = new Controller();
        Character player = controller.getPlayer();

        while (controller.getWorld().getLevelNumber() < 21) {
            // Рендерим текущее состояние
            switch (controller.getCurrentState()) {
                case START_SCREEN:
                    ScreenManager.renderStartScreen(screen);
                    break;
                case MENU_SCREEN:
                    ScreenManager.renderMenuScreen(screen, controller.getCurrentMenuLine());
                    break;
                case GAME_SCREEN:
                    ScreenManager.renderLevel(
                            screen,
                            controller,
                            Controller.isShowingMenu(),
                            Controller.getCurrentMenuType(),
                            controller.getCurrentMenuItems()
                    );
                    break;
                case SCOREBOARD_SCREEN:
                    //List<ScoreEntry> scores = DataLayer.loadLeaderboard(); // Это должен сделать разработчик А
                    //ScreenManager.scoreboardScreen(screen, scores);
                    //List<ScoreEntry> scores = leaderboardService.loadLeaderboard();
                    //ScreenManager.scoreboardScreen(screen, scores);
                    List<ScoreEntry> scores = new ArrayList<>();
                    scores.add(new ScoreEntry(player.getGold(), controller.getWorld().getLevelNumber(),
                            controller.getPlayer().getEnemiesKilled(), controller.getFoodUsed(),
                            controller.getElixirsUsed(), controller.getScrollsUsed(),
                            controller.getPlayer().getAttacksLanded(), controller.getPlayer().getAttacksMissed(),
                            controller.getMovesMade()));
                    ScreenManager.renderScoreboardScreen(screen, scores);
                    break;
                case DEAD_SCREEN:
                    ScreenManager.renderDefeatScreen(screen);
                    break;
                case ENDGAME_SCREEN:
                    ScreenManager.renderVictoryScreen(screen);
                    break;
            }

            screen.refresh();

            KeyStroke key = screen.readInput();
            if (key == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }

            controller.handleInput(key, screen);
            if (controller.getCurrentState() == Controller.GameState.GAME_SCREEN) {
                controller.enemyTurns();
                if (!controller.getPlayer().isAlive()) {
                    controller.setCurrentState(Controller.GameState.DEAD_SCREEN);
                }
            }
            if (controller.getWorld().getLevelNumber() >= 20) {
                controller.setCurrentState(Controller.GameState.ENDGAME_SCREEN);
            }
        }
    }

    // Закрытие ресурсов
    private static void shutdownTerminal(Screen screen, SwingTerminalFrame terminal) {
        try {
            if (screen != null) {
                screen.close();
            }
            if (terminal != null) {
                terminal.close();
            }
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии терминала: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
