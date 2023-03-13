package domain;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Tintolmarket {

	public static void main(String[] args) {

		String hostname = "localhost";
		int port = 12345;
		try (Socket socket = new Socket(hostname, port)) {

			ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

			Scanner clientInterface = new Scanner(System.in);
			
			System.out.println("ClientID:");
			String clientID = clientInterface.next();
			outStream.writeObject(clientID);
			if (args.length < 2) {
				System.out.println("Password:");
				String password = clientInterface.next();
				outStream.writeObject(password);
			} else {
				outStream.writeObject(args[1]);
			}

			try {

				String loginCheck = (String) inStream.readObject();
				
				if (loginCheck.equals("erroPass")) {
					System.out.println("Password invalida. Programa Terminado.");
					System.exit(0);
				} else if (loginCheck.equals("NovoRegisto")) {
					System.out.println("Novo cliente registado.");
				}

				String userAction = "";
				clientInterface.nextLine();

				while (!userAction.equals("exit")) {

					System.out.println((String) inStream.readObject()); // menu
					System.out.println("Choose action:\n");
					
					userAction = clientInterface.nextLine();
					outStream.writeObject(userAction);
					
					String[] userActionSplited = userAction.split(" ");
					int arraySize = userActionSplited.length;

					System.out.println((String)inStream.readObject());
				
				}

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		} catch (UnknownHostException ex) {

			System.out.println("Server not found: " + ex.getMessage());

		} catch (IOException ex) {

			System.out.println("I/O error: " + ex.getMessage());
		}
	}
}
