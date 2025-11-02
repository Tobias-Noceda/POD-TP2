package ar.edu.itba.pod.server.lib;

public class ParametersParser {

    public static ParsedParameters parseConsoleParameters() {
        String address = System.getProperty("addresses", "");

        return new ParsedParameters(address);
    }
}
