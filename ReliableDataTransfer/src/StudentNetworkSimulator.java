
public class StudentNetworkSimulator extends NetworkSimulator {
    /*
     * Predefined Constants (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and
     *                     Packet payload
     *
     *   int A           : a predefined integer that represents entity A
     *   int B           : a predefined integer that represents entity B
     *
     *
     * Predefined Member Methods:
     *
     *  void stopTimer(int entity): 
     *       Stops the timer running at "entity" [A or B]
     *  void startTimer(int entity, double increment): 
     *       Starts a timer running at "entity" [A or B], which will expire in
     *       "increment" time units, causing the interrupt handler to be
     *       called.  You should only call this with A.
     *  void toLayer3(int callingEntity, Packet p)
     *       Puts the packet "p" into the network from "callingEntity" [A or B]
     *  void toLayer5(int entity, String dataSent)
     *       Passes "dataSent" up to layer 5 from "entity" [A or B]
     *  double getTime()
     *       Returns the current time in the simulator.  Might be useful for
     *       debugging.
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for
     *       debugging, but probably not.
     *
     *
     *  Predefined Classes:
     *
     *  Message: Used to encapsulate a message coming from layer 5
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      boolean setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *          returns true on success, false otherwise
     *      String getData():
     *          returns the data contained in the message
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet that is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload)
     *          creates a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and a
     *          payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and
     *          an empty payload
     *    Methods:
     *      boolean setSeqnum(int n)
     *          sets the Packet's sequence field to "n"
     *          returns true on success, false otherwise
     *      boolean setAcknum(int n)
     *          sets the Packet's ack field to "n"
     *          returns true on success, false otherwise
     *      boolean setChecksum(int n)
     *          sets the Packet's checksum to "n"
     *          returns true on success, false otherwise
     *      boolean setPayload(String newPayload)
     *          sets the Packet's payload to "newPayload"
     *          returns true on success, false otherwise
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      String getPayload()
     *          returns the Packet's payload
     *
     */

    /*  QUESTIONS!!!!   */
    // Correct implementation of handeling duplicated packets
    // Checksum required when sending from B (checksum = seq+ack)
    // Define global variables
    protected boolean waiting = false;
    protected int packetSeq;
    protected int expectedPacketSeq;
    protected Packet savedPacket;

    // This is the constructor.  Don't touch!
    public StudentNetworkSimulator(int numMessages,
            double loss,
            double corrupt,
            double avgDelay,
            int trace,
            long seed) {
        super(numMessages, loss, corrupt, avgDelay, trace, seed);
    }

    // Checksum is passed a string, which is looped through
    // the value of each char is added to the checksum
    protected int checksum(String msg) {
        int checksum = 0;
        int count = 0;

        while (count < msg.length()) {
            checksum += msg.charAt(count);
            count++;
        }

        // returns the value of the computed checksum
        return (checksum);
    }

