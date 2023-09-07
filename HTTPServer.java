import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Date;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

public class HTTPServer extends Thread {
    public static void main(String args[]){
        int serverPort = 5000;
        ServerSocket listenSocket = null;
        Socket clienteSocket = null;

        try{
            listenSocket = new ServerSocket(serverPort);

            while(true){
                System.out.println("Servidor HTTP iniciado na porta 5000");
                System.out.println("Aguardando requests dos clientes...");
                clienteSocket = listenSocket.accept();
                System.out.println("Request recebido... SERVIDOR INICIADO");

                Connection c = new Connection(clienteSocket);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }  
}      
    
class Connection extends Thread {
    DataInputStream inCliente;
    DataOutputStream outCliente;
    Socket clienteSocket;

    //construtor da classe, passando como parametro o socket
    public Connection (Socket clien) throws Exception{
        clienteSocket = clien;
        inCliente = new DataInputStream(clienteSocket.getInputStream());
        outCliente = new DataOutputStream(clienteSocket.getOutputStream());
        this.start();
    }

    //iniciando a thread
    public void run(){
        try{
            System.out.println();
            System.out.println("O cliente " + clienteSocket.getInetAddress() + ": " + clienteSocket.getPort() + " esta conectado");
            System.out.println();
           
            String requestString = inCliente.readUTF();
            String headerLine = requestString;

            //para quando a requisicao eh vazia
            if (headerLine == null){
                sendResponse(500, "<b> Error </b>");
            }

            //tratando a requisicao do cliente
            StringTokenizer token = new StringTokenizer(headerLine, " ");
            ArrayList<String> comandos =  new ArrayList<>(); 

            while(token.hasMoreTokens()){
                comandos.add(token.nextToken());
            }

            String httpMethod = comandos.get(0);
            String httpQueryString = comandos.get(1);
                    
            System.out.println("HTTP Request: " + httpMethod + " " + httpQueryString);
            System.out.println();

            //METODOS HTTP
            if(httpMethod.equals("GET")){ //metodo GET

                if(httpQueryString.equals("/")){
                    String httpVersion = comandos.get(2);

                    if(httpVersion == "HTTP/1.0"){
                        //retorna a pagina inicial, status code 200
                        homePage();
                    }else{
                        System.out.println(httpVersion);
                        sendResponse(505, "<b> Versão HTTP utilizada na requisicao nao eh suportada pelo servidor! </b>");
                    }
                    
                }else if(httpQueryString.startsWith("/hello.html")){
                    //retorna a pagina hello
                    helloPage(httpQueryString.substring(httpQueryString.lastIndexOf('/') + 1, httpQueryString.length()));
                }else{
                    //mensagem de erro 404, pagina nao encontrada
                    sendResponse(404, "<b> A requisicao solicitada nao foi encontrada. </b>");
                }

            }else if(httpMethod.equals("POST")){ //metodo POST

                if(httpQueryString.startsWith("/hello.html")){
                    String httpRest = comandos.get(2);
                    postHello(httpRest);
                }else if(httpQueryString.startsWith("/helloo.html")){
                    sendResponse(304, "<b> Nada alterado, sem conteudo presente. </b>");
                }else{
                    //mensagem de erro 404, pagina nao encontrada
                    sendResponse(404, "<b> A requisicao solicitada nao foi encontrada. </b>");
                }

            }else if(httpMethod.equals("PUT")){ //metodo PUT

                if(httpQueryString.startsWith("/hello.html")){
                    //substitui a pagina hello
                    newHelloPage(httpQueryString.substring(httpQueryString.lastIndexOf('/') + 1, httpQueryString.length()));
                }else if(httpQueryString.startsWith("/helloRe.html")){
                    sendResponse(204, "<b> No content. </b>.");
                }else{
                    sendResponse(404, "<b> A requisicao solicitada nao foi encontrada. </b>");
                }

            }else{
                //mensagem de erro 404, pagina nao encontrada
                sendResponse(400, "<b> Requisicao mal colocada. </b>");
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

        
    //metodos de resposta para o cliente
    public void sendResponse(int statusCode, String responseString) throws Exception{
        if(statusCode == 200){ 
            //status de sucesso
            outCliente.writeUTF("HTTP/1.0 200 OK" + "\n" + 
                                "Date: " + getTimeStamp() + "\n" + 
                                "Server: localhost" + "\n" + 
                                "Content-Type: text/html; charset=UTF-8" + "\n" + 
                                "Connection: Closed" + "\n\n" +
                                responseString);

        }else if(statusCode == 201){ 
            //status de sucesso
            outCliente.writeUTF("HTTP/1.0 201 Created" + "\n" +
                                "Content-Location: /new.html" + "\n\n" +
                                responseString);

        }else if(statusCode == 204){ 
            //status de sucesso
            outCliente.writeUTF("HTTP/1.0 204 No Content" + "\n" +
                                "Content-Location: /hello.html" + "\n\n" +
                                responseString);

        }else if(statusCode == 304){ 
            //status de redirecionamento
            outCliente.writeUTF("HTTP/1.0 304 Not Modified" + "\n" +
                                "Date: " + getTimeStamp() + "\n" +
                                "Server: localhost" + "\n\n" +
                                responseString);

        }else if(statusCode == 400){ 
            //status de erro do cliente
            outCliente.writeUTF("HTTP/1.0 400 Bad Request" + "\n" +
                                "Content-Length: " + responseString.length() + "\n" +
                                "Content-Type: text.html; charset=UTF-8" + "\n" +
                                "Date: " + getTimeStamp() + "\n" +
                                "Server: localhost" + "\n\n" +
                                responseString);

        }else if(statusCode == 404){ 
            //status de erro do cliente
            outCliente.writeUTF("HTTP/1.0 404 Not Found" + "\n" +
                                "Date: " + getTimeStamp() + "\n" +
                                "Server: localhost" + "\n\n" +
                                responseString);

        }else if(statusCode == 411){ 
            //status de erro do cliente
            outCliente.writeUTF("HTTP/1.0 411 Length Required" + "\n" +
                                "Content-Type: text/html; charset=UTF-8" + "\n" +
                                "Referrer-Policy: no-referrer" + "\n" +
                                "Content-Length: " + responseString.length() + "\n" +
                                "Date: " + getTimeStamp() + "\n" +
                                responseString);

        }else if(statusCode == 500){ 
            //status de erro de servidor
            outCliente.writeUTF("HTTP/1.0 500 Internal Server Error" + "\n" +
                                "Date: " + getTimeStamp() + "\n" +
                                "Server: localhost" + "\n" +
                                "Content-Type: text/html; charset=UTF-8" + "\n" +
                                "Connection: Closed" + "\n\n" +
                                responseString);

        }else if(statusCode == 505){ 
            //status de erro de servidor, nao reconhecimento da versao solicitada na requisicao
            outCliente.writeUTF("505 Version Not Supported" + "\n" +
                                responseString);

        }else{
            outCliente.writeUTF("Status de codigo nao valido!" + "\n\n" +
                                responseString);
        }
    }

    public static String getTimeStamp(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    public void homePage() throws Exception{
        StringBuffer responseBuffer = new StringBuffer();
        responseBuffer.append("<b> HTTP SERVER HOME PAGE. </b><BR><BR>");
        sendResponse(200, responseBuffer.toString());
    }

    public void helloPage(String name) throws Exception{
        StringBuffer responseBuffer = new StringBuffer();
        responseBuffer.append("<b> Hello: ").append(name).append("</b><BR>");
        sendResponse(200, responseBuffer.toString());
    }

    public void postHello(String content) throws Exception{
        StringBuffer responseBuffer = new StringBuffer();
        String responseBuffer2 = " ";
        responseBuffer2 = content;
        responseBuffer.append("<b> Hello: ").append("hello").append("</b><BR>\n\n").append("<b> ").append(responseBuffer2).append(" </b><BR>");
        sendResponse(200, responseBuffer.toString());
    }

    public void newHelloPage(String name) throws Exception{
        StringBuffer responseBuffer = new StringBuffer();
        responseBuffer.append("<b> New hello: ").append(name).append("</b><BR>\n\n").append("<b> Criado. </b>");
        sendResponse(201, responseBuffer.toString());
    }

}
