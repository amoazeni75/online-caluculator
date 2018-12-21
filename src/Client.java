import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {

    private static String ipAdd;
    private static int port;

    public static void main(String[] args) {
        //default values;
        boolean continueFlag = true;
        ipAdd = "127.0.0.1";
        port = 7654;

        System.out.println("Hi, Please input empty line for default setting or server address ip and port in this format: \"$ip address$port$\"");
        Scanner scanner = new Scanner(System.in);
        String messages = scanner.nextLine();
        if (!getIPandPort(messages)) {
            System.out.println("something wrong, exiting...");
            return;
        }

        try (Socket server = new Socket(ipAdd, port)) {
            System.out.println("Connected to server.");
            OutputStream out = server.getOutputStream();
            InputStream in = server.getInputStream();
            while (continueFlag) {
                byte[] buffer = new byte[2048];
                System.out.println("please enter \"end\" to exit or expression in this format: \"$operator$operand1$operand2$\"");
                messages = scanner.nextLine();
                if(messages.equals("end"))
                    continueFlag = false;
                out.write(messages.getBytes());
                System.out.println("SENT: " + messages);
                int read = in.read(buffer);
                System.out.println("RECV: " + new String(buffer, 0, read));
            }

            System.out.print("All messages sent.\nClosing ... ");
        } catch (IOException ex) {
            System.err.println(ex);
        }
        System.out.println("done.");
    }

    private static boolean getIPandPort(String inpMsg) {
        boolean res = true;
        if (inpMsg.equals(""))
            return  true;
        ArrayList<Integer> dollarIndex = new ArrayList<Integer>();

        inpMsg = inpMsg.replaceAll("\\s+", "");
        int index = inpMsg.indexOf('$');
        while (index >= 0) {
            dollarIndex.add(index);
            index = inpMsg.indexOf('$', index + 1);
        }

        //this means it has incorrect format
        if (dollarIndex.size() != 3)
            return false;
        ipAdd = inpMsg.substring(dollarIndex.get(0) + 1, dollarIndex.get(1));
        try {
            port = Integer.parseInt(inpMsg.substring(dollarIndex.get(1) + 1, dollarIndex.get(2)));
        }catch (NumberFormatException e){
            return  false;
        }
        return true;
    }
}
