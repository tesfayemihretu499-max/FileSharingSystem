# Distributed File Sharing System Using Java RMI

This is a distributed application architecture tested across 3 different PCs connected via a single Wi-Fi Hotspot.

## Architecture & Setup Guide

### 1. Host Computer (PC 1 - Server & Database)
- **Role**: Acts as the central server and database host.
- **Components**: Runs `FileServer.java` and manages the MySQL database (XAMPP).
- **IP Address**: `10.172.59.17`

### 2. Client Computer 1 (PC 2)
- **Role**: Active client that connects to PC 1 via RMI.
- **Components**: Runs `FileClient.java` to Upload, Download, Search files, and Send messages.

### 3. Client Computer 2 (PC 3)
- **Role**: Second concurrent client in the distributed environment.
- **Components**: Runs the exact same `FileClient.java` to download files shared by PC 2 or exchange messages.
