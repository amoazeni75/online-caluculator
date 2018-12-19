import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) {

        ExecutorService pool = Executors.newCachedThreadPool();
        int serviceLimit = 10;
        int acceptedClient = 1;

        try (ServerSocket server = new ServerSocket(7654)) {
            System.out.print("Server started.\nWaiting for a client ... ");
            while (acceptedClient <= serviceLimit) {
                Socket client = server.accept();
                System.out.println("client" + acceptedClient + " accepted!");
                pool.execute(new ClientHandler(client, acceptedClient));
                acceptedClient++;
            }
            pool.shutdown();
            System.out.print("done.\nClosing server ... ");
        } catch (IOException ex) {
            System.err.println(ex);
        }
        System.out.println("done.");
    }

}

class ClientHandler implements Runnable {

    private Socket client;
    private int clientID;

    public ClientHandler(Socket client, int clientID) {
        this.client = client;
        this.clientID = clientID;
    }

    @Override
    public void run() {
        try {
            // for reading and writing from/to socket
            OutputStream out = client.getOutputStream();
            InputStream in = client.getInputStream();


            while(true) {
                byte[] buffer = new byte[2048];
                int read = in.read(buffer);
                String input = new String(buffer, 0, read);

                //check for closing connection
                if(input.equals("end"))
                    break;

                //process string execution
                Result res = processExpression(input);
                String resultMessage;
                if(res.isValidity()){
                    resultMessage = "$ calculation time : " + res.getElapsedTime() + "$ result : " + res.getValue() + "$";
                    out.write(resultMessage.getBytes());
                }else{
                    resultMessage = "Something wrong, please try again or end session";
                    out.write(resultMessage.getBytes());
                }
            }
            System.out.print("Closing client ... ");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }

    private Result processExpression(String inputReq){
        Result result = new Result(0, true, 0.0);
        parseInputString(inputReq, result);
        if(!result.isValidity())
            return result;

        long startTime = System.nanoTime();
        if(!calculate(result))
            return result;
        long endTime = System.nanoTime();
        long timeElapsed = (endTime - startTime) / 1000000; //for converting into milliseconds
        result.setElapsedTime(timeElapsed);
        return result;
    }

    /**
     * this function will parse entered string and extract op1 and op2 and operator
     * @param inp is entered string
     * @return true if expression is valid
     */
    private boolean parseInputString(String inp, Result result){
        ArrayList<Integer> dollarIndex = new ArrayList<Integer>();

        //remove all spaces from entered string
        inp = inp.replaceAll("\\s+","");

        //find all index of "$"
        int index = inp.indexOf('$');
        while (index >= 0) {
            dollarIndex.add(index);
            index = inp.indexOf('$', index + 1);
        }

        //this means it has incorrect format
        if(dollarIndex.size() != 3 || dollarIndex.size() != 4) {
            result.setValidity(false);
            return false;
        }

        String operator = inp.substring(dollarIndex.get(0), dollarIndex.get(1) - 1);
        operator = operator.toUpperCase();
        if(!validateOperator(operator)) {
            result.setValidity(false);
            return false;
        }

        Double op1 = getOperand(dollarIndex.get(1), dollarIndex.get(2), inp);
        if(op1 == null) {
            result.setValidity(false);
            return false;
        }

        Double op2 = null;
        if(dollarIndex.size() == 4){
            op2 = getOperand(dollarIndex.get(2), dollarIndex.get(3), inp);
            if(op2 == null) {
                result.setValidity(false);
                return false;
            }
        }

        result.setOperator(operator);
        result.setOp1(op1);
        result.setOp2(op2);
        return true;
    }

    /**
     * this function extract operand and convert it to double
     * @return
     */
    private Double getOperand(int startIndex, int endIndex, String inp){
        Double res = null;
        try {
            res = Double.parseDouble(inp.substring(startIndex, endIndex - 1));
        }
        catch (NumberFormatException E){
            return null;
        }
        return res;
    }

    /**
     * this function check operator in which support or not
     * @param inp
     * @return
     */
    private boolean validateOperator(String inp){
        if(inp.equals("ADD") || inp.equals("SUBTRACT") || inp.equals("DIVIDE") || inp.equals("MULTIPLY") ||
                inp.equals("SIN") || inp.equals("COS") || inp.equals("TAN") || inp.equals("COT"))
            return true;
        return false;
    }

    /**
     * in this function we try to calculate
     * @param result
     * @return
     */
    private boolean calculate(Result result){

        if(result.getOperator().equals("ADD")){
            result.setValue(result.getOp1() + result.getOp2());
            return true;
        }
        else if(result.getOperator().equals("SUBTRACT")){
            result.setValue(result.getOp1() - result.getOp2());
            return true;
        }
        else if(result.getOperator().equals("DIVIDE")){
            if(result.getOp2() == 0){
                result.setValidity(false);
                return false;
            }
            result.setValue(result.getOp1() / result.getOp2());
            return true;
        }
        else if(result.getOperator() == "MULTIPLY"){
            result.setValue(result.getOp1() * result.getOp2());
            return true;
        }
        else if(result.getOperator() == "SIN"){
            result.setValue(Math.sin(result.getOp1()));
            return true;
        }
        else if(result.getOperator() == "COS"){
            result.setValue(Math.cos(result.getOp1()));
            return true;
        }
        else if(result.getOperator() == "TAN"){
            if(result.getOp1() != 90){
                result.setValue(Math.tan(result.getOp1()));
                return true;
            }
            else{
                result.setValidity(false);
                return false;
            }
        }
        else if(result.getOperator() == "COT"){
            if(result.getOp1() != 0){
                result.setValue(Math.tan(result.getOp1()));
                return true;
            }
            else{
                result.setValidity(false);
                return false;
            }
        }
        return false;
    }
}

class Result{
    private double value;
    private boolean validity;
    private double elapsedTime;
    private Double op1;
    private Double op2;
    private String operator;

    public Result(double v, boolean b, double e){
        this.value = v;
        this.validity = b;
        this.elapsedTime = e;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setValidity(boolean validity) {
        this.validity = validity;
    }

    public double getValue() {
        return value;
    }

    public boolean isValidity() {
        return validity;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Double getOp1() {
        return op1;
    }

    public void setOp1(Double op1) {
        this.op1 = op1;
    }

    public Double getOp2() {
        return op2;
    }

    public void setOp2(Double op2) {
        this.op2 = op2;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

}