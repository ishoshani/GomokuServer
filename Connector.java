import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.Set;
/**
Thread that interacts directly with the Client, handling messages from the socket.
**/
public class Connector extends Thread{
  Socket client;
  String username;
  Integer CurrentGameKey;
  Room CurrentGame;
  Integer uID;
  Boolean spinDown=false;

  public Connector(Socket socket, Integer userNumber){
    super("Connection"+socket);
    username = "newUser"+userNumber;
    client = socket;
    uID = userNumber;
  }
  /**
  Main Loop of Server Thread
  **/
  public void run(){
    try(
    ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
    ObjectInputStream in = new ObjectInputStream(client.getInputStream());
    ){
      GamePacket inputPacket, outputPacket;
      while ((inputPacket = ((GamePacket)in.readObject())) != null) {//blocks on input unless connection is severed
        outputPacket=process(inputPacket);
        out.writeObject(outputPacket);
        if(spinDown){//User is exiting, proceed with voluntary disconnect
          spinDown();
          break;
        }
      }
    }catch(IOException e){
      spinDown=true;
      spinDown();
      System.err.println("the user"+uID+"at Socket"+client+ "Experienced Error "+ e);
    }catch(ClassNotFoundException e){
      System.err.println(e);
    }
  }
/**
Choice Tree for Input from client to server.
**/
  public GamePacket process(GamePacket input){
    GamePacket c;
    if(input.packetType.equals("OPENCONNECTION")){//Handle Handshake
      c = new GamePacket("OPENCONNECTION");//2 games avaialble, codes 0 and 1
      return c;
    }
    else if(input.packetType.equals("KEEPALIVE")){//Handle Standard KeepAlive
    return new GamePacket("KEEPALIVE");
  }
    else if(input.packetType.equals("WAITINGFORGAME")){//Handle Room Waiting KeepAlive
      if (CurrentGame.state==Room.PLAYING){
        c = new GamePacket("BEGINPLAY");
        return c;
      }else{
        return new GamePacket("KEEPALIVE");
      }
    }

    else if(input.packetType.equals("BEGINPLAY")){//Handle Ready For Game Message
      String type;
      if(CurrentGame.turn == uID){//Do I get to start?
        type = "YOURTURN";
      }
      else{
        type = "OTHERTURN";
      }
      c = new GamePacket(type);
      return c;
    }
    else if(input.packetType.equals("WAITINGFORTURN")){//Handle KeepAlive while waiting on a turn
      if(CurrentGame.state==Room.DONE){
        String finishing = CurrentGame.finish();
        return new GamePacket("GAMEOVER",finishing);
      }
      return CurrentGame.getNextMessage();
    }
    else if(input.packetType.equals("MOVE")){//Handle Message for Game Moves.
        CurrentGame.SendCommand(uID, input.row, input.col);
        return new GamePacket("OTHERTURN");
    }
    else if(input.packetType.equals("GAMEOVER")){//When the client finds a win
      CurrentGame.SendCommand(uID, input.row,input.col);
      CurrentGame.state=Room.DONE;
      return new GamePacket("GAMEOVER");

    }else if(input.packetType.equals("FINDGAME")){
        synchronized (ServerContainer.roomList){
          Integer gameKey = findGame();
          if(gameKey==null){
            ServerContainer.roomList.put(uID,new Room());
            Room newGame= ServerContainer.roomList.get(uID);
            newGame.AddPlayer(username, uID, this);
            CurrentGame = newGame;
            c = new GamePacket("WAITINGFORGAME");
            return c;
          }
          Room game = ServerContainer.roomList.get(gameKey);
          game.AddPlayer(username, uID, this);
          CurrentGame = game;
          c = new GamePacket("BEGINPLAY");
          return c;
        }
      }else if(input.packetType.equals("RESIGN")){
        spinDown=true;
        c = new GamePacket("SafeToExit","Have a wonderful day");
        return c;
    }else{
    c = new GamePacket("ERROR");//bad Command
    return c;
  }
  }
  /**
  Find a game on the server with the given Game ID. use whatever comes up first.
  **/
  public Integer findGame(){
    for(Integer k : ServerContainer.roomList.keySet()){
        if(ServerContainer.roomList.get(k).state==Room.WAITING){
          return k;
        }
    }
    return null;
  }
  /**
  Do all Operations necessary for a Client disconnect here.
  **/
  private void spinDown(){
    System.out.println("safely Ending Thread for "+uID);
    synchronized(ServerContainer.servState){
    ServerContainer.servState.currentUsers--;
    }
  }

}
