package uyun.bat.agent.impl.autosync.entity;


import java.util.ArrayList;
import java.util.List;

public class AutoSync {
	private Server server;
	private List<Client> clients = new ArrayList<Client>();

	public List<Client> getClients() {
		return clients;
	}

	public void setClient(Client[] clients) {
		System.out.println("set");
	}

	public void addClient(Client client) {
		clients.add(client);
	}

	public Server getServer() {
		if (server == null)
			server = new Server();
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

}
