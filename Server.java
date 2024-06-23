import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(69);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client  = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
            shutDown();
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler connection : connections) {
            if(connection != null) {
                connection.sendMessage(message);
            }
        }
    }

    public void shutDown() {
        done = true;
        pool.shutdown();
        try {
            if(!server.isClosed()) {
                server.close();
            }
            for(ConnectionHandler connection : connections) {
                connection.shutDown();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

     class ConnectionHandler implements Runnable{

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("please enter a Username: ");
                out.flush();
                username = in.readLine();
                System.out.println("Username Connected");
                broadcast(username + " joined the chat");
                String message;
                while ((message = in.readLine()) != null) {
                    if(message.startsWith("/nick ")) {
                        String[] messageSplit = message.split(" ", 2);
                        if(messageSplit.length == 2) {
                            broadcast(username + " has changed their username to: " + messageSplit[1]);
                            System.out.println(username + " has changed their username to: " + messageSplit[1]);
                            username = messageSplit[1];
                            out.println("You successfully changed your Username to " + username);
                        } else {
                            out.println("No nickname provided");
                        }
                    } else if(message.startsWith("/quit")) {
                        broadcast(username + "left the server");
                        System.out.println(username + "left the chat");
                        shutDown();
                    } else {
                        broadcast(username + ": " + message);
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
                shutDown();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }


        public void shutDown() {
            try {
                in.close();
                out.close();
                if(!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}

