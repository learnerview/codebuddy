// src/main/java/org/codebuddy/cli/CLIApp.java
package org.codebuddy.cli;

import org.codebuddy.core.models.*;
import org.codebuddy.core.services.ProblemService;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class CLIApp {
    private static final ProblemService service = new ProblemService();
    private static final Scanner scanner = new Scanner(System.in);
    private static final int DEFAULT_USER_ID = 1;

    public static void main(String[] args) {
        System.out.println("=== CodeBuddy CLI ===");
        System.out.println("Default user ID: " + DEFAULT_USER_ID);
        
        while (true) {
            printMenu();
            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1 -> addProblem();
                    case 2 -> listProblems();
                    case 3 -> System.exit(0);
                    default -> System.out.println("Invalid choice! Please enter 1, 2, or 3.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine(); // Clear invalid input
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n1. Add Problem");
        System.out.println("2. List Problems");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
    }

    private static void addProblem() {
        try {
            System.out.println("\n--- Add New Problem ---");
            
            System.out.print("Problem Name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Problem name cannot be empty!");
                return;
            }

            System.out.println("Available platforms:");
            Platform[] platforms = Platform.values();
            for (int i = 0; i < platforms.length; i++) {
                System.out.println((i + 1) + ". " + platforms[i].getDisplayName());
            }
            System.out.print("Choose platform (1-" + platforms.length + "): ");
            int platformChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            if (platformChoice < 1 || platformChoice > platforms.length) {
                System.out.println("Invalid platform choice!");
                return;
            }
            Platform platform = platforms[platformChoice - 1];

            System.out.println("Available difficulties:");
            Difficulty[] difficulties = Difficulty.values();
            for (int i = 0; i < difficulties.length; i++) {
                System.out.println((i + 1) + ". " + difficulties[i].getDisplayName());
            }
            System.out.print("Choose difficulty (1-" + difficulties.length + "): ");
            int difficultyChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            if (difficultyChoice < 1 || difficultyChoice > difficulties.length) {
                System.out.println("Invalid difficulty choice!");
                return;
            }
            Difficulty difficulty = difficulties[difficultyChoice - 1];

            System.out.print("Time Taken (minutes): ");
            int timeTaken = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            if (timeTaken <= 0) {
                System.out.println("Time taken must be positive!");
                return;
            }

            System.out.print("Notes (optional): ");
            String notes = scanner.nextLine().trim();

            System.out.print("Problem Link (optional): ");
            String link = scanner.nextLine().trim();

            Problem problem = new Problem(0, name, platform, difficulty, timeTaken, LocalDateTime.now(), 
                                       notes.isEmpty() ? "" : notes, link.isEmpty() ? "" : link, DEFAULT_USER_ID);

            service.addProblem(problem);
            System.out.println("Problem added successfully!");
            
        } catch (SQLException e) {
            System.err.println("Error saving problem: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void listProblems() {
        try {
            System.out.println("\n--- Your Problems ---");
            List<Problem> problems = service.getAllProblemsForUser(DEFAULT_USER_ID);
            
            if (problems.isEmpty()) {
                System.out.println("No problems found for user ID " + DEFAULT_USER_ID);
                return;
            }
            
            System.out.printf("%-30s %-15s %-10s %-8s %-20s%n", 
                "Name", "Platform", "Difficulty", "Time", "Date");
            System.out.println("-".repeat(85));
            
            for (Problem p : problems) {
                System.out.printf("%-30s %-15s %-10s %-8d %-20s%n",
                    p.getName().length() > 29 ? p.getName().substring(0, 26) + "..." : p.getName(),
                    p.getPlatform().getDisplayName(),
                    p.getDifficulty().getDisplayName(),
                    p.getTimeTakenMin(),
                    p.getSolvedDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching problems: " + e.getMessage());
        }
    }
}