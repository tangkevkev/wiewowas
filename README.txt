The application consists of the Android app, tested on the Nexus 5 and in the emulator, and the server.

Per default the app connects to our server running on a raspberry pi.
The HOST and PORT can be changed in the class ch.ethz.inf.vs.project.forstesa.wiewowas.server.ServerConfiguration

When running the server yourself, the port number can be provided as an argument, e.g. in the console.
The server uses two libraries:
	com.google.code.gson:gson:2.8.2
	org.xerial:sqlite-jdbc:3.21.0

We provided them with the project.
We also included a compiled version of the server in a jar file.