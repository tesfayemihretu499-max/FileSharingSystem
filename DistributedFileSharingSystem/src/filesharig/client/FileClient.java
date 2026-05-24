package filesharing.client;

import filesharing.service.FileService;
import java.rmi.Naming;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class FileClient extends JFrame {
    FileService fs;
    String currentUser = "";
    String serverIP = "192.168.1.5"; 

    public FileClient() {
        try {
            fs = (FileService) Naming.lookup("rmi://" + serverIP + "/FileService");
            showAuthWindow();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Server Not Found! Check IP.");
        }
    }

    private void showAuthWindow() {
        JFrame authFrame = new JFrame("Login / Register");
        authFrame.setSize(300, 250);
        authFrame.setLayout(new FlowLayout());

        JTextField uName = new JTextField(15);
        JPasswordField uPass = new JPasswordField(15);
        JButton logBtn = new JButton("Login");
        JButton regBtn = new JButton("Register");

        authFrame.add(new JLabel("Username:")); authFrame.add(uName);
        authFrame.add(new JLabel("Password:")); authFrame.add(uPass);
        authFrame.add(logBtn); authFrame.add(regBtn);

        logBtn.addActionListener(e -> {
            try {
                if (fs.userLogin(uName.getText(), new String(uPass.getPassword())).equals("Success")) {
                    currentUser = uName.getText();
                    authFrame.dispose();
                    showMainWindow();
                } else { JOptionPane.showMessageDialog(null, "Fail!"); }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        regBtn.addActionListener(e -> {
            try {
                String r = fs.registerUser(uName.getText(), new String(uPass.getPassword()), "user@uni.edu.et");
                JOptionPane.showMessageDialog(null, r);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        authFrame.setLocationRelativeTo(null);
        authFrame.setVisible(true);
    }

    private void showMainWindow() {
        JFrame main = new JFrame("File Sharing - " + currentUser);
        main.setSize(600, 500);
        main.setLayout(new BorderLayout());

        // File List Area
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        main.add(new JScrollPane(list), BorderLayout.CENTER);

        // Buttons
        JPanel pnl = new JPanel();
        JButton upBtn = new JButton("Upload File");
        JButton downBtn = new JButton("Download Selected");
        JButton refreshBtn = new JButton("Refresh");
        pnl.add(upBtn); pnl.add(downBtn); pnl.add(refreshBtn);
        main.add(pnl, BorderLayout.SOUTH);

        // Upload Action
        upBtn.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File f = jfc.getSelectedFile();
                try {
                    byte[] data = Files.readAllBytes(f.toPath());
                    fs.uploadFile(currentUser, f.getName(), data);
                    JOptionPane.showMessageDialog(null, "Done!");
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        // Refresh Action
        refreshBtn.addActionListener(e -> {
            try {
                model.clear();
                List<String> all = fs.getAllFiles();
                for (String s : all) model.addElement(s);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.setVisible(true);
    }

    public static void main(String[] args) { new FileClient(); }
}