package cppassignjava;

import java.util.ArrayList;

/**
 * This class is used to represent any fix data that is read in from the NMEA
 * formatted files.
 *
 * @author Dale Peters (dmp9@aber.ac.uk)
 */
public class FixData {

    /**
     * List of all the SNR values read in from the file.
     */
    private ArrayList<Integer> snrData;
    
    /**
     * The total number of sentences for a group of fix data.
     */
    private int numSentencesForFix;

    /**
     * Constructs an instance of FixData, populating an ArrayList with the SNR
     * values read in from the file.
     *
     * @param dataFromFile The data read in from the file.
     */
    public FixData(String dataFromFile[]) {
        this.numSentencesForFix = Integer.parseInt(dataFromFile[1]);
        snrData = new ArrayList<>();
        for (int i = 7; i < dataFromFile.length; i += 4) {
            // If a SNR value wasn't obtained for a particular satellite, add 0
            if (dataFromFile[i].equals("")) {
                snrData.add(0);
            } else {
                snrData.add(Integer.parseInt(dataFromFile[i]));
            }
        }
    }
    
    /**
     * Returns the total number of sentences for a group of fix data.
     *
     * @return the total number of sentences for a group of fix data.
     */
    public int getNumSentencesForFix(){
        return numSentencesForFix;
    }

    /**
     * Counts up the number of SNR readings with values greater than 35.
     *
     * @return The number of SNR readings with values greater than 35.
     */
    public int getNumGoodReadings() {
        int numGoodReadings = 0;
        // Loop through all the SNR readings for this sentence.
        for (Integer snrValue : snrData) {
            // If it is greater than 35, increment the counter.
            if (snrValue > 35) {
                numGoodReadings++;
            }
        }
        return numGoodReadings;
    }
}
