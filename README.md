# Reliable-Data-Transfer

This application handles the sending and receiving of data at the transport layer. The
application executes in a simulated network environment which mimics that of a real OS.

Packets are sent from entity A and received by entity B. B will have to send a response
packet to A, to acknowledge receipt of data. The program must be able to recover from
packet corruption and packet loss, which are common occurrence with data transfer.


Running this application

-Requirements:
	Jave Runtime Enviroment

Steps to Run:
	1. Clone/Download this Repository
	2. Open and run the Java Application in the enviroment (IDE) of your choice
	3. Follow the outputted instructions:
		Specify the number of packets to send
		Specify the probability of packt loss *(decimal 0.3)
		Specify the probability of packt corruption *(decimal 0.3)
		Specify the time between messages sent from A
		The trance level refers to the detail of the output (3=max)
	4. Inspect the output to note the recovery of packet loss and packet corruption