# Reliable-Data-Transfer

This application handles the sending and receiving of data at the transport layer. The
application executes in a simulated network environment which mimics that of a real OS.

Packets are sent from entity A and received by entity B. B will have to send a response
packet to A, to acknowledge receipt of data. The program must be able to recover from
packet corruption and packet loss, which are common occurrence with data transfer.


This Repository contains:

- The Java application
- A file with instructions of how to run the application 'RunConfig'.
- A file detailing in depth how the application was implemented 'ImplementingReliableDataTransferProtocol'.
- An image from the running application highlighting recovery from packet loss and packet corruption 'PacketRecovery'.
