import java.io.*;
import java.net.*;
import java.util.Arrays;
public class ClientContainer{
  static boolean showKeepAlive;
  public static void main(String[] args) {
      if(args.length != 2){
        System.err.println(
        "Usage: needs three arguments, java ClientContainer <hostname> <showKeepAlive>"
        );
        System.exit(1);
      }
      String hostName = args[0];
      int portNumber = 19;//Defined in RFC
      showKeepAlive=Boolean.parseBoolean(args[1]);
      //Java 7's new Try with recources. Automatically closes all opened recources when try ends or error is caught.
      try(
      Socket echoSocket = new Socket(hostName,portNumber);
      ObjectOutputStream out = new ObjectOutputStream(echoSocket.getOutputStream());
      ObjectInputStream in = new ObjectInputStream(echoSocket.getInputStream());

      BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in))
      ){
        echoSocket.setSoTimeout(2000);//set timeout on server to 20 seconds. If no keepalive messages, throw error and kill connection
        String userInput;
        out.writeObject(new GamePacket("OPENCONNECTION"));//Handshake and get usage
        out.flush();
        GamePacket welcome = (GamePacket)in.readObject();
        ClientProtocol.processProcedure(welcome);
      }catch (UnknownHostException e) {
    System.err.println("Don't know about host " + hostName);
  }
  catch (IOException e) {
    System.err.println("Something Happened to the server! It may not be taking connections at this port or it just died" +
    hostName);
  }
  catch (ClassNotFoundException e){
    System.err.println("Unexpected type of Object" + e);
  }finally{
    System.out.println("Exiting Client");
  }
}}
