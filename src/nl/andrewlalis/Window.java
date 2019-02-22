package nl.andrewlalis;

import javax.swing.*;
import javax.xml.crypto.Data;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Window extends JFrame {
    private JPanel mainPanel;
    private JPanel inputPanel;
    private JPanel outputPanel;
    private JTextArea outputTextArea;
    private JButton executeButton;
    private JTextArea templateTextArea;
    private JTextArea testingTextArea;
    private JTextField hostTextField;
    private JTextField portTextField;
    private JTextField userTextField;
    private JTextField passwordTextField;
    private JScrollPane outputScrollPane;
    private JTextArea testingOutputTextArea;
    private JTextArea templateOutputTextArea;
    private JTextArea initializationTextArea;
    private JPanel initializationPanel;
    private JButton clearOutputButton;
    private JButton loadTemplateFromFileButton;
    private JButton loadTestingFromFileButton;
    private JButton loadInitializationFromFileButton;

    public static final int OUTPUT_GENERAL = 0;
    public static final int OUTPUT_TEMPLATE = 1;
    public static final int OUTPUT_TESTING = 2;

    public static final String DB_TEMPLATE = "sql_assess_template";
    public static final String DB_TESTING = "sql_assess_testing";

    private int outputChannel;
    private int outputIndent;

    public Window(String applicationName) {
        super(applicationName);

        this.setOutputChannel(OUTPUT_GENERAL);

        this.setContentPane(mainPanel);

        executeButton.addActionListener(actionEvent -> {
            this.executeSQL();
        });

        clearOutputButton.addActionListener(actionEvent -> {
            this.templateOutputTextArea.setText(null);
            this.testingOutputTextArea.setText(null);
            this.outputTextArea.setText(null);
        });

        loadInitializationFromFileButton.addActionListener(actionEvent -> {

        });
    }

    /**
     * Executes the SQL in the two text areas, and provides output.
     */
    private void executeSQL() {
        this.setOutputChannel(OUTPUT_GENERAL);
        String host = this.hostTextField.getText();
        int port = Integer.parseInt(this.portTextField.getText());
        String user = this.userTextField.getText();
        String password = this.passwordTextField.getText();
        String initialization = this.initializationTextArea.getText();

        DatabaseHelper helper = new DatabaseHelper(host, port, user, password, this);
        helper.executeSQLComparison(initialization, this.templateTextArea.getText(), this.testingTextArea.getText());
    }

    int getOutputChannel() {
        return this.outputChannel;
    }

    void setOutputChannel(int channel) {
        this.outputChannel = channel;
    }

    void indentOutput() {
        this.outputIndent++;
    }

    void unindentOutput() {
        this.outputIndent--;
    }

    /**
     * Adds some text to the current output channel, followed by a new line.
     * @param text The text to append.
     */
    void appendOutput(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.outputIndent; i++) {
            sb.append('\t');
        }
        String tabs = sb.toString();
        StringBuilder resultSb = new StringBuilder();
        for (String line : text.split("\n")) {
            resultSb.append(tabs).append(line).append('\n');
        }
        String result = resultSb.toString();
        switch (this.outputChannel) {
            case OUTPUT_GENERAL:
                this.outputTextArea.append(result);
                break;

            case OUTPUT_TEMPLATE:
                this.templateOutputTextArea.append(result);
                break;

            case OUTPUT_TESTING:
                this.testingOutputTextArea.append(result);
                break;
        }
    }
}
