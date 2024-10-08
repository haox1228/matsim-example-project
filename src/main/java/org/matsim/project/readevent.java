package org.matsim.project;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
public class readevent {
    public static void main(String[] args) {
        String filePath = "D:\\MATSim\\LA_large\\sim2_network\\10_iter_200k_pop\\output\\events.xml"; // Replace with your XML file path

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineCount = 0;

            while ((line = br.readLine()) != null && lineCount < 200) {
                System.out.println(line);
                lineCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
