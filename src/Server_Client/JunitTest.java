package Server_Client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import Server_Client.Server.ClientThread;

class JunitTest {
	Server server1 = new Server(1500);
	Client client1 = new Client("localhost", 1500, "roi");
	Client client2 = new Client("localhost", 1500, "gal");
	@Test
	void testClientStringIntString() {
		String name  = client1.getUsername();
		assertEquals("roi", name);

	}

	@Test
	void start() {
		server1.start();
		client1.start();
		
	}
	
}
