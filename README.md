MineProxy
=========
Description
-----------
This proxy will simulate a Minecraft Server and will forward every requests between the user and the server.
It provides authentication to get the secret key used to cipher data for "online" servers.
It will need the file writen by Minecraft that contains tokens (this proxy does not need any credentials).

Take a look at the files in src/eu/mygb/mineproxy/test to see how to use the proxy.
Currently, it is configured to forward the packets (and not modify them) after authenticating the user.

How it works ?
--------------
Minecraft client opens connection to the proxy
The proxy opens a connection to the server.
The client sends a handshake, the proxy forwards to the server.
The server responds with a public key.
The proxy creates a new public key.
It decodes the secret given in the client response and changes the token.
To allow the server to validate the username (for online servers), the proxy sends a HTTPS request to the session-server.
At this point, the bridge is operationnal.

TODO
----
Write Javadoc.
Add functions to easily switch between "modification on the fly" or "listening" mode.
Optimize speed.
Add GUI.

Credits
-------
http://wiki.vg for protocol spec
