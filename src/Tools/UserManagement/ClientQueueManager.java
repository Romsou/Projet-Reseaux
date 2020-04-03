package Tools.UserManagement;

import Tools.Extended.SocketChannelExt;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientQueueManager {
    public HashMap<SocketChannelExt, ConcurrentLinkedQueue<String>> pendingMessages;

    public ClientQueueManager() {
        pendingMessages = new HashMap<>();
    }

    public void addClientQueue(SocketChannelExt client) {
        if (!pendingMessages.containsKey(client))
            pendingMessages.put(client, new ConcurrentLinkedQueue());
        else
            System.out.println("Clients already has a queue");
    }

    public void addMessage(SocketChannelExt client, String message) {
        if (pendingMessages.containsKey(client))
            pendingMessages.get(client).add(message);
        else
            System.out.println("client: " + client + " is not registered");
    }

    public void broadcast(String message) {
        for (SocketChannelExt client : pendingMessages.keySet()) {
            System.out.println("Client: " + client + "message: " + message);
            addMessage(client, message);
        }
    }

    public boolean queueIsEmpty(SocketChannelExt client) {
        return pendingMessages.get(client).isEmpty();
    }

    public boolean contains(SocketChannelExt client) {
        return pendingMessages.containsKey(client);
    }

    public String pollPendingMessage(SocketChannelExt client) {
        if (!pendingMessages.get(client).isEmpty())
            return pendingMessages.get(client).poll();
        return null;
    }

}
