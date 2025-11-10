package org.alexander.gui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A dialog that displays a summary of database changes (additions and deletions)
 * and prompts the user to save, discard, or cancel.
 */
public class ChangesSummaryDialog extends JDialog {
    private int result = JOptionPane.CANCEL_OPTION; // Default to cancel

    public ChangesSummaryDialog(Window owner, List<String> additions, List<String> deletions) {
        super(owner, "Summary of Changes", ModalityType.APPLICATION_MODAL);
        initComponents(additions, deletions);
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents(List<String> additions, List<String> deletions) {
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        JTextArea summaryArea = new JTextArea(15, 50);
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        StringBuilder summaryText = new StringBuilder();
        if (!additions.isEmpty()) {
            summaryText.append("--- Items Added ---\n");
            additions.forEach(s -> summaryText.append(" + ").append(s).append("\n"));
            summaryText.append("\n");
        }
        if (!deletions.isEmpty()) {
            summaryText.append("--- Items Removed ---\n");
            deletions.forEach(s -> summaryText.append(" - ").append(s).append("\n"));
        }
        summaryArea.setText(summaryText.toString());
        summaryArea.setCaretPosition(0); // Scroll to top

        JScrollPane scrollPane = new JScrollPane(summaryArea);

        JLabel promptLabel = new JLabel("Do you want to save these changes?");
        promptLabel.setFont(promptLabel.getFont().deriveFont(Font.BOLD));
        promptLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Changes");
        JButton discardButton = new JButton("Discard Changes");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            result = JOptionPane.YES_OPTION;
            dispose();
        });
        discardButton.addActionListener(e -> {
            result = JOptionPane.NO_OPTION;
            dispose();
        });
        cancelButton.addActionListener(e -> {
            result = JOptionPane.CANCEL_OPTION;
            dispose();
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(discardButton);
        buttonPanel.add(cancelButton);

        add(promptLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Shows the dialog and waits for user input.
     * @return The user's choice (JOptionPane.YES_OPTION, NO_OPTION, or CANCEL_OPTION).
     */
    public int showDialog() {
        setVisible(true);
        return result;
    }
}