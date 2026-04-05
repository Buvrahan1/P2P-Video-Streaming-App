# 🛰️ P2P Video Streaming Application

A robust, decentralized peer-to-peer video streaming system developed in **Java**. [cite_start]This project demonstrates advanced networking concepts including node discovery via **UDP**, reliable data transfer through **multi-threaded TCP**, and real-time video playback orchestration. [cite: 38, 39, 74]

## 🚀 Key Features

* [cite_start]**UDP Node Discovery**: Implements automatic peer discovery within a network using UDP broadcast/multicast. [cite: 39, 74]
* [cite_start]**Multi-threaded TCP Transfer**: Orchestrates concurrent TCP connections to handle high-speed video chunk transfers efficiently. [cite: 39, 74]
* [cite_start]**Chunk-Based Streaming**: Video files are fragmented into manageable chunks to optimize network throughput and minimize buffering. [cite: 39, 74]
* [cite_start]**Concurrent Architecture**: Utilizes Java's concurrency utilities to manage simultaneous upload and download streams without blocking the main UI or playback. [cite: 39]
* [cite_start]**Video Playback Support**: Integrated support for seamless video rendering during the streaming process. [cite: 74]

## 🛠️ Technical Stack

* [cite_start]**Language**: Java [cite: 38, 43, 82]
* [cite_start]**Protocols**: TCP (for reliable data delivery), UDP (for discovery) [cite: 38, 39, 45]
* [cite_start]**Concurrency**: Multi-threading for handling asynchronous network operations [cite: 39]
* [cite_start]**Architecture**: Peer-to-Peer (Decentralized) [cite: 39]

## 🏗️ System Logic

1.  [cite_start]**Discovery Phase**: When a node joins, it sends a UDP packet to find other active peers on the network. [cite: 39, 74]
2.  [cite_start]**Requesting Chunks**: The client identifies which peer has the required video segments and initiates a TCP handshake. [cite: 39, 74]
3.  [cite_start]**Transfer**: The video is sent in multi-threaded chunks to ensure that one slow connection doesn't bottleneck the entire stream. [cite: 39]

## 🔧 Installation & Setup

1.  **Clone the Repository**:
    ```bash
    git clone [https://github.com/Buvrahan1/P2P-Video-Streaming-App.git](https://github.com/Buvrahan1/P2P-Video-Streaming-App.git)
    ```
2.  **Compile the Project**:
    Ensure you have JDK 8 or higher installed.
    ```bash
    javac *.java
    ```
3.  **Run Multiple Instances**:
    Open two terminals to simulate different peers.
    ```bash
    # Peer 1
    java MainClass
    # Peer 2
    java MainClass
    ```

---
