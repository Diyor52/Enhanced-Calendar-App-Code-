import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.io.*;
import java.net.URL;






public class CalendarGUI extends JFrame {
    private int currentYear;
    private int currentMonth;
    private int selectedDay;
    private Map<String, List<String>> dayNotes;
    private static final String NOTES_FILE = "calendar_notes.txt";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private UserManager userManager;
    private String currentUser;




    // Color scheme
    private final Color HEADER_BG = new Color(70, 130, 180);
    private final Color HEADER_FG = Color.WHITE;
    private final Color NAV_PANEL_BG = new Color(100, 149, 237);
    private final Color DAY_NAMES_BG = new Color(100, 149, 237);
    private final Color DAY_NAMES_FG = Color.WHITE;
    private final Color WEEKEND_BG = new Color(240, 248, 255);
    private final Color NOTE_DAY_BG = new Color(152, 251, 152);
    private final Color SELECTED_DAY_BG = new Color(255, 215, 0);
    private final Color MAIN_BG = new Color(211, 211, 211);
    private final Color BUTTON_BG = new Color(70, 130, 180);
    private final Color BUTTON_FG = Color.WHITE;




    private JLabel monthYearLabel;
    private JPanel calendarPanel;
    private JTextArea notesArea;
    private JComboBox<Integer> yearComboBox;
    private JComboBox<String> monthComboBox;




    public CalendarGUI() {
        userManager = new UserManager();
        showLoginScreen();
    }


    private void showLoginScreen() {
        JFrame loginFrame = new JFrame("Calendar Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(350, 200);
        loginFrame.setLayout(new BorderLayout());


        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));


        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();


        loginPanel.add(new JLabel("Email:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);


        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");


        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());


            if (userManager.login(username, password)) {
                currentUser = username;
                loginFrame.dispose(); //Close login window
                initializeCalendar(); //Show calendar window
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());


            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(loginFrame, "Email and password required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            if (userManager.register(username, password)) {
                currentUser = username;
                loginFrame.dispose(); //Close login window
                initializeCalendar(); //Show calendar window
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Email already exists", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);


        loginFrame.add(loginPanel, BorderLayout.CENTER);
        loginFrame.add(buttonPanel, BorderLayout.SOUTH);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);
    }


    private void initializeCalendar() {
        LocalDate today = LocalDate.now();
        this.currentYear = today.getYear();
        this.currentMonth = today.getMonthValue();
        this.selectedDay = -1;
        this.dayNotes = new HashMap<>();


        // Load notes for current user
        loadNotes();


        // Set up main window
        setTitle("Calendar - " + currentUser);
        setSize(1000, 800);
        setLayout(new BorderLayout());
        getContentPane().setBackground(MAIN_BG);


        createNavigationPanel();
        createCalendarPanel();
        createNotesPanel();


        updateCalendar();
        setLocationRelativeTo(null);
        setVisible(true);
    }


    private String getUserNotesFilePath() {
        return "notes_" + currentUser + ".txt";
    }




