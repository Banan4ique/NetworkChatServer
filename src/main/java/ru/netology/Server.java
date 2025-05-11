package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final String SETTINGS_FILE = "src/main/resources/settings.txt";
    private static final String LOG_FILE = "src/main/resources/file.log";
    private int port;
    private List<ClientHandler> clients;
    private ExecutorService threadPool;
    private Logger logger;

    public Server() {
        setSettings(SETTINGS_FILE);
        clients = new ArrayList<>();
        threadPool = Executors.newCachedThreadPool();
        logger = new Logger(LOG_FILE);
    }

    public Server(String settingsPath) {
        setSettings(settingsPath);
        clients = new ArrayList<>();
        threadPool = Executors.newCachedThreadPool();
        logger = new Logger(LOG_FILE);
    }

    private void setSettings(String settingsPath) {
        Settings settings = new Settings(settingsPath);
        this.port = settings.getPort();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running on port " + port);
            logger.log("Server is running on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void broadcastMessage(String message, ClientHandler sender) {
        String formattedMessage = String.format("%s: %s", sender.getClientName(), message);
        logger.log(formattedMessage);
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(formattedMessage);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        broadcastMessage(client.getClientName() + " has left the chat", client);
        clients.remove(client);
    }

    public Logger getLogger() {
        return logger;
    }

    public int getPort() {
        return port;
    }

    public List<ClientHandler> getClients() {
        return clients;
    }
}
