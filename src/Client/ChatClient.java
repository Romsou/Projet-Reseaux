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

        if (client == null)
            throw new NullPointerException("Pas de client");

        client.getLogin();
        client.sendLogin();

        while (true) {
            client.send();
            client.receive();
        }
    }

    private void connect() {
        InetAddress remoteAddress = createAddress();
        this.socket = connectSocket(remoteAddress);
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

    private void openStreams() {
        this.reader = createReader();
        this.writer = createWriter();
    }

    private BufferedReader createReader() {
        try {
            return new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("createReader: opening of the input stream of the socket has failed");
            System.exit(13);
        }
        return null;
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

    public void send() {
        Scanner scanner = new Scanner(System.in);
        String line = "";

        System.out.print("> ");
        if (scanner.hasNextLine()) {
            line = scanner.nextLine();
            this.writer.println(line);
        }
    }

    public void receive() {
        try {
            if (this.reader.ready())
                System.out.println(this.reader.readLine());
        } catch (IOException e) {
            System.err.println("receive: problem with the input reader");
            this.closeConnection();
            System.exit(16);
        }
    }
}
