package com.example.teacherhoursdesktop.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:teacher_hours.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC драйвер не найден!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void initialize() {
        try (Connection conn = createConnection();
             Statement stmt = conn.createStatement()) {

            createTeachersTable(stmt);
            createWorkRecordsTable(stmt);
            insertTestData(stmt);

            System.out.println("База данных успешно инициализирована");
        } catch (SQLException e) {
            System.err.println("Ошибка инициализации базы данных:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Connection createConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void createTeachersTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS teachers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT NOT NULL, " +
                "department TEXT NOT NULL)";
        stmt.execute(sql);
    }

    private static void createWorkRecordsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS work_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "teacher_id INTEGER NOT NULL, " +
                "date TEXT NOT NULL, " +
                "hours INTEGER NOT NULL, " +
                "lesson_type TEXT NOT NULL, " +
                "FOREIGN KEY (teacher_id) REFERENCES teachers(id))";
        stmt.execute(sql);
    }

    private static void insertTestData(Statement stmt) throws SQLException {
        var rs = stmt.executeQuery("SELECT COUNT(*) FROM teachers");
        if (rs.getInt(1) == 0) {
            stmt.executeUpdate("INSERT INTO teachers (full_name, department) VALUES " +
                    "('Иванов А.А.', 'Кафедра информатики'), " +
                    "('Петрова С.М.', 'Кафедра математики')");

            stmt.executeUpdate("INSERT INTO work_records (teacher_id, date, hours, lesson_type) VALUES " +
                    "(1, '2024-01-15', 4, 'Лекция'), " +
                    "(1, '2024-01-16', 2, 'Практика'), " +
                    "(2, '2024-01-15', 3, 'Семинар')");
        }
    }
}