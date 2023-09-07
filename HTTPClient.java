import java.net.*;
import java.io.*;
import java.util.Scanner;

public class HTTPClient{
    public static void main(String[] args){
        Socket socket = null;
        int serverPort = 5000;
        Scanner sc = new Scanner(System.in);

        try{
            socket = new Socket ("127.0.0.1", serverPort);

            DataInputStream inCliente = new DataInputStream(socket.getInputStream());
            DataOutputStream outCliente = new DataOutputStream(socket.getOutputStream());

            while(true){
                System.out.println("-----------------------------------------------");
                System.out.println("SERVIDOR HTTP AGUARDANDO REQUEST...");
                System.out.println("-----------------------------------------------");

                String request = sc.nextLine();
                outCliente.writeUTF(request);
                String data = inCliente.readUTF();
                System.out.println(data);
                System.out.println("Conexao ao host perdida.");
            }
        }catch (UnknownHostException ex){
            ex.printStackTrace();
        }catch (IOException ex){
            ex.printStackTrace();
        }

    }
}