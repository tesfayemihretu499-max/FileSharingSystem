package filesharing.server;

import filesharing.service.FileService;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;

public class FileServer extends UnicastRemoteObject implements FileService {
    DBConnection db = new DBConnection();
    public static JTextArea logArea = new JTextArea();

    public FileServer() throws Exception { super(); }

    @Override
    public String registerUser(String u, String p, String e) {
        try (Connection con = db.getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO users (username, password, email) VALUES (?, ?, ?)");
            ps.setString(1, u); ps.setString(2, p); ps.setString(3, e);
            ps.executeUpdate();
            logArea.append("[REGISTER]: " + u + " registered successfully.\n");
            return "Registration Success!";
        } catch (Exception ex) { 
            logArea.append("[REG ERROR]: " + ex.getMessage() + "\n");
            return "Error: " + ex.getMessage(); 
        }
    }

    @Override
    public String userLogin(String u, String p) {
        try (Connection con = db.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
            ps.setString(1, u); ps.setString(2, p);
            if (ps.executeQuery().next()) {
                logArea.append("[LOGIN]: " + u + " is now online.\n");
                return "Success";
            }
        } catch (Exception e) { 
            logArea.append("[LOGIN ERROR]: " + e.getMessage() + "\n");
            return "Error"; 
        }
        return "Fail";
    }

    @Override
    public String uploadFile(String u, String name, byte[] data) {
        try (Connection con = db.getConnection()) {
                     // Table Column 
            String sql = "INSERT INTO files (username, file_name, file_data) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, u);
            ps.setString(2, name);
            ps.setBytes(3, data);
            
            int rowsAffected = ps.executeUpdate(); 
            
            if (rowsAffected > 0) {
                logArea.append("[SUCCESS]: " + name + " saved to database table.\n");
                return "File Uploaded!";
            } else {
                logArea.append("[WARNING]: Query executed but no rows added.\n");
                return "Upload Failed";
            }
        } catch (Exception e) { 
            logArea.append("[DB ERROR]: " + e.getMessage() + "\n");
            e.printStackTrace();
            return "Error"; 
        }
    }

    @Override
    public List<String> getAllFiles() {
        List<String> files = new ArrayList<>();
        try (Connection con = db.getConnection(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT file_name FROM files");
            while (rs.next()) {
                files.add(rs.getString("file_name"));
            }
            logArea.append("[FETCH]: File list refreshed.\n");
        } catch (Exception e) { 
            logArea.append("[FETCH ERROR]: " + e.getMessage() + "\n");
        }
        return files;
    }

    @Override
    public byte[] downloadFile(String name) {
        try (Connection con = db.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT file_data FROM files WHERE file_name=?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                logArea.append("[DOWNLOAD]: " + name + " is being downloaded.\n");
                return rs.getBytes("file_data");
            }
        } catch (Exception e) { 
            logArea.append("[DOWNLOAD ERROR]: " + e.getMessage() + "\n");
        }
        return null;
    }

    @Override
    public String sendMessage(String s, String r, String m) {
        try (Connection con = db.getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO messages (sender, receiver, message) VALUES (?, ?, ?)");
            ps.setString(1, s); ps.setString(2, r); ps.setString(3, m);
            ps.executeUpdate();
            return "Sent";
        } catch (Exception e) { return "Error"; }
    }

    @Override
    public List<String> getMessages(String r) {
        List<String> msgs = new ArrayList<>();
        try (Connection con = db.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT sender, message FROM messages WHERE receiver=?");
            ps.setString(1, r);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) msgs.add(rs.getString("sender") + ": " + rs.getString("message"));
        } catch (Exception e) { e.printStackTrace(); }
        return msgs;
    }

    public static void main(String[] args) {
        try {
            JFrame f = new JFrame("RMI Distributed Server - PC 1");
            f.setSize(500, 400);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            logArea.setEditable(false);
            logArea.setBackground(Color.BLACK);
            logArea.setForeground(Color.GREEN);
            f.add(new JScrollPane(logArea));
            f.setLocationRelativeTo(null);
            f.setVisible(true);

            try {
                LocateRegistry.createRegistry(1099);
            } catch (Exception e) {
                logArea.append("Registry already running...\n");
            }
            
            Naming.rebind("rmi://localhost/FileService", new FileServer());
            logArea.append(">>> Distributed Server is Ready on PC 1...\n");
            logArea.append(">>> Waiting for Clients (PC 2, PC 3)...\n");
        } catch (Exception e) { 
            logArea.append("CRITICAL ERROR: " + e.getMessage() + "\n");
            e.printStackTrace(); 
        }
    }
}