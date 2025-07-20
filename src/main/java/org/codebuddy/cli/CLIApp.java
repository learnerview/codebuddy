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
        while (true) {
            printMenu();
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> addProblem();
                case 2 -> listProblems();
                case 3 -> System.exit(0);
                default -> System.out.println("Invalid choice!");
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
            String name = scanner.nextLine();

            System.out.print("Platform (LeetCode/CodeChef/HackerRank/Other): ");
            Platform platform = Platform.valueOf(scanner.nextLine().toUpperCase());

            System.out.print("Difficulty (Easy/Medium/Hard): ");
            Difficulty difficulty = Difficulty.valueOf(scanner.nextLine().toUpperCase());

            System.out.print("Time Taken (minutes): ");
            int timeTaken = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            System.out.print("Notes (optional): ");
            String notes = scanner.nextLine();

            System.out.print("Problem Link (optional): ");
            String link = scanner.nextLine();

            Problem problem = new Problem(0, name, platform, difficulty, timeTaken, LocalDateTime.now(), notes == null ? "" : notes, link == null ? "" : link, DEFAULT_USER_ID);

            service.addProblem(problem);
            System.out.println("Problem added successfully!");
        } catch (SQLException e) {
            System.err.println("Error saving problem: " + e.getMessage());
        }
    }

    private static void listProblems() {
        try {
            System.out.println("\n--- Your Problems ---");
            service.getAllProblems().forEach(p -> System.out.println(
                    p.getName() + " (" + p.getPlatform().getDisplayName() + " | " + p.getDifficulty().getDisplayName() + ")"
            ));
        } catch (SQLException e) {
            System.err.println("Error fetching problems: " + e.getMessage());
        }
    }
}