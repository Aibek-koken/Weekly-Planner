package TODO;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;



public class SimpleToDo2 {
    private JPanel currentPanel = null;
    private final Map<String, JPanel> dayPanels = new HashMap<>();
    private final Map<String, JFrame> dayFrames = new HashMap<>();
    private final  Map<String, JButton> dayButtons = new HashMap<>();
    private JPanel trashPanel = new JPanel();
    private List<String> deletedTasks = new ArrayList<>();
    private final File trashFile = new File("trash.ser");

    Map<String, List<Task>> tasks = new HashMap<>();

    private final File saveFile = new File("tasks.ser");

    public void runApp() {

        JFrame mainFrame =new JFrame("TO DO ");
        JPanel mainPanel =new JPanel();
        JButton bTrash = new JButton("🗑");
        JButton bAdd = new JButton("➕");
        JTextField text =new JTextField(15);

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for(String day: days){
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
            dayPanels.put(day,panel);
        }

        for(String day: days){
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            frame.setSize(300,500);
            dayFrames.put(day,frame);
            dayFrames.get(day).setTitle(day);
        }

        for(String day: days){
            JButton button = new JButton(day);
            dayButtons.put(day,button);
            dayPanels.get(day).setLayout(new BoxLayout(dayPanels.get(day), BoxLayout.Y_AXIS));
            dayFrames.get(day).add(new JScrollPane(dayPanels.get(day)));
            dayButtons.get(day).addActionListener(e -> {
                currentPanel = dayPanels.get(day);
                dayFrames.get(day).setVisible(true);
            });
        }
        loadTasks();
        loadTrash();

        // Отображаем ранее сохраненные задачи
        for (String day : tasks.keySet()) {
            List<Task> taskList = tasks.get(day);
            JPanel panel = dayPanels.get(day);
            for (Task task : taskList) {
                JButton taskButton = new JButton(task.getText());

                taskButton.addActionListener(ae1 -> {
                    int result = JOptionPane.showConfirmDialog(
                            null,
                            "Хорош ✅,Было ли задание успешно выполнено ?",
                            "Подтверждение удаления",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (result == JOptionPane.YES_OPTION) {
                        panel.remove(taskButton);
                        panel.revalidate();
                        panel.repaint();

                        JButton trashButton = new JButton(task.getText());
                        trashButton.addActionListener(e1 -> ConfirmButtonForTrash(trashButton));
                        trashPanel.add(trashButton);
                        trashPanel.revalidate();
                        trashPanel.repaint();

                        tasks.get(day).remove(task);
                        saveTasks();
                    }
                });

                panel.add(taskButton);
            }
        }


        JFrame fTrash = new JFrame("Trash");
        fTrash.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        fTrash.setSize(270, 500);
        trashPanel.setLayout(new BoxLayout(trashPanel, BoxLayout.Y_AXIS));
        fTrash.add(new JScrollPane(trashPanel));
        bTrash.addActionListener(e -> fTrash.setVisible(true));


        for (String deletedText : deletedTasks) {
            JButton trashButton = new JButton(deletedText);
            trashButton.addActionListener(e1 -> ConfirmButtonForTrash(trashButton));
            trashPanel.add(trashButton);
        }


        mainPanel.setLayout(new GridLayout(3,3));
        for(String day : days){

            mainPanel.add(dayButtons.get(day));
        }

        mainPanel.add(bTrash);
        mainPanel.add(bAdd);


        bAdd.addActionListener(e -> {
            String taskText = text.getText();
            if (currentPanel == null) {
                JOptionPane.showMessageDialog(null, "Сначала выбери день!⛔️");
            } else if (taskText.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Задача не может быть пустой!");
            } else {

                JButton taskButton = new JButton(taskText);

                taskButton.addActionListener(ae1 -> {

                    // Диалог подтверждения
                    int result = JOptionPane.showConfirmDialog(
                            null,
                            "Хорош ✅,Было ли задание успешно выполнено ?",
                            "Подтверждение удаления",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (result == JOptionPane.YES_OPTION) {

                        currentPanel.remove(taskButton);
                        currentPanel.revalidate();
                        currentPanel.repaint();

                        JButton trashButton = new JButton(taskText);
                        trashButton.addActionListener(e1 -> ConfirmButtonForTrash(trashButton));
                        trashPanel.add(trashButton);
                        trashPanel.revalidate();
                        trashPanel.repaint();

                        deletedTasks.add(taskText);
                        saveTrash();

                        for (Map.Entry<String, JPanel> entry : dayPanels.entrySet()) {
                            if (entry.getValue() == currentPanel) {
                                String selectedDay = entry.getKey();
                                List<Task> taskList = tasks.get(selectedDay);
                                if (taskList != null) {
                                    taskList.removeIf(t -> t.getText().equals(taskText));
                                }
                                break;
                            }
                        }
                        saveTasks();
                    }
                });
                for (Map.Entry<String, JPanel> entry : dayPanels.entrySet()) {
                    if (entry.getValue() == currentPanel) {
                        String selectedDay = entry.getKey();
                        tasks.computeIfAbsent(selectedDay, k -> new ArrayList<>()).add(new Task(taskText));
                        break;
                    }
                }

                currentPanel.add(taskButton);
                currentPanel.revalidate();
                text.setText("");
            }
        });

        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveTasks();
                saveTrash();
            }
        });


        mainFrame.getContentPane().add(mainPanel);
        mainFrame.getContentPane().add(BorderLayout.NORTH,text);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(250,250);
        mainFrame.setVisible(true);
    }
    private void saveTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            oos.writeObject(tasks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Загрузка задачи
    private void loadTasks() {
        if (!saveFile.exists()) {
            tasks = new HashMap<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                tasks = (Map<String, List<Task>>) obj;
            } else {
                System.out.println("Файл не содержит корректные данные.");
                tasks = new HashMap<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            tasks = new HashMap<>();
        }
    }

    public void ConfirmButtonForTrash(JButton trashButton) {
        int result = JOptionPane.showConfirmDialog(null,
                "Вы точно хотите удалить?",
                "Подтверждение удаления из корзины",
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            trashPanel.remove(trashButton);
            deletedTasks.remove(trashButton.getText());
            trashPanel.revalidate();
            trashPanel.repaint();
            saveTrash();
        }
    }

    private void saveTrash(){
        try{ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("trash.ser"));
            oos.writeObject(deletedTasks);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void loadTrash() {
        if (!trashFile.exists()) {
            deletedTasks = new ArrayList<>();
            return;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(trashFile));
            Object obj = ois.readObject();
            if (obj instanceof List) {
                deletedTasks = (List<String>) obj;
            } else {
                System.out.println("Файл мусорки поврежден.");
                deletedTasks = new ArrayList<>();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            deletedTasks = new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        SimpleToDo2 go = new SimpleToDo2();
        go.runApp();
    }
}
