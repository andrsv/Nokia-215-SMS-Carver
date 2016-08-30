# Nokia-215-SMS-Carver
Note: this program is intended used for legal usage only. Do not attempt to restore other peoples SMS's unless you have the permission to do so. :)

What does the program do?
This program is intended to be used to carve for deleted SMS's on a Nokia 215 (RM-1111). The Nokia 215 runs Series 30+ OS, developed by Mediatek. This OS is not comparable to the Series 30 and Series 40 which is developed by Nokia. I assume that other phones using the same OS also have the same artifacts, so my guess is that this program will work for any Series 30+ phone.

Why was did program created?
XRY(www.msab.com) supports physical extraction of this phone. XRY is able to read SMS's, but does not carve for deleted SMS's. The purpose of this program is to fill this missing feature, XRY or similiar program is required to get any use of this program. 

How to use the program?
This program requires:
- a binary dump of the phones internal memory. The binary dump file should be named "binary.dd". In my knowledge there is only one program which is capable of doing such a dump, which is XRY.
- a list of known SMS Service numbers in order to carve primarily for received SMS's. This list is saved to "ServiceNumbers.txt" (some Norwegian service numbers are already included). Note that the service numbers are encoded, read further to find out how to populate the list.
- a list of known phone numbers in order to carve for sent SMS's. This list is saved to "PhoneNumbers.txt".Note that the service numbers are encoded, read further to find out how to populate the list.
- the encryption key. Based on testing on 2 phones, this key is the same for both phones. The encrypted key is attached in the filer "encryptionkey.txt".

populate all the above file, the result will be written to System.out.

How to get the input data?
The method is generic, but as XRY is the only program which I am aware of which supports the specific phone, my explanation will be given using XRY.

- binary.dd -
Do a physical extraction of the phone in XRY. Open the "*.xry" file in XACT(www.msab.com). Right click "Data", choose "Export-->Data..." and choose the filename "binary.dd", put the "binary.dd" file in the same folder you find this "README.md" file.

- ServiceNumbers.txt -
Open the "*.xry" file in XACT(www.msab.com). Open "Volumes-->Volume-->NVRAM-->NVD_DATA-->MPA3_001". Here you find all SMS's stored on the phone. Right click a SMS, choose "View-->Service Center-->Linked Data", copy the Hexadecimal data to a new line in "ServiceNumbers.txt" file. Do the same for all SMS's. Alternatively the program could be rewritten to read service numbers as clear text, then encode it before searching. List of Servicenumbers may be found by searching for "SMSC list" on Google. 

- PhoneNumbers.txt -
Open the "*.xry" file in XACT(www.msab.com). Open "Volumes-->Volume-->NVRAM-->NVD_DATA-->MPA3_001". Here you find all SMS's stored on the phone. press the (+) next to an outgoing SMS. Right click the phone number and choose "View-->Tel-->Linked Data", copy the Hexadecimal data to a new line in "PhoneNumbers.txt" file. Do the same for all outgoing SMS's.

- encryptionkey.txt -
This file contaions a XOR-key, the on allready provided probably works, so you don't have to do anything. I am unsure where the encryptionkey actually is located, but here are some hints on how to find it:
Each SMS is stored as 186 bytes in the "Volumes-->Volume-->NVRAM-->NVD_DATA-->MPA3_001" file. This file seems to have fixed size, and when SMS's are deleted from the phone, the 186 bytes are filled either with 0xFF's or 0x00's which is encoded with XOR. So, you might find the encryption key by locating 2 or more equal SMS's(186 bytes). Then this SMS will actually be the XOR-key, or you will find the XOR-key by XORing it with 0xFF

What the program does and what it does not do?
This program was made to read SMS's from 2 specific phones, both was Nokia 215 (RM-1111) phones. The program has not been tested on other phones. The program succesfully decodes all SMS's from the 2 phones. The SMS's are stored partly in PDU format. I did only implement the part of the PDU specifications which I needed for my phones. This means that implementation is not complete. Feel free to complete it, or let me know if you need anything fixed :)

Be aware that the data which you get from XACT-->Data does not have correct order for the pages(sectors), so parts of the SMS's might be wrong. It's not impossible to fix, but I didn't try to fix it, as I did not need to for my cases.

Let me know if you use my program, or need any help using it :)
