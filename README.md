# CS455 p1-chatserver

* Authors: Nate St. George, Tanner Purves
* Class: CS455 or CS555 [Distributed Systems] Section #1 Spring 2020

## Overview

TimeServer which accepts 1 request every 5 second window and replies with the current time.

## Files
    * Channel.java    - Source file
    * ChatClient.java - Client entrypoint file
    * ChatServer.java - Server entrypoint file
    * Connection.java - Source file
    * Data.java       - Source file
    * Message.java    - Source file
    * User.java       - Source file
    * README.md       - This file

## Building the code

From within the root directory of the project:

    $ make
    $ java ChatServer -p <port>

Then in another terminal:
    $ java ChatClient

## Testing

Testing this code was fairly simple. Through simply interacting with the chat application and adding debug statements throughout the code, we were able to simply and swiftly test and debug the code. We ran through every feature that was listed on the assignment documentation when testing the program by hand so that we could ensure we had implemented the necessary features.

## Reflection

Throughout the development process, we communicated well as a team. This allowed for easy distribution of the workload. I (Tanner) was able to tackle implementing features such as server/client initial communication and connection, as well as the creation of channels, server timeout, shutdown hook, and the help and stats command. Nate was able to perfect the communication between the client and server as well as develop the code to spin off new threads for each incoming connection, as well as the nick, join, leave, and quit commands.

As far as the development process goes, we simply used a github repository and communication to tackle the project. This was a system that worked quite well in this scenario.
