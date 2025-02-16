import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AIMealTracker {
    private JFrame frame;
    private JPanel mealPanel;
    private JScrollPane scrollPane;
    private JLabel dateLabel, summaryLabel;
    private JButton addButton, prevDayButton, nextDayButton;
    private LocalDate currentDate;

    public AIMealTracker() {
        Database.databaseSetup();
        frame = new JFrame("AI Meal Tracker");
        frame.setSize(400, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBackground(Color.decode("#262626"));

        JPanel datePanel = new JPanel();
        datePanel.setBackground(Color.decode("#262626"));
        prevDayButton = new JButton("<");
        nextDayButton = new JButton(">");
        currentDate = LocalDate.now();
        dateLabel = new JLabel(currentDate.toString(), SwingConstants.CENTER);
        dateLabel.setForeground(Color.decode("#F2F2F2"));

        prevDayButton.addActionListener(e -> changeDate(-1));
        nextDayButton.addActionListener(e -> changeDate(1));

        prevDayButton.setFocusPainted(false);
        prevDayButton.setBackground(Color.decode("#F2F2F2"));
        nextDayButton.setFocusPainted(false);
        nextDayButton.setBackground(Color.decode("#F2F2F2"));

        datePanel.add(prevDayButton);
        datePanel.add(dateLabel);
        datePanel.add(nextDayButton);

        summaryLabel = new JLabel("Calories: 0 kcal  Protein: 0g  Fats: 0g  Carbs: 0g", SwingConstants.CENTER);
        summaryLabel.setForeground(Color.decode("#F2F2F2"));

        topPanel.add(datePanel, BorderLayout.NORTH);
        topPanel.add(summaryLabel, BorderLayout.SOUTH);
        frame.add(topPanel, BorderLayout.NORTH);

        mealPanel = new JPanel();
        mealPanel.setLayout(new BoxLayout(mealPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(mealPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.addMouseWheelListener(e -> {
            int notches = e.getWheelRotation();
            int scrollAmount = notches * 40;
            JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
            verticalScrollBar.setValue(verticalScrollBar.getValue() + scrollAmount);
        });
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI()
        {
            protected void configureScrollBarColors() {
                this.thumbColor = Color.decode("#F2D64B");
                this.trackColor = Color.decode("#262626");
            }
        });

        frame.add(scrollPane, BorderLayout.CENTER);

        addButton = new JButton("+");
        addButton.setFont(new Font("Arial", Font.BOLD, 20));
        addButton.addActionListener(e -> addNewMeal());
        addButton.setFocusPainted(false);
        addButton.setBackground(Color.decode("#F2F2F2"));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.decode("#262626"));
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(addButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        if (AppConfig.isFirstRun()) {
            String apiKey = javax.swing.JOptionPane.showInputDialog(null,
                    "Enter your API Key:", "First Run Setup", JOptionPane.PLAIN_MESSAGE);
            AppConfig.saveApiKey(apiKey);
        }
        AIPart.setApiKey(AppConfig.loadApiKey());

        updateMealList();
        updateSummary();
    }

    private void changeDate(int days) {
        currentDate = currentDate.plusDays(days);
        dateLabel.setText(currentDate.toString());

        updateMealList();
        updateSummary();
    }

    private void addNewMeal() {
        JTextField nameField = new JTextField();
        JTextArea descriptionField = new JTextArea(5, 20);
        descriptionField.setText("Describe your meal");
        descriptionField.setForeground(Color.GRAY);
        descriptionField.setWrapStyleWord(true);
        descriptionField.setLineWrap(true);

        descriptionField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (descriptionField.getText().equals("Describe your meal")) {
                    descriptionField.setText("");
                    descriptionField.setForeground(Color.BLACK);
                }
            }

            public void focusLost(FocusEvent evt) {
                if (descriptionField.getText().isEmpty()) {
                    descriptionField.setText("Describe your meal");
                    descriptionField.setForeground(Color.GRAY);
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        JPanel namePanel = new JPanel(new GridLayout(1, 2));
        namePanel.add(new JLabel("Meal Name:"));
        namePanel.add(nameField);

        JPanel descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.add(new JScrollPane(descriptionField), BorderLayout.CENTER);

        panel.add(namePanel, BorderLayout.NORTH);
        panel.add(descriptionPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Add New Meal", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String userDescription = descriptionField.getText();

            new Thread(() -> {
                String aiResponse = AIPart.processMealDescription(userDescription);
                if(aiResponse.startsWith("ERROR:")){
                    JOptionPane.showMessageDialog(frame, aiResponse, "Error", JOptionPane.ERROR_MESSAGE);
                }
                else if(aiResponse.trim().contains("AI_ERROR")){
                    JOptionPane.showMessageDialog(frame, "Wrong meal input!", "Error", JOptionPane.ERROR_MESSAGE);
                }
                else{
                    Database.insertMeal(dateLabel.getText(), nameField.getText(), aiResponse);
                    SwingUtilities.invokeLater(() -> {
                        updateMealList();
                        updateSummary();
                    });
                }
            }).start();
        }
    }


public void updateMealList() {
    mealPanel.removeAll();
    mealPanel.setLayout(new GridBagLayout());
    mealPanel.setBackground(Color.decode("#262626"));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTH;

    List<Map<String, String>> meals = Database.getMealsByDate(dateLabel.getText());

    for (int i = 0; i < meals.size(); i++) {
        Map<String, String> meal = meals.get(i);

        JPanel mealItem = new JPanel();
        mealItem.setLayout(new BorderLayout());
        mealItem.setPreferredSize(new Dimension(300, 50));
        mealItem.setBorder(BorderFactory.createLineBorder(Color.black, 2, true));

        int mealId = Integer.parseInt(meal.get("id"));
        mealItem.setName(String.valueOf(mealId));
        mealItem.setBackground(Color.DARK_GRAY);

        String name = meal.get("name");
        String description = meal.get("description");

        String[] nutritionalInfo = description.split("Summary:");
        String formattedNutritionalInfo = "";
        if (nutritionalInfo.length > 1) {
            String[] values = nutritionalInfo[1].trim().split("\n");
            if (values.length >= 4) {
                formattedNutritionalInfo = (values[values.length - 4] + " " +
                        values[values.length - 3] + " " +
                        values[values.length - 2] + " " +
                        values[values.length - 1]).replace("*", "");
            }
        }

        JLabel mealLabel = new JLabel(name + " - " + formattedNutritionalInfo);
        mealLabel.setText("<html><body style='width: 60%'>" + mealLabel.getText() + "</body></html>");

        JButton expandButton = new JButton("▼");
        expandButton.setFocusPainted(false);
        expandButton.setBackground(Color.decode("#F2F2F2"));

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.add(new JLabel("Description:"));
        detailsPanel.setBackground(Color.decode("#F2F2F2"));

        String wrappedDescription = "<html><div style='width: 300px;'>" + description.replace("\n", "<br/>") + "</div></html>";
        JLabel descriptionLabel = new JLabel(wrappedDescription);
        detailsPanel.add(descriptionLabel);

        JButton deleteButton = new JButton("🗑");
        deleteButton.setFocusPainted(false);
        deleteButton.setToolTipText("Delete meal");
        deleteButton.setBackground(Color.decode("#F2F2F2"));

        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to delete this meal?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int idToDelete = Integer.parseInt(mealItem.getName());
                Database.deleteMeal(idToDelete);
                mealPanel.remove(mealItem);
                mealPanel.revalidate();
                mealPanel.repaint();
                updateMealList();
                updateSummary();
            }
        });

        detailsPanel.add(deleteButton);
        detailsPanel.setVisible(false);

        expandButton.addActionListener(e -> {
            boolean isVisible = detailsPanel.isVisible();
            detailsPanel.setVisible(!isVisible);
            mealItem.setPreferredSize(new Dimension(350, isVisible ? 50 : detailsPanel.getPreferredSize().height + 50));
            mealPanel.revalidate();
            mealPanel.repaint();
            expandButton.setText(isVisible ? "▼" : "▲");
        });

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#F2E527"));
        headerPanel.setBorder(BorderFactory.createLineBorder(Color.black, 2, true));
        headerPanel.add(mealLabel, BorderLayout.WEST);
        headerPanel.add(expandButton, BorderLayout.EAST);

        mealItem.add(headerPanel, BorderLayout.CENTER);
        mealItem.add(detailsPanel, BorderLayout.SOUTH);
        mealItem.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        gbc.weighty = (i == meals.size() - 1) ? 1.0 : 0.0;
        gbc.gridy = i;
        mealPanel.add(mealItem, gbc);
    }

    mealPanel.revalidate();
    mealPanel.repaint();
    updateSummary();
}

    public void updateSummary() {
        Map<String, Double> summary = Database.getMealSummaryByDate(dateLabel.getText());

        summaryLabel.setText(String.format(
                "Calories: %.1f kcal  Protein: %.1f g  Fats: %.1f g  Carbs: %.1f g",

                summary.getOrDefault("totalCalories", 0.0),
                summary.getOrDefault("totalProtein", 0.0),
                summary.getOrDefault("totalFat", 0.0),
                summary.getOrDefault("totalCarbs", 0.0)
        ));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AIMealTracker::new);
    }
}
