package IFTO.ChatThreadss;

import javax.swing.*;
import javax.swing.text.*;

import com.formdev.flatlaf.FlatLightLaf;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.awt.event.*;

public class Client {
    private String hostname;
    private int port;
    private String userName;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private JFrame frame;
    private JTextPane textPane;  
    private JTextField textField;
    private JButton sendButton;
    private JFrame connectionFrame;
    private JTextField ipField;
    private JTextField portField;
    private JTextField userNameField;

    private Style defaultStyle;
    private Style userStyle;
    private Style systemStyle;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        createGUI();
    }

    private void createGUI() {
        frame = new JFrame("Chat - " + hostname + ":" + port);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(textPane);

        textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        sendButton = new JButton("Enviar");
        sendButton.setFont(new Font("Arial", Font.PLAIN, 14));

        sendButton.addActionListener(e -> sendMessage());
        textField.addActionListener(e -> sendMessage());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(textField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);


        StyledDocument doc = textPane.getStyledDocument();
        defaultStyle = textPane.addStyle("default", null);
        userStyle = textPane.addStyle("user", null);
        systemStyle = textPane.addStyle("system", null);

        StyleConstants.setItalic(systemStyle, true);
        StyleConstants.setForeground(systemStyle, new Color(0x006400)); 


        frame.setVisible(false);
    }

    private void createConnectionWindow() {
        connectionFrame = new JFrame("Conectar ao servidor");
        connectionFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        connectionFrame.setSize(400, 250);
        connectionFrame.setLocationRelativeTo(null);

        JLabel ipLabel = new JLabel("Digite o IP do servidor:");
        ipField = new JTextField();
        JLabel portLabel = new JLabel("Digite a porta do servidor:");
        portField = new JTextField();
        JLabel userNameLabel = new JLabel("Digite seu nome de usuário:");
        userNameField = new JTextField();

        JButton connectButton = new JButton("Conectar");
        connectButton.addActionListener(e -> connectToServer());

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(ipLabel);
        panel.add(ipField);
        panel.add(portLabel);
        panel.add(portField);
        panel.add(userNameLabel);
        panel.add(userNameField);
        panel.add(connectButton);

        connectionFrame.add(panel);
        connectionFrame.setVisible(true);
    }

    private void connectToServer() {
        try {
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());
            userName = userNameField.getText();

            connectionFrame.dispose();  

            socket = new Socket(ip, port);

            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            writer.println(userName);

            frame.setTitle("Chat - " + userName+"(você):");

            new ReadThread().start();

            frame.setVisible(true);

        } catch (UnknownHostException ex) {
            JOptionPane.showMessageDialog(frame, "Servidor não encontrado: " + ex.getMessage());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Erro de I/O: " + ex.getMessage());
        }
    }

    private void sendMessage() {
        String text = textField.getText();
        if (!text.isEmpty()) {
            writer.println(text);
            textField.setText("");
        }
    }

    class ReadThread extends Thread {
        public void run() {
            try {
                String response;
                while ((response = reader.readLine()) != null) {
                    
                    if (response.contains("entrou no chat") || response.contains("saiu do chat")) {
                        textPane.getStyledDocument().insertString(textPane.getStyledDocument().getLength(), response + "\n", systemStyle);
                    } else {
                        String style = response.startsWith(userName) ? "user" : "default";
                        textPane.getStyledDocument().insertString(textPane.getStyledDocument().getLength(), response + "\n", textPane.getStyle(style));
                    }
                    textPane.setCaretPosition(textPane.getDocument().getLength());  
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Conexão perdida: " + ex.getMessage());
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client("", 0);
        client.createConnectionWindow(); 
    }
}



