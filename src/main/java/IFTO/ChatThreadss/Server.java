package IFTO.ChatThreadss;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;

public class Server {
    private static Set<ClientHandler> clientHandlers = new HashSet<>();
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private static ServerSocket serverSocket;
    private static JFrame frame;
    private static JButton startButton, exitButton;
    private static JLabel statusLabel;
    private static int port;

    public static void main(String[] args) {
        try {

            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }


        createGUI();
    }


    private static void createGUI() {
    	
        frame = new JFrame("Servidor de Chat");

        startButton = new JButton("Iniciar Servidor");
        exitButton = new JButton("Sair Servidor");
        statusLabel = new JLabel("Servidor não iniciado.");

        exitButton.setEnabled(false);

        startButton.addActionListener(e -> startServer());
        exitButton.addActionListener(e -> stopServer());

        JPanel panel = new JPanel();
        panel.add(startButton);
        panel.add(exitButton);
        panel.add(statusLabel);

        frame.add(panel);
        frame.setSize(300, 150);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }


    private static void startServer() {
        String portStr = JOptionPane.showInputDialog("Digite a porta para o servidor:");
        port = Integer.parseInt(portStr);

        try {

            serverSocket = new ServerSocket(port);
            statusLabel.setText("Servidor iniciado na porta: " + port);


            startButton.setEnabled(false);
            exitButton.setEnabled(true);


            new Thread(() -> {
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        ClientHandler clientHandler = new ClientHandler(socket);
                        clientHandlers.add(clientHandler);
                        new Thread(clientHandler).start();
                    } catch (IOException e) {
                        if (!serverSocket.isClosed()) {
                            System.out.println("Erro ao aceitar conexão: " + e.getMessage());
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Erro ao iniciar o servidor: " + e.getMessage());
        }
    }


    private static void stopServer() {
        try {

            for (ClientHandler client : clientHandlers) {
                client.sendMessage("O servidor foi fechado. Você foi desconectado.");
                client.closeConnection();
            }
            clientHandlers.clear();
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            statusLabel.setText("Servidor não iniciado.");

            startButton.setEnabled(true);
            exitButton.setEnabled(false);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Erro ao fechar o servidor: " + e.getMessage());
        }
    }


    static void broadcast(String message) {
        for (ClientHandler client : clientHandlers) {
            client.sendMessage(message);
        }
    }

    static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter writer;
        private String userName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                OutputStream output = socket.getOutputStream();
                writer = new PrintWriter(output, true);

                userName = reader.readLine();
                broadcast(sdf.format(new Date()) + " - " + userName + " entrou no chat.");

                String serverMessage = "Novo usuário conectado: " + userName;
                System.out.println(serverMessage);

                String clientMessage;

                while ((clientMessage = reader.readLine()) != null) {
                    String message = sdf.format(new Date()) + " - " + userName + ": " + clientMessage;
                    System.out.println(message);
                    broadcast(message);
                }
            } catch (IOException ex) {
                System.out.println("Erro no cliente: " + ex.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                removeClient(this);
                String disconnectMessage = sdf.format(new Date()) + " - " + userName + " saiu do chat.";
                broadcast(disconnectMessage);  
                System.out.println(userName + " desconectou.");
            }
        }

        void sendMessage(String message) {
            writer.println(message);
        }

        void closeConnection() throws IOException {
            socket.close();
        }
    }
}
