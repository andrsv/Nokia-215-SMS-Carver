package main;

import main.PDU.SMS;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static byte[] XORKey;

    public static void main(String[] args) throws IOException {

//        System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("output.txt"))));

        //Read XOR-key
        String XORKeyFilename = "encryptionkey.bin";
        RandomAccessFile XorKeyFile = new RandomAccessFile(XORKeyFilename, "r");
//        System.out.println("Reading file " + filename + " (size: " + inputFile.length() + " bytes)....");
        XORKey = new byte[(int) XorKeyFile.length()];
        XorKeyFile.readFully(XORKey);


        //found SMS's
        ArrayList<String> SMSs = new ArrayList<String>();

        //ServiceNumbers reference file
        RandomAccessFile ServiceNumbersFile = new RandomAccessFile("ServiceNumbers.txt", "r");
        RandomAccessFile phoneNumbersFile = new RandomAccessFile("PhoneNumbers.txt", "r");

        //Read complete dd file and put it in memory (stupid way to do this, but I don't care as my file is small anyway)
        String filename = "13610704_2016_1215_7_data_fra_XACT.dd";
        RandomAccessFile inputFile = new RandomAccessFile(filename, "r");
        System.out.println("Reading file " + filename + " (size: " + inputFile.length() + " bytes)....");
        //TODO: Obs, dette blir ikke bra dersom filstørrelsen er større enn maksstørrelsen på en int...
        byte[] fileContent = new byte[(int) inputFile.length()];
        inputFile.readFully(fileContent);
        if (fileContent.length != inputFile.length()) {
            System.out.println("ALERT!!!! Can not read such big files. File must be splitted or program rewritten");
        }

        //carve for SMSs
        carveServiceNumber(SMSs, ServiceNumbersFile, fileContent);
        carvePhoneNumber(SMSs, phoneNumbersFile, fileContent);
        System.out.println("Total: Found " + SMSs.size() + " potential SMS's.");
    }

    private static void carveServiceNumber(ArrayList<String> SMSs, RandomAccessFile serviceNumbersFile, byte[] fileContent) throws IOException {
        while (true) {
            String hexServiceNumber = serviceNumbersFile.readLine();
            if (hexServiceNumber == null) {
                break;
            }
            byte[] hexServiceNumberAsBytes = DatatypeConverter.parseHexBinary(hexServiceNumber);

            int index = 0;
            while (index != -1) {
                index = KMPMatch.indexOf(fileContent, hexServiceNumberAsBytes, index + 1);
                if (index != -1) {
                    byte[] SMSBytes = Arrays.copyOfRange(fileContent, index - SMS.SERVICENUMBER_DEFAULT_START_INDEX, index - SMS.SERVICENUMBER_DEFAULT_START_INDEX + SMS.SMS_SIZE);
                    byte[] SMSBytesAfterXOR;

                    SMSBytesAfterXOR = new byte[SMSBytes.length];

                    for (int i = 0; i < SMSBytes.length; i++) {
                        SMSBytesAfterXOR[i] = (byte) (SMSBytes[i] ^ XORKey[i % XORKey.length]);
                    }

                    SMS sms = new SMS(SMSBytesAfterXOR);
                    System.out.println("Position in sector " + index % 512 + ".");
                    if (!SMSs.contains(sms.getCompleteSMS())) {
                        //       System.out.println("Index: " + (index / 186 + 1) + " ... rest: " + index % 186);
                        System.out.println(sms);
                        SMSs.add(sms.getCompleteSMS());
                    }
                }
            }
        }
    }

    private static void carvePhoneNumber(ArrayList<String> SMSs, RandomAccessFile phoneNumbersFile, byte[] fileContent) throws IOException {
        while (true) {
            String hexPhoneNumber = phoneNumbersFile.readLine();
            if (hexPhoneNumber == null) {
                break;
            }
            byte[] hexPhoneNumberAsBytes = DatatypeConverter.parseHexBinary(hexPhoneNumber);

            int index = 0;
            while (index != -1) {
                index = KMPMatch.indexOf(fileContent, hexPhoneNumberAsBytes, index + 1);
                if (index != -1) {
                    byte[] SMSBytes = Arrays.copyOfRange(fileContent, index - 4, index - 4 + SMS.SMS_SIZE);
                    byte[] SMSBytesAfterXOR;

                    SMSBytesAfterXOR = new byte[SMSBytes.length];

                    for (int i = 0; i < SMSBytes.length; i++) {
                        SMSBytesAfterXOR[i] = (byte) (SMSBytes[i] ^ XORKey[i % XORKey.length]);
                    }
                    SMS sms = new SMS(SMSBytesAfterXOR);
                    // System.out.println("Position in sector " + index % 512 + ".");
                    if (!SMSs.contains(sms.getCompleteSMS())) {
                        //    System.out.println("Index: " + (index / 186 + 1) + " ... rest: " + index % 186);
                        System.out.println(sms);
                        SMSs.add(sms.getCompleteSMS());
                    }
                }
            }
        }
    }


}
