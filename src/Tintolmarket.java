
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
						
//					if (userActionSplited[0].equals("add") || userActionSplited[0].equals("a") && arraySize == 3) {
//					} else if (userActionSplited[0].equals("sell") || userActionSplited[0].equals("s") && arraySize == 3) {
//					} else if (userActionSplited[0].equals("view") || userActionSplited[0].equals("v") && arraySize == 2) {
//					} else if (userActionSplited[0].equals("buy") || userActionSplited[0].equals("b")) {
//
//					} else if (userActionSplited[0].equals("wallet") || userActionSplited[0].equals("w")) {
//
//					} else if (userActionSplited[0].equals("classify") || userActionSplited[0].equals("c")) {
//
//					} else if (userActionSplited[0].equals("talk") || userActionSplited[0].equals("t")) {
//
//					} else if (userActionSplited[0].equals("read") || userActionSplited[0].equals("r")) {
//
//					} else if (userActionSplited[0].equals("exit") || userActionSplited[0].equals("e")) {
//						break;
//					} else {
//						continue;
//					}
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
