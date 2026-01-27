# Battleship

Project for the course "Programmierpraktikum Java".  
A graphical Battleship game implemented with Java and JavaFX.

## Table of Contents
- Overview
- Features
- Project Structure
- Technologies
- Installation
- Usage
- Contributors
- License

## Overview
Battleship is a Java / JavaFX implementation of the classic Battleship game.  
The project combines game logic written in Java with a graphical user interface built using JavaFX.

The game supports singleplayer and multiplayer modes. In singleplayer mode, players compete against an  
AI with three difficulty levels (Easy, Medium, Hard), each featuring distinct shooting behavior.  
The board size is configurable between 3x3 and 12x12.

Multiplayer mode allows players to create and join lobbies on the local network using socket-based communication.  
The host can define the lobby name and board size.

## Features
- Classic Battleship gameplay (ship placement, input-validation, turn-based shooting, win/lose conditions)
- Singleplayer mode
  - AI opponent with three difficulty levels: Easy, Medium, Hard
- Multiplayer mode
  - Host and join lobbies via socket-based networking
  - Server/lobby browser with refresh (lists available lobbies)
- Configurable board size (3x3 to 12x12) in singleplayer and multiplayer
- JavaFX graphical user interface
- Drag-and-drop ship placement 
- Rotate ships after placement

## Project Structure
The project is divided into three main components:
- GUI layer implemented with JavaFX
- Game logic implemented in Java
- Networking layer based on socket communication

## Technologies
- used Gradle for managing the JAVA dependencies and builds
- communication protocol is HTTP using JAVA socket with JSON as data type for the payload
- JFX as GUI framework
    - [Tutorial Video](https://youtu.be/9YrmON6nlEw?si=Iqt_kq4Gv8PRQ8Zr)

## Installation


## Usage
After starting the application, select the desired game mode in the main menu.

In singleplayer mode, choose the board size and the AI difficulty level.  
Place your ships on the board using drag and drop, rotate them as needed, and start the game.   
Players and AI take turns shooting until all ships of one side are destroyed.

In multiplayer mode, either host a new lobby or join an existing one on the local network.  
The host defines the lobby name and board size.  
Once all players have joined, each player places their ships and the game begins with alternating turns.

During gameplay, all actions are performed via the graphical user interface.

## Contributors
- Matthis Geissler (@M4tt1-Coder) – core development, game logic
- Thomas Weigl (@Thomas-Weigl) – GUI
- Fabian ?? (@WoFabian) – networking

## License
This project is licensed under the MIT License