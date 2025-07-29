# INTRODUCTION

The work consists of implementing the Tintolmarket system, which is a client-server type system 
offering a similar service to that of Vivino, but allowing the purchase and sale of wines by system users.

A server application "Tintolmarket server" maintains information about registered users and listed wines, 
including their value, rating, and quantity made available by each user.

Users can:

- Add wines  
- Indicate available quantity  
- Rate each wine  
- Send private messages to other users  

Registered users interact with the server through a client application.

# USAGE

## Running in an IDE:

1. Run the `TintolMarketServer` class  
2. Then run the `TintolMarket` client class  

You can run multiple clients — it's a multithreaded application.

## Running from command prompt:

1. Navigate (`cd`) into the project folder  
2. Run the server, then the client  

### Server:

```
java -jar server.jar <port>
```

- `<port>`: TCP port to accept client connections (default: `12345`)

### Client:

```
java -jar client.jar <serverAddress> <userID> [password]
```

- `<serverAddress>`: IP or hostname of the server, with optional `:port` (default port is `12345`)  
- `<userID>`: Unique identifier for the user  
- `[password]`: Optional — if not provided, the client prompts for it

# LIMITATIONS

The client must follow the exact input format for actions:

```
Actions:
  - add <wine> <image>
  - sell <wine> <value> <quantity>
  - view <wine>
  - buy <wine> <seller> <quantity>
  - wallet
  - classify <wine> <stars>
  - talk <user> <message>
  - read
  - exit
```

📌 Note: To use the `add` command, the wine image file must exist in the local directory of the client.
