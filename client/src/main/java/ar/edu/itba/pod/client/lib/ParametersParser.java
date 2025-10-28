package ar.edu.itba.pod.client.lib;

public class ParametersParser {

    public static ParsedParameters parseConsoleParameters() {
        String address = System.getProperty("addresses", "");
        String inPath = System.getProperty("inPath", "");
        String outPath = System.getProperty("outPath", "");
        String borough = System.getProperty("borough", "");

        return new ParsedParameters(address, inPath, outPath, borough);
    }
}
