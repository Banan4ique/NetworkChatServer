package ru.netology;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class ServerTest {
    private Server server;

    @BeforeEach
    public void setUp() {
        server = new Server();
    }

    @Test
    public void testSetSettings() {
        File tempFile = new File("test_settings.txt");
        try (PrintWriter writer = new PrintWriter(tempFile)) {
            writer.println("port=12345");
        } catch (IOException e) {
            fail("Failed to create test settings file");
        }

        Server testServer = new Server("test_settings.txt");
        assertEquals(12345, testServer.getPort());

        tempFile.delete();
    }

    @Test
    public void testBroadcastMessage() {
        ClientHandler mockClient1 = mock(ClientHandler.class);
        ClientHandler mockClient2 = mock(ClientHandler.class);

        server.getClients().add(mockClient1);
        server.getClients().add(mockClient2);

        server.broadcastMessage("Test message", mockClient1);

        verify(mockClient2, times(1)).sendMessage(anyString());
        verify(mockClient1, never()).sendMessage(anyString());
    }
}
