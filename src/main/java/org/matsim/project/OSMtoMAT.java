package org.matsim.project;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.signals.SignalSystemsConfigGroup;
import org.matsim.contrib.signals.data.SignalsData;
import org.matsim.contrib.signals.data.SignalsDataLoader;
import org.matsim.contrib.signals.data.SignalsScenarioWriter;
import org.matsim.contrib.signals.data.consistency.LanesAndSignalsCleaner;
import org.matsim.contrib.signals.network.SignalsAndLanesOsmNetworkReader;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.algorithms.NetworkSimplifier;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.lanes.Lanes;
import org.matsim.lanes.LanesWriter;
public class OSMtoMAT {
    /**
     * @param args first argument input OSM file, second argument output directory
     */
    public static void main(String[] args) {

        String inputOSM = "D:\\MATSim\\multimodal-network\\JOSM\\planet_-118.517_33.673_da424cbe.osm";
        String outputDir = "D:\\MATSim\\LA_large\\SignalSim\\lametro\\";
        if (args != null && args.length > 1) {
            inputOSM = args[0];
            outputDir = args[1];
        }

        // ** adapt this according to your scenario **
        CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84,
                "EPSG:6423");

        // create a config
        Config config = ConfigUtils.createConfig();
        SignalSystemsConfigGroup signalSystemsConfigGroup = ConfigUtils.addOrGetModule(config,
                SignalSystemsConfigGroup.GROUP_NAME, SignalSystemsConfigGroup.class);
        signalSystemsConfigGroup.setUseSignalSystems(true);
        config.qsim().setUseLanes(true);

        // create a scenario
        Scenario scenario = ScenarioUtils.createScenario(config);
        scenario.addScenarioElement(SignalsData.ELEMENT_NAME, new SignalsDataLoader(config).loadSignalsData());
        // pick network, lanes and signals data from the scenario
        SignalsData signalsData = (SignalsData) scenario.getScenarioElement(SignalsData.ELEMENT_NAME);
        Lanes lanes = scenario.getLanes();
        Network network = scenario.getNetwork();

        // create and configure the signals and lanes osm reader
        SignalsAndLanesOsmNetworkReader reader = new SignalsAndLanesOsmNetworkReader(network, ct, signalsData, lanes);
        reader.setMergeOnewaySignalSystems(false);
        reader.setAllowUTurnAtLeftLaneOnly(true);
        reader.setMakePedestrianSignals(false);

        // set bounding box for signals and lanes (south, west, north, east)
        // ** adapt this according to your scenario **
        //<bounds minlat="33.724" minlon="-118.4799999" maxlat="34.198" maxlon="-117.8609999"/>
        //reader.setBoundingBox(33.73, -118.46, 34.19, -117.87); // this is la metro
        //palms: 34.022, -118.4259999, 34.031, -118.42
        // create network, lanes and signal data
        reader.parse(inputOSM);

        // Simplify the network except the junctions with signals as this might mess up already created plans
        NetworkSimplifier netSimplify = new NetworkSimplifier();
        netSimplify.setNodesNotToMerge(reader.getNodesNotToMerge());
        netSimplify.run(network);

        /*
         * Clean the Network. Cleaning means removing disconnected components, so that
         * afterwards there is a route from every link to every other link. This may not
         * be the case in the initial network converted from OpenStreetMap.
         */
        new NetworkCleaner().run(network);
        new LanesAndSignalsCleaner().run(scenario);

        // write the files out
        new NetworkWriter(network).write(outputDir + "network.xml");
        new LanesWriter(lanes).write(outputDir + "lanes.xml");
        SignalsScenarioWriter signalsWriter = new SignalsScenarioWriter();
        signalsWriter.setSignalSystemsOutputFilename(outputDir + "signalSystems.xml");
        signalsWriter.setSignalGroupsOutputFilename(outputDir + "signalGroups.xml");
        signalsWriter.setSignalControlOutputFilename(outputDir + "signalControl.xml");
        signalsWriter.writeSignalsData(scenario);
    }
}
