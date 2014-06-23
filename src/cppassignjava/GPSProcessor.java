package cppassignjava;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.io.IOException;
import java.io.FileInputStream;


/**
 * This class handles all the processing of the GPS data read in from the NMEA
 * formatted files.
 *
 * @author Dale Peters (dmp9@aber.ac.uk)
 */
public class GPSProcessor {

    /**
     * The prefix for a GSV sentence.
     */
    public static final String GSV = "$GPGSV";
    /**
     * The prefix for a RMC sentence.
     */
    public static final String RMC = "$GPRMC";

    /**
     * Writes the processed GPS data out to a GPX file.
     *
     * @param outputFileName The name of the file to write the GPX data to.
     * @param outputData The data to write out the the GPX file.
     */
    public void writeGPXFile(String outputFileName, ArrayList<Location> outputData) {
        try {
            try (PrintWriter gpxFileWriter = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(outputFileName)))) {
                gpxFileWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
                gpxFileWriter.println("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:"
                        + "gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" "
                        + "xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" "
                        + "creator=\"Oregon 400t\" version=\"1.1\" "
                        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                        + "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1"
                        + " http://www.topografix.com/GPX/1/1/gpx.xsd "
                        + "http://www.garmin.com/xmlschemas/GpxExtensions/v3 "
                        + "http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd "
                        + "http://www.garmin.com/xmlschemas/TrackPointExtension/v1 "
                        + "http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\">");
                gpxFileWriter.println("\t<metadata>");
                gpxFileWriter.println("\t\t<link href=\"http://www.garmin.com\">");
                gpxFileWriter.println("\t\t\t<text>Garmin International</text>");
                gpxFileWriter.println("\t\t</link>");
                gpxFileWriter.println("\t</metadata>");
                gpxFileWriter.println("\t<trk>");
                gpxFileWriter.println("\t\t<name>CS22510 Java GPX Output File</name>");
                gpxFileWriter.println("\t\t<trkseg>");
                // For each location, output the latitude and longitude in an
                // XML attribute.
                for (Location rmcSentence : outputData) {
                    double rmcLatitudeDecimal = rmcSentence.getLatitudeDecimal();
                    double rmcLongitudeDecimal = rmcSentence.getLongitudeDecimal();
                    gpxFileWriter.println("\t\t\t<trkpt lat=\"" + rmcLatitudeDecimal
                            + "\" lon=\"" + rmcLongitudeDecimal + "\">");
                    gpxFileWriter.println("\t\t\t</trkpt>");
                }
                gpxFileWriter.println("\t\t</trkseg>");
                gpxFileWriter.println("\t</trk>");
                gpxFileWriter.println("</gpx>");
            }

        } catch (FileNotFoundException fnf) {
            System.out.println("There was an error printing to the file");
            System.exit(1);
        }

    }

    /**
     * Reads in two plain text files containing NMEA sentences and processes the
     * data.
     *
     * @param fileName1 The name of the first file containing the NMEA data.
     * @param fileName2 The name of the second file containing the NMEA data.
     *
     * @return An ArrayList containing the processed data.
     */
    public ArrayList<Location> processNMEAFiles(String fileName1, String fileName2) {
        ArrayList<Location> outputLocations = new ArrayList<>(); // Arraylist to hold reliable locations.
        try {
            // Assume that locations received before fix information are of a good fix.
            boolean receiver1GoodFix = true, receiver2GoodFix = true;
            // Initial offsets of 0.
            double latOffset = 0, longOffset = 0;
            // Instantiate file reader for the file with the first parameter.
            Scanner file1Reader = new Scanner(new InputStreamReader(new FileInputStream(fileName1)));
            // Instantiate second file reader.
            Scanner file2Reader = new Scanner(new InputStreamReader(new FileInputStream(fileName2)));
            while (file1Reader.hasNextLine()) { // Until all lines read.
                Location location1 = null, location2 = null;
                String nextLine = file1Reader.nextLine();
                if (nextLine.startsWith(GSV)) {
                    receiver1GoodFix = isGoodFix(processFixData(file1Reader, nextLine));
                } else if (nextLine.startsWith(RMC)) {
                    // Instantiate new location from file 1
                    location1 = new Location(splitAndStripSentence(nextLine));
                    String file2NextLine; // Line from 2nd file
                    // Read lines until the sentence is of type RMC or GSV
                    do {
                        file2NextLine = file2Reader.nextLine();
                    } while (!file2NextLine.startsWith(RMC) && !file2NextLine.startsWith(GSV));
                    if (file2NextLine.startsWith(GSV)) {
                        receiver2GoodFix = isGoodFix(processFixData(file2Reader, file2NextLine));
                        // Read lines until the sentence is of RMC type (contains location information)
                        do {
                            file2NextLine = file2Reader.nextLine();
                        } while (!file2NextLine.startsWith(RMC));
                        location2 = new Location(splitAndStripSentence(file2NextLine));
                    } else if (file2NextLine.startsWith(RMC)) {
                        location2 = new Location(splitAndStripSentence(file2NextLine));
                    }
                }
                //If receiver 1 and receiver 2 have a good fix
                if (receiver1GoodFix && receiver2GoodFix && location1 != null && location2 != null) {
                    // Calculate the latitude and longitude offsets
                    latOffset = location1.getLatitudeDecimal() - location2.getLatitudeDecimal();
                    longOffset = location1.getLongitudeDecimal() - location2.getLongitudeDecimal();
                    outputLocations.add(location1);
                // If receiver one has a good fix, assume 2nd receiver is of a good fix
                } else if (!receiver1GoodFix && location2 != null) {
                    // Add offset to the location retrieved from receiver 1
                    Location fixedLocation = calculateFixedPosition(location2, latOffset, longOffset);
                   outputLocations.add(fixedLocation);
                }
            }
        } catch (IOException e) { // In case the file wasn't found.
            System.err.println("There was an error loading the file. The program will exit.");
            System.exit(1);
        }
        // Returns the reliable locations
        return outputLocations;
    }

    /**
     * Takes each line of GSV sentences from a GSV group of data and adds them
     * to an ArrayList and returns it.
     *
     * @param fileReader the Scanner to use to read the file.
     * @param lineFromFile the previous line read in from the file.
     * @return an ArrayList containing the GSV data processed from the file.
     */
    public ArrayList<FixData> processFixData(Scanner fileReader, String lineFromFile) {
        ArrayList<FixData> processedData = new ArrayList<>(); // Stores fix data
        processedData.add(new FixData(splitAndStripSentence(lineFromFile))); // Add the first one
        for (int i = 0; i < processedData.get(0).getNumSentencesForFix() - 1; i++) {
            String nextLine = fileReader.nextLine(); // Read until the end of the fix group, depending on number of sentences
            processedData.add(new FixData(splitAndStripSentence(nextLine)));
        }
        return processedData;
    }

    /**
     * Strips the checksum off the end of the NMEA sentence and tokenizes the
     * remaining String in to an array of String objects.
     *
     * @param sentence The NMEA sentence to strip the checksum off and tokenize.
     * @return An array of Strings that were the result of tokenizing the NMEA
     * sentence.
     */
    public String[] splitAndStripSentence(String sentence) {
        String[] splitAndStrippedSentence = (sentence
                .replaceAll("\\*..", "")) // Remove anything after the * in the sentence
                .split(","); // Split the sentence, using "," as the delimeter.
        return splitAndStrippedSentence;
    }

    /**
     * Determines whether a set of GSV sentences read in from the file contains
     * enough SNR values to determine the data as a "good fix".
     *
     * @param gsvData The SNR values read in from the file.
     * @return true if there are three or more good SNR values.
     */
    public boolean isGoodFix(ArrayList<FixData> gsvData) {
        int numGoodReadings = 0; // Variable keeps track of number of good SNR values
        for (FixData g : gsvData) {
            numGoodReadings += g.getNumGoodReadings(); // tot up number of good readings
        }
        return numGoodReadings >= 3; // Return true if number of good SNR reainds >= 3
    }

    /**
     * Calculates a new location by adding the parameterised offset on to the
     * unprocessed location.
     *
     * @param unfixedLocation An object containing the data before processing.
     * @param latOffset The offset to add on to the unprocessed latitude.
     * @param longOffset The offset to add on to the unprocessed longitude.
     * @return New, processed location data, encapsulated in an object.
     */
    public Location calculateFixedPosition(Location unfixedLocation,
            double latOffset, double longOffset) {
        Location fixedRMC = unfixedLocation;
        fixedRMC.setLatitudeDecimal(unfixedLocation.getLatitudeDecimal() + latOffset);
        fixedRMC.setLongitudeDecimal(unfixedLocation.getLongitudeDecimal() + longOffset);
        return fixedRMC;
    }

    /**
     * Main method for running the program. Makes a call to the processNMEAFiles
     * method.
     *
     * @param args the command line arguments. Not used in this program.
     */
    public static void main(String[] args) {
        GPSProcessor gp = new GPSProcessor();
        ArrayList<Location> outputData = gp.processNMEAFiles("gps_1.dat", "gps_2.dat");
        gp.writeGPXFile("output.gpx", outputData);
    }
}
