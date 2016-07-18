package main;

import java.util.Arrays;

public class ServiceNumber {
    private byte serviceNumberTypeOfAddress = 0;
    private String serviceNumber;

    public ServiceNumber(byte[] ServiceNumber) {
        if (ServiceNumber.length > 0) {
            for (int i = 0; i < ServiceNumber.length; i++) {
                ServiceNumber[i] = (byte) ((ServiceNumber[i] & 0x0F) << 4 | (ServiceNumber[i] & 0xF0) >> 4);
            }
            serviceNumberTypeOfAddress = ServiceNumber[0];
            if (serviceNumberTypeOfAddress == (byte) (0x19)) {
                serviceNumber = "+" + Utility.bytesToHex(Arrays.copyOfRange(ServiceNumber, 1, ServiceNumber.length));
            } else {
                serviceNumber = "Encoding for this service number type is not implemented: 0x" + Utility.bytesToHex(Arrays.copyOfRange(ServiceNumber, 0, ServiceNumber.length));
            }
        } else {
            serviceNumber = "no service number";
        }
    }

    @Override
    public String toString() {
        return serviceNumber;
    }
}