    // This routine will be called whenever the upper layer at the sender [A]
    // has a message to send.  It is the job of your protocol to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving upper layer. Return 1 if accepting the message to send, 
    // return 0 if refusing to send the message
    protected int aOutput(Message message) {
        //System.out.println("");
        //System.out.println("SEND MESSAGE");

        // Use a stop and wait functionality
        // Refusing to send the next packet until an acknowledgment has been returned
        // Allowing only one packet to be sent at a time        
        if (waiting) {
            return 0;
        } // no packet currently transmitting
        else {
            System.out.println("Packet " + packetSeq + " being sent, Stop and Wait");

            //Get the message data as a string
            String msg = message.getData();

            // passes the message data to the checksum method
            // includes the sequence number and ack, 
            // as these are also susceptible to corruption
            int checksum = checksum(msg) + packetSeq + 0;

            // Build packet
            // Include the sequence number, checksum and message data
            // No acknowledgment so set to 0
            Packet p = new Packet(packetSeq, 0, checksum, message.getData());

            //save packet, incase it needs to be resent
            savedPacket = p;
            //System.out.println("Packet num " + packetSeq + " saved");

            //start timer
            // (entity the timer starts at, time before it is interupted)
            System.out.println("Timer started");
            startTimer(A, 50);

            // pass the packet to the lower transport layer i.e layer3
            System.out.println("Send packet to B");
            toLayer3(A, p);

            //set waiting to true (stop and wait)
            //System.out.println("Waiting is true");
            waiting = true;

            // Packet accepted and sent
            return 1;
        }
    }

    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by a B-side procedure)
    // arrives at the A-side.  "packet" is the (possibly corrupted) packet
    // sent from the B-side.
    // Dealing with acknowledgments sent from B
    protected void aInput(Packet packet) {
        //System.out.println("");
        //System.out.println("CONFIRM PACKAGE Acknowledgement");    	         

        //extract the checksum, sequence number and payload from the packet
        int receivedChk = packet.getChecksum();
        int receivedSeq = packet.getSeqnum();
        int receivedAck = packet.getAcknum();

        // packet is still susceptible to corruption even without a payload        
        int checksum = receivedSeq + receivedAck;

        // confirms the re-computed checksum matches the checksum sent in the packet
        // confirms the recieved ack is possitive
        if (checksum == receivedChk && receivedAck == 1) {
            System.out.println("Packet " + packet.getSeqnum() + " recieved sucessfully");

            //stop timer
            stopTimer(A);
            System.out.println("Timer Stoped");

            //set waiting false
            System.out.println("Waiting false");
            waiting = false;

            // increment the packet number
            //System.out.println("Sequence incremented");
            packetSeq++;
        } else {
            System.out.println("Packet was corrupted, restart timer, resend packet");

            //stop timer
            stopTimer(A);
            //System.out.println("Timer Stoped");

            //restart timer
            //System.out.println("Restart timer");
            startTimer(A, 50);

            //resend packet
            //System.out.println("Resend packet");
            toLayer3(A, savedPacket);
        }
    }

    // This routine will be called when A's timer expires (thus generating a 
    // timer interrupt). You'll probably want to use this routine to control 
    // the retransmission of packets. See startTimer() and stopTimer(), above,
    // for how the timer is started and stopped. 
    protected void aTimerInterrupt() {
        //System.out.println("");
        System.out.println("PACKET LOST");

        //restart timer
        System.out.println("Restart timer");
        startTimer(0, 50);

        //resend packet
        System.out.println("Resend packet");
        toLayer3(0, savedPacket);
    }

    // This routine will be called once, before any of your other A-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity A).    
    protected void aInit() {
        packetSeq = 0;
    }

    // This routine will be called whenever a packet sent from the A-side 
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side.  "packet" is the (possibly corrupted) packet
    // sent from the A-side.   
    // Deals with packet sent from A
    protected void bInput(Packet packet) {
        //System.out.println("");
        //System.out.println("CONFIRM MESSAGE WAS RECIEVED");

        //extract the checksum, sequence number and payload from the packet
        int receivedChk = packet.getChecksum();
        int receivedSeq = packet.getSeqnum();
        int receivedAck = packet.getAcknum();
        String msg = packet.getPayload();

        // the packet should be re-computed using the checksum method
        int checksum = checksum(msg) + receivedSeq + receivedAck;

        int ack = 1;
        int nack = 0;

        // confirms the re-computed checksum matches the checksum sent in the packet        
        if (checksum == receivedChk) {
            //System.out.println("Packet is NOT corrupt");            

            // confirem the expected sequence number matches the packet sequence number
            if (expectedPacketSeq == receivedSeq) {

                System.out.println("Sucessful packet return ack");

                // send the payload up to the application layer 
                toLayer5(B, packet.getPayload());

                // packet is still susceptible to corruption even without a payload
                int chkAck = expectedPacketSeq + ack;

                // return positive response i.e ack
                Packet p = new Packet(expectedPacketSeq, ack, chkAck, null);
                // send the ack packet down to the transport layer
                toLayer3(B, p);

                // increment the expected packet sequence number
                expectedPacketSeq++;
            } // An acknowledgement packet has already been sent,
            // as a result 'expectedPacketSeq' has also already been incremented.        
            // Meaning 'B' will only accept the next expected packet,                
            // not a retransmission of the packet that has just been acknowledged.
            // So in the event where the acknowledgement packet is lost returning to 'A'
            // A retransmition of the already acknowledged packet is resent from 'A'
            // To handel the duplication of this packet the 'expectedPacketSeq' must be deprecated i.e previousSeq
            // This will then match the sequence number of the retransmitted packet
            else {
                // Packet is a duplicate (already recieved)
                System.out.println("Packet is a duplicate");
                // packet is still susceptible to corruption even without a payload
                int chkAck = receivedSeq + ack;
                // return positive response i.e ack
                // resend the packet using the recieved sequence number
                Packet p = new Packet(receivedSeq, ack, chkAck, null);
                // send the ack packet down to the transport layer
                toLayer3(B, p);
            }
        } else {
            // Packet must be corrupt
            System.out.println("Packet is corrupt, return nack");

            // packet is still susceptible to corruption even without a payload
            int chkAck = expectedPacketSeq + nack;

            // return negative response i.e ack
            Packet p = new Packet(expectedPacketSeq, nack, chkAck, null);

            // send the nack packet down to the transport layer
            toLayer3(B, p);
        }
    }

    // This routine will be called once, before any of your other B-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity B).
    protected void bInit() {
        expectedPacketSeq = 0;

    }

}
