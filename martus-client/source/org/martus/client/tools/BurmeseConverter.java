package org.martus.client.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.martus.common.utilities.BurmeseUtilities;

/**
 * @author roms
 *         Date: 3/1/13
 */
public class BurmeseConverter {

    public BurmeseConverter(String convertTo, String inFile, String outFile) {
        this.convertTo = convertTo;
        this.inFile = inFile;
        this.outFile = outFile;
    }

    public final static void main( String[] args )
    {
        if( args.length < 3 )
        {
            usage("Not enough arguments.");
        }

        String convertTo = args[0];
        String inFile = args[1];
        String outFile = args[2];

        if (!(convertTo.equalsIgnoreCase("t")) && !(convertTo.equalsIgnoreCase("z")))
        {
            usage("unrecognized desired font");
        }

        final BurmeseConverter converter = new BurmeseConverter(convertTo, inFile, outFile);
        converter.convert();
    }

    public void convert() {
        boolean toTharlon = convertTo.equalsIgnoreCase("t");
        try {
            FileInputStream fstream = new FileInputStream(inFile);

            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(outFile), "UTF-8"));

            String strLine;
            while ((strLine = br.readLine()) != null)   {
              String convertedLine = (toTharlon) ? BurmeseUtilities.getStorable(strLine) : BurmeseUtilities.getDisplayable(strLine);
              writer.write(convertedLine);
              writer.newLine();
            }

            in.close();
            writer.close();

        } catch (Exception e) {

        }
    }

    /**
     * Prints command line usage.
     *
     * @param msg A message to include with usage info.
     */
    private static void usage( String msg )
    {
        System.err.println( msg );
        System.err.println( "Usage: java BurmeseConverter  <first initial of desired font (either z or t)> <file to convert> <converted file>" );
    }

    private String convertTo;
    private String inFile;
    private String outFile;
}
