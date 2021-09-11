
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
public class Client {

    Socket socketClient;
    DataInputStream din;
    DataOutputStream dout;
    final int PORT = 8080;
    int FileLength;
    Object[][] fileList;
    String file;
    Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        new Client().run();
    }

    public Client() {
        try {
            socketClient = new Socket("localhost", PORT);
            System.out.println("Connecing...");
        } catch (Exception e) {
            //TODO: handle exception
        }
    }

    public void reciveAllFile() {
        JFrame frameReciveAllFile = new JFrame();
        frameReciveAllFile.setTitle("DOWNLOADER");
        frameReciveAllFile.setSize(780, 570);
        frameReciveAllFile.setResizable(false);
        frameReciveAllFile.setFont(new Font("TH-Sarabun-PSK", Font.BOLD, 13));
        frameReciveAllFile.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameReciveAllFile.setLocationRelativeTo(null); // ปรับให้ frame อยู่กลางจอ
        frameReciveAllFile.setBackground(Color.YELLOW);
        frameReciveAllFile.setVisible(true);

        try {
            din = new DataInputStream(socketClient.getInputStream());
            dout = new DataOutputStream(socketClient.getOutputStream());
            FileLength = din.readInt();
            fileList = new Object[FileLength][4];

            String[] colHeaderFileList = {"All File", "File Type", "Size", "Action"}; //เก็บชื่อหัวตาราง
            String[][] rowFileList = new String[FileLength][4]; //เก็บชื่อไฟล์ต่างๆ
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
                rowFileList[i][0] = "  " + fileList[i][0].toString().substring(0,fileList[i][0].toString().indexOf("."));
                rowFileList[i][1] = fileList[i][1].toString().substring(fileList[i][1].toString().indexOf("/") + 1, fileList[i][1].toString().length());;
                rowFileList[i][2] = fileList[i][2].toString() + " KB  ";
            }

                JPanel panelFileList = new JPanel();
                panelFileList.setBackground(Color.YELLOW);
                    JTable tableFileList = new JTable(rowFileList, colHeaderFileList); //แสดง rowFileList กับ colHeaderFileList ลงในตาราง
                    tableFileList.setVisible(true);
                    tableFileList.setFont(new Font("TH-Sarabun-PSK", Font.BOLD, 13)); //แก้ตัวอักษร
                    tableFileList.setRowHeight(40); //ปรับความสูง row
                    tableFileList.setPreferredScrollableViewportSize(new Dimension(750,500)); //ปรับขนาดตาราง
                    tableFileList.getColumnModel().getColumn(0).setPreferredWidth(400); //ปรับขนาดคอลัม
                    tableFileList.setFocusable(false);
                    tableFileList.setBackground(Color.getHSBColor(0, 0, 30));
                    // ปรับข้อความชิดซ้ายชิดขวา
                    DefaultTableCellRenderer d = new DefaultTableCellRenderer();
                    DefaultTableCellRenderer d2 = new DefaultTableCellRenderer();
                    d.setHorizontalAlignment(JLabel.CENTER);
                    tableFileList.getColumnModel().getColumn(1).setCellRenderer(d);
                    d2.setHorizontalAlignment(JLabel.RIGHT);
                    tableFileList.getColumnModel().getColumn(2).setCellRenderer(d2);

                    JScrollPane scrollPaneFileList = new JScrollPane(tableFileList);
                    panelFileList.add(scrollPaneFileList);
            frameReciveAllFile.add(panelFileList);

            //JButton dowloadButton = new JButton();
            //dowloadButton.addActionListener(e -> {});
            reqFile();

        } catch (Exception e) {
            System.out.println("can't connecting");
        }

    }

    public void reqFile() {
        while (true) {
            file = sc.nextLine();
            if (file.equals("Exit")) {
                break;
            }
            try {
                dout = new DataOutputStream(socketClient.getOutputStream());
                dout.writeUTF(file);
                reciveReqrFile();
            } catch (Exception e) {

            }
        }
    }

    public void reciveReqrFile() throws IOException {
        din = new DataInputStream(socketClient.getInputStream());
        int size = din.readInt();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    System.out.println(Thread.currentThread().getName());
                    Socket socket = new Socket("localhost", 8087);
                    DataInputStream dinClient = new DataInputStream(socket.getInputStream());
                    String filePath = "C:/Users/katakarn/Desktop/Client Files/" + file;
                    int startIndex = dinClient.readInt();
                    int fileLength = dinClient.readInt();
                    RandomAccessFile writer = new RandomAccessFile(filePath, "rw");
                    writer.seek(startIndex);
                    byte[] data = new byte[fileLength];
                    int receive = 0;
                    while (receive > -1) {
                        receive = dinClient.read(data);
                        if (receive == -1) {
                            break;
                        }
                        writer.write(data, 0, receive);
                    }
                    System.out.println("finish");
                    //File fileDownload = new File(filePath);
                    //FileOutputStream fout = new FileOutputStream(fileDownload);
                    dinClient.close();
                    writer.close();
                    socket.close();
                    System.out.println("Recive");
                } catch (Exception e) {
                    //System.out.println("Can't Recive");
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public void run() throws IOException {
        reciveAllFile();
    }
}
