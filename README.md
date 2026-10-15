# 🌐 NetChat — TCP/UDP Chat Application

A command-line chat application built with **Java** that demonstrates communication using TCP and UDP sockets. NetChat allows users to exchange messages between a client and server, monitor communication statistics, and run network diagnostics to observe round-trip time (RTT).

## ✨ Features

- **TCP Chat:** Reliable, connection-oriented client–server messaging.
- **UDP Chat:** Connectionless messaging using UDP datagrams.
- **Message Statistics:** View sent and received message counts and byte statistics.
- **Network Diagnostics:** Run diagnostic probes to measure application-level RTT and response rates.
- **Packet Loss Simulation:** Configure simulated packet loss on the UDP server to observe its effects on communication.
- **Command-Line Interface:** Select the protocol and choose whether to start a server or client.

## 🛠️ Technologies Used

- Java
- TCP and UDP sockets (`java.net`)
- Java I/O (`java.io`)
- Multithreading
- Concurrent collections and atomic variables
- Maven

## 📋 Prerequisites

- **JDK 25 or compatible Java 25 toolchain**, as configured in `pom.xml`.
- Apache Maven
- A terminal or command-line environment

Check your installations:

```bash
java -version
javac -version
mvn -version
```

## 📥 Installation

Clone the repository:

```bash
git clone https://github.com/SuperAnand67/chatapp.git
cd chatapp
```

Build the project using Maven:

```bash
mvn clean compile
```

## 🚀 Running NetChat

After compiling, start the application:

```bash
java -cp target/classes com.axiroren.App
```

The application presents a menu similar to this:

```text
========== NetChat - a TCP/UDP Chat App ==========
1. TCP
2. UDP
Select Mode :
```

Choose a protocol, then select whether to start the server or client.

### 🖥️ Starting a Server

1. Open a terminal.
2. Run the application.
3. Select `1` for TCP or `2` for UDP.
4. Select `1` to start the server.
5. Keep the server running while connecting a client.

### 💻 Starting a Client

1. Open another terminal.
2. Run the application.
3. Select the same protocol as the server.
4. Select `2` to start the client.
5. Enter the server's IP address.

For testing on the same computer, enter:

```text
127.0.0.1
```

For communication between computers on the same network, enter the server computer's LAN IP address. Ensure the server is reachable and the required port is allowed through the firewall.

**Default port:** `8088` for both TCP and UDP.

> Start the server before connecting the client.

## 🎮 Available Commands

### Common commands

| Command | Description |
|---|---|
| `/stats` | Displays message and byte statistics. |
| `/diagnose` | Runs network diagnostic probes and reports response and RTT measurements. |
| `exit` | Requests termination of the chat session. |

### UDP-specific command

The UDP server supports configurable simulated packet loss.

Enter this command in the **UDP server terminal**:

```text
/simulateloss 25
```

This configures a simulated packet-loss probability of 25% for incoming UDP messages.

| Command | Effect |
|---|---|
| `/simulateloss 0` | Disables simulated packet loss. |
| `/simulateloss 25` | Simulates 25% packet loss. |
| `/simulateloss 50` | Simulates 50% packet loss. |
| `/simulateloss 100` | Drops all eligible incoming messages subject to the simulation. |

The configured percentage can be changed from the server terminal while the server is running.

## 🔍 Network Diagnostics

NetChat includes diagnostic functionality for both TCP and UDP.

### TCP diagnostics

The TCP client sends diagnostic requests and measures the time taken to receive corresponding server responses.

The diagnostic report includes:

- Number of probes sent
- Number of responses received
- Number of timed-out probes
- Response rate
- Minimum RTT
- Average RTT
- Maximum RTT

### UDP diagnostics

The UDP client sends diagnostic messages and uses application-level acknowledgements (ACKs) to measure response times.

The diagnostic report includes:

- Number of probes sent
- ACKs received
- ACK timeouts
- ACK response rate
- Minimum RTT
- Average RTT
- Maximum RTT

UDP's configurable packet-loss simulation can be used to observe how missing datagrams or acknowledgements affect diagnostic results.

**Note:** These measurements represent application-level request–response times. They are not direct measurements of raw network latency or TCP's internal retransmission statistics.

## ⚖️ TCP vs UDP

| Property | TCP | UDP |
|---|---|---|
| Connection | Connection-oriented | Connectionless |
| Delivery | Reliable, ordered byte stream | Delivery is not guaranteed |
| Built-in retransmission | Yes | No |
| Application-level diagnostics | Request–response timing | ACK-based timing |
| Packet-loss simulation | Not implemented in the current version | Supported |
| Typical use | Applications requiring reliable delivery | Applications that need datagrams and can handle loss |

The project provides a practical way to explore how these two transport protocols behave differently.

## 📁 Project Structure

```text
chatapp/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── axiroren/
    │               ├── App.java
    │               ├── TCP/
    │               │   ├── TCPClient.java
    │               │   └── TCPServer.java
    │               └── UDP/
    │                   ├── UDPClient.java
    │                   └── UDPServer.java
    └── test/
        └── java/
            └── com/
                └── axiroren/
                    └── AppTest.java
```

## 🎓 Learning Objectives

This project demonstrates:

- Socket programming in Java
- Client–server communication
- Differences between TCP and UDP
- Multithreading for handling incoming messages
- Network statistics and RTT measurement
- Application-level acknowledgements
- Simulated packet loss and its effects on UDP communication

## ⚠️ Limitations

- This is an educational command-line project, not a production-ready messaging service.
- The application does not implement message encryption or user authentication.
- Diagnostic results depend on the network, machine, and application response time.
- UDP does not guarantee delivery, ordering, or duplicate protection.
- The current simulated packet-loss feature applies to eligible incoming UDP messages handled by the server.

## 📄 License

No license has been specified yet. Add a license file if you intend to distribute or allow others to reuse the project.
