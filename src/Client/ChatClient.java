package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ChatClient {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private String remoteHost;
    private int remotePort;

    private String pseudo;

    public ChatClient(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.connect();
        this.openStreams();
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost", 1234);

        client.getLogin();
        client.sendLogin();

        Thread sender = new Thread(new Sender(client.socket, client.reader, client.writer));
        Thread receiver = new Thread(new Receiver(client.socket, client.reader, client.writer));

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

    static class Sender implements Runnable {
        Socket socket;
        BufferedReader reader;
        PrintWriter writer;

        public Sender(Socket socket, BufferedReader reader, PrintWriter writer) {
            this.socket = socket;
            this.reader = reader;
            this.writer = writer;
        }

        @Override
        public void run() {
            this.send();
        }

        public void send() {
            Scanner scanner = new Scanner(System.in);
            String line;

            while (socket.isConnected()) {
                System.out.print("> ");
                if (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    writer.println("MESSAGE ".concat(line).concat(" envoye"));
                    writer.flush();
                } else {
                    this.closeConnection();
                    System.exit(17);
                }
            }
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


    static class Receiver implements Runnable {
        Socket socket;
        BufferedReader reader;
        PrintWriter writer;

        public Receiver(Socket socket, BufferedReader reader, PrintWriter writer) {
            this.socket = socket;
            this.reader = reader;
            this.writer = writer;
        }

        @Override
        public void run() {
            this.receive();
        }

        public void receive() {
            String line;
            while (socket.isConnected()) {
                try {
                    if (reader.ready()) {
                        line = reader.readLine();
                        System.out.print(line + "\n> ");
                    }
                } catch (IOException e) {
                    closeConnection();
                    System.exit(16);
                }
            }
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

}



