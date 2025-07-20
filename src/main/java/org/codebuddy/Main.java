// src/main/java/org/codebuddy/Main.java
package org.codebuddy;

import org.codebuddy.cli.CLIApp;
import org.codebuddy.gui.GUIApp;
import org.codebuddy.core.dao.DatabaseManager;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseManager.initializeDatabase();
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            System.exit(1);
        }
        if (args.length > 0 && args[0].equals("--cli")) {
            CLIApp.main(args);  // Run command-line version
        } else {
            GUIApp.launch(GUIApp.class, args);  // Run GUI version
        }
    }
}