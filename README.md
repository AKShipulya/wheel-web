# Rainbow Wheel of Fortune 🎯

An interactive spin-the-wheel game built with Compose Multiplatform that brings excitement to decision-making and competitions!

🌐 **[Live Demo](https://akshipulya.github.io/wheel-web/)**

<img width="1315" height="913" alt="image" src="https://github.com/user-attachments/assets/daf1bd18-804c-4f49-9a36-1ded69ebcb8f" />

## 🎮 Features

### 🎡 Interactive Spinning Wheel
- **Beautiful rainbow-colored segments** with gradient effects
- **Smooth spinning animation** with customizable duration
- **Visual countdown timer** during wheel rotation
- **Precise winner detection** with clear visual indicators

### 👥 Player Management
- **Easy player addition** - add multiple players at once (line by line input)
- **Color-coded participants** - each player gets a unique vibrant color
- **Dynamic wheel updates** - wheel automatically adjusts to number of players
- **Score tracking** - keep track of wins and losses for each player

### 🏆 Advanced Scoring System
- **Real-time leaderboard** with top 10 players
- **Medal system** - gold, silver, and bronze highlights for top 3 places
- **Win/Loss tracking** - separate buttons for winners and losers
- **Persistent scoring** - scores are maintained across game sessions
- **Round reset** functionality for fresh starts

### ⚙️ Game Controls
- **Customizable spin duration** (adjustable in seconds)
- **Player elimination** - winners/losers are removed from wheel after scoring
- **Clear visual feedback** - current player highlighting and status updates
- **Complete game reset** options

## 🛠️ Technology Stack

- **Kotlin** - Primary programming language
- **Compose Multiplatform** - Modern UI framework for cross-platform development
- **Canvas API** - Custom wheel rendering with gradients and animations
- **Coroutines** - Smooth animations and async operations
- **WebAssembly (WASM)** - Web deployment target

## 🚀 Getting Started

### Prerequisites
- JDK 11 or higher
- Gradle (included wrapper)

### Build and Run Web Application

To build and run the development version of the web app, use the run configuration from the run widget
in your IDE's toolbar or run it directly from the terminal:

**macOS/Linux:**
```shell
 ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```
**Windows:**
```shell
.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun
```

The application will automatically open in your default browser at `http://localhost:8080`

```shell
./gradlew :composeApp:wasmJsBrowserDistribution
```

## 🎯 How to Play
1. **Add Players**: Click "Add Players" and enter participant names (one per line)
2. **Spin the Wheel**: Click "Start Wheel" to begin the spinning animation
3. **Wait for Result**: Watch the countdown timer as the wheel spins
4. **Record Outcome**: Use "Won" or "Lost" buttons to record the result
5. **Track Progress**: View the leaderboard to see overall standings
6. **Continue Playing**: The selected player is removed from the wheel, continue with remaining players

## 🎨 Visual Features
- **Rainbow gradient effects** on wheel segments
- **Smooth rotation animations** with easing
- **Responsive design** that works on different screen sizes
- **Intuitive color coding** for easy player identification
- **Professional leaderboard** with medal system
- **Real-time visual feedback** and status updates

## 🔧 Configuration
- **Spin Duration**: Adjustable from 1-10+ seconds
- **Player Colors**: Automatically assigned from predefined color palette
- **Animation Speed**: Optimized for smooth 60fps performance

## 🌟 Perfect For
- Team building activities
- Classroom games
- Decision making
- Competitions and tournaments
- Party games
- Random selection tools

Built with ❤️ using Compose Multiplatform
