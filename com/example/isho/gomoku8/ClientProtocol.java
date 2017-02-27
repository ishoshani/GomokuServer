package com.example.isho.gomoku8;

class ClientProtocol{
  public static void processProcedure(GamePacket input){
    if(input.packetType.equals("OPENCONNECTION")){
      System.out.println("Connection successful");
    }
  }
}
