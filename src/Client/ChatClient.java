package Client;

import Protocol.ProtocolHandler;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ChatClient {
    Socket socket;
    BufferedReader reader;
    PrintWriter writer;

    String remoteHost;
    int remotePort;

    String pseudo;

    public ChatClient(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.connect();
        this.openStreams();
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost", 12345);

        client.getLogin();
        client.sendLogin();

        Thread sender = new Thread(new Sender(client));
        Thread receiver = new Thread(new Receiver(client));

        sender.start();
        receiver.start();
    }

    private InetAddress createAddress() {
        try {
            return InetAddress.getByName(this.remoteHost);
        } catch (UnknownHostException e) {
            System.err.println("createAdress: Create of the address has failed");
            System.exit(11);
        }
        return null;
    }

    private Socket connectSocket(InetAddress remoteAddress) {
        try {
            return new Socket(remoteAddress, this.remotePort);
        } catch (IOException e) {
            System.err.println("connectSocket: Connection to ".concat(this.remoteHost).concat("has failed"));
            System.exit(12);
        }
        return null;
    }

    private void connect() {
        InetAddress remoteAddress = createAddress();
        this.socket = connectSocket(remoteAddress);
    }

    private void openStreams() {
        this.reader = createReader();
        this.writer = createWriter();
    }

    private PrintWriter createWriter() {
        try {
            return new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (IOException e) {
            System.err.println("createWriter: opening of the output stream of the socket has failed");
            System.exit(14);
        }
        return null;
    }

    public void getLogin() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Entrez login: ");
        this.pseudo = scanner.nextLine().trim();
    }

    public void sendLogin() {
        if (this.pseudo == null)
            throw new NullPointerException("sendLogin: No login found");
        writer.println("LOGIN ".concat(this.pseudo));
    }

    private BufferedReader createReader() {
        try {
            return new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("createReader: opening of the input stream of the socket has failed");
            System.exit(13);
        }
        return null;
    }

    public void closeConnection() {
        this.writer.close();
        try {
            this.reader.close();
            this.socket.close();
        } catch (IOException e) {
            System.err.println("closeConnection: Connection could not close properly");
            System.exit(15);
        }
    }

}


class Sender implements Runnable {
    ChatClient client;

    public Sender(ChatClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        this.send();
    }

    public void send() {
        Scanner scanner = new Scanner(System.in);
        String message;

        while (client.socket.isConnected()) {
            System.out.print("> ");
            if (scanner.hasNextLine()) {
                message = scanner.nextLine();
                client.writer.println(new ProtocolHandler().addProtocolHeaders(message));
                client.writer.flush();
            } else {
                client.closeConnection();
                System.exit(17);
            }
        }
    }


}


class Receiver implements Runnable {
    ChatClient client;

    public Receiver(ChatClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        this.receive();
    }

    public void receive() {
        String message;
        while (client.socket.isConnected()) {
            try {
                if (client.reader.ready()) {
                    message = client.reader.readLine();

                    String[] messageParts = message.split(" ");
                    if (ProtocolHandler.isLoginError(messageParts)) {
                        client.closeConnection();
                    }
                    System.out.print(new ProtocolHandler().stripProtocolHeaders(message) + "\n> ");
                }
            } catch (IOException e) {
                client.closeConnection();
                System.exit(16);
            }
        }
    }

}



