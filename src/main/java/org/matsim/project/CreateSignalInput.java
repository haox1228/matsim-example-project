package org.matsim.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.codeexamples.fixedTimeSignals.CreateSignalInputExample;
import org.matsim.contrib.signals.SignalSystemsConfigGroup;
import org.matsim.contrib.signals.controller.fixedTime.DefaultPlanbasedSignalSystemController;
import org.matsim.contrib.signals.data.SignalsData;
import org.matsim.contrib.signals.data.SignalsScenarioWriter;
import org.matsim.contrib.signals.data.signalcontrol.v20.SignalControlData;
import org.matsim.contrib.signals.data.signalcontrol.v20.SignalGroupSettingsData;
import org.matsim.contrib.signals.data.signalcontrol.v20.SignalPlanData;
import org.matsim.contrib.signals.data.signalgroups.v20.SignalGroupsData;
import org.matsim.contrib.signals.data.signalsystems.v20.SignalData;
import org.matsim.contrib.signals.data.signalsystems.v20.SignalSystemControllerData;
import org.matsim.contrib.signals.data.signalsystems.v20.SignalSystemData;
import org.matsim.contrib.signals.data.signalsystems.v20.SignalSystemsData;
import org.matsim.contrib.signals.model.Signal;
import org.matsim.contrib.signals.model.SignalGroup;
import org.matsim.contrib.signals.model.SignalPlan;
import org.matsim.contrib.signals.model.SignalSystem;
import org.matsim.contrib.signals.utils.SignalUtils;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.groups.QSimConfigGroup.SnapshotStyle;
import org.matsim.core.scenario.ScenarioUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;

