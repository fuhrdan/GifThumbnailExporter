import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class GifThumbnailExporter {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GifThumbnailExporter().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("GIF Thumbnail Exporter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel panel = new JPanel(new BorderLayout());

        JButton openButton = new JButton("Open GIF");
        panel.add(openButton, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane();
        JPanel thumbnailPanel = new JPanel(new GridLayout(0, 5, 10, 10));
        scrollPane.setViewportView(thumbnailPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        openButton.addActionListener(e -> openGif(thumbnailPanel));

        frame.add(panel);
        frame.setVisible(true);
    }

    private void openGif(JPanel thumbnailPanel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("GIF Images", "gif"));

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
                Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");

                if (!readers.hasNext()) {
                    JOptionPane.showMessageDialog(null, "No GIF reader found.");
                    return;
                }

                ImageReader reader = readers.next();
                reader.setInput(input);
                ArrayList<BufferedImage> frames = new ArrayList<>();

                for (int i = 0; i < reader.getNumImages(true); i++) {
                    frames.add(reader.read(i));
                }

                displayThumbnails(frames, thumbnailPanel, file);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error reading GIF: " + ex.getMessage());
            }
        }
    }

    private void displayThumbnails(ArrayList<BufferedImage> frames, JPanel thumbnailPanel, File file) {
        thumbnailPanel.removeAll();

        for (int i = 0; i < frames.size(); i++) {
            BufferedImage frame = frames.get(i);
            Image thumbnail = frame.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel label = new JLabel(new ImageIcon(thumbnail));
            thumbnailPanel.add(label);
        }

        JButton exportButton = new JButton("Export Frames");
        exportButton.addActionListener(e -> exportFrames(frames, file));
        thumbnailPanel.add(exportButton);

        thumbnailPanel.revalidate();
        thumbnailPanel.repaint();
    }

    private void exportFrames(ArrayList<BufferedImage> frames, File file) {
        String folderName = file.getName().replaceFirst("\\.gif$", "");
        File outputFolder = new File(file.getParent(), folderName);

        if (!outputFolder.exists() && !outputFolder.mkdir()) {
            JOptionPane.showMessageDialog(null, "Failed to create folder: " + outputFolder.getPath());
            return;
        }

        try {
            for (int i = 0; i < frames.size(); i++) {
                File outputFile = new File(outputFolder, i + ".png");
                ImageIO.write(frames.get(i), "png", outputFile);
            }

            JOptionPane.showMessageDialog(null, "Frames exported to " + outputFolder.getPath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error exporting frames: " + ex.getMessage());
        }
    }
}
