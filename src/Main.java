import java.util.ArrayList;
import java.util.Scanner;

// Console menu version (no browser needed)
public class Main {

    private static final StudentStore store = new StudentStore();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== Student Manager =====");
            System.out.println("1. Add student");
            System.out.println("2. Display all students");
            System.out.println("3. Search by roll number");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            if (!scanner.hasNextLine()) break;
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": addStudent(scanner); break;
                case "2": displayAll(); break;
                case "3": searchByRoll(scanner); break;
                case "4":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Please enter 1-4.");
            }
        }
    }

    private static void addStudent(Scanner scanner) {
        System.out.print("Enter name: ");
        String name = scanner.nextLine().trim().replace(",", " ");
        System.out.print("Enter roll number: ");
        String roll = scanner.nextLine().trim().replace(",", "");

        if (name.isEmpty() || roll.isEmpty()) {
            System.out.println("Name and roll number cannot be empty.");
            return;
        }
        if (store.add(name, roll)) {
            System.out.println("Student added successfully.");
        } else {
            System.out.println("A student with roll number " + roll + " already exists.");
        }
    }

    private static void displayAll() {
        ArrayList<Student> list = store.getAll();
        if (list.isEmpty()) {
            System.out.println("No students found.");
            return;
        }
        System.out.println("\nRoll Number | Name");
        System.out.println("-------------------------");
        for (Student s : list) {
            System.out.println(s);
        }
        System.out.println("Total: " + list.size());
    }

    private static void searchByRoll(Scanner scanner) {
        System.out.print("Enter roll number to search: ");
        String roll = scanner.nextLine().trim();
        Student found = store.findByRoll(roll);
        if (found == null) {
            System.out.println("No student found with roll number " + roll + ".");
        } else {
            System.out.println("Found: " + found);
        }
    }
}
