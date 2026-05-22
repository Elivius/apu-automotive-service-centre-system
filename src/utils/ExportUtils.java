package utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportUtils {
    
    /**
     * Prompts the user to save a string of content (HTML, CSV, TXT) to a file.
     */
    public static void exportStringToFile(Component parent, String defaultFileName, String content) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Export As...");
        fileChooser.setSelectedFile(new File(defaultFileName));
        
        int userSelection = fileChooser.showSaveDialog(parent);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(fileToSave)) {
                fw.write(content);
                JOptionPane.showMessageDialog(parent, 
                    "Export successful!\nSaved to: " + fileToSave.getAbsolutePath(), 
                    "Export Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent, 
                    "Failed to export: " + e.getMessage(), 
                    "Export Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
