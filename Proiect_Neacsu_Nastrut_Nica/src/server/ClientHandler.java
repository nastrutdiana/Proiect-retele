package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler implements Runnable {

	private static Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;

	public ClientHandler(Socket socket) throws IOException {
		Server.listaSockets.add(socket);
		Server.nrSockets++;
		this.socket = socket;
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.writer = new PrintWriter(socket.getOutputStream(),true);
	}

	@Override
	public void run() {
		while (!socket.isClosed()) {
			try {
				writer.println("Numele va rog ");
				writer.println("");
				String tmp;
				int stadiu=0;
				String numeClient="";
				int masaRezervata=0;
				Timer timer = new Timer();
				try {
					while((tmp = reader.readLine()) != null) {

						String mesaj="";
						//nu ai voie exit din prima
						if ("exit".equals(tmp)) {
							socket.close();
						}
						if(stadiu==0){

							if(tmp!=null && tmp!="\n") {
								numeClient=tmp;
								String mese = "";
								for (int i = 0; i <= 100; i++) {
									mese += i + "        ";
								}

								writer.println("Resursele si starea lor: ");
								writer.println(mese);
								writer.println(Server.listaResurse().toString());
								writer.println("Ce resursa dintre cele libere doriti blocati pentru a finaliza rezervarea? " +
										" Sau scrieti 'anulare' pentru a va anula rezervarea precedenta.");
								writer.println("");
								stadiu++;
							}
						}
						else if (stadiu==1){
							if(tmp.compareTo("anulare")==0){
								for (String element : Server.listaResurse){
									if (element.contains(numeClient)){
										Server.listaResurse.set(Server.listaResurse().indexOf(numeClient),"libera");
									}

								}
								writer.println("V-ati anulat rezervarea cu succes. "+"La revedere! "+"Numele din nou va rog sau 'exit' pentru a inchide:");
								writer.println("");
								stadiu=0;



							}else if(Server.listaResurse.get(Integer.parseInt(tmp)).compareTo("libera")==0){
								masaRezervata=Integer.parseInt(tmp);
								Server.listaResurse.set(Integer.parseInt(tmp),numeClient);

								writer.println("Ati blocat resursa "+tmp +" pentru urmatoarele 2 minute."+"Doriti sa finalizati rezervarea?");
								writer.println("");
								System.out.println(Server.listaResurse());
								//notificare clienti
								Server.notifySocketsBlocata(masaRezervata);




								final int tmp2=Integer.parseInt(tmp);
								final String numeClient2=numeClient;
								timer.scheduleAtFixedRate(new TimerTask() {
									int i = 120;
									public void run() {
										i--;
										if (i < 0) {

											Server.listaResurse.set(tmp2,"libera");
											try {
												Server.notifySocketsLibera(tmp2);
											} catch (IOException e) {
												e.printStackTrace();
											}
											timer.cancel();

										}
									}
								}, 0, 1000);

								stadiu ++;
							}else{
								writer.println("Resursa nu e libera sau ati introdus gresit! Reincercati!");
								writer.println("");
							}
						}
						else if(stadiu==2){
							if(tmp.compareTo("da")==0){
								if(Server.listaResurse.get(masaRezervata).compareTo(numeClient)==0) {
									Server.listaResurse.set(masaRezervata, numeClient);
									writer.println("Rezervare finalizata!" + "Numele din nou va rog sau 'exit' pentru a inchide:");
									writer.println("");
									System.out.println(Server.listaResurse());
									//notificare clienti
									Server.notifySocketsRezervata(masaRezervata);

									stadiu = 0;
								}else{

									writer.println("Rezervare anulata! Timpul a expirat! Va rugam sa va reintroduceti numele pentru a primi resursele libere sau 'exit' pentru a inchide!");
									writer.println("");
									stadiu-=2;
								}

							}else{
								timer.cancel();
								writer.println("Rezervare anulata! Va rugam sa va reintroduceti numele pentru a primi resursele libere sau 'exit' pentru a inchide!");
								writer.println("");
								Server.listaResurse.set(masaRezervata,"libera");
								//notificare clienti
								Server.notifySocketsLibera(masaRezervata);
								//System.out.println(Server.listaResurse());
								stadiu-=2;
							}

						}else{
							stadiu=0;
							writer.println("Numele din nou va rog sau 'exit' pentru a inchide:");
							writer.println("");
						}




						System.out.println("Client: " + tmp);

					}
				} catch (IOException e) {
					e.printStackTrace();
				}


			} catch (Exception e) {
				writer.println(e.getMessage());
				writer.println("");
			}
		}
	}

}