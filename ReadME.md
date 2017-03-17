Server for the Gomoku8 Project.

Activate using sudo java com.example.isho.Gomoku8.ServerContainer

Gomoku Server Protocol

Message Types:

OPENCONNECTION--used to do intial connection find

FINDGAME -- begin search for game

WAITINGFORGAME -- signal to continue looking for game

BEGINPLAY -- signal that game has been found and aknowledgment

YOURTURN -- signal that it is the clients turn

OTHERTURN -- signal that it is not the clients turn

WAITINGFORTURN -- signal from client that they are still waiting for turn

MOVE(I,J) -- signal of move being sent by client

GAMEOVER(I,J) -- signal that the client is ending the game, with move if needed

RAGEQUIT -- signal that another client in the game dropped

RESIGN --signal that a client is no longer interested in continueing the game

KEEPALIVE --generic signal to make sure that the connection is still happening
