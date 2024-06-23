import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable{

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 69);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inputHandler = new InputHandler();
            Thread thread = new Thread(inputHandler);
            thread.start();

            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void shutDown() {
        done = true;
        try {
           in.close();
           out.close();
           if (!client.isClosed()) {
               client.close();
           }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class InputHandler implements Runnable {

        @Override
        public void run() {
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                while(!done) {
                    String message = inputReader.readLine();
                    if(message.equals("/quit")) {
                        out.println("/quit");
                        inputReader.close();
                        shutDown();
                    } else {
                        out.println(message);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                shutDown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
