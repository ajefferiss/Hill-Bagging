package uk.co.openmoments.lib.updater;

public class HillsDBUpdater {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Missing input CSV file");
            System.exit(1);
        }
        new HillDB().update(args[0]);
    }
}
