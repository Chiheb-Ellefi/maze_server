# 🧠 Labyrinthe de Mots – Server

This is the **server-side** implementation of the *Labyrinthe de Mots* game. It handles all core logic including maze generation, word placement, path validation, scoring, and multiplayer session management via TCP sockets.

## 📌 Features

- ✅ Maze generation using **Prim**, **DFS**, and **loop creation**
- 📚 Word population using a **JSON dictionary** organized by themes
- 🧩 BFS for identifying paths in single-player mode
- 📍 Dijkstra's algorithm for finding shortest paths in multiplayer
- 🧮 Scoring logic based on word length and pathfinding
- 👥 Multiplayer support with **TCP socket communication**
- 🕹️ Solo/local game mode included in the same codebase

---

## 🧱 Architecture

- **Language**: Java  
- **Game Modes**:  
  - 🔹 Local (Solo): Maze is generated and played on the same machine  
  - 🔹 Online Multiplayer: Server coordinates the session and sends updates to clients  

### Algorithms Used

| Feature                   | Algorithm        |
|--------------------------|------------------|
| Maze Generation (Solo)   | Prim             |
| Maze Generation (Multi)  | DFS + Looping    |
| Path Discovery (Solo)    | BFS              |
| Shortest Path (Multi)    | Dijkstra         |

---

## 🎮 Game Modes

### 🧍 Local (Solo)

- Generates a **perfect maze** using **Prim’s algorithm**
- A unique path is extracted using **BFS** and filled with valid words
- Remaining cells contain random letters
- User has **30 seconds** to find the correct path
- Score = **sum of letters in found words**

### 👥 Multiplayer (Online)

- Uses **DFS** to generate the maze with added **loops**
- Words and junk characters are mixed in the paths
- Players take turns with **15-second** timers
- Bonus Scoring:
  - 🥇 +5 points for finishing first
  - 📏 +10 points for taking the shortest path (via **Dijkstra**)

---

## 📡 Server Responsibilities

- Manages player connections and sessions
- Generates mazes and computes valid paths
- Handles scoring and game logic
- Sends real-time updates to JavaFX clients via TCP

---

## 🔮 Future Improvements

- AI-based difficulty adjustment  
- Online ranking system  
- Support for more than 2 players  
- 3D animations and UI improvements

---

**Université de Tunis El Manar – ISI | 2024–2025**

