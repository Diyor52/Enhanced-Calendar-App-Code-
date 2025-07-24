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





//main calendar class which uses Jframe to create the GUI
public class CalendarGUI extends JFrame {
    //all private instance variables used in order to get the month day and year
    private int currentYear;
    private int currentMonth;
    private int selectedDay;
    //stores notes for each day using Map so it saves and load it back in
    private Map<String, List<String>> dayNotes;
    //used for the files in order to once again save and load the data
    private static final String NOTES_FILE = "calendar_notes.txt";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private UserManager userManager;
    private String currentUser;




    //colors in whih we used, they are all final because they are constants(dont change)
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



    //Ui features and components allowing the GUI to actually functions
    private JLabel monthYearLabel;
    private JPanel calendarPanel;
    private JTextArea notesArea;
    private JComboBox<Integer> yearComboBox;
    private JComboBox<String> monthComboBox;



    //constructor for the class
    public CalendarGUI() {
        //shows the login screen through the other 2 classes we made
        //initilizes the user as well
        userManager = new UserManager();
        showLoginScreen();
    }

    //displays the login screen for others
    private void showLoginScreen() {
        //named calendar logsin
        JFrame loginFrame = new JFrame("Calendar Login");
        //creates the display features here
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(350, 200);
        loginFrame.setLayout(new BorderLayout());

        //grid layout with the login
        //for the GUI makes it more appealing
        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        //make the username and password fields in order for the person to type and register them in
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        //adding the components in order to login requiring email and passowrd
        loginPanel.add(new JLabel("Email:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);

        //creates the buttons in order to click login and register
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        //gets the username and password and uses action listener for further action
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            //if correct login credentials then displays calendar window
            if (userManager.login(username, password)) {
                currentUser = username;
                loginFrame.dispose(); //takes away the login screen
                initializeCalendar(); //opens up the calendar display
            } else {//if wrong then says that the email and password are wrong credentials used
                JOptionPane.showMessageDialog(loginFrame, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        //same thing with the login the register button also has an action listenr incase email already used for another account
        registerButton.addActionListener(e -> {
            //making username and pass
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            //if empty it requires the person to input them
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(loginFrame, "Email and password required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            //checks if email already used
            if (userManager.register(username, password)) {
                currentUser = username;
                loginFrame.dispose(); //takes away login screen
                initializeCalendar(); //displays calendar window
            } else {
                //email already taken
                JOptionPane.showMessageDialog(loginFrame, "Email already exists", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        //makes the button panels
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        //add more features to the frame
        loginFrame.add(loginPanel, BorderLayout.CENTER);
        loginFrame.add(buttonPanel, BorderLayout.SOUTH);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);
    }

    //displays the calendar and intilzizes it after login was a success
    private void initializeCalendar() {
        LocalDate today = LocalDate.now();//get current date
        this.currentYear = today.getYear();
        this.currentMonth = today.getMonthValue();
        this.selectedDay = -1;//-1 used as a base right now as user does not select anything yet
        this.dayNotes = new HashMap<>();


        //load notes for current user if had notes before
        loadNotes();


        //main display screen shown here
        setTitle("Calendar - " + currentUser);
        setSize(1000, 800);
        setLayout(new BorderLayout());
        getContentPane().setBackground(MAIN_BG);

        //creates more GUI features
        createNavigationPanel();
        createCalendarPanel();
        createNotesPanel();

        //updates the display screen of the calendar now
        updateCalendar();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    //gets the notes the user inputted through the text file made
    private String getUserNotesFilePath() {
        return "notes_" + currentUser + ".txt";
    }



    //creates the navigation panel at the top to scroll through
    private void createNavigationPanel() {
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(NAV_PANEL_BG);
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));




        //displays the month and year
        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        monthYearLabel.setForeground(HEADER_FG);




        //person can move back and forth through months using previous and next buttons
        JButton prevButton = createNavButton("\u25C0");
        prevButton.addActionListener(e -> {
            previousMonth();
            updateCalendar();
            updateComboBoxes();
        });



        //person can use the next button to move to the next month/year
        JButton nextButton = createNavButton("\u25B6");
        nextButton.addActionListener(e -> {
            nextMonth();
            updateCalendar();//updates the display of the calendar
            updateComboBoxes();
        });




        //allows the person to return to the date it is currently
        JButton todayButton = createNavButton("Today");
        todayButton.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            currentYear = today.getYear();
            currentMonth = today.getMonthValue();
            selectedDay = today.getDayOfMonth();
            updateCalendar();//updates the display once again
            updateComboBoxes();
            showDayNotes(selectedDay);//shows the notes the person had for this day
        });




        //month selection combo box
        monthComboBox = new JComboBox<>();
        //can select which month to go to
        for (Month month : Month.values()) {
            monthComboBox.addItem(month.getDisplayName(TextStyle.FULL, Locale.getDefault()));
        }
        monthComboBox.setSelectedIndex(currentMonth - 1);
        monthComboBox.addActionListener(e -> {
            currentMonth = monthComboBox.getSelectedIndex() + 1;
            updateCalendar();//updates to which month the person chose to go to
        });




        //alongside the month is the year version
        yearComboBox = new JComboBox<>();
        int startYear = currentYear - 10;
        int endYear = currentYear + 10;
        for (int year = startYear; year <= endYear; year++) {
            yearComboBox.addItem(year);//person can choose which year to go to 10 years back 10 years forward from now
        }
        yearComboBox.setSelectedItem(currentYear);
        yearComboBox.addActionListener(e -> {
            currentYear = (int) yearComboBox.getSelectedItem();
            updateCalendar();//updates to which yer they went to
        });




        //creates the panels for both month and year combo boxes
        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        comboPanel.setOpaque(false);
        comboPanel.add(monthComboBox);
        comboPanel.add(yearComboBox);




        //creates the panels for the navigation such as prev, next, and today
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(prevButton);
        buttonPanel.add(todayButton);
        buttonPanel.add(nextButton);



        //adds the features to the panels of naviagation
        navPanel.add(buttonPanel, BorderLayout.WEST);
        navPanel.add(monthYearLabel, BorderLayout.CENTER);
        navPanel.add(comboPanel, BorderLayout.EAST);



        //adds it to the calendar frame displayed
        add(navPanel, BorderLayout.NORTH);
    }



    //helped method in order to create the buttons of navigation
    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(BUTTON_BG);//font and colors here
        button.setForeground(BUTTON_FG);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return button;
        //basically just the color and font
    }



    //same thing here it creates the calendar panel which is the main frame
    private void createCalendarPanel() {
        calendarPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        calendarPanel.setBackground(MAIN_BG);
        add(calendarPanel, BorderLayout.CENTER);//colors and borders
    }



    //creates the notes panel with the colors and borders allowing person to add or remove notes
    private void createNotesPanel() {
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        notesPanel.setBackground(MAIN_BG);



        //allows the person to text the note and the area of it
        notesArea = new JTextArea(5, 20);
        notesArea.setEditable(false);
        notesArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));



        //made a scrolling feature for the notes incase too much notes in 1 day
        JScrollPane scrollPane = new JScrollPane(notesArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());



        //panel for the note actions such as add and remove note
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));



        //adds the note
        JButton addNoteButton = createNavButton("Add Note");
        addNoteButton.addActionListener(e -> addNoteForSelectedDay());



        //removes the note
        JButton removeNoteButton = createNavButton("Remove Note");
        removeNoteButton.addActionListener(e -> removeNoteForSelectedDay());



        //adds the buttons to the frame
        buttonPanel.add(addNoteButton);
        buttonPanel.add(removeNoteButton);



        //features of the notes panel
        notesPanel.add(new JLabel("Notes:", SwingConstants.LEFT), BorderLayout.NORTH);
        notesPanel.add(scrollPane, BorderLayout.CENTER);
        notesPanel.add(buttonPanel, BorderLayout.SOUTH);




        add(notesPanel, BorderLayout.SOUTH);
    }



