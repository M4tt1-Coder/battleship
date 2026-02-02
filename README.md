# Battleship

This project was developed as part of the university course **“Programmierpraktikum Java”**.  
It is a graphical implementation of the classic Battleship game using **Java** and **JavaFX**, with a strong focus on clean architecture, usability, and learning new technologies from scratch.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Project Structure](#project-structure)
- [Technologies](#technologies)
- [Installation](#installation)
- [Usage](#usage)
- [Problems and Challenges](#problems-and-challenges)
- [Contributors](#contributors)
- [License](#license)

---

## Overview

**Battleship** is a Java/JavaFX-based implementation of the well-known Battleship board game.  
The project combines object-oriented game logic written in Java with a modern graphical user interface built using JavaFX.

### Game Modes & Features

- Supports both **Singleplayer** and **Multiplayer** modes.
- **Singleplayer Mode**:
  - Play against an AI with three difficulty levels (**Easy, Medium, Hard**), each with a distinct shooting strategy.
  - Configurable board size.
  - Drag-and-drop ship placement with rotation.
  - In-game error handling via pop-ups.
  - Save and load game states.
- **Multiplayer Mode**:
  - Conceptual implementation for hosting and joining games over a local network using socket communication.
  - Lobby discovery, creation, and joining.
  - Loading screens while waiting for opponents.
  - _(Note: Multiplayer features are partially implemented and may have limitations.)_

---

## Features

### General

- Classic Battleship gameplay:
  - Ship placement with input validation.
  - Turn-based shooting.
  - Win and lose conditions.
- User-friendly JavaFX GUI.
- Navigation through dedicated views.
- Responsive layout.

### Singleplayer Mode

- AI with adjustable difficulty levels.
- Configurable ship occupancy (15%, 20%, 30%).
- Dynamic board size.
- Drag-and-drop ship placement and rotation.
- Error handling with pop-up messages.
- Save/load game functionality.

### Multiplayer Mode

- Lobby creation and joining over local network.
- Server discovery and refresh.
- Waiting/loading screens.
- Gameplay similar to Singleplayer.

_(Note: Multiplayer is still a work in progress; see “Problems and Challenges” below.)_

---

## Project Structure

The project is divided into three main components:

- **GUI**  
  Built with JavaFX, responsible for all visual elements and user interactions.

- **Game Logic**  
  Core Java classes managing board state, ship placement, turn sequence, and game rules.

- **Networking**  
  Socket-based communication framework for multiplayer gameplay.

---

## Technologies

- **Java** (version 11 or higher recommended)
- **JavaFX** (OpenJFX) for GUI
- **Apache Maven** for dependency management and build automation
- **Gradle** (used during development)
- Socket-based networking with JSON payloads
- Version control with Git

---

## Installation

1. Install **Git**.
2. Install **Java (>=11)**.
3. Clone the repository:

```bash
git clone https://github.com/M4tt1-Coder/battleship.git
```

## Usage

### 1. Starting the Application

There are two ways to start the application:

- Using the executable JAR file:
  - double-click on the compiled JAR-file

- Using Maven (Windows, Linux, macOS):

```bash
mvn clean package
```

```bash
java -jar ./target/battleship-1.0.1-jar-with-dependencies.jar
```

### 2. Game Mode Selection

After launching the application, select the desired game mode from the main menu.
<img src="README_ressources/lobby_screen_screenshot.png" width="400">

### 3. Singleplayer Mode

- Select the desired board size and AI difficulty level
<img src="README_ressources/singleplayer_settings_screenshot.png" width="400">
- The board size must be within the valid range (default value: 10)
- Optionally load a previously saved game state
<img src="README_ressources/load_game_screenshot.png" width="400">
- Place ships on the board using drag-and-drop
- Rotate ships after they have been placed
<img src="README_ressources/select_position_of_boats_screenshot1.png" width="400">
<img src="README_ressources/select_position_of_boats_screenshot2.png" width="400">
- Take turns shooting against the AI opponent
<img src="README_ressources/shoot_on_enemy_field_screenshot1.png" width="400">
     <img src="README_ressources/shoot_on_enemy_field_screenshot2.png" width="400">
- Save the current game at any time and return to the main menu

### 4. Multiplayer Mode

- Host a new lobby or attempt to join an existing one on the local network
<img src="README_ressources/join_other_players_screenshot.png" width="400">
- The host defines the lobby name and board size
<img src="README_ressources/multiplayer_settings_screenshot.png" width="400">
- A waiting screen is displayed until another player joins
<img src="README_ressources/loading_screen_screenshot.png" width="400">
- Gameplay follows the same rules as in Singleplayer mode

_Note: The Multiplayer mode is partially implemented; see “Problems and Challenges” below._

### 5. Documentation

To generate the JavaDoc documentation (the project must be compiled beforehand), run:

```bash
mvn javadoc:javadoc
```

... or with PNPM (optional):

```bash
pnpm generate:docs
```

## Problems and Challenges

This project represented a substantial learning challenge and served as an intensive introduction to several new technologies for the entire team.

- None of the team members had prior experience with **Java** before starting this project.
- Developing a graphical user interface with **JavaFX** was completely new and required significant time to understand layouts, event handling, and responsive design.
- **Socket-based networking** was an unfamiliar topic and proved to be particularly complex, especially in combination with GUI synchronization.
- Due to limited time, the team deliberately focused on delivering a **stable and fully functional Singleplayer mode**.

The **Multiplayer mode** is conceptually implemented but not fully functional yet:

- Server discovery logic is implemented.
- Lobby creation and naming mechanisms exist.
- JavaFX user interface components for multiplayer are present.
- Due to time constraints, these features could not be fully tested, integrated, and stabilized.

Despite these challenges:

- The Singleplayer mode works reliably and as intended.
- The AI behaves correctly across all difficulty levels.
- The graphical user interface is consistent, responsive, and user-friendly.
- Important architectural groundwork for Multiplayer functionality has already been established.

Overall, the project demonstrates strong learning progress, solid problem-solving skills, and a successful implementation of a non-trivial software system, despite all technologies being new to the team at the beginning.

## Contributors

- Matthis Geissler (@M4tt1-Coder) – core development, game logic
- Thomas Weigl (@Thomas-Weigl) – GUI
- Fabian Wottke (@WoFabian) – networking

## License

This project is licensed under the MIT License

