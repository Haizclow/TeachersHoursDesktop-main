package com.example.teacherhoursdesktop.utils;

import com.example.teacherhoursdesktop.models.Teacher;
import com.example.teacherhoursdesktop.models.WorkRecord;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:teacher_hours.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void initialize() {
        try (Connection conn = createConnection();
             Statement stmt = conn.createStatement()) {

            dropTables(stmt);
            createTables(stmt);
            insertTestData(stmt);

            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Database initialization error:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void dropTables(Statement stmt) throws SQLException {
        String[] tables = {"work_records", "teacher_groups", "groups", "teachers"};
        for (String table : tables) {
            try {
                stmt.execute("DROP TABLE IF EXISTS " + table);
            } catch (SQLException e) {
                System.err.println("Error dropping table " + table + ": " + e.getMessage());
            }
        }
    }

    private static void createTables(Statement stmt) throws SQLException {
        // Teachers table with planned hours
        stmt.execute("CREATE TABLE teachers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT NOT NULL, " +
                "department TEXT NOT NULL, " +
                "planned_hours INTEGER DEFAULT 0)");

        // Groups table
        stmt.execute("CREATE TABLE groups (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE)");

        // Teacher-Groups relation
        stmt.execute("CREATE TABLE teacher_groups (" +
                "teacher_id INTEGER NOT NULL, " +
                "group_id INTEGER NOT NULL, " +
                "PRIMARY KEY (teacher_id, group_id), " +
                "FOREIGN KEY (teacher_id) REFERENCES teachers(id), " +
                "FOREIGN KEY (group_id) REFERENCES groups(id))");

        // Work records
        stmt.execute("CREATE TABLE work_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "teacher_id INTEGER NOT NULL, " +
                "date TEXT NOT NULL, " +
                "group_id INTEGER NOT NULL, " +
                "hours INTEGER NOT NULL, " +
                "lesson_type TEXT NOT NULL, " +
                "FOREIGN KEY (teacher_id) REFERENCES teachers(id), " +
                "FOREIGN KEY (group_id) REFERENCES groups(id))");
    }

    private static void insertTestData(Statement stmt) throws SQLException {
        // Test teachers
        stmt.executeUpdate("INSERT INTO teachers (full_name, department, planned_hours) VALUES " +
                "('Иванов А.А.', 'Кафедра информатики', 120), " +
                "('Петрова С.М.', 'Кафедра математики', 90)");

        // Test groups
        stmt.executeUpdate("INSERT INTO groups (name) VALUES " +
                "('ИВТ-101'), ('ИВТ-102'), ('МАТ-201'), ('МАТ-202')");

        // Teacher-group relations
        stmt.executeUpdate("INSERT INTO teacher_groups (teacher_id, group_id) VALUES " +
                "(1, 1), (1, 2), (2, 3), (2, 4)");

        // Work records
        stmt.executeUpdate("INSERT INTO work_records (teacher_id, date, group_id, hours, lesson_type) VALUES " +
                "(1, '2024-01-15', 1, 4, 'Лекция'), " +
                "(1, '2024-01-16', 2, 2, 'Практика'), " +
                "(2, '2024-01-15', 3, 3, 'Семинар')");
    }

    // Teacher methods
    public static List<Teacher> getAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        String sql = "SELECT t.id, t.full_name, t.department, t.planned_hours, " +
                "COALESCE(SUM(wr.hours), 0) as completed_hours " +
                "FROM teachers t " +
                "LEFT JOIN work_records wr ON t.id = wr.teacher_id " +
                "GROUP BY t.id, t.full_name, t.department, t.planned_hours";

        try (Connection conn = createConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Teacher teacher = new Teacher(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("department"),
                        rs.getInt("planned_hours"),
                        rs.getInt("completed_hours"));

                teacher.getGroups().addAll(getGroupsForTeacher(teacher.getId()));
                teachers.add(teacher);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teachers;
    }

    public static boolean addTeacher(String fullName, String department, List<String> groups) {
        String sql = "INSERT INTO teachers (full_name, department) VALUES (?, ?)";

        try (Connection conn = createConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);
            pstmt.setString(1, fullName);
            pstmt.setString(2, department);
            pstmt.executeUpdate();

            int teacherId;
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                teacherId = rs.next() ? rs.getInt(1) : -1;
            }

            if (teacherId == -1) {
                conn.rollback();
                return false;
            }

            for (String groupName : groups) {
                int groupId = getOrCreateGroup(conn, groupName);
                if (groupId == -1) {
                    conn.rollback();
                    return false;
                }

                String linkSql = "INSERT INTO teacher_groups (teacher_id, group_id) VALUES (?, ?)";
                try (PreparedStatement linkStmt = conn.prepareStatement(linkSql)) {
                    linkStmt.setInt(1, teacherId);
                    linkStmt.setInt(2, groupId);
                    linkStmt.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateTeacherHours(int teacherId, int plannedHours) {
        String sql = "UPDATE teachers SET planned_hours = ? WHERE id = ?";

        try (Connection conn = createConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, plannedHours);
            pstmt.setInt(2, teacherId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Group methods
    public static List<String> getGroupsForTeacher(int teacherId) {
        List<String> groups = new ArrayList<>();
        String sql = "SELECT g.name FROM groups g " +
                "JOIN teacher_groups tg ON g.id = tg.group_id " +
                "WHERE tg.teacher_id = ?";

        try (Connection conn = createConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                groups.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    private static int getOrCreateGroup(Connection conn, String groupName) throws SQLException {
        // Try to find existing group
        String findSql = "SELECT id FROM groups WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(findSql)) {
            pstmt.setString(1, groupName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }

        // Create new group
        String createSql = "INSERT INTO groups (name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(createSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, groupName);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    // Work records methods
    public static boolean addWorkRecord(int teacherId, String date, String groupName, int hours, String lessonType) {
        String sql = "INSERT INTO work_records (teacher_id, date, group_id, hours, lesson_type) " +
                "VALUES (?, ?, (SELECT id FROM groups WHERE name = ?), ?, ?)";

        try (Connection conn = createConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teacherId);
            pstmt.setString(2, date);
            pstmt.setString(3, groupName);
            pstmt.setInt(4, hours);
            pstmt.setString(5, lessonType);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<WorkRecord> getScheduleForTeacher(int teacherId, LocalDate startDate, LocalDate endDate) {
        List<WorkRecord> records = new ArrayList<>();
        String sql = "SELECT wr.id, wr.teacher_id, wr.date, g.name as group_name, wr.hours, wr.lesson_type " +
                "FROM work_records wr " +
                "JOIN groups g ON wr.group_id = g.id " +
                "WHERE wr.teacher_id = ? AND wr.date BETWEEN ? AND ? " +
                "ORDER BY wr.date";

        try (Connection conn = createConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teacherId);
            pstmt.setString(2, startDate.toString());
            pstmt.setString(3, endDate.toString());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                records.add(new WorkRecord(
                        rs.getInt("id"),
                        rs.getInt("teacher_id"),
                        rs.getString("date"),
                        rs.getString("group_name"),
                        rs.getInt("hours"),
                        rs.getString("lesson_type")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public static boolean updateWorkRecord(int recordId, int hours) {
        String sql = "UPDATE work_records SET hours = ? WHERE id = ?";

        try (Connection conn = createConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hours);
            pstmt.setInt(2, recordId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getCompletedHoursForTeacher(int teacherId) {
        String sql = "SELECT COALESCE(SUM(hours), 0) as total FROM work_records WHERE teacher_id = ?";

        try (Connection conn = createConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt("total") : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static List<WorkRecord> getWorkRecordsForTeacher(int teacherId) {
        List<WorkRecord> records = new ArrayList<>();
        String sql = "SELECT wr.id, wr.teacher_id, wr.date, g.name as group_name, wr.hours, wr.lesson_type " +
                "FROM work_records wr " +
                "JOIN groups g ON wr.group_id = g.id " +
                "WHERE wr.teacher_id = ? " +
                "ORDER BY wr.date";

        try (Connection conn = createConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(new WorkRecord(
                        rs.getInt("id"),
                        rs.getInt("teacher_id"),
                        rs.getString("date"),
                        rs.getString("group_name"),
                        rs.getInt("hours"),
                        rs.getString("lesson_type")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    private static Connection createConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}