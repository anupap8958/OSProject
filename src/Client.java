import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class Client {
    Socket socketClient;
    DataInputStream din;
    DataOutputStream dout;
    final int PORT = 8080;
    int FileLength;
    Object[][] fileList;
    String file;
    JButton downloadButton = new JButton();
    JPanel panelFileList;


    public static void main(String[] args) throws IOException {
        new Client().run();
    }

    public Client() {
        try {
            socketClient = new Socket("localhost", PORT);
            System.out.println("Connecing...");
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void reciveAllFile() {
        JFrame frameReciveAllFile = new JFrame();
        frameReciveAllFile.setTitle("DOWNLOADER [" + clientNo + "]");
        frameReciveAllFile.setSize(780, 570);
        frameReciveAllFile.setResizable(false);
        frameReciveAllFile.setFont(new Font("TH-Sarabun-PSK", Font.BOLD, 13));
        frameReciveAllFile.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameReciveAllFile.setLocationRelativeTo(null); // ปรับให้ frame อยู่กลางจอ
        frameReciveAllFile.setVisible(true);

        try {
            din = new DataInputStream(socketClient.getInputStream());
            dout = new DataOutputStream(socketClient.getOutputStream());
            FileLength = din.readInt();
            fileList = new Object[FileLength][4];

            String[] colHeaderFileList = { "All File", "File Type", "Size", "Action" };
            String[][] rowfileList = new String[FileLength][4];
            for (int i = 0; i < FileLength; i++) {
                fileList[i][0] = din.readUTF(); // ชื่อไฟล์
            }
            for (int i = 0; i < FileLength; i++) {
                fileList[i][1] = din.readUTF(); // ชนิดข้อมูลไฟล์
            }
            for (int i = 0; i < FileLength; i++) {
                fileList[i][2] = din.readUTF(); // ขนาดไฟล์
            }
            for (int i = 0; i < FileLength; i++) {
                rowfileList[i][0] = "  "
                        + fileList[i][0].toString().substring(0, fileList[i][0].toString().lastIndexOf("."));
                rowfileList[i][1] = fileList[i][1].toString().substring(fileList[i][1].toString().indexOf("/") + 1,
                        fileList[i][1].toString().length());
                rowfileList[i][2] = fileList[i][2].toString() + " KB ";
                rowfileList[i][3] = fileList[i][0].toString();
            }

            TableCellRenderer tableRenderer;
            DefaultTableModel model = new DefaultTableModel();
            model.setDataVector(rowfileList, colHeaderFileList);
            JTable tableFileList = new JTable(model);
            tableFileList.getColumn("Action").setCellRenderer(new ButtonRenderer(rowfileList));
            tableFileList.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
            panelFileList = new JPanel();
            tableFileList.setFont(new Font("TH-Sarabun-PSK", Font.BOLD, 13));
            tableFileList.setRowHeight(40);
            tableFileList.setPreferredScrollableViewportSize(new Dimension(750, 500)); // ปรับขนาดตาราง
            tableFileList.getColumnModel().getColumn(0).setPreferredWidth(400); // ปรับขนาดคอลัม

            // ปรับข้อความชิดซ้ายชิดขวา
            DefaultTableCellRenderer d = new DefaultTableCellRenderer();
            DefaultTableCellRenderer d2 = new DefaultTableCellRenderer();
            d.setHorizontalAlignment(JLabel.CENTER);
            tableFileList.getColumnModel().getColumn(1).setCellRenderer(d);
            d2.setHorizontalAlignment(JLabel.RIGHT);
            tableFileList.getColumnModel().getColumn(2).setCellRenderer(d2);

            JScrollPane scrollPaneFileList = new JScrollPane(tableFileList);
            panelFileList.add(scrollPaneFileList);
            downloadButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int confirm = JOptionPane.showConfirmDialog(panelFileList,
                            "Do you want to download " + downloadButton.getName() + " ?", "Customized Dialog",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            new ImageIcon("C:/Users/tubti/OneDrive - Silpakorn University/Documents/Thread/among.png"));
                    if (confirm == 0) {
                        reqFile();
                    }
                }

            });

            frameReciveAllFile.add(panelFileList);

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void reqFile() {

        try {
            dout.writeUTF(downloadButton.getName().toString());
            reciveReqrFile();
        } catch (Exception e) {

        }

    }

    public void reciveReqrFile() throws IOException {
        int size = din.readInt();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    System.out.println(Thread.currentThread().getName());
                    Socket socket = new Socket("localhost", 8087);
                    DataInputStream dinClient = new DataInputStream(socket.getInputStream());
                    String filePath = "C:/Users/api_q/OneDrive/เดสก์ท็อป/OSProject/FileClient/"
                            + downloadButton.getName();
                    int startIndex = dinClient.readInt();
                    int fileLength = dinClient.readInt();
                    RandomAccessFile writer = new RandomAccessFile(filePath, "rw");
                    writer.seek(startIndex);
                    byte[] data = new byte[1024];
                    int receive = 0;
                    while (receive > -1) {
                        receive = dinClient.read(data);
                        if (receive == -1) {
                            break;
                        }
                        writer.write(data, 0, receive);
                    }
                    System.out.println("finish");
                    // File fileDownload = new File(filePath);
                    // FileOutputStream fout = new FileOutputStream(fileDownload);
                    dinClient.close();
                    writer.close();
                    socket.close();
                    System.out.println("Recive");
                } catch (Exception e) {
                    // System.out.println("Can't Recive");
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public void run() throws IOException {
        reciveAllFile();
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer(String[][] dataValues) {

            setOpaque(true);

        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {

            setText("Download");
            setName(value.toString());

            return this;

        }

    }

    class ButtonEditor extends DefaultCellEditor {

        private String label;

        public ButtonEditor(JCheckBox checkBox) {

            super(checkBox);

        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {

            label = "Download";

            downloadButton.setText("Download");
            downloadButton.setName(value.toString());
            return downloadButton;

        }

        public Object getCellEditorValue() {

            return new String(label);

        }

    }
}