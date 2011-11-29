import java.io.Serializable;
import java.util.ArrayList;

public class CompressedMessage implements Serializable
{   // this instance variable will store the original, compressed and decompressed message
	private String message;

	public CompressedMessage(String message)
	{	// initialise with original message
		this.message = message;
	}

	public String getMessage()
	{	// return (compressed or decompressed) message
		return message;
	}

    private boolean punctuationChar(String str)
    {   // check if the last character in the string is a punctuation
       	return(str.charAt(str.length()-1) == ',' || str.charAt(str.length()-1) == '.');
    }

	private String getWord(String str)
   	{   // if last character in string is punctuation then remove 
   		if(punctuationChar(str))
      		str = str.substring(0, str.length()-1);
       	return str;
   	}

	public void compress()
	{	// array list to temporarily store previous words encountered in text
		ArrayList<String> dictionary = new ArrayList<String>();
		String compressedStr = "";

		// split message string into words, using space character as delimiter
		String[] words = message.split(" \\s*");
		for(int i = 0; i < words.length; i++)
		{	int foundPos = dictionary.indexOf(getWord(words[i]));
			if(foundPos == -1)
  			{   // word is not found therefore add to end of array list
   				dictionary.add(getWord(words[i]));
         		// add word to compressed message
    			compressedStr += getWord(words[i]);
        	}
       		else
   				/* match found in array list - add corresponding position
    			   of word to compressed message */
     			compressedStr += foundPos;

    		if(punctuationChar(words[i]))
     			compressedStr += words[i].charAt(words[i].length()-1);
           	compressedStr += " ";
		}

    		// store compressed message in instance variable
  		message = compressedStr;
  	}

	public void decompress()
	{	// array list to temporarily store previous words encountered in text
		ArrayList<String> dictionary = new ArrayList<String>();
		String decompressedStr = "";
		int position;

		// split message string into words, using space character as delimiter
		String[] words = message.split(" \\s*");
		for(int i = 0; i < words.length; i++)
		{	// test if the first character of this string is a digit
			if (words[i].charAt(0) >= '0' && words[i].charAt(0) <= '9')
			{	/* it is a digit - this indicates that this string represents
				   the position of a word in the previous words list.
				   convert this string to an int value */
				position = Integer.parseInt(getWord(words[i]));
				// get word at this position & add to decompressed message
				decompressedStr += dictionary.get(position);
			}
			else
			{	// this string is a word - add to previous words list
				dictionary.add(getWord(words[i]));
				// add word to compressed message
				decompressedStr += getWord(words[i]);
			}

			if(punctuationChar(words[i]))
         		decompressedStr += words[i].charAt(words[i].length()-1);
         	decompressedStr += " ";
		}

		// store decompressed message in instance variable
   		message = decompressedStr;
	}
}

