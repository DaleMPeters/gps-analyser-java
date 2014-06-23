package cppassignjava;

/**
 * This class defines the data to be held about the position of the GPS
 * receiver. This data is read in from GPRMC sentences in the files.
 *
 * @author Dale Peters (dmp9@aber.ac.uk)
 */
public final class Location {

    /**
     * The degrees value for the latitude of the GPS receiver.
     */
    private int latitudeDegrees;
    /**
     * The minutes value for the latitude of the GPS receiver.
     */
    private double latitudeMinutes;
    /**
     * The degrees value for the longitude of the GPS receiver.
     */
    private int longitudeDegrees;
    /**
     * The longitude of the location in minutes.
     */
    private double longitudeMinutes;
    /**
     * The latitude of the location in decimal form.
     */
    private double latitudeDecimal;
    /**
     * The longitude of the location in decimal form.
     */
    private double longitudeDecimal;

    /**
     * Constructs a new instance of a Location object by taking in a tokenized
     * sentence from a file.
     *
     * @param dataFromFile The tokenized NMEA sentence from a file, in the form
     * of an array of Strings.
     */
    public Location(String dataFromFile[]) {
        latitudeDegrees = Integer.parseInt(dataFromFile[3].substring(0, 2));
        longitudeDegrees = Integer.parseInt(dataFromFile[5].substring(0, 3));
        latitudeMinutes = Double.parseDouble(dataFromFile[3].substring(2, 9));
        longitudeMinutes = Double.parseDouble(dataFromFile[5].substring(3, 10));

        // Converts the lat and long from the file into decimal form
        latitudeDecimal = latitudeDegrees + latitudeMinutes / 60;
        longitudeDecimal = -1 * (longitudeDegrees + longitudeMinutes / 60);
    }

    /**
     * Converts the latitude of the location in to decimal form from degrees and
     * minutes form and returns it.
     *
     * @return The latitude of the current location object.
     */
    public double getLatitudeDecimal() {
        return latitudeDecimal;
    }

    /**
     * Converts the latitude of the location in to decimal form from degrees and
     * minutes form and returns it.
     *
     * @return The longitude of the current location object.
     */
    public double getLongitudeDecimal() {
        return longitudeDecimal;
    }

    /**
     * Sets the decimal latitude of the location.
     *
     * @param latDecimal The latitude to set.
     */
    public void setLatitudeDecimal(double latDecimal) {
        this.latitudeDecimal = latDecimal;
    }

    /**
     * Sets the decimal longitude of the location.
     *
     * @param longDecimal The longitude to set.
     */
    public void setLongitudeDecimal(double longDecimal) {
        this.longitudeDecimal = longDecimal;
    }
}
