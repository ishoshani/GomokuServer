package com.example.isho.gomoku8;

import java.net.*;
import java.io.*;
/**
Room
param int gameID
creates a Room dedicated to playing the game given in the arguments.
 Handles things like keeping track of whose turn it is, who is playing,
 And other things not specific to the game being played.
**/
public class Room{
  String[] players;
  Integer[] playerID;//Id's of players
  Connector[] connections;
  GamePacket nextMessage;//Defines the chat packet that tells what is happening in the game
  int turnSwitch =0;//whose turn it is in terms of player 0, 1.
  Integer turn = 0;//whose turn it is in terms of player ID
  Integer state;
  Integer gameID;
  int lastRow;
  int lastCol;
  final static int WAITING = 0;
  final static int PLAYING = 1;
  final static int DONE = 2;


  public Room(){
    players = new String[2];
    playerID = new Integer[2];
    connections = new Connector[2];
    nextMessage = null;
    state = WAITING;
  }
  /**
  Opening Message
  **/
  /**
  SendCommand
  id: players ID
  String: Messsage: Payload of instructions to send to game
  **/
  public String SendCommand(int id, int row, int col){
    if(turn == playerID[0]){//make sure its the correct turn
      if(id != playerID[0]){
        return "please wait your turn";
      }
      lastRow = row;
      lastCol = col;
      GamePacket out = new GamePacket("YOURTURN", row, col);

      nextMessage = out;
      if(state != DONE){//check if game was won
        turnSwitch = 1;
        turn = playerID[turnSwitch];
      }
      return "";//probably options on what to send here.
    }
    if(turn == playerID[1]){
      if(id != playerID[1]){
        return "please wait your turn";
      }
      lastRow = row;
      lastCol = col;
      GamePacket out = new GamePacket("YOURTURN", row, col);
      nextMessage = out;
      if(state != DONE){
        turnSwitch=0;
        turn = playerID[turnSwitch];
      }
      return "";
    }
    return "wut";

  }
  /**
  Send Ending Message from game.
  **/
  public String finish(){
    String s = "winner is "+playerID[turnSwitch];
    return s;
  }
  /**
  Add a player to the Room
  String username
  Integer User Id
  Connector Users Connection thread, to make sure it is still running.
  Currently only supports 2 players
  **/
  public boolean AddPlayer(String username, Integer id, Connector connector){
    if(playerID[0]==null){
      players[0] = username;
      playerID[0] = id;
      connections[0] = connector;
      return true;
    }
    if(playerID[1]==null){
      players[1] = username;
      playerID[1] = id;
      connections[1] = connector;
      state = PLAYING;
      turnSwitch=0;
      turn = playerID[turnSwitch];
      return true;
    }
    System.err.println("someone tried to connect to a full room");
    return false;
  }
  /**
  Use this to get status updates from game
  **/
  public GamePacket getNextMessage(){
    GamePacket n;
    n =  nextMessage;
    if(bothConnected()){//Test that both players are connected
      state=DONE;
      if(connections[0].spinDown){//Whoever is still in the room wins.
        turnSwitch=1;
      }
      if(connections[1].spinDown){
        turnSwitch=0;
      }
      return new GamePacket("RAGEQUIT","A Player disconnected");
    }
    if(n == null){
      n = new GamePacket("KEEPALIVE");//If no move was made, just do keepAlive
    }else{
      nextMessage=null;
    }
    return n;
  }
  /**
  Fast access to turn change
  **/
  public void changeTurnSwitch(){
    if (turnSwitch==0){
      turnSwitch=1;
    }else{
      turnSwitch=0;
    }
  }
  private Boolean bothConnected(){
    return connections[0].spinDown||connections[1].spinDown;
  }
}