    //updates the calendar frame displayed
    private void updateCalendar() {
        calendarPanel.removeAll();



        //information to get the current date/month
        YearMonth yearMonth = YearMonth.of(currentYear, currentMonth);
        LocalDate firstDay = yearMonth.atDay(1);
        DayOfWeek startDay = firstDay.getDayOfWeek();
        int daysInMonth = yearMonth.lengthOfMonth();
        int startDayValue = startDay.getValue(); // Monday = 1, Sunday = 7



        //updates the month and year labels displayed
        monthYearLabel.setText(yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentYear);




        //the calendar frames display of the days of the week
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String name : dayNames) {
            JLabel label = new JLabel(name, SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 14));
            label.setOpaque(true);//font and coloring of each with the weekends having a different color
            label.setBackground(DAY_NAMES_BG);
            label.setForeground(DAY_NAMES_FG);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.WHITE, 1),
                    BorderFactory.createEmptyBorder(5, 0, 5, 0)
            ));
            calendarPanel.add(label);
        }




        //used for days before the first day of the month(empty)
        for (int i = 1; i < startDayValue; i++) {
            calendarPanel.add(new JLabel(""));
        }




        //day buttons made here
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("SansSerif", Font.PLAIN, 14));




            //finding the day features here
            String key = currentYear + "-" + currentMonth + "-" + day;
            boolean isWeekend = isWeekend(day);//is weekend
            //has notes inside of it
            boolean hasNotes = dayNotes.containsKey(key) && !dayNotes.get(key).isEmpty();
            boolean isSelected = (day == selectedDay);



            //styles the button if the features are true  above
            dayButton.setOpaque(true);
            dayButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            dayButton.setFocusPainted(false);




            if (isSelected) {//here are the comopnents added if true
                dayButton.setBackground(SELECTED_DAY_BG);
                dayButton.setFont(new Font("SansSerif", Font.BOLD, 16));
            } else if (hasNotes) {
                dayButton.setBackground(NOTE_DAY_BG);
            } else if (isWeekend) {
                dayButton.setBackground(WEEKEND_BG);
            } else {
                dayButton.setBackground(Color.WHITE);//else its just white
            }



            //text color here
            if (isWeekend) {
                dayButton.setForeground(new Color(70, 130, 180));
            } else {
                dayButton.setForeground(Color.BLACK);
            }



            //day selection
            final int finalDay = day;
            dayButton.addActionListener(e -> {
                selectedDay = finalDay;
                showDayNotes(finalDay);
                updateCalendar();
            });




            calendarPanel.add(dayButton);
        }



        //refreshes the display of the calendar panel here
        calendarPanel.revalidate();
        calendarPanel.repaint();
    }



    //current month and year updating the combo boxes
    private void updateComboBoxes() {
        monthComboBox.setSelectedIndex(currentMonth - 1);
        yearComboBox.setSelectedItem(currentYear);
    }



    //check if weekend here based on the days
    private boolean isWeekend(int day) {
        LocalDate date = LocalDate.of(currentYear, currentMonth, day);
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }



        //shows the notes in that current day selected
    private void showDayNotes(int day) {
        String key = currentYear + "-" + currentMonth + "-" + day;
        notesArea.setText("");



        //here checks the notes based on the saving and loading data of the hashmap using keys
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



    //adds notes for the day the user selected
    private void addNoteForSelectedDay() {
        if (selectedDay == -1) {
            JOptionPane.showMessageDialog(this, "Please select a day first!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }



        //gets the notes from user
        String noteText = JOptionPane.showInputDialog(this,
                "Enter note for " + currentMonth + "/" + selectedDay + "/" + currentYear + ":",
                "Add Note", JOptionPane.PLAIN_MESSAGE);



        //if note is acceptable then it takes it in
        if (noteText != null && !noteText.trim().isEmpty()) {
            String key = currentYear + "-" + currentMonth + "-" + selectedDay;
            dayNotes.putIfAbsent(key, new ArrayList<>());
            dayNotes.get(key).add(noteText);
            saveNoteToFile(noteText, currentYear, currentMonth, selectedDay);
            updateCalendar();
            showDayNotes(selectedDay);
        }
    }



    //same thing with the add notes, here is the remove notes
    private void removeNoteForSelectedDay() {
        if (selectedDay == -1) {
            JOptionPane.showMessageDialog(this, "Please select a day first!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }



        //makes a key in order to see if note
        String key = currentYear + "-" + currentMonth + "-" + selectedDay;
        if (!dayNotes.containsKey(key) || dayNotes.get(key).isEmpty()) {
            JOptionPane.showMessageDialog(this, "No notes to remove for this day!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }




        List<String> notes = dayNotes.get(key);



        //handles the single notes here and remove it with a yes or no
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
            //multiple notes in 1 selected day right here
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



    //save the notes to a file in order to be able to regain data
    private void saveAllNotesToFile() {
        try (FileWriter writer = new FileWriter(NOTES_FILE)) {
            for (Map.Entry<String, List<String>> entry : dayNotes.entrySet()) {
                String[] dateParts = entry.getKey().split("-");
                int year = Integer.parseInt(dateParts[0]);//parses it in order to be able to store in integer
                int month = Integer.parseInt(dateParts[1]);
                int day = Integer.parseInt(dateParts[2]);



                //each note has a time stamp invovled with it
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



    //save 1 note to file
    private void saveNoteToFile(String note, int year, int month, int day) {
        try (FileWriter writer = new FileWriter(getUserNotesFilePath(), true)) {
            String timestamp = LocalDate.of(year, month, day).format(TIMESTAMP_FORMAT);
            String formattedNote = String.format("[%s] %s%n", timestamp, note);
            writer.write(formattedNote);//saving the note to file
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving note: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    //loading the saved notes here baased on the text file made
    private void loadNotes() {
        File file = new File(getUserNotesFilePath());
        if (!file.exists()) {
            return;//returns nothing it the file does not exist
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



                    //storing the notes in the map
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



    //goes to next month
    private void nextMonth() {
        if (currentMonth == 12) {
            currentMonth = 1;
            currentYear++;
        } else {
            currentMonth++;
        }
        selectedDay = -1;
    }



    //logic for previous month
    private void previousMonth() {
        if (currentMonth == 1) {
            currentMonth = 12;
            currentYear--;
        } else {
            currentMonth--;
        }
        selectedDay = -1;
    }



    //runs the gui and establishes everything
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalendarGUI calendar = new CalendarGUI();
            calendar.setVisible(true);
        });
    }
}
