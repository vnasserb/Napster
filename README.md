# Napster
Adaptação do Napster em Java, onde o servidor armazena as informações sobre os peers e permite a troca de arquivos de até 2 GB entre eles.
O código presente neste repositório permite que os Peers se conectem a um servidor e relatem quais arquivos eles possuem, para que assim o servidor possa dizer a um cliente quem possui o arquivo que ele deseja.

- **Servidor**
  - JOIN_OK (UDP): Armazenamento das portas TCP/UDP do Peer novo, a porta UDP do Peer e uma String com todos os arquivos do Peer separados por vírgulas.
  - LEAVE_OK (UDP): Remoção do Peer e de seus arquivos do servidor
  - SEARCH_OK (UDP): Obtenção dos Peers que possuem o arquivo que o cliente deseja 
  - UPDATE_OK (UDP): Atualização da lista de arquivos que um determinado Peer possui

- **Cliente**
  - JOIN (UDP): Requisição para ter suas informações armazenadas pelo Servidor
  - LEAVE (UDP): Requisição para ter suas informações removidas pelo Servidor
  - SEARCH (UDP): Requisição para solicitar para o Servidor a lista de Peers que possuem um determinado arquivo. Não é uma função específica, sendo que ele ocorre quando o usuário digita “SEARCH” e o nome de um arquivo. 
  - UPDATE (UDP): O UPDATE funciona de maneira similar ao SEARCH, criando uma requisição para ter uma nova informação armazenada pelo Servidor. 
  - DOWNLOAD (TCP): Envio para todos os Peers que possuem o arquivo até que um deles aceite.

- **Mensagem**
  É uma classe que define a mensagem que será enviada. Para a sua criação, envio e recebimento, é necessário baixar a biblioteca externa GSON.
