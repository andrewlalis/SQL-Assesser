package nl.andrewlalis;

import nl.andrewlalis.util.FileLoader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import java.io.IOException;

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
    private JButton clearTestingButton;
    private JButton clearTemplateButton;

    static final int OUTPUT_GENERAL = 0;
    static final int OUTPUT_TEMPLATE = 1;
    static final int OUTPUT_TESTING = 2;

    static final String DB_TEMPLATE = "sql_assess_template";
    static final String DB_TESTING = "sql_assess_testing";

    private int outputChannel;
    private int outputIndent;

    Window(String applicationName) {
        super(applicationName);

        // Setup autoscrolling on text areas.
        DefaultCaret caret = (DefaultCaret) this.outputTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        // Setup default SQL values.
        this.fillDefaultSQL();

        this.setOutputChannel(OUTPUT_GENERAL);

        this.setContentPane(mainPanel);

        executeButton.addActionListener(actionEvent -> this.executeSQL());

        clearOutputButton.addActionListener(actionEvent -> {
            this.templateOutputTextArea.setText(null);
            this.testingOutputTextArea.setText(null);
            this.outputTextArea.setText(null);
        });

        loadInitializationFromFileButton.addActionListener(actionEvent -> this.fillSQLFromFileChooser(this.initializationTextArea));
        loadTemplateFromFileButton.addActionListener(actionEvent -> this.fillSQLFromFileChooser(this.templateTextArea));
        loadTestingFromFileButton.addActionListener(actionEvent -> this.fillSQLFromFileChooser(this.testingTextArea));
        clearTemplateButton.addActionListener(actionEvent -> this.templateTextArea.setText(null));
        clearTestingButton.addActionListener(actionEvent -> this.testingTextArea.setText(null));
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

    /**
     * Fills the input elements from the SQL packaged with this application.
     */
    private void fillDefaultSQL() {
        try {
            this.initializationTextArea.setText(FileLoader.readResource("initialization.sql"));
            this.templateTextArea.setText(FileLoader.readResource("template.sql"));
            this.testingTextArea.setText(FileLoader.readResource("example_test.sql"));
        } catch (IOException e) {
            this.appendOutput("Could not load default SQL resources.");
            e.printStackTrace();
        }
    }

    private void fillSQLFromFileChooser(JTextArea textArea) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("SQL", "sql"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            textArea.setText(FileLoader.readFile(fileChooser.getSelectedFile()));
        }
    }
}
