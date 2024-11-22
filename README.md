### Setup Instructions
1. Clone the repo and add it to eclipse
2. Make sure you're on java 21. run `java --version`
3. Click on help at the top and search for "marketplace" to open the eclipse marketplace.
4. Install `Spring Tools 4`
5. Might take a min
6. Right click on the project and click on Gradle > Refresh Gradle Project
7. Right click on the project and click on Properties.
8. Go to Java Compiler, Enable project specific settings at the top, enable 'Store information about method parameters (usbale via reflection) at the bottom.
9. Apply and Close
10. Right click on the project and click Run as > Spring Boot App
11. Go to http://localhost:8000/hello/world and if it says `Hello, world!` then we good !
