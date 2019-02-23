package nl.andrewlalis;

import javax.swing.*;

public class Main {

    private static final String APPLICATION_NAME = "SQL-Assesser";

    public static void main(String[] args) {
        Window window = new Window(APPLICATION_NAME);
        window.pack();
        window.setSize(1000, 800);
        window.setVisible(true);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

}
