# My Cellular Automatons

An interactive Java implementation of Conway's Game of Life and other cellular automata using JavaFX.

## Features

- Intuitive graphical interface with JavaFX
- Simulation control (pause/resume)
- Adjustable initial cell density
- Direct drawing on the grid with mouse
- Zoom in/out with mouse wheel
- Real-time FPS display

## Prerequisites

- Java 11 or higher
- JavaFX SDK
- Visual Studio Code with Extension Pack for Java

## VSCode Setup

1. Install the "Extension Pack for Java" in VSCode

2. Create a `settings.json` file in your project's `.vscode` folder with the following configuration:
```json
{
    "java.project.sourcePaths": ["src"],
    "java.project.outputPath": "bin",
    "java.project.referencedLibraries": [
        "lib/**/*.jar",
        "path/to/javafx-sdk/lib/*.jar"
    ]
}
```

3. Create a `launch.json` file in the `.vscode` folder:
```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Launch Cellular Automata",
            "request": "launch",
            "mainClass": "DynamicGrid",
            "vmArgs": "--module-path \"path/to/javafx-sdk/lib\" --add-modules javafx.controls,javafx.fxml"
        }
    ]
}
```

Replace "path/to/javafx-sdk/lib" with the actual path to your JavaFX installation.

## Usage

- Left click to activate a cell
- Right click to deactivate a cell
- Use mouse wheel to zoom in/out
- "Pause/Resume" button to control the simulation
- "Reset" button to reinitialize the grid
- Adjust cell density with the slider

## Game of Life Rules

1. A dead cell with exactly 3 live neighbors becomes alive
2. A live cell with 2 or 3 live neighbors stays alive
3. In all other cases, cells die or stay dead

## Contributing

Contributions are welcome! Feel free to:
1. Fork the project
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.