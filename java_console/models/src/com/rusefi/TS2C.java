package com.rusefi;

import java.io.*;
import java.util.Arrays;
import java.util.Date;

/**
 * tuner studio project to C data structure converter command line utility
 *
 * 12/27/2014
 * Andrey Belomutskiy, (c) 2012-2016
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class TS2C {
    // todo: replace with loadCount & rpmCount
    private final int size;

    private int rpmCount;
    private int loadCount;

    private float loadBins[];
    private float rpmBins[];
    private float table[][];

    public static void main(String[] args) throws IOException {
        new TS2C(args);
    }

    private TS2C(String[] args) throws IOException {
        if (args.length != 4 && args.length != 5) {
            System.out.println("Four parameters expected: ");
            System.out.println("  INPUT_MSQ_FILE NAME LOAD_SECTION_NAME RPM_SECTION_NAME TABLE_NAME");
            System.out.println("for example");
            // section names are needed in order to generate comments about cell content
            System.out.println("  currenttune.msq veLoadBins veRpmBins veTable");
            System.exit(-1);
        }
        String fileName = args[0];
        String loadSectionName = args[1];
        String rpmSectionName = args[2];
        String tableName = args[3];
        size = Integer.parseInt(args.length > 4 ? args[4] : "16");

        if (!loadSectionName.equalsIgnoreCase("none")) {
            BufferedReader r = readAndScroll(fileName, loadSectionName);

            loadCount = size;
            loadBins = new float[loadCount];
            readAxle(loadBins, r);
        }
        if (!rpmSectionName.equalsIgnoreCase("none")) {
            BufferedReader r = readAndScroll(fileName, rpmSectionName);
            rpmCount = size;
            rpmBins = new float[rpmCount];
            readAxle(rpmBins, r);
        }

        table = new float[size][];
        for (int i = 0; i < size; i++) {
            table[i] = new float[size];
        }

        BufferedReader r = readAndScroll(fileName, tableName);
        readTable(table, r);

        BufferedWriter w = new BufferedWriter(new FileWriter("output.c"));
        writeTable(w, new ValueSource() {
            @Override
            public float getValue(int loadIndex, int rpmIndex) {
                return table[loadIndex][rpmIndex];
            }
        }, "TS2C");


        w.write("\r\n\r\n/* rpm bins */\r\n\r\n");
        w.write(toString(rpmBins));

        w.write("\r\n\r\n/* load bins */\r\n\r\n");
        w.write(toString(loadBins));

        w.close();

    }

    private String toString(float[] a) {
        StringBuilder b = new StringBuilder();
        int iMax = a.length - 1;
        b.append('{');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append('}').toString();
            b.append(", ");
        }
    }

    private void writeTable(BufferedWriter w, ValueSource valueSource, String toolName) throws IOException {
        w.write("/* Generated by " + toolName + " on " + new Date() + "*/\r\n");
        for (int loadIndex = 0; loadIndex < loadCount; loadIndex++)
            writeLine(valueSource, w, loadIndex);
    }

    /**
     * @param fileName text file to open
     * @param magicStringKey magic string content to scroll to
     * @return Reader after the magicStringKey line
     */
    private static BufferedReader readAndScroll(String fileName, String magicStringKey) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        System.out.println("Reading from " + fileName + ", scrolling to " + magicStringKey);
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(magicStringKey)) {
                System.out.println("Found " + line);
                break;
            }
        }
        return reader;
    }

    private void writeLine(ValueSource valueSource, BufferedWriter w, int loadIndex) throws IOException {
        StringBuilder sb = new StringBuilder("{");

        sb.append("/* " + loadIndex + " " + String.format("%3.3f", loadBins[loadIndex]) + "\t*/");
        for (int rpmIndex = 0; rpmIndex < rpmCount; rpmIndex++) {
            sb.append("/* " + rpmIndex + " " + rpmBins[rpmIndex] + "*/" + String.format("%3.3f", valueSource.getValue(loadIndex, rpmIndex)) + ",\t");
        }
        sb.append("},\r\n");

        w.write(sb.toString());
    }

    interface ValueSource {
        float getValue(int loadIndex, int rpmIndex);
    }

    private void readTable(float[][] table, BufferedReader r) throws IOException {
        int index = 0;

        while (index < size) {
            String line = r.readLine();
            if (line == null)
                throw new IOException("End of file?");
            line = line.trim();
            if (line.isEmpty())
                continue;

            String[] values = line.split("\\s");
            if (values.length != size)
                throw new IllegalStateException("Expected " + size + " but got " + Arrays.toString(values) + ". Unexpected line: " + line);

            for (int i = 0; i < size; i++) {
                String str = values[i];
                try {
                    table[index][i] = Float.parseFloat(str);
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("While reading " + str, e);
                }
            }
            System.out.println("Got line " + index + ": " + Arrays.toString(table[index]));
            index++;
        }
    }

    private void readAxle(float[] bins, BufferedReader r) throws IOException {
        int index = 0;

        while (index < size) {
            String line = r.readLine();
            if (line == null)
                throw new IOException("End of file?");
            line = line.trim();
            if (line.isEmpty())
                continue;
            bins[index++] = Float.parseFloat(line);
        }

        System.out.println("Got bins " + Arrays.toString(bins));
    }
}
