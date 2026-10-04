import java.io.*;
import java.util.ArrayList;

// Holds the ArrayList and saves/loads it from a text file (local storage)
public class StudentStore {

    private static final String DATA_FILE = "data/students.txt";
    private final ArrayList<Student> students = new ArrayList<>();

    public StudentStore() {
        load();
    }

    // Returns false if the roll number already exists
    public synchronized boolean add(String name, String rollNumber) {
        if (findByRoll(rollNumber) != null) {
            return false;
        }
        students.add(new Student(name, rollNumber));
        save();
        return true;
    }

    public synchronized ArrayList<Student> getAll() {
        return new ArrayList<>(students);
    }

    public synchronized Student findByRoll(String rollNumber) {
        for (Student s : students) {
            if (s.getRollNumber().equalsIgnoreCase(rollNumber)) {
                return s;
            }
        }
        return null;
    }

    private void save() {
        File file = new File(DATA_FILE);
        file.getParentFile().mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (Student s : students) {
                writer.println(s.getRollNumber() + "," + s.getName());
            }
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }

    private void load() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    students.add(new Student(parts[1], parts[0]));
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load data: " + e.getMessage());
        }
    }
}
