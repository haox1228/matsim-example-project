package org.matsim.project;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class LoadThetaAndCluster {

    private Map<String, Map<String, Double>> thetaValues;
    private Map<String, Integer> linkToClusterLabel;

    public LoadThetaAndCluster(String thetaValuesPath, String linkToClusterLabelPath) throws IOException {
        this.thetaValues = loadThetaValues(thetaValuesPath);
        this.linkToClusterLabel = loadLinkToClusterLabel(linkToClusterLabelPath);
    }


    private static Map<String, Map<String, Double>> loadThetaValues(String filePath) throws IOException {
        Map<String, Map<String, Double>> thetaValues = new HashMap<>();

        try (Reader in = new FileReader(filePath)) {
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : parser) {
                String clusterLabel = record.get("Cluster_Labels");
                Map<String, Double> timeBins = new HashMap<>();
                timeBins.put("6-9", Double.valueOf(record.get("6-9")));
                timeBins.put("9-15", Double.valueOf(record.get("9-15")));
                timeBins.put("15-19", Double.valueOf(record.get("15-19")));
                timeBins.put("19-21", Double.valueOf(record.get("19-21")));
                timeBins.put("21-6", Double.valueOf(record.get("21-6")));
                thetaValues.put(clusterLabel, timeBins);
            }
        }

        return thetaValues;
    }

    public Map<String, Map<String, Double>> getThetaValues() {
        return thetaValues;
    }

    public Map<String, Integer> getLinkToClusterLabel() {
        return linkToClusterLabel;
    }

    private static Map<String, Integer> loadLinkToClusterLabel(String filePath) throws IOException {
        Map<String, Integer> linkToClusterLabel = new HashMap<>();

        try (Reader in = new FileReader(filePath)) {
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : parser) {
                String linkId = record.get("link");
                Integer clusterLabel = Integer.valueOf(record.get("Cluster_Labels"));
                linkToClusterLabel.put(linkId, clusterLabel);
            }
        }

        return linkToClusterLabel;
    }
}
