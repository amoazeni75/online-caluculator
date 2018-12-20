import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        try (Socket server = new Socket("127.0.0.1", 7654)) {
            System.out.println("Connected to server.");
            OutputStream out = server.getOutputStream();
            InputStream in = server.getInputStream();
            byte[] buffer = new byte[2048];
            System.out.println("please enter the message in this format: \"$ operator $ operand1 $ operand2 $\"");
//            Scanner scanner = new Scanner(System.in);
//            String messages = scanner.nextLine();
            String messages = "$ ADD $ 3 $ 5 $";
            out.write(messages.getBytes());
            System.out.println("SENT: " + messages);
            int read = in.read(buffer);
            System.out.println("RECV: " + new String(buffer, 0, read));
            System.out.print("All messages sent.\nClosing ... ");
        } catch (IOException ex) {
            System.err.println(ex);
        }
        System.out.println("done.");
    }
}
