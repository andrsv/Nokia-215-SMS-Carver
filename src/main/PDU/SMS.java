package main.PDU;

import main.ServiceNumber;
import main.Utility;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * TODO: This code only handles PDU's with type: SMS-DELIVER, SMS-STATUS-REPORT, SMS-SUBMIT. Not tested for other messages.
 */


public class SMS {
    public static final int SERVICENUMBER_DEFAULT_START_INDEX = 1;
    public static final int SMS_SIZE = 186;

    private String completeSMS;
    byte byte0, dataCodingScheme;
    FirstOctet firstOctet;
    private byte servicenumberLength = 0;
    int phonenumberLength = 0;
    ServiceNumber serviceNumber;
    private byte messageReference;
    String phoneNumber = "";
    String timeStamp = "";
    private int textLength = 168;
    private byte[] SMSBytes;
    private byte[] SMSBytesAfterMirrored;
    String smsText = "";
    String warningText = "";
    byte protocolIdentifier;

    public SMS(byte[] SMSbytes) {
        this.SMSBytes = SMSbytes;
        this.completeSMS = Utility.bytesToHex(SMSbytes);
        SMSBytesAfterMirrored = new byte[SMSBytes.length];

        for (int i = 0; i < SMSBytes.length; i++) {
            SMSBytesAfterMirrored[i] = (byte) ((SMSBytes[i] & 0x01) << 7 | (SMSBytes[i] & 0x02) << 5 | (SMSBytes[i] & 0x04) << 3 | (SMSBytes[i] & 0x08) << 1 | (SMSBytes[i] & 0x10) >> 1 | (SMSBytes[i] & 0x20) >> 3 | (SMSBytes[i] & 0x40) >> 5 | (SMSBytes[i] & 0x80) >> 7);
        }
        byte0 = SMSBytes[0];
        byte phonenumberOctetLength;

        byte[] TextBytes;

        int pduIndex = 1; //skipping byte 0, as it is not part of PDU.

        //Reading Service number
        servicenumberLength = SMSBytes[pduIndex++];
        serviceNumber = new ServiceNumber(Arrays.copyOfRange(SMSBytes, pduIndex, pduIndex + servicenumberLength));
        pduIndex += servicenumberLength;

        //Reading firstOctet
        firstOctet = new FirstOctet(SMSBytes[pduIndex++], servicenumberLength != 0); //TODO: don't know how to find out if sms is incomming or outgoing. Using the hack where Service number is normally not set for outgoing, but is normaly set for incomming on Nokia 215 RM-1111)

        //Reading Failure Cause
        if (firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_SUBMIT_REPORT || firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_DELIVER_REPORT) {
            pduIndex++; //TODO: not implemented, Read Failure Cause
        }

        //Reading message Reference
        if (firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_SUBMIT || firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_STATUS_REPORT ||
                firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_COMMAND) {
            messageReference = SMSBytes[pduIndex++];
        }

        //Reading phone number
        if (hasPhoneNumber()) { // TODO: A small hack here, phone number is orginiating for incomming messages, destination for outgoing messages, recipient for SMS-STATUS-REPORT
            //Reading phone number length
            phonenumberLength = SMSBytes[pduIndex++];
            phonenumberOctetLength = (byte) ((phonenumberLength + 1) / 2);

            //Reading phone number
            if (phonenumberLength > 0) {
                byte[] PhoneNumber;
                byte phoneNumberTypeOfAddress = SMSBytes[pduIndex++];
                PhoneNumber = (Arrays.copyOfRange(SMSBytes, pduIndex, pduIndex + phonenumberOctetLength));

                for (int i = 0; i < PhoneNumber.length; i++) {
                    PhoneNumber[i] = (byte) ((PhoneNumber[i] & 0x0F) << 4 | (PhoneNumber[i] & 0xF0) >> 4);
                }
                if (phoneNumberTypeOfAddress == (byte) (0x91)) {
                    phoneNumber = "+" + Utility.bytesToHex(Arrays.copyOfRange(PhoneNumber, 0, phonenumberOctetLength));
                    if (phonenumberLength % 2 == 1) {
                        phoneNumber = phoneNumber.substring(0, phonenumberLength);
                    }
                } else {
                    phoneNumber = "Encoding for this phone number type is not implemented: 0x" + Utility.bytesToHex(phoneNumberTypeOfAddress) + ", phone nr: 0x" + Utility.bytesToHex(Arrays.copyOfRange(PhoneNumber, 0, phonenumberOctetLength + 1));
                }
            } else {
                phoneNumber = "No phone number";
            }
            pduIndex += phonenumberOctetLength;
        }

        //Reading timestamp for SMS_STATUS_REPORT
        if (firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_STATUS_REPORT) {
            byte[] TimeStamp = (Arrays.copyOfRange(SMSBytes, pduIndex, pduIndex + 7));
            for (int i = 0; i < TimeStamp.length; i++) {
                TimeStamp[i] = (byte) ((TimeStamp[i] & 0x0F) << 4 | (TimeStamp[i] & 0xF0) >> 4);
            }
            timeStamp = Utility.bytesToHex(TimeStamp[2]) + "." + Utility.bytesToHex(TimeStamp[1]) + ".20" + Utility.bytesToHex(TimeStamp[0]) + " " + Utility.bytesToHex(TimeStamp[3]) + ":" + Utility.bytesToHex(TimeStamp[4]) + ":" + Utility.bytesToHex(TimeStamp[5]) + ", Timezone: 0x" + Utility.bytesToHex(TimeStamp[6]);
            pduIndex += 7;
            pduIndex += 8;
        }

        //Reading Parameter Indicator
        if (firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_DELIVER_REPORT || firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_SUBMIT_REPORT || firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_STATUS_REPORT) {
            pduIndex++; //TODO: not implemented
        }

        //Reading Service Center time stamp
        if (firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_SUBMIT_REPORT) {
            pduIndex++; //TODO: Not implemented
        }
        //Reading protocol identifier
        protocolIdentifier = SMSBytes[pduIndex++];

        //Reading Data Coding Scheme
        if (firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_DELIVER || firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_DELIVER_REPORT || firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_SUBMIT || firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_SUBMIT_REPORT || firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_STATUS_REPORT) {
            dataCodingScheme = SMSBytes[pduIndex++];
        }


        //Reading timestamp
        if (firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_DELIVER) {
            byte[] TimeStamp = (Arrays.copyOfRange(SMSBytes, pduIndex, pduIndex + 7));
            for (int i = 0; i < TimeStamp.length; i++) {
                TimeStamp[i] = (byte) ((TimeStamp[i] & 0x0F) << 4 | (TimeStamp[i] & 0xF0) >> 4);
            }
            timeStamp = Utility.bytesToHex(TimeStamp[2]) + "." + Utility.bytesToHex(TimeStamp[1]) + ".20" + Utility.bytesToHex(TimeStamp[0]) + " " + Utility.bytesToHex(TimeStamp[3]) + ":" + Utility.bytesToHex(TimeStamp[4]) + ":" + Utility.bytesToHex(TimeStamp[5]) + ", Timezone: 0x" + Utility.bytesToHex(TimeStamp[6]);
            pduIndex += 7;
        }

        //Reading Validity Period
        if (firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_SUBMIT) {
            //TODO: Not completely implemented
            if (firstOctet.TP_VPF == 0 || firstOctet.TP_VPF == 2) {
                pduIndex++;
            } else {
                pduIndex += 7;
            }
        }

        //Reading SMS text
        textLength = SMSBytes[pduIndex++] & 0xFF;
        if (dataCodingScheme == 0x00) {
            byte UDH_Length = 0;
            //Read User DAta Header
            if (firstOctet.TP_UDHI) {
                UDH_Length = (byte) (((SMSbytes[pduIndex] + 1) * 8 + 6) / 7); // +1 because of the first byte describing the length, *8 because of octets of 8 bytes, +6 to get Roof(x/7) instead of floor(x/7), /7 because 7bit ASCII/GSM table
            }

            TextBytes = (Arrays.copyOfRange(SMSBytesAfterMirrored, pduIndex, pduIndex + textLength));
            for (int i = UDH_Length; i < textLength; i++) {
                int index = (7 * i) / 8; //index of first byte where the 7 bits is
                int data;
                data = (TextBytes[index] & 0xff) << 8 | (TextBytes[index + 1] & 0xff); //the 2 bytes which contains the relevant 7 bits
                int offset = (7 * i) % 8; // which position in the 2 bytes (16 bits) the 7 bits start at.
                int revertedAsciiCode = (data >> 9 - offset) & 0x7F; // extract the relevant 7 bits.
                int asciiCode = (byte) ((revertedAsciiCode & 0x01) << 6 | (revertedAsciiCode & 0x02) << 4 | (revertedAsciiCode & 0x04) << 2 | (revertedAsciiCode & 0x08) | (revertedAsciiCode & 0x10) >> 2 | (revertedAsciiCode & 0x20) >> 4 | (revertedAsciiCode & 0x40) >> 6);

                //reading GSM 7-bit alphabet (code not verified, might be errors, but is good enough for my usage)
                if (asciiCode == 0x0F) {
                    smsText += 'å';
                } else if (asciiCode == 0x0E) {
                    smsText += 'Å';
                } else if (asciiCode == 0x1D) {
                    smsText += 'æ';
                } else if (asciiCode == 0x1C) {
                    smsText += 'Æ';
                } else if (asciiCode == 0x0C) {
                    smsText += 'ø';
                } else if (asciiCode == 0x0B) {
                    smsText += 'Ø';
                } else {
                    smsText += (char) (asciiCode);
                }
            }
        } else if (dataCodingScheme == 0x08) {
            try {
                //Read User DAta Header
                if (firstOctet.TP_UDHI) {
                    //TODO: Not implemented
                }
                smsText = new String(Arrays.copyOfRange(SMSBytes, pduIndex, pduIndex + textLength), "UTF16");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        //Reading Command specific data
        if (firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_COMMAND) {
            //TODO: Not yet implented
        }

    }

    public SMS(String s) {
        this(Utility.hexStringToByteArray(s));
    }


    private boolean hasPhoneNumber() {
        return firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_DELIVER || firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_SUBMIT || firstOctet.getTP_MTI() == FirstOctet.MessageType.SMS_STATUS_REPORT;
    }

    @Override
    public String toString() {
        String returnText = "";
        if (!warningText.equals("")) {
            returnText += "WARNING: " + warningText + "\n";
        }
        if (hasServiceNumber()) {
            returnText += "Service Number(" + servicenumberLength + "): " + serviceNumber + "\n";
        }
        returnText += "First octet: " + firstOctet + "\n";
        if (hasPhoneNumber()) {
            returnText += "Phone Number(" + phonenumberLength + "): " + phoneNumber + "\n";
        }
        returnText += "Protocol identifier:" + Utility.bytesToHex(protocolIdentifier) + "\n";
        returnText += "Data Encoding Scheme:" + Utility.bytesToHex(dataCodingScheme) + "\n";

        returnText += "Timestamp: " + timeStamp + "\n";


        returnText += "Textmessage(" + textLength + "): " + smsText + "\n";
        returnText += "Alle data(Original): " + Utility.bytesToHex(SMSBytes) + "\n";
        returnText += "Alle data(XOR):      " + Utility.bytesToHex(SMSBytes) + "\n";
        returnText += "Alle data(Mirror):   " + Utility.bytesToHex(SMSBytesAfterMirrored) + "\n";
        return returnText;
    }


    private boolean hasServiceNumber() {
        return servicenumberLength > 0;
    }

    public String getCompleteSMS() {
        return completeSMS;
    }


    // Get binary value of byte
    private String bytePrinter(byte b) {
        return ("00000000" + Integer.toBinaryString(b & 0xff)).substring(Integer.toBinaryString(b & 0xff).length());
    }

    public String getText() {
        return smsText;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public FirstOctet getFirstOctet() {
        return firstOctet;
    }
}
