/**
 * 
 */
package HMMPOSTagger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author mirrorlol
 *
 */
public class GroupWord {
	public static void main (String[] args) {
		if (args.length != 2) {
			System.err.println ("GroupWord requires 2 arguments:  CharFileName WordFileName");
			System.exit(1);
		}
		String source = args[0];
		String output = args[1];
		try {
			BufferedReader dataReader = new BufferedReader (new FileReader(source));
			PrintWriter outputWriter = new PrintWriter (new FileWriter (output));
			String inputLine = null;
			String lastTag = null;
			StringBuilder builder = new StringBuilder();
			while ((inputLine = dataReader.readLine()) != null) {
				if (inputLine.isEmpty()) {
					if (builder.length() != 0) {
						outputWriter.println(builder.toString() + "\t" + lastTag);
						builder = new StringBuilder();
					}
					outputWriter.println();
				}
				else {
					String[] charInfo = inputLine.split("\\s+");
					if ((charInfo[1].equals("B") || charInfo[1].equals("S"))) {
						if (builder.length() != 0) {
							outputWriter.println(builder.toString() + "\t" + lastTag);
							builder = new StringBuilder();
						}
						builder.append(charInfo[0]);
					}
//					else if (charInfo[1].equals("E")) {
//						builder.append(charInfo[0]);
//						outputWriter.println(builder.toString() + "\t" + lastTag);
//						builder = new StringBuilder();
//					}
					else {
						builder.append(charInfo[0]);
					}
					lastTag = charInfo[2];
				}
			}
			dataReader.close();
			outputWriter.flush();
			outputWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
