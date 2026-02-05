package presentation;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import domain.ScoreEntry;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

//import static presentation.Controller.leaderboardService;

public class Presentation {
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
        TerminalSize terminalSize = new TerminalSize(140, 40);
        Font font = new Font("Monospaced", Font.BOLD, 15);
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
        while (true) {
            // Рендерим текущее состояние
            switch (Controller.getCurrentState()) {
                case START_SCREEN:
                    ScreenManager.renderStartScreen(screen);
                    break;
                case MENU_SCREEN:
                    ScreenManager.menuScreen(screen, Controller.getCurrentMenuLine());
                    break;
                case GAME_SCREEN:
                    ScreenManager.renderRoom(
                            screen,
                            Controller.getPlayerX(),
                            Controller.getPlayerY(),
                            Controller.isShowingMenu(),
                            Controller.getCurrentMenuType(),
                            Controller.getCurrentMenuItems()
                    );
                    break;
                case SCOREBOARD_SCREEN:
                    //List<ScoreEntry> scores = DataLayer.loadLeaderboard(); // Это должен сделать разработчик А
                    //ScreenManager.scoreboardScreen(screen, scores);
                    //List<ScoreEntry> scores = leaderboardService.loadLeaderboard();
                    //ScreenManager.scoreboardScreen(screen, scores);
                    List<ScoreEntry> scores = new ArrayList<>();
                    scores.add(new ScoreEntry(123, 7, 15, 3, 2, 1, 45, 10, 200));
                    scores.add(new ScoreEntry(89, 5, 10, 2, 1, 0, 30, 5, 150));
                    ScreenManager.scoreboardScreen(screen, scores);
                    break;
                case DEAD_SCREEN:
                    ScreenManager.deadScreen(screen);
                    break;
                case ENDGAME_SCREEN:
                    ScreenManager.endgameScreen(screen);
                    break;
            }

            screen.refresh();

            KeyStroke key = screen.readInput();
            if (key == null) {
                try {
                    Thread.sleep(50); // Небольшая пауза для снижения нагрузки на CPU
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break; // Прерываем цикл при прерывании потока
                }
                continue;
            }

            Controller.handleInput(key, screen);
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
