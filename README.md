[![Open in Visual Studio Code](https://classroom.github.com/assets/open-in-vscode-2e0aaae1b6195c2367325f4f02e2d04e9abb55f0b24a779b69b11b9e10269abc.svg)](https://classroom.github.com/online_ide?assignment_repo_id=21005516&assignment_repo_type=AssignmentRepo)
# COSC2803 2450 Week 10 Workshop 7 Starter Code
Starter code for Week 07 Workshop for Javalin and Thymeleaf.

Folders:
```bash
├── /src/main                    - Location of all files as required by build configuration
│         ├── java               - Java Source location
│         │    └── app           - package location for all Java files
│         └── resources          - Web resources (html templates / style sheets)
│               ├── css          - CSS Style-sheets
│               └── images       - Image files
│ 
├── /target                      - build directory (DO NOT MODIFY)
├── /database                    - The folder to store sqlite database files (*.db files)
├── pom.xml                      - Configure Build (DO NOT MODIFY)
└── README.md                    - This file ;)
```

Current Libraries
* org.xerial.sqlite-jdbc         - SQLite JDBC library
* javalin (lightweight java webserver)
* thymeleaf (HTML template) - https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html

Libraries required as dependencies
* By javalin/thymeleaf
   * slf4j-simple (lightweight logging)
* By xerial/jdbc
   * sqlite-jdbc

# Building & Running the code
1. Open this project within VSCode
2. Allow VSCode to read the pom.xml file
 - Allow the popups to run and "say yes" to VSCode configuring the build
 - Allow VSCode to download the required Java libraries
3. To Build & Run
 - Select the "Launch App" launcher profile from the VSCode Run/Debugger
 - Alternatively, open the src/main/java/app/App.java source file, and select "Run" from the pop-up above the main function
4. Go to: http://localhost:7001

# Important Notes
1. ONLY modify the files which you are allowed to edit. The other files are placed in important places to make our "big software project" work.
1. DO NOT move the Java files from the ```src/main/java/app``` folder. These Java files need to be in this location to ensure our "big software project" works.
1. These exercises contain examples for *both* pre-req and co-req students. There is no separation of code in these exercises, as all students are able to complete them.

# DEV Container for GitHub Codespaces
The ```.devcontainer``` folder contains configuration files for GitHub Codespaces.
This ensures that when the GitHub classroom is cloned, the workspace is correctly configured for Java (V16) and with the required VSCode extensions.
This folder will not affect a *local* VSCode setup on a computer.

**🚨 DO NOT MODIFY THE CONTENTS OF THIS FOLDER. 🚨**

# Authors
* Dr. Halil Ali, School of Computing Technologies, STEM College, RMIT University.
* Dr. Timothy Wiley, School of Computing Technologies, STEM College, RMIT University.
* Prof. Santha Sumanasekara, School of Computing Technologies, STEM College, RMIT University.

Copyright RMIT University (c) 2024
