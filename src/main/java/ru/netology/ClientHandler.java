package ru.netology;

import java.io.*;
import java.net.Socket;

/**
 * Обработчик клиентского подключения. Работает в отдельном потоке для каждого клиента.
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Server server;
    private final Logger logger;
    private String clientName;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, Server server) {
        this.clientSocket = socket;
        this.server = server;
        this.logger = server.getLogger();
    }

    @Override
    public void run() {
        try {
            // Инициализация потоков ввода/вывода
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Получаем имя клиента (первое сообщение)
            clientName = in.readLine();
            if (clientName == null || clientName.isEmpty()) {
                clientName = "Anonymous" + Thread.currentThread().getId();
            }

            // Уведомляем всех о новом пользователе
            server.broadcastMessage(clientName + " joined the chat!", this);
            logger.log("SYSTEM: " + clientName + " connected");

            // Обрабатываем входящие сообщения
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Проверяем команду выхода
                if ("/exit".equalsIgnoreCase(inputLine)) {
                    break;
                }
                server.broadcastMessage(inputLine, this);
            }
        } catch (IOException e) {
            System.err.println("Error handling client #" + clientName + ": " + e.getMessage());
        } finally {
            try {
                // Удаляем клиента и закрываем соединение
                server.removeClient(this);
                clientSocket.close();
                logger.log("SYSTEM: " + clientName + " disconnected");
            } catch (IOException e) {
                System.err.println("Couldn't close client socket: " + e.getMessage());
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getClientName() {
        return clientName;
    }
}
