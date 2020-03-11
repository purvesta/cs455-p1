JFLAGS =
JC = javac
.SUFFIXES: .class .java

.java.class:
	$(JC) $(JFLAGS) $<

CLASSES = \
        ChatServer.class \
        Server.class \
        Channel.class \
        ChatClient.class \
        Client.class \
        Connection.class \
        Data.class \
        Message.class \
        MessageWatcher.class \
        User.class

all: classes

classes: $(CLASSES:.java=.class) 
		
clean:
	/bin/rm -f *.class
