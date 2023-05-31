package sd;
import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server{

  // Mapa com o nome do arquivo como chave e uma lista de peers que o possuem como valor
  public static Map<String, List<String>> Files = new HashMap<>();

  // Mapa com o TCP/UDP do Peer como chave e uma lista de arquivos que ele possue como valor
  public static Map<String, List<String>> Peers = new HashMap<>();

  // Lista de Peers para enviar o Alive
  public static ArrayList<String> Alive_Peers;

  public static class ThreadAtualizacao extends Thread{

    public ThreadAtualizacao(){ }

    @Override
    public void run(){

      List<String> Updated_Peers;
      List<String> Dead_Peer_Files; 
      
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
      
      while(true){
        
        // Itera sobre um set com todos os peers
        for(String key : Peers.keySet()){

          // Se o Peer não está na lista de vivos, vamos limpar seus registros
          if(Alive_Peers.indexOf(key) < 0){

            // Lista de arquivos que o Peer morto tem
            Dead_Peer_Files = Peers.get(key);
            
              try {
                  System.out.println("Peer 127.0.0.1:" + key + " morto. Eliminando seus arquivos " + List2String(Dead_Peer_Files));
              } catch (IOException ex) {
                  Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
              }

            // Tira o Peer da relação de Peers do Servidor
            Peers.remove(key);

            // Atualização da relação de Peers que possuem cada arquivo
            for(int f = 0;f < Dead_Peer_Files.size();f++){

              // Cria uma cópia da relação de Peers que possuem o arquivo e remove o peer dessa cópia 
              Updated_Peers = Files.get(Dead_Peer_Files.get(f));
              Updated_Peers.remove(String.valueOf(key));

              // Se a nova lista está vazia, tira o arquivo do HashMap. Caso contrário, atualiza o HashMap com a lista atualizada de Peers que possuem o arquivo 
              if(Updated_Peers.isEmpty()){
                Files.remove(Dead_Peer_Files.get(f));
              }else{
                Files.put(Dead_Peer_Files.get(f), Updated_Peers);                
              }
            }
          }
        }
  
          try {
              Thread.sleep(30000);
          } catch (InterruptedException ex) {
              Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
    }
  }

  public static class ThreadAlive extends Thread{

    // Socket
    public DatagramSocket no;

    public ThreadAlive(DatagramSocket node){
      no = node;
    }

    public void run(){

      // Cria a mensagem de ALIVE
      Mensagem msg = new Mensagem(true, 4, "ALIVE", "", "UDP", 10098);
      Gson gson = new Gson();
      
      while(true){
        int Alive_Peers_Size = Alive_Peers.size();
        
        // Pergunta para cada um dos Peers da rede se ele está vivo
        for(int p=0;p<Alive_Peers_Size;p++){
            try {
                sendDatagramPacket(no, gson.toJson(msg).getBytes(), "127.0.0.1", Integer.parseInt(Alive_Peers.get(0).split("/")[1]));
                Alive_Peers.remove(0);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
  
          try {
              Thread.sleep(30000);
          } catch (InterruptedException ex) {
              Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
    }
  }

  public static class ThreadAtendimento extends Thread{

    // Socket
    public DatagramSocket no;

    //Mensagem recebida
    public Mensagem msg;

    // IP de quem mandou a mensagem
    public String ipAddress;

    // Porta de quem mandou a mensagem
    public int port;

    public ThreadAtendimento(DatagramSocket node, Mensagem M, String Ip, int P){
      no = node;
      msg = M;
      ipAddress = Ip;
      P = port;
    }

    @Override
    public void run(){
      
      switch (msg.ID) {

        // Se recebeu uma solicitação JOIN
        case 0:
      {
          try {
              Join_ok(no, msg.FileName, port, msg.Message);
              Alive_Peers.add(msg.FileName);
          } catch (IOException ex) {
              Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
        break;
        
        // Se recebeu uma solicitação LEAVE
        case 1:
      {
          try {
              Leave_ok(no, msg.FileName, port);
              Alive_Peers.remove(String.valueOf(msg.FileName));
          } catch (IOException ex) {
              Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
        break;
        
        // Se recebeu uma solicitação SEARCH
        case 2:
      {
          try {
              Search_ok(no, msg.FileName, port, msg.Message);
          } catch (IOException ex) {
              Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
        break;

        // Se recebeu uma solicitação UPDATE
        case 3:
      {        
          try {
              Update_ok(no, msg.FileName, port, msg.Message);
          } catch (IOException ex) {
              Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
        break;
        
        // Se recebeu um ACK de ALIVE
        case 4:
            Alive_Peers.add(msg.FileName);
        break;

      }
    }
  }

  public static void sendDatagramPacket(DatagramSocket socket, byte[] outputDataBuffer, String ipAddress, int port) throws IOException {
		InetAddress inetAddress = InetAddress.getByName(ipAddress);

		DatagramPacket outputPacket = new DatagramPacket(outputDataBuffer, outputDataBuffer.length, inetAddress, port);
		socket.send(outputPacket);
	}
  
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

  public static void Join_ok(DatagramSocket ServerSocket, String Ports, int port, String NewPeerFiles) throws IOException {
    List<String> Peer_Files = Arrays.asList(NewPeerFiles.split(","));
          
    //Se o Peer não está armazenado, adicionar à lista
    if(!Peers.containsKey(Ports)){ 
      Peers.put(Ports, Peer_Files);
      List<String> Updated_Peer_Files;
  
      // Para cada um dos arquivos que o Peer enviou
      for(int f=0;f<Peer_Files.size();f++){
        String file = Peer_Files.get(f);
              
        //Se ninguém na rede enviou esse arquivo anteriormente, criar uma chave - valor novo para o arquivo 
        if(!Files.containsKey(file)){
          Files.put(file, Arrays.asList(Ports));
        }else{
            
          // Se esse arquivo já está presente na rede, adicionar na lista de Peers que o possuem
          Updated_Peer_Files = Files.get(file);
          Updated_Peer_Files.add(Ports);
          Files.put(file, Updated_Peer_Files);
        }
      }

      // Envia o Socket para o Peer com o JOIN_OK
      Mensagem msg = new Mensagem(true, 0, "JOIN_OK", "", "UDP", 10098);
      Gson gson = new Gson();
      sendDatagramPacket(ServerSocket, gson.toJson(msg).getBytes(), "127.0.0.1", port);
      System.out.println("Peer 127.0.0.1:" + Ports + " adicionado com arquivos " + NewPeerFiles);
    }
	}

  public static void Leave_ok(DatagramSocket ServerSocket, String Ports, int port) throws IOException {
    
    // Cria lista de arquivos a serem deletados
    List<String> Files_To_Delete;
    Files_To_Delete = Peers.get(Ports);
  
    // Se o Peer está na lista de Peers do Servidor
    if(Peers.containsKey(Ports)){

      // Cópia da lista de arquivos cuja relação de peers deve ser editada 
      List<String> Files_To_Delete_Copy;
      
      // Tira o Peer da lista de peers da rede
      Peers.remove(Ports);
  
      // Atualiza a lista de arquivos, removendo o Peer que saiu da relação de peers que possuem o arquivo 
      for(int f = 0;f<Files_To_Delete.size();f++){
        String file = Files_To_Delete.get(f);
        Files_To_Delete_Copy = Files.get(file);
        Files_To_Delete_Copy.remove(String.valueOf(Ports));
        Files.put(file, Files_To_Delete_Copy);
      }
  
      // Envia o LEAVE_OK
      Mensagem msg = new Mensagem(true, 1, "LEAVE_OK", "", "UDP", 10098);
      Gson gson = new Gson();
      sendDatagramPacket(ServerSocket, gson.toJson(msg).getBytes(), "127.0.0.1", port);
    } 
	}

  public static void Search_ok(DatagramSocket ServerSocket, String Ports, int port, String File) throws IOException {
          
    System.out.println("Peer 127.0.0.1:" + Ports + " solicitou o arquivo " + File);
    Gson gson = new Gson();
    
    // Se o peer que fez a solicitação e o arquivo desejado estão na rede
    if(Peers.containsKey(Ports) && Files.containsKey(File)){
      
      // Cria lista com os peers que possuem o arquivo desejado
      List<String> MatchedPeers;
      MatchedPeers = Files.get(File);
  
      // Envia a lista de peers que possuem o arquivo
      Mensagem msg = new Mensagem(true, 2, List2String(MatchedPeers), File, "UDP", 10098);
      sendDatagramPacket(ServerSocket, gson.toJson(msg).getBytes(), "127.0.0.1", port);		
    }else{
      Mensagem msg = new Mensagem(true, 2, "", File, "UDP", 10098);
      sendDatagramPacket(ServerSocket, gson.toJson(msg).getBytes(), "127.0.0.1", port);
    }	
	}

  public static void Update_ok(DatagramSocket ServerSocket, String Ports, int port, String File) throws IOException {
    
    // Se o peer que enviou o arquivo está na rede
    if(Peers.containsKey(Ports)){

      // Atualiza a lista de arquivos que o Peer possui
      List<String> UpdatedFiles;
      UpdatedFiles = Peers.get(Ports);
      UpdatedFiles.add(File);
      
      // Nome do arquivo baixado é acrescido na lista de arquivos do peer
      Peers.put(Ports, UpdatedFiles);

      // Envia a confirmação do UPDATE
      Mensagem msg = new Mensagem(true, 3, "UPDATE_OK", "", "UDP", 10098);
      Gson gson = new Gson();
      sendDatagramPacket(ServerSocket, gson.toJson(msg).getBytes(), "127.0.0.1", port);		
    }
	}
  
  public static void main(String [] args) throws Exception{

    DatagramSocket ServerSocket = new DatagramSocket();

    byte[] recBuffer = new byte[1024];
    DatagramPacket recPkt = new DatagramPacket(recBuffer, recBuffer.length);

    ThreadAlive thread_a = new ThreadAlive(ServerSocket);
    thread_a.start();

    ThreadAtualizacao thread_at = new ThreadAtualizacao();
    thread_at.start();

    while(true){

      ServerSocket.receive(recPkt);

      String Request = new String(recPkt.getData(), recPkt.getOffset(), recPkt.getLength());

      Gson gson = new Gson();
      Mensagem msg = gson.fromJson(Request, Mensagem.class);

      ThreadAtendimento thread = new ThreadAtendimento(ServerSocket, msg, "127.0.0.1", msg.port);
      thread.start();
    }
  }
}
