import java.io.Serializable;

public class EncryptedMessage implements Serializable
{   // this instance variable will store the original, encrypted and decrypted message
	private String message;

	// this variable stores the key
	static String KEY = "cable";

    public EncryptedMessage(String message)
    {	// initialise original message
    	this.message = message;
    }

    public String getMessage()
    {	// return (encrypted or decrypted) message
    	return message;
    }

    public void encrypt()
    {	/* this variable stores the letters of the alphabet (in order) as a
	   string - this string will be used to encrypt text */
	String charSet = "abcdefghijklmnopqrstuvwxyz";
        String cipherText = "";

    	for(int i = 0; i < message.length(); i++)
    	{	// find position of plaintext character in the character set
    		int plainTxtCh = charSet.indexOf(message.charAt(i));

                // get position of next key character
    		int keyPos = i % KEY.length();
                // find position of key character in the character set
                int keyCh = charSet.indexOf(KEY.charAt(keyPos));

    		/* add key character to plaintext character - this shifts the
    		   plaintext character - then divide by length of
			   character reference set and get remainder to wrap around */
    		int cipherTxtCh = (plainTxtCh + keyCh) % 26;
    		/* get character at corresponding position in character reference
			   set and add to cipherText */
			char c = charSet.charAt(cipherTxtCh);
			cipherText += c;
    	}
    	message = cipherText;
    }

    public void decrypt()
    {	/* this variable stores the letters of the alphabet (in order) as a
	   string - this string will be used to decrypt text */
	String charSet = "abcdefghijklmnopqrstuvwxyz";
        String plainText = "";

    	for(int i = 0; i < message.length(); i++)
    	{	// find position of ciphertext character
    		int cipherTxtCh = charSet.indexOf(message.charAt(i));

                // get position of next key character
    		int keyPos = i % KEY.length();
                // find position of key character in the character set
                int keyCh = charSet.indexOf(KEY.charAt(keyPos));

    		/* subtract original shift from character reference set length to
			   get new shift, add shift to ciphertext character then
			   divide by character reference set length and get remainder to
			   wrap around */
    		int plainTxtCh = (cipherTxtCh + (charSet.length() - keyCh)) % 26;

    		/* get character at corresponding position in character reference
			   set and add to plainText */
			char c = charSet.charAt(plainTxtCh);
			plainText += c;
    	}
    	message = plainText;
    }
}