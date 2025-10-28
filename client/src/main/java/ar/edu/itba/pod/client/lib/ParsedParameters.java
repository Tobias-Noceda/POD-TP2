package ar.edu.itba.pod.client.lib;

public class ParsedParameters {

    public final String address;
    public final String inPath;
    public final String outPath;
    public final String borough;

    public ParsedParameters(String address, String inPath, String outPath, String borough) {
        this.address = address;
        this.inPath = inPath;
        this.outPath = outPath;
        this.borough = borough;
    }

}
