package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;

import java.util.Scanner;

/**
 * Single entry point of the CreditTrack system.
 * Flow: Initialize Database -> Instantiate DAOs & Utilities -> Authentication.start()
 */
public class Main {
    public static void main(String[] args) {
        // 1. Initialize database (creates tables if missing)
        DatabaseInitializer.initialize();

        // 2. Instantiate shared DAOs & utilities (one instance each)
        UserDao userDao = new UserDao();
        Scanner scanner = new Scanner(System.in);

        // 3. Delegate to Authentication
        new Authentication(userDao, scanner).start();

        scanner.close();
    }
}