public class CreateSignalInput {
    //private static final Logger log = LogManager.getLogger(CreateSignalInputExample.class);
    //private static final String INPUT_DIR = "/Users/mahaoxuan/Downloads/westholly/testSignal2/";
    private static HashMap<String, ArrayList<String>> signalNodes;
    public static HashMap<String, ArrayList<String>> parseJsonToHashMap(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<String, ArrayList<String>> resultMap = new HashMap<>();

        try {
            // Parse the JSON file to a HashMap
            resultMap = objectMapper.readValue(new File(filePath), new TypeReference<HashMap<String, ArrayList<String>>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultMap;
    }
    /**
     * This method creates the locations of signals, i.e. it specifies signalized intersections.
     * Furthermore groups for the signals are created that specify which signals will always have the same control.
     *
     * @param systems the so far empty object for information about signalized intersections
     * @param groups the so far empty object for information about signals that are controlled together as groups
     */
    private void createSignalSystemsAndGroups(SignalSystemsData systems, SignalGroupsData groups){
        for(Map.Entry<String, ArrayList<String>> entry: signalNodes.entrySet()){
            String key = entry.getKey();
            ArrayList<String> values = entry.getValue();
            createSignalSystemsAndGroupsHelper(key, values, systems, groups);
        }
    }
    private void createSignalSystemsAndGroupsHelper(String key, ArrayList<String> values, SignalSystemsData systems, SignalGroupsData groups){
        SignalSystemData sys = systems.getFactory().createSignalSystemData(Id.create(key, SignalSystem.class));
        systems.addSignalSystemData(sys);
        int numLinks = 1;
        for(String str: values){
            SignalData signal = systems.getFactory().createSignalData(Id.create(Integer.toString(numLinks), Signal.class));
            sys.addSignalData(signal);
            signal.setLinkId(Id.create(str, Link.class));
            numLinks++;
        }
        SignalUtils.createAndAddSignalGroups4Signals(groups, sys);
    }
    /**
     * Create a fixed time traffic signal control for all signal groups in the scenario,
     * i.e. specify when their signals show green or red.
     *
     * Each signal system (signalized intersection) is equipped with a control,
     * namely each with the same. The control contains the following information.
     * - A cylce time of 120 seconds.
     * - An offset (for green waves) of 0 seconds.
     * - Each direction gets green for second 0 to 55 within the cycle.
     *
     * @param control the so far empty object for information about when to show green and red
     */
    private void createSignalControl(SignalControlData control) {
        int cycle = 120;
        List<Id<SignalSystem>> ids = new LinkedList<>();
        for(Map.Entry<String, ArrayList<String>> entry: signalNodes.entrySet()){
            ids.add(Id.create(entry.getKey(), SignalSystem.class));
        }
        for(Id<SignalSystem> id: ids){
            //System.out.println(id.toString());
            SignalSystemControllerData controller = control.getFactory().createSignalSystemControllerData(id);
            control.addSignalSystemControllerData(controller);
            controller.setControllerIdentifier(DefaultPlanbasedSignalSystemController.IDENTIFIER);

            SignalPlanData plan = control.getFactory().createSignalPlanData(Id.create("1", SignalPlan.class));
            controller.addSignalPlanData(plan);
            plan.setEndTime(23.0*3600);
            plan.setOffset(0);
            plan.setCycleTime(cycle);
            int timeTracker = 0;
            int numOfIntersection = signalNodes.get(id.toString()).size();
            int incremental = cycle/numOfIntersection;
            final int YELLOW = 5;
            for(int i = 1; i <= numOfIntersection; i++){
                SignalGroupSettingsData setting = control.getFactory().createSignalGroupSettingsData(Id.create(Integer.toString(i), SignalGroup.class));
                plan.addSignalGroupSettings(setting);
                setting.setOnset(timeTracker);
                timeTracker = timeTracker + incremental;
                timeTracker = timeTracker > 120 ? cycle : timeTracker;
                setting.setDropping(timeTracker);
                timeTracker += YELLOW;
            }
        }

    }

    /**
     * Set up the config and scenario, create signal information
     * and write them to file as input for further simulations.
     *
     * @throws IOException
     */
    public void run(String outputDir) throws IOException {
        // create an empty config
        Config config = ConfigUtils.createConfig();

        // set network and population files
        config.network().setInputFile("D:\\MATSim\\LA_large\\fareless transit\\dedicated_network.xml");
        //config.plans().setInputFile(INPUT_DIR + "population.xml.gz");

        // add the signal config group to the config file
        SignalSystemsConfigGroup signalSystemsConfigGroup =
                ConfigUtils.addOrGetModule(config, SignalSystemsConfigGroup.GROUP_NAME, SignalSystemsConfigGroup.class);

        /* the following makes the contrib load the signal input files, but not to do anything with them
         * (this switch will eventually go away) */
        signalSystemsConfigGroup.setUseSignalSystems(true);

        // specify some details for the visualization
        config.qsim().setNodeOffset(20.0);
        config.qsim().setSnapshotStyle(SnapshotStyle.queue);

        // --- create the scenario
        Scenario scenario = ScenarioUtils.loadScenario(config);
        /* create the information about signals data (i.e. create an empty SignalsData object)
         * and add it to the scenario as scenario element */
        SignalsData signalsData = SignalUtils.createSignalsData(signalSystemsConfigGroup);
        scenario.addScenarioElement(SignalsData.ELEMENT_NAME, signalsData);

        /* fill the SignalsData object with information:
         * signal systems - specify signalized intersections
         * signal groups - specify signals that always have the same signal control
         * signal control - specify cycle time, onset and dropping time, offset... for all signal groups
         */
        this.createSignalSystemsAndGroups(signalsData.getSignalSystemsData(), signalsData.getSignalGroupsData());
        this.createSignalControl(signalsData.getSignalControlData());

        // create the path to the output directory if it does not exist yet
        Files.createDirectories(Paths.get(outputDir));

        // set output filenames
        signalSystemsConfigGroup.setSignalSystemFile(outputDir + "signal_systems.xml");
        signalSystemsConfigGroup.setSignalGroupsFile(outputDir + "signal_groups.xml");
        signalSystemsConfigGroup.setSignalControlFile(outputDir + "signal_control.xml");

        //write config to file
        String configFile = outputDir  + "config.xml";
        ConfigWriter configWriter = new ConfigWriter(config);
        configWriter.write(configFile);

        // write signal information to file
        SignalsScenarioWriter signalsWriter = new SignalsScenarioWriter();
        signalsWriter.setSignalSystemsOutputFilename(signalSystemsConfigGroup.getSignalSystemFile());
        signalsWriter.setSignalGroupsOutputFilename(signalSystemsConfigGroup.getSignalGroupsFile());
        signalsWriter.setSignalControlOutputFilename(signalSystemsConfigGroup.getSignalControlFile());
        signalsWriter.writeSignalsData(scenario);

        //log.info("Config of simple traffic light scenario is written to " + configFile);
        //log.info("Visualize scenario by calling VisSimpleTrafficSignalScenario in the same package.");
    }

    public static void main(String[] args) throws IOException {
        String filePath = "D:\\MATSim\\LA_large\\SignalSim\\lametro\\input\\signal_intersections.json";
        signalNodes = parseJsonToHashMap(filePath);
        new CreateSignalInput().run("D:\\MATSim\\LA_large\\SignalSim\\lametro\\signalOutput\\");
    }
}
