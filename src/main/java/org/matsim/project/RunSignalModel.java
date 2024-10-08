package org.matsim.project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Set;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.signals.SignalSystemsConfigGroup;
import org.matsim.contrib.signals.builder.Signals;
import org.matsim.contrib.signals.controller.laemmerFix.LaemmerConfigGroup;
import org.matsim.contrib.signals.controller.laemmerFix.LaemmerConfigGroup.Regime;
import org.matsim.contrib.signals.data.SignalsData;
import org.matsim.contrib.signals.data.SignalsDataLoader;
import org.matsim.contrib.signals.otfvis.OTFVisWithSignalsLiveModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vis.otfvis.OTFVisConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.NetworkChangeEvent.ChangeType;
import org.matsim.core.network.NetworkChangeEvent.ChangeValue;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.count.CountsControlerListener;
import org.matsim.count.VolumesAnalyzer;
import org.matsim.counts.Counts;
import org.matsim.api.core.v01.network.Link;
import javax.inject.Inject;
public class RunSignalModel {

    private static class CountsInitializer {
        @Inject
        CountsInitializer(Counts<Link> counts, Scenario scenario) {
            Counts<Link> scenarioCounts = (Counts<Link>) scenario.getScenarioElement(Counts.ELEMENT_NAME);
            if (scenarioCounts == null) {
                scenario.addScenarioElement(Counts.ELEMENT_NAME, counts);
            } else {
                if (counts != scenarioCounts) {
                    throw new RuntimeException();
                }
            }
        }
    }

    public static void main(String[] args) {
        String configFileName = "D:\\MATSim\\LA_large\\SignalSim\\lametro\\signalOutput\\config.xml";
        String outputDir = "D:\\MATSim\\LA_large\\SignalSim\\lametro\\output\\";
        run(configFileName,outputDir,false);
    }

    public static void run(String configFileName, String outputDir, boolean visualizer) {
        double speedfactor[] = {0.913819888, 0.907529595, 0.905589126, 0.908653448, 0.920459311, 0.906343947, 0.738518096, 0.661577977, 0.626169987, 0.669341615, 0.698480649, 0.702863113, 0.679543179, 0.637576004, 0.569647807, 0.526635806, 0.52234041, 0.539521992, 0.626505016, 0.711358501, 0.799820253, 0.898303461, 0.912174085, 0.911181505};
        double speedfactor_wknd[] = {0.99940158281, 0.99708319426, 0.99554179196, 0.99238514130, 0.98963242224, 0.9906198106, 0.97447750699, 0.94228266235, 0.92388133387, 0.90811304100, 0.88140867405, 0.83981867959, 0.8160764777, 0.78060529898, 0.74882934637, 0.70524961476, 0.73241775503, 0.76926529329, 0.83458252921, 0.88820070912, 0.90447765659, 0.96620438939, 0.97916012147, 0.97580898523};
        double ExpressFactor[] = {0.443784647, 0.462896104, 0.488964576, 0.451651106, 1.627747433}; //2nd iter
        //double ExpressFactor []= {0.77203356,0.604663736,1.0166032,1.826581032,0.484916064};
        //double ExpressFactor []= {0.769495702,0.611477229,1.033875646,1.823944406,0.486589862};
        //double ExpressFactor []= {0.744481127,0.515295794,1.293328122,4.23754805,0.369681752};
        double ArterialFactor[] = {0.8, 0.8, 0.8, 0.8, 0.8};
        Config config = ConfigUtils.loadConfig(configFileName);
        config.controler().setOutputDirectory(outputDir);
        Scenario scenario = ScenarioUtils.loadScenario(config);

        SignalSystemsConfigGroup signalsConfigGroup = ConfigUtils.addOrGetModule(config,
                SignalSystemsConfigGroup.GROUP_NAME, SignalSystemsConfigGroup.class);
        signalsConfigGroup.setUseSignalSystems(true);
        if (signalsConfigGroup.isUseSignalSystems()) {
            scenario.addScenarioElement(SignalsData.ELEMENT_NAME, new SignalsDataLoader(config).loadSignalsData());
        }
        Controler controler = new Controler(scenario);
        Signals.configure(controler);

//        for (Link link : scenario.getNetwork().getLinks().values()) {
//            double speed = link.getFreespeed();
//            //final double threshold = 5./3.6;
//            double capacity = link.getCapacity();
//            Set<String> linkType = link.getAllowedModes();
//
//            if (linkType.contains("car")) {
//                if (speed > 33) {
//                    for (int day = 0; day < 7; day++) {
//                        for (int i = 0; i < 24; i++) {
//                            int timeindex = day * 24 + i;
//                            NetworkChangeEvent event = new NetworkChangeEvent(timeindex * 3600.);
//                            if (day % 7 == 5 || day % 7 == 6) {
//                                event.setFreespeedChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, speed * speedfactor_wknd[i]));
//                            } else {
//                                event.setFreespeedChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, speed * speedfactor[i]));
//                            }
//
//                            if (i < 6) {
//                                event.setFlowCapacityChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, capacity / 3600 * ExpressFactor[4]));
//                            } else if (i < 9 && i >= 6) {
//                                event.setFlowCapacityChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, capacity / 3600 * ExpressFactor[0]));
//                            } else if (i < 15 && i >= 9) {
//                                event.setFlowCapacityChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, capacity / 3600 * ExpressFactor[1]));
//                            } else if (i < 19 && i >= 15) {
//                                event.setFlowCapacityChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, capacity / 3600 * ExpressFactor[2]));
//                            } else if (i < 21 && i >= 19) {
//                                event.setFlowCapacityChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, capacity / 3600 * ExpressFactor[3]));
//                            } else if (i >= 21) {
//                                event.setFlowCapacityChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, capacity / 3600 * ExpressFactor[4]));
//                            }
//                            event.addLink(link);
//                            NetworkUtils.addNetworkChangeEvent(scenario.getNetwork(), event);
//                        }
//                    }
//                }
//            }
//        }
        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                // We add a class which reacts on people who enter a link and lets it rain on them
                // if we are within a certain time window.
                // The class registers itself as an EventHandler and also produces events by itself.

                bind(VolumesAnalyzer.class).asEagerSingleton();
                //bind(TaxiEngine.class).asEagerSingleton();
            }
        });

        controler.addOverridingModule(new AbstractModule() {
            public void install() {
                addControlerListenerBinding().to(CountsControlerListener.class);
                bind(CountsInitializer.class).asEagerSingleton();
            }
        });

        controler.run();
    }
}