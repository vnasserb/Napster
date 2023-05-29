package sd;
import com.google.gson.Gson;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Peer{

  // HashMap onde a chave é o nome do arquivo e o valor é uma lista de Peers que contém esse arquivo
  public static Map<String, List<String>> PeersWithFiles = new HashMap<>();
  
  // Check se o Peer está inserido no Servidor
  public static boolean IsInServer = true; 

  public static void sendDatagramPacket(DatagramSocket socket, byte[] outputDataBuffer, String ipAddress, int port) throws IOException {
    InetAddress inetAddress = InetAddress.getByName(ipAddress);
    DatagramPacket outputPacket = new DatagramPacket(outputDataBuffer, outputDataBuffer.length, inetAddress, port);
    socket.send(outputPacket);
  }

  // Função que converte uma Lista em String
  public static String List2String(List<String> list) throws IOException{
    String delim = ",";
    StringBuilder sb = new StringBuilder();
    int i = 0;
    while (i < list.size() - 1){
      sb.append(list.get(i));
      sb.append(delim);
      i++;
    }
    sb.append(list.get(i));
    String res = sb.toString();
    return res;
  }
  
  public static void sendFile(Socket socket, String File) throws Exception{

    int bytes = 0;
    
    // Escolhe o arquivo a ser enviado
    File file = new File("C:\\"+File);
    
    // Cria um FileInputStream  
    FileInputStream fileInputStream = new FileInputStream(file);
        
    // Cria um DataOutputStream
    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
    dataOutputStream.writeLong(file.length());  
    
    // Quebra e envio de partes do arquivo iterativamente
    byte[] buffer = new byte[4*1024];
    while ((bytes=fileInputStream.read(buffer))!=-1){
        dataOutputStream.write(buffer,0,bytes);
        dataOutputStream.flush();
    }
    fileInputStream.close();
  }

  private static void receiveFile(Socket socket, String File) throws Exception{

    int bytes = 0;
    
    // Criação do arquivo novo
    FileOutputStream fileOutputStream = new FileOutputStream("C:\\"+File);
    
    // Criação do DataInputStream
    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
    
    // Leitura do tamanho do arquivo
    long size = dataInputStream.readLong();     
    byte[] buffer = new byte[4*1024];
    
    // Passagem do conteúdo para o FileOutputStream de forma iterativa
    while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
        fileOutputStream.write(buffer,0,bytes);
        size -= bytes;      
    }
    fileOutputStream.close();
  }

  public static class ThreadAtendimento_Do_UDP extends Thread{

    //Socket a ser utilizado
    public DatagramSocket clientSocket;
    
    //Mensagem JSON como String
    private String JSON_Message;

    //Combinação de portas TCP/UDP do Peer
    private String Ports;
    
    // Construtor da ThreadAtendimento_Do
    public ThreadAtendimento_Do_UDP(DatagramSocket CS, String JSON, String p){
      clientSocket = CS;
      JSON_Message = JSON;
      Ports = p;
    }

    public void run(){ 

      // Transforma a String em JSON e depois na classe Mensagem
      Gson gson = new Gson();
      Mensagem msg = gson.fromJson(JSON_Message, Mensagem.class);

        switch (msg.ID) { 
            
          // Se a resposta for um LEAVE_OK, sair 
          case 1:
            Peer.IsInServer = !Peer.IsInServer;
          break;

          // Se a resposta for um SEARCH_OK, salvar a lista de Peers com o arquivo 
          case 2:
            String FileName = msg.FileName;
            List<String> List_Of_Peers; 
            List_Of_Peers = Arrays.asList(msg.Message.split(","));
            PeersWithFiles.put(FileName, List_Of_Peers);
            {
                try {
                    System.out.println("Peers com arquivo solicitado: " + List2String(List_Of_Peers));
                } catch (IOException ex) {
                    Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
          break;    
    
          // Se for um ALIVE, enviar o ALIVE_OK
          case 4:
            Mensagem msg_Alive = new Mensagem(false, 4, "ALIVE_OK", Ports, "UDP", Integer.parseInt(Ports.split("/")[1]));
            {
                try {
                    UDP_Request(clientSocket, "127.0.0.1", 10098, msg_Alive);
                } catch (Exception ex) {
                    Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
          break;
        }
      }
  }
  
  public static class ThreadAtendimentoTCP extends Thread{
    
    // Porta TCP do peer
    public int port;
    
    // Construtor da ThreadAtendimento_Do
    public ThreadAtendimentoTCP(int p){
      port = p;
    }

    public void run(){
        
        try {
            ServerSocket ss = new ServerSocket(port);
            
            while(Peer.IsInServer){

                Socket s = ss.accept();

                //Ler informações pelo Socket
                InputStreamReader is = new InputStreamReader(s.getInputStream());
                BufferedReader reader = new BufferedReader(is);
                String JSON_Message = reader.readLine(); 

                // Transforma a String em JSON e depois na classe Mensagem
                Gson gson = new Gson();
                Mensagem msg = gson.fromJson(JSON_Message, Mensagem.class);

                switch(msg.ID){

                    // Se for um pedido de DOWNLOAD
                    case 5:
                {
                    try {
                        Make_Download_Analysis("127.0.0.1", msg, port);
                    } catch (Exception ex) {
                        Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                    break;

                    // Se for um aceite de DOWNLOAD
                    case 6:
                        PeersWithFiles.remove(msg.FileName);

                        // Fica esperando receber o Socket com o arquivo
                        Socket s2 = ss.accept();
                {
                    try {
                        receiveFile(s2, msg.FileName);
                    } catch (Exception ex) {
                        Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                    break;    

                    // Se for um DOWNLOAD_NEGADO
                    case 7:
                      System.out.println("Peer 127.0.0.1:" + msg.port + " negou o download, pedindo agora para o próximo");
                    break;     
                    }
      }
        } catch (IOException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  }

  public static class ThreadAtendimentoUDP extends Thread{
    
    // Socket a ser utilizado
    public DatagramSocket UDPSocket;

    // Porta UDP do Peer 
    public String Ports;
    
    // Construtor da ThreadAtendimentoUDP
    public ThreadAtendimentoUDP(DatagramSocket Udp, String p){
      UDPSocket = Udp;
      Ports = p;
    }
    
    public void run(){ 

      byte[] recBuffer = new byte[1024];
      DatagramPacket recPkt = new DatagramPacket(recBuffer, recBuffer.length);
      
      while(Peer.IsInServer){
        
          try {
              //Recebe um Socket
              UDPSocket.receive(recPkt);
              
              //Lê a informação do Socket
              String UDPSocketData = new String(recPkt.getData(), recPkt.getOffset(), recPkt.getLength());

              // Cria a ThreadAtendimento_Do_UDP
              ThreadAtendimento_Do_UDP TA_D = new ThreadAtendimento_Do_UDP(UDPSocket, UDPSocketData, Ports);
              TA_D.start();
              
          } catch (IOException ex) {
              Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
    }
  }

  public static class ThreadRequests extends Thread{

    // Socket
    private DatagramSocket clientSocket;

    // Porta TCP do Peer
    private int TCP_port;

    // Porta UDP do Peer
    private int UDP_port;

    public ThreadRequests(DatagramSocket node, int tcp, int udp){
      clientSocket = node;
      TCP_port = tcp;
      UDP_port = udp;
    }

    public void run(){
  
      //Buffer que recebe informações do Teclado
      BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

      // Lista de Peers que podem mandar o arquivo
      List<String> PossibleSenders;
      
      String Ports = TCP_port + "/" + UDP_port;

      while(Peer.IsInServer){

        System.out.println("Olá, Peer!");
        System.out.println("Digite algum comando");
        System.out.println("Se for uma requisição de DOWNLOAD, SEARCH ou UPDATE, não se esqueça de dar um espaço entre o nome do comando e o do arquivo");
      
        String Message_Body = null;
        
        // Espera leitura do teclado
          try {
              Message_Body = inFromUser.readLine();
          } catch (IOException ex) {
              Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
          }
        
        // Se o usuário digitou LEAVE  
        if(Message_Body.toUpperCase().contains("LEAVE")){
          Mensagem msg;
            msg = new Mensagem(false, 1, Message_Body, Ports, "UDP", UDP_port);
            try {
                UDP_Request(clientSocket, "127.0.0.1", 10098, msg);
            } catch (Exception ex) {
                Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        // Se o usuário digitou SEARCH    
        }else if(Message_Body.toUpperCase().contains("SEARCH")){
          Mensagem msg = new Mensagem(false, 2, Message_Body.split(" ")[1], Ports, "UDP", UDP_port);
          try {
                UDP_Request(clientSocket, "127.0.0.1", 10098, msg);
            } catch (Exception ex) {
                Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        // Se o usuário digitou UPDATE  
        }else if(Message_Body.toUpperCase().contains("UPDATE")){
          Mensagem msg = new Mensagem(false, 3, Message_Body.split(" ")[1], Ports, "UDP", UDP_port);
          try {
                UDP_Request(clientSocket, "127.0.0.1", 10098, msg);
            } catch (Exception ex) {
                Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        // Se o usuário digitou DOWNLOAD  
        }else if(Message_Body.toUpperCase().contains("DOWNLOAD")){
          Mensagem msg = new Mensagem(false, 5, Message_Body, Message_Body.split(" ")[1], "TCP", TCP_port);
          Gson gson = new Gson();
          String json_MSG = gson.toJson(msg);
          
          // Isola o nome do arquivo
          String File = Message_Body.split(" ")[1];
          
          // Obtém a lista de Peers com o arquivo
          PossibleSenders = PeersWithFiles.get(File);
          
          int c = 0;
          
          // Enquanto ainda não foi recebido um aceite e cada um dos Peers não recebeu duas solicitações de Download 
          while(PeersWithFiles.containsKey(File) && c < 2 * PossibleSenders.size()){
            String NextPeer = PossibleSenders.get(0);  
              try {
                  Make_Download_Request(Integer.parseInt(NextPeer.split("/")[0]), json_MSG);
              } catch (Exception ex) {
                  Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
              }
            
            //Coloca o Peer no fim da fila 
            PossibleSenders.remove(0);
            PossibleSenders.add(NextPeer);
            c++;

              try {
                  // Espera 1000 ms para enviar a solicitação para o próximo peer da lista
                  Thread.sleep(1000);
              } catch (InterruptedException ex) {
                  Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
              }
          }
        }
      }
    }
  }

  public static void Join(DatagramSocket clientSocket, String Files, String Ports) throws Exception{
    String ipAddress = "127.0.0.1";
    int port = 10098;
    int MyPort = Integer.parseInt(Ports.split("/")[1]);

    Mensagem msg = new Mensagem(false, 0, Files, Ports, "UDP", MyPort);
      
    UDP_Request(clientSocket, ipAddress, port, msg);
  }

  public static void UDP_Request(DatagramSocket clientSocket, String ipAddress, int port, Mensagem msg) throws Exception{

    //Envio do Pacote
    byte[] sendData = new byte[1024];
    Gson gson = new Gson();
	  String json_MSG = gson.toJson(msg);
    sendData = json_MSG.getBytes();
    sendDatagramPacket(clientSocket, sendData, ipAddress, port);
  }

  public static void Make_Download_Request(int port, String JSON_String) throws Exception{
    Socket s = new Socket("127.0.0.1", port);
    
    //Envio de informações pelo Socket
    OutputStream os = s.getOutputStream();
    DataOutputStream writer =  new DataOutputStream(os);
  
    //Escreve no Socket
    writer.writeBytes(JSON_String + "\n");
  }

  public static void Make_Download_Analysis(String IP, Mensagem msg, int MyPort) throws Exception{
    int port = msg.port;

    String file = msg.FileName;
    
    Socket s = new Socket(IP, port);
    
    //Envio de informações pelo Socket
    OutputStream os = s.getOutputStream();
    DataOutputStream writer =  new DataOutputStream(os);
  
    //Escreve no Socket
    Random decisor = new Random();
    if(decisor.nextInt(1) == 1){
      
      // Envia um socket avisando que aceitou a solicitação de Download
      Mensagem msg2 = new Mensagem(false, 6, "DOWNLOAD_OK", file, "TCP", MyPort);
      Gson gson = new Gson();
	    String AcceptMSG = gson.toJson(msg2);
      writer.writeBytes(AcceptMSG + "\n");

      // Envia o arquivo
      sendFile(s, file);
      
    }else{
     
      // Cria mensagem de recusa e envia ela pelo Socket  
      Mensagem msg2 = new Mensagem(false, 7, "DOWNLOAD_NEGADO", file, "TCP", MyPort);
      Gson gson = new Gson();
	    String DenialMSG = gson.toJson(msg2);
      writer.writeBytes(DenialMSG + "\n");  
    }
  }

  public static void main(String [] args) throws Exception{
  
    //Buffer que recebe informações do Teclado
    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("Olá, Peer!");
    System.out.println("Para começar, digite a sua porta TCP");
    String TCP_portS = inFromUser.readLine();
    int TCP_port = Integer.parseInt(TCP_portS);

    System.out.println("Agora digite a sua porta UDP");
    String UDP_portS = inFromUser.readLine();
    int UDP_port = Integer.parseInt(UDP_portS);
    
    System.out.println("Para colocar você no servidor, digite o nome de um arquivo que você tenha...");
    System.out.println("Digite o nome de todos os seus arquivo separados por vírgulas e aperte ENTER na sequência");
    System.out.println("Exemplo: BrunoMars.mp4, Queen.mp4, Nirvana.mp4");
    System.out.println("Lembre-se: Estou considerando que todos os seus arquivos estão na raíz dos diretórios");
    System.out.println("Ou seja, os caminhos do exemplo seriam: C:\\BrunoMars.mp4, C:\\Queen.mp4, C:\\Nirvana.mp4");

    String NewPeerFilesString = inFromUser.readLine();

    DatagramSocket clientSocket = new DatagramSocket();

    Join(clientSocket, NewPeerFilesString, TCP_port +"/"+ UDP_port);

    //Espera receber o JOIN_OK do Servidor
    byte[] recBuffer = new byte[1024];
    DatagramPacket recPkt = new DatagramPacket(recBuffer, recBuffer.length);
    clientSocket.receive(recPkt);
    
    System.out.println("Sou peer 127.0.0.1 :"+ TCP_port +"/"+ UDP_port + " com arquivo(s) "+ NewPeerFilesString);
    
    ThreadAtendimentoTCP TCP = new ThreadAtendimentoTCP(TCP_port);
    TCP.start();

    ThreadAtendimentoUDP UDP = new ThreadAtendimentoUDP(clientSocket, TCP_port +"/"+ UDP_port);
    UDP.start();
    
    ThreadRequests TR = new ThreadRequests(clientSocket, TCP_port, UDP_port);
    TR.start();
  }
}
