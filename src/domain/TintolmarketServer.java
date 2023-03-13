package domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import catalogs.SellsCatalog;
import catalogs.UserCatalog;
import catalogs.WineCatalog;

public class TintolmarketServer {

	public static final String USERSCATFILE = "./src/usersCatalog.txt";;
	public static final String WINECATFILE = "./src/wineCatalog.txt";
	public static final String SELLSCATFILE = "./src/sellsCatalog.txt";
	public static final String MSGCATFILE = "./src/messageCatalog.txt";
	public static final String WALLETFILE = "./src/userWallet.txt";
	
	public static UserCatalog userCatalog;
	public static SellsCatalog sellsCatalog;
	public static WineCatalog wineCatalog;
	
	public static void main(String[] args) {
		System.out.println("servidor: main");
		
		userCatalog = UserCatalog.getUserCatalog();
		sellsCatalog = SellsCatalog.getSellsCatalog();
		wineCatalog = WineCatalog.getWineCatalog();
		
		TintolmarketServer tintolServer = new TintolmarketServer();
		tintolServer.startServer();
	}

	@SuppressWarnings("resource")
	public void startServer() {
		ServerSocket tintolSocket = null;
		initializeMemory();

		try {

			tintolSocket = new ServerSocket(12345);
			tintolSocket.setReuseAddress(true);

		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		while (true) {
			try {

				Socket inSocket = tintolSocket.accept();
				System.out.println("New client connected " + inSocket.getInetAddress().getHostAddress());
				ClientHandler clientSock = new ClientHandler(inSocket);
				clientSock.start();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
//		tintolSocket.close();
	}
	
	protected void initializeMemory() {
		
		initializeUserCatalog();
		initializeSellsCatalog();
		initializeWineCatalog();
		
	}
	
	private void initializeUserCatalog() {
		File usersFile = new File(USERSCATFILE);

		Scanner fileSc = null;
		try {
			fileSc = new Scanner(usersFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		while (fileSc.hasNextLine()) {
			String[] currentLine = fileSc.nextLine().split(":");
			userCatalog.add(new User(currentLine[0], currentLine[1]));
		}
		
		File userWallets = new File(WALLETFILE);
		Scanner walletSc = null;
		try {
			walletSc = new Scanner(userWallets);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		while (walletSc.hasNextLine()) {
			String[] currentLine = walletSc.nextLine().split(":");
			userCatalog.getUserByID(currentLine[0]).setBalance(Integer.parseInt(currentLine[1]));
		}
		
		fileSc.close();
		walletSc.close();
	}
	
	private void initializeSellsCatalog() {
		File sellsFile = new File(SELLSCATFILE);

		Scanner sc = null;
		try {
			sc = new Scanner(sellsFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		while (sc.hasNextLine()) {
			String[] currentLine = sc.nextLine().split(";");
			sellsCatalog.add(new Sell(currentLine[0],
										currentLine[1],
										Integer.parseInt(currentLine[2]),
										Integer.parseInt(currentLine[3]),
										currentLine[4]));
		}
		
		sc.close();
	}
	
	private void initializeWineCatalog() {
		
		File wineFile = new File(SELLSCATFILE);

		Scanner sc = null;
		try {
			sc = new Scanner(wineFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		while (sc.hasNextLine()) {
			String[] currentLine = sc.nextLine().split(";");
			wineCatalog.add(new Wine(currentLine[0], currentLine[1]));
		}
		
		sc.close();
	}

	class ClientHandler extends Thread {
		
		private Socket socket = null;
		private ObjectOutputStream outStream;
		private ObjectInputStream inStream;

		ClientHandler(Socket tintolSocket) {
			socket = tintolSocket;

			try {
				outStream = new ObjectOutputStream(socket.getOutputStream());
				inStream = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Erro nas streams da socket");
			}
		}

		public void run() {
			try {

				String clientID = null;
				String password = null;

				try {
					clientID = (String) inStream.readObject();
					password = (String) inStream.readObject();
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}

				File usersCatalog = new File(USERSCATFILE);
				File userWallets = new File(WALLETFILE);

				Scanner fileSc = new Scanner(usersCatalog);
				Scanner walletSc = new Scanner(userWallets);
				
				Boolean registed = false;
				Boolean isUserFileEmpty = true;
				Boolean isWalletFileEmpty = true;

				// wallet
				if (walletSc.hasNextLine()) {
					isWalletFileEmpty = false;
				}

				while (fileSc.hasNextLine()) {

					isUserFileEmpty = false;
					String actual = fileSc.nextLine();
					String cID = actual.split(":")[0];
					String givenPass = actual.split(":")[1];

					if (clientID.equals(cID) && password.equals(givenPass)) {
						registed = true;
						break;
					} else if (clientID.equals(cID) && !password.equals(givenPass)) {
						outStream.writeObject("erroPass");
						fileSc.close();
						walletSc.close();
						socket.close();
						// System.out.println("Programa Terminado");

						System.exit(0);
					}
				}

				if (registed) {

					outStream.writeObject("registado");

				} else {
					outStream.writeObject("NovoRegisto"); // Cliente registado

					String newClient = "";
					if (isUserFileEmpty) {
						newClient = new StringBuilder().append(clientID + ":" + password).toString();
					} else {
						newClient = new StringBuilder().append("\n" + clientID + ":" + password).toString();
					}

					OutputStream clientRegister = new FileOutputStream(usersCatalog, true);
					clientRegister.write(newClient.getBytes(), 0, newClient.length());
					clientRegister.close();

					String usersBalance = "";
					if (isWalletFileEmpty) {
						usersBalance = new StringBuilder().append(clientID + ";200").toString();
					} else {
						usersBalance = new StringBuilder().append("\n" + clientID + ";200").toString();
					}
					OutputStream wallet = new FileOutputStream(userWallets, true);
					wallet.write(usersBalance.getBytes(), 0, usersBalance.length());
					wallet.close();

				}

				interactWUser(clientID);

				fileSc.close();
				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void interactWUser(String clientID) {

			String menu = getMenu();
			String userAction = "";

			try {
				while (!userAction.equals("exit")) {

					outStream.writeObject(menu);

					try {
						userAction = (String) inStream.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

					String[] userActionSplited = userAction.split(" ");
					int arraySize = userActionSplited.length;

					if (userActionSplited[0].equals("add") || userActionSplited[0].equals("a") && arraySize == 3) {
						outStream.writeObject(
								addFunc(WINECATFILE, userActionSplited[1], userActionSplited[2]));

					} else if (userActionSplited[0].equals("sell")
							|| userActionSplited[0].equals("s") && arraySize == 4) {
						outStream.writeObject(sellFunc(WINECATFILE, SELLSCATFILE,
								userActionSplited[1], Integer.parseInt(userActionSplited[2]),
								Integer.parseInt(userActionSplited[3]), clientID));

					} else if (userActionSplited[0].equals("view")
							|| userActionSplited[0].equals("v") && arraySize == 2) {
						outStream.writeObject(
								viewFunc(WINECATFILE, SELLSCATFILE, userActionSplited[1]));

					} else if (userActionSplited[0].equals("buy")
							|| userActionSplited[0].equals("b") && arraySize == 4) {
						outStream.writeObject(
								buyFunc(SELLSCATFILE, WALLETFILE, userActionSplited[1],
										Integer.parseInt(userActionSplited[3]), userActionSplited[2], clientID));

					} else if (userActionSplited[0].equals("wallet") || userActionSplited[0].equals("w")) {
						outStream.writeObject("Saldo: " + walletFunc(WALLETFILE, clientID));

					} else if ((userActionSplited[0].equals("classify") || userActionSplited[0].equals("c"))
							&& arraySize == 3) {
						outStream.writeObject(classifyFunc(WINECATFILE, userActionSplited[1],
								Integer.parseInt(userActionSplited[2])));

					} else if ((userActionSplited[0].equals("talk") || userActionSplited[0].equals("t"))) {

						String message = "";
						for (int i = 2; i < userActionSplited.length; i++) {
							message += userActionSplited[i] + " ";
						}

						outStream.writeObject(talkFunc(USERSCATFILE, MSGCATFILE, clientID,
								message, userActionSplited[1]));

					} else if (userActionSplited[0].equals("read") || userActionSplited[0].equals("r")) {
						outStream.writeObject(readFunc(MSGCATFILE, clientID));

					} else if (userActionSplited[0].equals("exit") || userActionSplited[0].equals("e")) {
						break;

					} else {
						outStream.writeObject("Invalid action.\n");
						continue;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private String addFunc(String filename, String wine, String image) throws IOException {

			File winesCatalog = new File(filename);
			Scanner winesSc = null;

			try {
				winesSc = new Scanner(winesCatalog);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			Boolean isWineFileEmpty = true;
			String wineOfFile = "";

			while (winesSc.hasNextLine()) {
				isWineFileEmpty = false;
				wineOfFile = winesSc.nextLine().split(";")[0];
				if (wine.equals(wineOfFile))
					return "This wine already exists.";
			}

			String wineRegist = "";
			if (isWineFileEmpty) {
				wineRegist = (wine + ";" + image + ";0;0");
			} else {
				wineRegist = ("\n" + wine + ";" + image + ";0;0");
			}

			OutputStream addWine = new FileOutputStream(filename, true);
			addWine.write(wineRegist.getBytes(), 0, wineRegist.length());
			addWine.close();

			return "Wine added.";
		}

		private String sellFunc(String filenameToRead, String filenameToWrite, String wine, int value, int quantity,
				String clientID) throws IOException {

			File winesCatalog = new File(filenameToRead);
			Scanner winesSc = null;

			try {
				winesSc = new Scanner(winesCatalog);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			Boolean isFound = false;
			String[] wineFileLineSplitted = null;

			while (winesSc.hasNextLine() && !isFound) {

				String wineFileLine = winesSc.nextLine();
				wineFileLineSplitted = wineFileLine.split(";");

				if (wine.equals(wineFileLineSplitted[0])) { // nome vinho
					isFound = true;

				}
			}

			File winesCatalogSell = new File(filenameToWrite);
			Scanner winesToSellSc = null;

			try {
				winesToSellSc = new Scanner(winesCatalogSell);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			Boolean isSellsFileEmpty = true;

			if (winesToSellSc.hasNextLine()) {
				isSellsFileEmpty = false;
			}

			String wineRegist = "";
			Boolean wineExists = false;

			if (isFound) {

				while (winesToSellSc.hasNextLine()) {
					String sellCheck = winesToSellSc.nextLine();
					String[] sellCheckSplitted = sellCheck.split(";");

					if (wine.equals(sellCheckSplitted[0]) && clientID.equals(sellCheckSplitted[5])) {
						wineExists = true;

						if (value == Integer.parseInt(sellCheckSplitted[2])) {
							editFile(filenameToWrite, sellCheck, value, quantity, "sell");
							return "Wine is now on sale.";
						} else {
							editFile(filenameToWrite, sellCheck, value, quantity, "sellDifPrice");
							return "Wine is now on sale.";
						}

					}
				}

				if (!wineExists) {
					if (isSellsFileEmpty) {
						wineRegist = (wine + ";" + wineFileLineSplitted[1] + ";" + value + ";" + quantity + ";" + clientID);
					} else {
						wineRegist = ("\n" + wine + ";" + wineFileLineSplitted[1] + ";" + value + ";" + quantity + ";" + clientID);
					}
					OutputStream addWineSell = new FileOutputStream(filenameToWrite, true);
					addWineSell.write(wineRegist.getBytes(), 0, wineRegist.length());
					addWineSell.close();

					return "Wine is now on sale.";
				}
			}

			return "This wine doesnt exist.";
		}

		private String viewFunc(String wineCatalogName, String wineMarketName, String wine) {

			File wineCatalogFile = new File(wineCatalogName);
			File wineMarketFile = new File(wineMarketName);
			StringBuilder result = new StringBuilder();
			Boolean isIn = false;
			int count = 0;

			Scanner catalogSc = null;
			try {
				catalogSc = new Scanner(wineCatalogFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			Scanner marketSc = null;
			try {
				marketSc = new Scanner(wineMarketFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			String[] wineCatalogFileLine = null;
			while (catalogSc.hasNextLine()) {
				wineCatalogFileLine = catalogSc.nextLine().split(";");
				if (wineCatalogFileLine[0].equals(wine))
					;
				break;
			}

			while (marketSc.hasNextLine()) {

				String wineFileLine = marketSc.nextLine();
				String[] wineFileLineSplitted = wineFileLine.split(";");

				if (wine.equals(wineFileLineSplitted[0])) {
					isIn = true;
					count++;

					String classification = "0";
					if (count <= 1) {

						if (!wineCatalogFileLine[2].equals("0")) {
							classification = String.format("%.2f", Float.parseFloat(wineCatalogFileLine[2])
									/ Float.parseFloat(wineCatalogFileLine[3]));
						}

						result.append(wine + " information:\n Image: " + wineFileLineSplitted[1]
								+ "\n Average Classification: " + classification + "\n Wine Sellers:\n");
					}

					if (Integer.parseInt(wineFileLineSplitted[3]) > 0)
						result.append("  Seller: " + wineFileLineSplitted[5] + "; Price: " + wineFileLineSplitted[2]
								+ "; In Stock: " + wineFileLineSplitted[3] + "\n");

				}
			}

			marketSc.close();
			catalogSc.close();

			if (isIn)
				return result.toString();

			return result.append("This Wine doesnt exist").toString();
		}

		private String buyFunc(String filename, String wallet, String wine, int quantity, String sellerID,
				String clientID) throws IOException {

			File winesCatalogBuy = new File(filename);
			Scanner winesToBuySc = null;

			try {
				winesToBuySc = new Scanner(winesCatalogBuy);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			Boolean isFound = false;
			Boolean isPurchasable = false;
			String[] wineFileLineSplitted = null;
			String wineRequired = "";

			while (winesToBuySc.hasNextLine()) {

				String wineFileLine = winesToBuySc.nextLine();
				wineFileLineSplitted = wineFileLine.split(";");

				if (wine.equals(wineFileLineSplitted[0]) && sellerID.equals(wineFileLineSplitted[5])
						&& !sellerID.equals(clientID)) {
					wineRequired = wineFileLine;
					isFound = true;
					if (walletFunc(wallet, clientID) >= quantity * Integer.parseInt(wineFileLineSplitted[2])
							&& quantity <= Integer.parseInt(wineFileLineSplitted[3])) {
						isPurchasable = true;
						break;
					}
				}

				if (!isFound && !isPurchasable) {
					isFound = false;
					isPurchasable = false;
				}

			}

			if (!isFound || !isPurchasable) {
				return "\nReasons why you can't buy this wine:\n\n"
						+ " - This wine does not exists or it isn't available on this seller's stock;\n"
						+ " - Wine's seller and client are the same;\n"
						+ " - Quantity not available or insufficient funds.";
			}

			int clientNewBalance = walletFunc(wallet, clientID) - quantity * Integer.parseInt(wineFileLineSplitted[2]);
			int sellerNewBalance = walletFunc(wallet, sellerID) + quantity * Integer.parseInt(wineFileLineSplitted[2]);
			String clientBalance = new StringBuilder().append(clientID + ";" + String.valueOf(clientNewBalance))
					.toString();
			String sellerBalance = new StringBuilder().append(sellerID + ";" + String.valueOf(sellerNewBalance))
					.toString();

			// client's wallet
			editWalletFile(wallet, clientBalance, clientNewBalance, "client");

			// seller's wallet
			editWalletFile(wallet, sellerBalance, sellerNewBalance, "seller");

			editFile(filename, wineRequired, Integer.parseInt(wineFileLineSplitted[2]), quantity, "buy");
			return "Wine purchased.";

		}

		private int walletFunc(String filename, String clientID) {

			File wallet = new File(filename);
			Scanner walletSc = null;

			try {
				walletSc = new Scanner(wallet);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			while (walletSc.hasNextLine()) {

				String walletFileLine = walletSc.nextLine();
				String[] walletFileSplitted = walletFileLine.split(";");

				if (clientID.equals(walletFileSplitted[0])) {
					return Integer.parseInt(walletFileSplitted[1]);
				}
			}

			return 0;
		}

		private String classifyFunc(String wineCatalogFile, String wine, int stars) {

			if (stars < 0 || stars > 5)
				return "Your classification must be from 0 to 5";

			File winesCatalog = new File(wineCatalogFile);

			Scanner winesSc = null;

			try {
				winesSc = new Scanner(winesCatalog);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			Boolean isFound = false;
			while (winesSc.hasNextLine() && !isFound) {

				String wineFileLine = winesSc.nextLine();
				String[] wineFileLineSplitted = wineFileLine.split(";");

				if (wineFileLineSplitted[0].equals(wine)) {

					File fileToBeModified = new File(wineCatalogFile);
					BufferedReader reader = null;
					FileWriter writer = null;

					isFound = true;
					String oldContent = "";

					try {
						reader = new BufferedReader(new FileReader(fileToBeModified));

						String line = reader.readLine();
						// Reading all the lines of input text file into oldContent
						while (line != null) {
							oldContent = oldContent + line + System.lineSeparator();
							line = reader.readLine();
						}

						String newContentWithoutNewLine = "";

						// Replacing oldString with newString in the oldContent
						String newString = wineFileLineSplitted[0] + ";" + wineFileLineSplitted[1] + ";"
								+ (String.valueOf(Integer.parseInt(wineFileLineSplitted[2]) + stars)) + ";"
								+ String.valueOf(Integer.parseInt(wineFileLineSplitted[3]) + 1);
						String newContent = oldContent.replace(wineFileLine, newString);
						newContentWithoutNewLine = newContent.substring(0, newContent.length() - 2);

						// Rewriting the input text file with newContent
						writer = new FileWriter(fileToBeModified);
						writer.write(newContentWithoutNewLine);

					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							reader.close();
							writer.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

			return "Classification atributed";
		}

		private String talkFunc(String usersFilename, String messagesFilename, String clientIDSender, String message,
				String clientIDDest) throws IOException {

			File usersCatalog = new File(usersFilename);

			Scanner usersSC = null;

			try {
				usersSC = new Scanner(usersCatalog);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			Boolean isUserFound = false;
			while (usersSC.hasNextLine() && !isUserFound) {
				String userFileLine = usersSC.nextLine();
				String[] userFileLineSplitted = userFileLine.split(":");

				if (clientIDDest.equals(userFileLineSplitted[0])) {
					isUserFound = true;
				}
			}

			usersSC.close();

			if (isUserFound) {
				OutputStream addMessage = new FileOutputStream(messagesFilename, true);

				String messageRegist = (clientIDSender + ";" + clientIDDest + ";" + message);

				addMessage.write(messageRegist.getBytes(), 0, messageRegist.length());
				addMessage.close();

				return "Message sent.";
			}

			return "Unsent message, user not found.";
		}

		private String readFunc(String filename, String clientID) {

			File messagesFile = new File(filename);

			Scanner messagesSC = null;

			try {
				messagesSC = new Scanner(messagesFile);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			StringBuilder sb = new StringBuilder();
			Boolean isEmpty = true;
			int count = 0;
			Boolean isForYou = false;

			while (messagesSC.hasNextLine()) {

				isEmpty = false;
				String messageFileLine = messagesSC.nextLine();
				String[] messageFileLineSplitted = messageFileLine.split(";");

				if (clientID.equals(messageFileLineSplitted[1])) {
					count++;
					isForYou = true;
					if (count <= 1)
						sb.append("Mensagens recebidas: \n Remetente: " + messageFileLineSplitted[0] + ";\n Mensagem: "
								+ messageFileLineSplitted[2]);
					else
						sb.append("\n\n Remetente: " + messageFileLineSplitted[0] + ";\n Mensagem: "
								+ messageFileLineSplitted[2]);

					editFile(filename, messageFileLine, 0, 0, "read");
				}

			}

			if (isEmpty || !isForYou)
				return "You dont have any message to read.";
			else {
				return sb.toString();
			}
		}

		@SuppressWarnings("unused")
		private void exitFunc(Scanner sc1, Socket sock) throws IOException {
			sc1.close();
			sock.close();
			System.exit(0);
		}

		private void editFile(String filename, String editFileLine, int value, int quantity, String operation) {

			File winesCatalog = new File(filename);
			Scanner winesSc = null;

			try {
				winesSc = new Scanner(winesCatalog);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			Boolean isFound = false;
			while (winesSc.hasNextLine() && !isFound) {

				String wineFileLine = winesSc.nextLine();
				String[] wineFileLineSplitted = wineFileLine.split(";");

				if (editFileLine.equals(wineFileLine)) {
					isFound = true;

					File fileToBeModified = new File(filename);

					String oldContent = "";

					BufferedReader reader = null;

					FileWriter writer = null;

					try {
						reader = new BufferedReader(new FileReader(fileToBeModified));

						// Reading all the lines of input text file into oldContent

						String line = reader.readLine();

						while (line != null) {

							oldContent = oldContent + line + System.lineSeparator();
							line = reader.readLine();
						}

						String newContentWithoutNewLine = "";

						switch (operation) {

						case "buy":
							String newStringBuy = (wineFileLineSplitted[0] + ";" + wineFileLineSplitted[1] + ";" + value
									+ ";" + String.valueOf(Integer.parseInt(wineFileLineSplitted[3]) - quantity) + ";"
									+ wineFileLineSplitted[4] + ";" + wineFileLineSplitted[5]);
							String newContentBuy = oldContent.replace(wineFileLine, newStringBuy);
							newContentWithoutNewLine = newContentBuy.substring(0, newContentBuy.length() - 2);
							break;

						case "sell":
							String newStringSell = (wineFileLineSplitted[0] + ";" + wineFileLineSplitted[1] + ";"
									+ value + ";" + String.valueOf(Integer.parseInt(wineFileLineSplitted[3]) + quantity)
									+ ";" + wineFileLineSplitted[4] + ";" + wineFileLineSplitted[5]);
							
							String newContentSell = oldContent.replace(wineFileLine, newStringSell);
							newContentWithoutNewLine = newContentSell.substring(0, newContentSell.length() - 2);
							break;

						case "sellDifPrice":
							String newStringSellDifPrice = (wineFileLineSplitted[0] + ";" + wineFileLineSplitted[1]
									+ ";" + value + ";" + quantity + ";" + wineFileLineSplitted[4] + ";"
									+ wineFileLineSplitted[5]);
							String newContentSellDifPrice = oldContent.replace(wineFileLine, newStringSellDifPrice);
							newContentWithoutNewLine = newContentSellDifPrice.substring(0,
									newContentSellDifPrice.length() - 2);
							break;

						case "read":
							String newStringRead = "";
							String newContentRead = oldContent.replace(wineFileLine + "\r\n", newStringRead);
							newContentWithoutNewLine = newContentRead.substring(0, newContentRead.length() - 2);
							break;
						}

						writer = new FileWriter(fileToBeModified);

						writer.write(newContentWithoutNewLine);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							// Closing the resources

							reader.close();

							writer.close();
						} catch (IOException e) {
							e.printStackTrace();

						}
					}
				}

			}
		}

		private void editWalletFile(String userWallet, String editFileline, int balance, String ID) {

			File usersWallet = new File(userWallet);
			Scanner walletSc = null;

			try {
				walletSc = new Scanner(usersWallet);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			Boolean isFound = false;
			while (walletSc.hasNextLine() && !isFound) {

				String walletFileLine = walletSc.nextLine();
				String[] walletFileLineSplitted = walletFileLine.split(";");

				if (editFileline.split(";")[0].equals(walletFileLineSplitted[0])) {
					isFound = true;

					File fileToBeModified = new File(userWallet);

					String oldContent = "";

					BufferedReader reader = null;

					FileWriter writer = null;

					try {
						reader = new BufferedReader(new FileReader(fileToBeModified));

						String line = reader.readLine();

						while (line != null) {

							oldContent = oldContent + line + System.lineSeparator();
							line = reader.readLine();
						}

						String newContentWithoutNewLine = "";

						switch (ID) {

						case "client":
							String newStringCWallet = (walletFileLineSplitted[0] + ";" + String.valueOf(balance));
							String newContentCWallet = oldContent.replace(walletFileLine, newStringCWallet);
							newContentWithoutNewLine = newContentCWallet.substring(0, newContentCWallet.length() - 2);
							break;

						case "seller":
							String newStringSWallet = (walletFileLineSplitted[0] + ";" + String.valueOf(balance));
							String newContentSWallet = oldContent.replace(walletFileLine, newStringSWallet);
							newContentWithoutNewLine = newContentSWallet.substring(0, newContentSWallet.length() - 2);
							break;

						}

						writer = new FileWriter(fileToBeModified);

						writer.write(newContentWithoutNewLine);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							// Closing the resources

							reader.close();

							writer.close();
						} catch (IOException e) {
							e.printStackTrace();

						}
					}
				}

			}
		}

		private String getMenu() {
			return "\nActions:\nadd <wine> <image>\n" + "sell <wine> <value> <quantity>\n" + "view <wine>\n"
					+ "buy <wine> <seller> <quantity>\n" + "wallet\n" + "classify <wine> <stars>\n"
					+ "talk <user> <message>\n" + "read\n" + "exit\n";
		}

	}

}
