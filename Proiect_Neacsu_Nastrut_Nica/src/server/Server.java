package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Server implements AutoCloseable {

	private ServerSocket serverSocket;
	private static ArrayList<Socket> socketList;
	public static int nrSockets=0;
	public static ArrayList<String> listaResurse= new ArrayList<String>();
	public static ArrayList<Socket> listaSockets= new ArrayList<Socket>();

	public Server(int port) throws IOException {
		System.out.println("Server start:");
		serverSocket = new ServerSocket(port);
		listaResurse.add(0,"nevalabil");
		for(int i=1;i<=100;i++)
		{
			listaResurse.add(i,"libera");

		}
		//System.out.println("Initializare resurse");
		//socketList.add(0,serverSocket.accept());
		//System.out.println("Initializare socketuri");
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		executorService.execute(() -> {
			while (!serverSocket.isClosed()) {
				try {
					//System.out.println("Initializare socketuri2");
					//Socket s = serverSocket.accept();
					//socketList.add(serverSocket.accept());
					//System.out.println(Server.listaResurse);

					//System.out.println("Initializare socketuri3");
					//executorService.submit(new ClientHandler(socketList.get(nrSockets-1)));
					executorService.submit(new ClientHandler(serverSocket.accept()));

					System.out.println("Initializare socketuri4");
					//System.out.println(Server.listaResurse);
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
		});
	}
	public static void notifySocketsBlocata(int resursaBlocata) throws IOException {
		for(int i=0;i<nrSockets;i++){
			PrintWriter writer = new PrintWriter(listaSockets.get(i).getOutputStream(),true);
			writer.println("Resursa blocata "+resursaBlocata);
			//new ClientHandler(listaSockets.get(i)).notifyBlocked(resursaBlocata);
		}
	}
	public static void notifySocketsRezervata(int resursaRezervata) throws IOException {
		for(int i=0;i<nrSockets;i++){
			PrintWriter writer = new PrintWriter(listaSockets.get(i).getOutputStream(),true);
			writer.println("Resursa Rezervata "+resursaRezervata);
			//new ClientHandler(listaSockets.get(i)).notifyRezervata(resursaRezervata);
		}
	}
	public static void notifySocketsLibera(int resursaLibera) throws IOException {
		for(int i=0;i<nrSockets;i++){
			PrintWriter writer = new PrintWriter(listaSockets.get(i).getOutputStream(),true);
			writer.println("Resursa Libera "+resursaLibera);
			//new ClientHandler(listaSockets.get(i)).notifyLibera(resursaLibera);
		}
	}
	public static ArrayList<String> listaResurse() throws IOException {

		return listaResurse;
	}

	@Override
	public void close() throws Exception {
		serverSocket.close();	
	}

}