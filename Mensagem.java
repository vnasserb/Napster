package sd;
public class Mensagem{
  
  // True se a mensagem veio do Servidor e Falso caso contrário
  public Boolean IsFromServer;
  
  // ID da mensagem
  public int ID;

  // Conteúdo a mensagem
  public String Message;

  // Alguma informação que auxilie
  public String FileName;

  // Transferência é TCP ou UDP
  public String ConnType;

  // Porta de quem enviou a mensagem
  public int port;
  
  public Mensagem(Boolean server, int id, String M, String FN, String CT, int p) {
    IsFromServer = server;
    ID = id;
    Message = M;
    FileName = FN; 
    ConnType = CT;
    port = p; 
  }
}
