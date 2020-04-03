package Tools.UserManagement;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientQueueManager {
    private HashMap<SocketChannel, ConcurrentLinkedQueue<String>> pendingMessages;

    public ClientQueueManager() {
        pendingMessages = new HashMap<>();
    }

    public void addClientQueue(SocketChannel client) {
        if (!pendingMessages.containsKey(client))
            pendingMessages.put(client, new ConcurrentLinkedQueue());
        else
            System.out.println("Clients already has a queue");
    }

    public void addMessage(SocketChannel client, String message) {
        if (pendingMessages.containsKey(client))
            pendingMessages.get(client).add(message);
        else
            System.out.println("client: " + client + " is not registered");
    }

    public void broadcast(String message) {
        for (SocketChannel client : pendingMessages.keySet())
            addMessage(client, message);
    }

}
