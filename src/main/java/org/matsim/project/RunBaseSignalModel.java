package org.matsim.project;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.signals.builder.Signals;
import org.matsim.contrib.signals.data.SignalsData;
import org.matsim.contrib.signals.data.SignalsDataLoader;
import org.matsim.contrib.signals.otfvis.OTFVisWithSignalsLiveModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

/**
 * Minimal example how to start your matsim run with traffic signals
 *
 * @author tthunig
 */
public class RunBaseSignalModel {
    // do not change name of class; matsim book refers to it.  theresa, apr'18

    /**
     * @param args the path to your config file
     */
    public static void main(String[] args) {
        Config config;
        if (args.length==0 || args.length>1) {
            // --- create a config for the example scenario
            config = ConfigUtils.loadConfig("D:\\MATSim\\LA_large\\SignalSim\\signal_test_config.xml");
            config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
            config.controler().setLastIteration(0);
            config.controler().setOutputDirectory("D:\\MATSim\\LA_large\\SignalSim\\output_holly\\");
        } else {
            // --- create a config from the arguments you provided
            config = ConfigUtils.loadConfig(args[0]);
        }

        run(config, true); // The run method is extracted so that a test can operate on it.
    }

    public static void run(Config config, boolean visualize) {
        // adjustments for live visualization
        OTFVisConfigGroup otfvisConfig = ConfigUtils.addOrGetModule(config, OTFVisConfigGroup.class);
        otfvisConfig.setDrawTime(true);
        config.qsim().setSnapshotStyle(QSimConfigGroup.SnapshotStyle.withHoles);

        // --- create the scenario
        Scenario scenario = ScenarioUtils.loadScenario(config);
        // load the information about signals data (i.e. fill the SignalsData object) and add it to the scenario as scenario element
        scenario.addScenarioElement(SignalsData.ELEMENT_NAME, new SignalsDataLoader(config).loadSignalsData());

        // --- create the controler
        Controler c = new Controler(scenario);
        // add the signals module to the simulation such that SignalsData is not only contained in the scenario but also used in the simulation
//		c.addOverridingModule(new SignalsModule());
        Signals.configure( c ) ;

        // add live visualization module
        if (visualize) {
            c.addOverridingModule(new OTFVisWithSignalsLiveModule());
        }

        // --- run the simulation
        c.run();
    }

}