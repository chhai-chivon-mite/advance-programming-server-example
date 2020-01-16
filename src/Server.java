import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/*
 * @author: Chhai Chivon on Jan 15, 2020
 * Senior Application Developer
 */

public class Server {
	
	public static final String[] SUPPORTED_CURRENCIES;
    public static final Map<String, Double> RATES;
    
    static {
        SUPPORTED_CURRENCIES = new String[] { "cny", "jpy", "khr", "sgd", "thb", "usd", "vnd" };
        RATES = new HashMap<String, Double>() {
            {
                this.put("cny", 7.02608);
                this.put("jpy", 108.607);
                this.put("khr", 4060.0);
                this.put("sgd", 1.37);
                this.put("thb", 30.1812);
                this.put("usd", 1.0);
                this.put("vnd", 23170.01);
            }
        };
    }
    
	public static void main(String[] agrs) {
		System.out.println("===Exchange Currency Server===");
		System.out.println("Bind to port 1234");
        try {
            Throwable t = null;
            try {
                final ServerSocket serverSocket = new ServerSocket(1234);
                try {
                    while (true) {
                        System.out.println("Waiting for clients...");
                        final Socket connection = serverSocket.accept();
                        System.out.println("Accept a client: " + connection.getInetAddress());
                        final ClientHandlerThred thread = new ClientHandlerThred(connection);
                        thread.start();
                    }
                }
                finally {
                    if (serverSocket != null) {
                        serverSocket.close();
                    }
                }
            }
            finally {
                if (t == null) {
                    final Throwable exception;
                }
                else {
                    final Throwable exception;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
	}

	static class ClientHandlerThred extends Thread
    {
        Socket connection;
        
        public ClientHandlerThred(final Socket connection) {
            this.connection = connection;
        }
        
        @Override
        public void run() {
            try {
                final Scanner streamReader = new Scanner(this.connection.getInputStream());
                final String request = streamReader.nextLine();
                this.processRequest(request);
                streamReader.close();
                this.connection.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        private void processRequest(final String request) {
            final String[] parts = request.split("#");
            final String action = parts[0];
            if (action.equals("=>list<=")) {
                this.responseListRequest();
            }
            else if (action.equals("=>convert<=")) {
                if (parts.length != 2) {
                    this.responseInvalidRequest();
                    return;
                }
                this.processConvertRequest(parts[1]);
            }
            else {
                this.responseUnknowRequest();
            }
        }
        
        private void processConvertRequest(final String data) {
            final String[] dataValues = data.split("=>");
            if (dataValues.length != 4) {
                this.responseInvalidRequest();
                return;
            }
            for (int i = 1; i < 4; ++i) {
                final String value = dataValues[i];
                if (!value.endsWith("<=")) {
                    this.responseInvalidRequest();
                    return;
                }
            }
            final String sourceCurrency = dataValues[1].replace("<=", "").toLowerCase();
            final String amountStr = dataValues[2].replace("<=", "");
            final String destinationCurrency = dataValues[3].replace("<=", "").toLowerCase();
            if (!Arrays.asList(Server.SUPPORTED_CURRENCIES).contains(sourceCurrency) || !Arrays.asList(Server.SUPPORTED_CURRENCIES).contains(destinationCurrency)) {
                this.responseInvalidRequest();
                return;
            }
            float amount;
            try {
                amount = Float.parseFloat(amountStr);
            }
            catch (NumberFormatException e) {
                this.responseInvalidRequest();
                return;
            }
            final double convertedAmount = this.convertCurrency(sourceCurrency, amount, destinationCurrency);
            System.out.println("Convert request: " + convertedAmount);
            this.responseToClient("ok", new String[] { new StringBuilder(String.valueOf(convertedAmount)).toString() });
        }
        
        private double convertCurrency(final String source, final double amount, final String destination) {
            final double sourceRate = Server.RATES.get(source);
            final double destinationRate = Server.RATES.get(destination);
            return amount * destinationRate / sourceRate;
        }
        
        private void responseToClient(final String status, final String[] dataValues) {
            final String formatedStatus = String.format("=>%s<=", status);
            String formatedData = "";
            for (final String value : dataValues) {
                formatedData = String.valueOf(formatedData) + String.format("=>%s<=", value);
            }
            try {
                final OutputStreamWriter streamWriter = new OutputStreamWriter(this.connection.getOutputStream());
                final String response = String.valueOf(formatedStatus) + "#" + formatedData + "\n";
                streamWriter.write(response);
                streamWriter.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        private void responseUnknowRequest() {
            System.out.println("Unknown request.");
            this.responseToClient("unknown", new String[] { "Unknown request." });
        }
        
        private void responseInvalidRequest() {
            System.out.println("Invalid request.");
            this.responseToClient("invalid", new String[] { "Invalid request." });
        }
        
        private void responseListRequest() {
            System.out.println("List request.");
            this.responseToClient("ok", Server.SUPPORTED_CURRENCIES);
        }
    }

}