    private void createNavigationPanel() {
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(NAV_PANEL_BG);
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));




        // Month/year display
        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        monthYearLabel.setForeground(HEADER_FG);




        // Arrow buttons
        JButton prevButton = createNavButton("\u25C0");
        prevButton.addActionListener(e -> {
            previousMonth();
            updateCalendar();
            updateComboBoxes();
        });




        JButton nextButton = createNavButton("\u25B6");
        nextButton.addActionListener(e -> {
            nextMonth();
            updateCalendar();
            updateComboBoxes();
        });




        // Today button
        JButton todayButton = createNavButton("Today");
        todayButton.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            currentYear = today.getYear();
            currentMonth = today.getMonthValue();
            selectedDay = today.getDayOfMonth();
            updateCalendar();
            updateComboBoxes();
            showDayNotes(selectedDay);
        });




        // Month selection combo box
        monthComboBox = new JComboBox<>();
        for (Month month : Month.values()) {
            monthComboBox.addItem(month.getDisplayName(TextStyle.FULL, Locale.getDefault()));
        }
        monthComboBox.setSelectedIndex(currentMonth - 1);
        monthComboBox.addActionListener(e -> {
            currentMonth = monthComboBox.getSelectedIndex() + 1;
            updateCalendar();
        });




        // Year selection combo box
        yearComboBox = new JComboBox<>();
        int startYear = currentYear - 10;
        int endYear = currentYear + 10;
        for (int year = startYear; year <= endYear; year++) {
            yearComboBox.addItem(year);
        }
        yearComboBox.setSelectedItem(currentYear);
        yearComboBox.addActionListener(e -> {
            currentYear = (int) yearComboBox.getSelectedItem();
            updateCalendar();
        });




        // Panel for combo boxes
        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        comboPanel.setOpaque(false);
        comboPanel.add(monthComboBox);
        comboPanel.add(yearComboBox);




        // Panel for navigation buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(prevButton);
        buttonPanel.add(todayButton);
        buttonPanel.add(nextButton);




        navPanel.add(buttonPanel, BorderLayout.WEST);
        navPanel.add(monthYearLabel, BorderLayout.CENTER);
        navPanel.add(comboPanel, BorderLayout.EAST);




        add(navPanel, BorderLayout.NORTH);
    }




    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(BUTTON_BG);
        button.setForeground(BUTTON_FG);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return button;
    }




    private void createCalendarPanel() {
        calendarPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        calendarPanel.setBackground(MAIN_BG);
        add(calendarPanel, BorderLayout.CENTER);
    }




    private void createNotesPanel() {
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        notesPanel.setBackground(MAIN_BG);




        notesArea = new JTextArea(5, 20);
        notesArea.setEditable(false);
        notesArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));




        JScrollPane scrollPane = new JScrollPane(notesArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());




        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));




        JButton addNoteButton = createNavButton("Add Note");
        addNoteButton.addActionListener(e -> addNoteForSelectedDay());




        JButton removeNoteButton = createNavButton("Remove Note");
        removeNoteButton.addActionListener(e -> removeNoteForSelectedDay());




        buttonPanel.add(addNoteButton);
        buttonPanel.add(removeNoteButton);




        notesPanel.add(new JLabel("Notes:", SwingConstants.LEFT), BorderLayout.NORTH);
        notesPanel.add(scrollPane, BorderLayout.CENTER);
        notesPanel.add(buttonPanel, BorderLayout.SOUTH);




        add(notesPanel, BorderLayout.SOUTH);
    }




    private void updateCalendar() {
        calendarPanel.removeAll();




        YearMonth yearMonth = YearMonth.of(currentYear, currentMonth);
        LocalDate firstDay = yearMonth.atDay(1);
        DayOfWeek startDay = firstDay.getDayOfWeek();
        int daysInMonth = yearMonth.lengthOfMonth();
        int startDayValue = startDay.getValue(); // Monday = 1, Sunday = 7




        monthYearLabel.setText(yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentYear);




        // Day names header
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String name : dayNames) {
            JLabel label = new JLabel(name, SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 14));
            label.setOpaque(true);
            label.setBackground(DAY_NAMES_BG);
            label.setForeground(DAY_NAMES_FG);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.WHITE, 1),
                    BorderFactory.createEmptyBorder(5, 0, 5, 0)
            ));
            calendarPanel.add(label);
        }




        // Empty cells for days before the first day of month
        for (int i = 1; i < startDayValue; i++) {
            calendarPanel.add(new JLabel(""));
        }




        // Day buttons
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("SansSerif", Font.PLAIN, 14));




            // Style the button based on day properties
            String key = currentYear + "-" + currentMonth + "-" + day;
            boolean isWeekend = isWeekend(day);
            boolean hasNotes = dayNotes.containsKey(key) && !dayNotes.get(key).isEmpty();
            boolean isSelected = (day == selectedDay);




            dayButton.setOpaque(true);
            dayButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            dayButton.setFocusPainted(false);




            if (isSelected) {
                dayButton.setBackground(SELECTED_DAY_BG);
                dayButton.setFont(new Font("SansSerif", Font.BOLD, 16));
            } else if (hasNotes) {
                dayButton.setBackground(NOTE_DAY_BG);
            } else if (isWeekend) {
                dayButton.setBackground(WEEKEND_BG);
            } else {
                dayButton.setBackground(Color.WHITE);
            }




            if (isWeekend) {
                dayButton.setForeground(new Color(70, 130, 180));
            } else {
                dayButton.setForeground(Color.BLACK);
            }




            final int finalDay = day;
            dayButton.addActionListener(e -> {
                selectedDay = finalDay;
                showDayNotes(finalDay);
                updateCalendar();
            });




            calendarPanel.add(dayButton);
        }




        calendarPanel.revalidate();
        calendarPanel.repaint();
    }




    private void updateComboBoxes() {
        monthComboBox.setSelectedIndex(currentMonth - 1);
        yearComboBox.setSelectedItem(currentYear);
    }




    private boolean isWeekend(int day) {
        LocalDate date = LocalDate.of(currentYear, currentMonth, day);
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }




    private void showDayNotes(int day) {
        String key = currentYear + "-" + currentMonth + "-" + day;
        notesArea.setText("");




        if (dayNotes.containsKey(key)) {
            List<String> notes = dayNotes.get(key);
            if (!notes.isEmpty()) {
                notesArea.append("Notes for " + currentMonth + "/" + day + "/" + currentYear + ":\n\n");
                for (String note : notes) {
                    notesArea.append("• " + note + "\n");
                }
            } else {
                notesArea.setText("No notes for this day.");
            }
        } else {
            notesArea.setText("No notes for this day.");
        }
    }




    private void addNoteForSelectedDay() {
        if (selectedDay == -1) {
            JOptionPane.showMessageDialog(this, "Please select a day first!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }




        String noteText = JOptionPane.showInputDialog(this,
                "Enter note for " + currentMonth + "/" + selectedDay + "/" + currentYear + ":",
                "Add Note", JOptionPane.PLAIN_MESSAGE);




        if (noteText != null && !noteText.trim().isEmpty()) {
            String key = currentYear + "-" + currentMonth + "-" + selectedDay;
            dayNotes.putIfAbsent(key, new ArrayList<>());
            dayNotes.get(key).add(noteText);
            saveNoteToFile(noteText, currentYear, currentMonth, selectedDay);
            updateCalendar();
            showDayNotes(selectedDay);
        }
    }




    private void removeNoteForSelectedDay() {
        if (selectedDay == -1) {
            JOptionPane.showMessageDialog(this, "Please select a day first!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }




        String key = currentYear + "-" + currentMonth + "-" + selectedDay;
        if (!dayNotes.containsKey(key) || dayNotes.get(key).isEmpty()) {
            JOptionPane.showMessageDialog(this, "No notes to remove for this day!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }




        List<String> notes = dayNotes.get(key);




        if (notes.size() == 1) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Remove this note?\n\"" + notes.get(0) + "\"",
                    "Confirm Removal",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                notes.clear();
                saveAllNotesToFile();
                updateCalendar();
                showDayNotes(selectedDay);
            }
        } else {
            String[] noteArray = notes.toArray(new String[0]);
            String selectedNote = (String) JOptionPane.showInputDialog(this,
                    "Select note to remove:",
                    "Remove Note",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    noteArray,
                    noteArray[0]);




            if (selectedNote != null) {
                notes.remove(selectedNote);
                saveAllNotesToFile();
                updateCalendar();
                showDayNotes(selectedDay);
            }
        }
    }




    private void saveAllNotesToFile() {
        try (FileWriter writer = new FileWriter(NOTES_FILE)) {
            for (Map.Entry<String, List<String>> entry : dayNotes.entrySet()) {
                String[] dateParts = entry.getKey().split("-");
                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);
                int day = Integer.parseInt(dateParts[2]);




                for (String note : entry.getValue()) {
                    String timestamp = LocalDate.of(year, month, day).format(TIMESTAMP_FORMAT);
                    String formattedNote = String.format("[%s] %s%n", timestamp, note);
                    writer.write(formattedNote);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving notes: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }




    private void saveNoteToFile(String note, int year, int month, int day) {
        try (FileWriter writer = new FileWriter(getUserNotesFilePath(), true)) {
            String timestamp = LocalDate.of(year, month, day).format(TIMESTAMP_FORMAT);
            String formattedNote = String.format("[%s] %s%n", timestamp, note);
            writer.write(formattedNote);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving note: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }




    private void loadNotes() {
        File file = new File(getUserNotesFilePath());
        if (!file.exists()) {
            return;
        }




        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[") && line.contains("]")) {
                    int endBracket = line.indexOf("]");
                    String dateStr = line.substring(1, endBracket);
                    String noteContent = line.substring(endBracket + 1).trim();




                    String[] dateParts = dateStr.split("-");
                    int year = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]);
                    int day = Integer.parseInt(dateParts[2]);




                    String key = year + "-" + month + "-" + day;
                    dayNotes.putIfAbsent(key, new ArrayList<>());
                    dayNotes.get(key).add(noteContent);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading notes: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }




    private void nextMonth() {
        if (currentMonth == 12) {
            currentMonth = 1;
            currentYear++;
        } else {
            currentMonth++;
        }
        selectedDay = -1;
    }




    private void previousMonth() {
        if (currentMonth == 1) {
            currentMonth = 12;
            currentYear--;
        } else {
            currentMonth--;
        }
        selectedDay = -1;
    }




    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalendarGUI calendar = new CalendarGUI();
            calendar.setVisible(true);
        });
    }
}
