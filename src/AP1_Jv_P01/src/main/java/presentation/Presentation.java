package presentation;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;

import java.awt.*;
import java.io.IOException;

public class Presentation {

    public static void main(String[] args) {
        try {
            // Настройка SWING-терминала
            TerminalSize terminalSize = new TerminalSize(100, 35);
            Font font = new Font("Monospaced", Font.BOLD, 16);
            SwingTerminalFontConfiguration fontConfig =
                    SwingTerminalFontConfiguration.newInstance(font);

            DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory()
                    .setInitialTerminalSize(terminalSize)
                    .setTerminalEmulatorFontConfiguration(fontConfig);

            SwingTerminalFrame terminal = (SwingTerminalFrame) terminalFactory.createTerminal();
            terminal.setResizable(true);

            Screen screen = new TerminalScreen(terminal);
            screen.startScreen();

            while (true) {
                // Передаём данные в Screen для отрисовки
                ScreenManager.renderRoom(
                        screen,
                        Controller.getPlayerX(),
                        Controller.getPlayerY(),
                        Controller.isShowingMenu(),
                        Controller.getCurrentMenuType(),
                        Controller.getCurrentMenuItems()
                );

                screen.refresh();

                KeyStroke key = screen.readInput();
                if (key == null) continue;

                if (key.getKeyType() == KeyType.Escape) {
                    break;
                }

                Controller.handleInput(key, screen);

            }

            screen.close();
            terminal.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}