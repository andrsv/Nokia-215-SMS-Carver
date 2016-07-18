package main.PDU;

import main.Utility;

/**
 * Created by Anders on 08.07.16.
 */
public class FirstOctet {

    public MessageType getTP_MTI() {
        return TP_MTI;
    }

    public boolean getTP_UDHI() {
        return TP_UDHI;
    }

    enum MessageType {
        SMS_DELIVER, SMS_SUBMIT_REPORT, SMS_STATUS_REPORT, SMS_DELIVER_REPORT, SMS_SUBMIT, SMS_COMMAND
    }

    /**
     * Message Type Indicator
     * Depending if the SMS is incomming/outgoing
     * Incomming
     * 0 0 : This PDU is a PDU-DELIVER
     * 0 1 : This PDU is a SMS-SUBMIT-REPORT
     * 1 0 : This PDU is a SMS-STATUS-REPORT
     * 1 1 : Reserved
     * <p/>
     * Outgoing
     * 0 0 : SMS-DELIVER-REPORT
     * 0 1 : SMS-SUBMIT
     * 1 0 : SMS-COMMAND
     * 1 1 : Reserved
     */
    MessageType TP_MTI;

    /**
     * More messages to send. This bit is set to 0 if there are more messages to send
     */
    boolean TP_MMS;

    /**
     * Reject duplicates. Parameter indicating whether or not the SC shall accept an PDU-SUBMIT for an SM still held in the SC which has the same TP-MR and the same TP-DA as a previously submitted SM from the same OA.
     */
    boolean TP_RD;

    /**
     * Loop Prevention
     */
    boolean TP_LP;

    /**
     * Validity Period Format.<br>
     * see PDU.FirstOctet.ValidityPeriodFormat to explain
     */
    int TP_VPF;

    /**
     * Status report indication. This bit is set to 1 if a status report is going to be returned to the SME
     */
    boolean TP_SRI;

    /**
     * Status report request. This bit is set to 1 if a status report is requested
     */
    boolean TP_SRR;

    /**
     * Status Report Qualifier
     */
    boolean TP_SRQ;

    /**
     * User data header indicator. This bit is set to 1 if the User Data field starts with a header
     */
    boolean TP_UDHI;

    /**
     * Reply path. Parameter indicating that reply path exists.
     */
    boolean TP_RP;


    public FirstOctet(byte firstOctet, boolean incomming) {

        if (incomming) {
            TP_MTI = MessageType.values()[Utility.intFromIntegerSubset(firstOctet, 0, 1)];
        } else {
            TP_MTI = MessageType.values()[Utility.intFromIntegerSubset(firstOctet, 0, 1) + 3];
        }
        if (TP_MTI == MessageType.SMS_DELIVER) {
            TP_MMS = Utility.bitAt(firstOctet, 2);
            TP_LP = Utility.bitAt(firstOctet, 3);
            TP_SRI = Utility.bitAt(firstOctet, 5);
            TP_RP = Utility.bitAt(firstOctet, 7);
        } else if (TP_MTI == MessageType.SMS_SUBMIT) {
            TP_RD = Utility.bitAt(firstOctet, 2);
            TP_VPF = Utility.intFromIntegerSubset(firstOctet, 3, 4);
            TP_SRR = Utility.bitAt(firstOctet, 5);
            TP_RP = Utility.bitAt(firstOctet, 7);
        } else if (TP_MTI == MessageType.SMS_STATUS_REPORT) {
            TP_MMS = Utility.bitAt(firstOctet, 2);
            TP_LP = Utility.bitAt(firstOctet, 3);
            TP_SRQ = Utility.bitAt(firstOctet, 5);
        } else if (TP_MTI == MessageType.SMS_COMMAND) {
            TP_SRR = Utility.bitAt(firstOctet, 5);
        }
        TP_UDHI = Utility.bitAt(firstOctet, 6);


    }

    @Override
    public String toString() {
        return "FirstOctet{" +
                "TP_MTI=" + TP_MTI +
                ", TP_MMS=" + TP_MMS +
                ", TP_RD=" + TP_RD +
                ", TP_LP=" + TP_LP +
                ", TP_VPF=" + TP_VPF +
                ", TP_SRI=" + TP_SRI +
                ", TP_SRR=" + TP_SRR +
                ", TP_SRQ=" + TP_SRQ +
                ", TP_UDHI=" + TP_UDHI +
                ", TP_RP=" + TP_RP +
                '}';
    }
}
