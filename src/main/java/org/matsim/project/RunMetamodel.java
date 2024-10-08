package org.matsim.project;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import org.matsim.contrib.signals.SignalSystemsConfigGroup;
import org.matsim.contrib.signals.builder.Signals;
import org.matsim.contrib.signals.data.SignalsData;
import org.matsim.contrib.signals.data.SignalsDataLoader;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
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
//import org.matsim.score.GondolaScoringFunctionFactory;
//import org.matsim.score.ScoreEngine;

//import ch.sbb.matsim.mobsim.qsim.SBBTransitModule;
//import ch.sbb.matsim.mobsim.qsim.pt.SBBTransitEngineQSimModule;


public class RunMetamodel {

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




    public void run_MATSim(int k, String path) throws Exception, IOException {
        LoadThetaAndCluster csvLoader = new LoadThetaAndCluster("optimized_thetha.csv", "link_vds_cluster.csv");
        Map<String, Map<String, Double>> thetaValues = csvLoader.getThetaValues();
        Map<String, Integer> linkToClusterLabel = csvLoader.getLinkToClusterLabel();

//		double speedfactor [][] = new double [5][24];
        //double speedfactor [] = {0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8};
//		double speedfactor [] = {0.913819888,0.907529595,0.905589126,0.908653448,0.920459311,0.906343947,0.738518096,0.650561535,0.630794439,0.678957403,0.724268665,0.750880124,0.753074063,0.734076102,0.685970047,0.638554922,0.613693656,0.595692115,0.626505016,0.711358501,0.799820253,0.898303461,0.912174085,0.911181505};
        double speedfactor[] = {0.913819888, 0.907529595, 0.905589126, 0.908653448, 0.920459311, 0.906343947, 0.738518096, 0.661577977, 0.626169987, 0.669341615, 0.698480649, 0.702863113, 0.679543179, 0.637576004, 0.569647807, 0.526635806, 0.52234041, 0.539521992, 0.626505016, 0.711358501, 0.799820253, 0.898303461, 0.912174085, 0.911181505};
        double speedfactor_wknd[] = {0.99940158281, 0.99708319426, 0.99554179196, 0.99238514130, 0.98963242224, 0.9906198106, 0.97447750699, 0.94228266235, 0.92388133387, 0.90811304100, 0.88140867405, 0.83981867959, 0.8160764777, 0.78060529898, 0.74882934637, 0.70524961476, 0.73241775503, 0.76926529329, 0.83458252921, 0.88820070912, 0.90447765659, 0.96620438939, 0.97916012147, 0.97580898523};

        // iter 0
        //double ExpressFactor []= {0.443784647,0.462896104,0.488964576,0.451651106,1.627747433};
        //iter 1 new pop
        //double ExpressFactor[] = {0.4008603331208553, 0.527246508591815, 0.6109091054823261, 0.4995465167057919, 1.3233720896262235};
        //iter 2 new pop
        //double ExpressFactor[] = {0.38076294058053417, 0.5423825304059426, 0.6195808784638728, 0.46733479240263476, 1.054237684053824};
        //iter 3 new pop new count station
        //double ExpressFactor[] = {0.374583243579669, 0.5562160466691131, 0.6249485289076462, 0.44175971854589136, 0.8594029338086698};
        //iter 4
        double ExpressFactor[] = {0.442565, 0.595464, 0.734858, 0.422234, 0.734913};
        double ArterialFactor[] = {0.8, 0.8, 0.8, 0.8, 0.8};


        Config config = ConfigUtils.loadConfig(path);
        config.network().setTimeVariantNetwork(true);
        config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
        //config.controler().setOutputDirectory("C:\\MATSim\\calibration\\" + k);

        Scenario scenario = ScenarioUtils.loadScenario(config);
        SignalSystemsConfigGroup signalsConfigGroup = ConfigUtils.addOrGetModule(config,
                SignalSystemsConfigGroup.GROUP_NAME, SignalSystemsConfigGroup.class);
        signalsConfigGroup.setUseSignalSystems(true);
        if (signalsConfigGroup.isUseSignalSystems()) {
            scenario.addScenarioElement(SignalsData.ELEMENT_NAME, new SignalsDataLoader(config).loadSignalsData());
        }
        //Network network = scenario.getNetwork();
        Controler controler = new Controler(scenario);
//				controler.addOverridingModule(new SwissRailRaptorModule());
        Signals.configure(controler);
        for (Link link : scenario.getNetwork().getLinks().values()) {
            double speed = link.getFreespeed();
            //final double threshold = 5./3.6;
            double capacity = link.getCapacity();
            Set<String> linkType = link.getAllowedModes();

            if (linkType.contains("car")) {
                if (speed > 33) {
                    String linkId = link.getId().toString();
                    if (linkToClusterLabel.containsKey(linkId)) {
                        int clusterLabel = linkToClusterLabel.get(linkId);
                        for (int day = 0; day < k; day++) {
                            for (int i = 0; i < 24; i++) {
                                int timeIndex = day * 24 + i;
                                NetworkChangeEvent event = new NetworkChangeEvent(timeIndex * 3600.);
                                if (day % 7 == 5 || day % 7 == 6) {
                                    event.setFreespeedChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, speed * speedfactor_wknd[i]));
                                } else {
                                    event.setFreespeedChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, speed * speedfactor[i]));
                                }
                                String timeBin = getTimeBin(i);

                                String key = clusterLabel + "_" + timeBin;
                                System.out.println(key);
                                if (thetaValues.containsKey((key))) {
                                    double expressFactor = thetaValues.get(key).get(timeBin);
                                    event.setFlowCapacityChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, capacity / 3600 * expressFactor));
                                }

                                event.addLink(link);
                                NetworkUtils.addNetworkChangeEvent(scenario.getNetwork(), event);
                            }
                        }
                    } else {
                        for (int day = 0; day < k; day++) {
                            for (int i = 0; i < 24; i++) {
                                int timeindex = day * 24 + i;
                                NetworkChangeEvent event = new NetworkChangeEvent(timeindex * 3600.);
                                if (day % 7 == 5 || day % 7 == 6) {
                                    event.setFreespeedChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, speed * speedfactor_wknd[i]));
                                } else {
                                    event.setFreespeedChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, speed * speedfactor[i]));
                                }

                                if (i < 6) {
                                    event.setFlowCapacityChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, capacity / 3600 * ExpressFactor[4]));
                                } else if (i < 9 && i >= 6) {
                                    event.setFlowCapacityChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, capacity / 3600 * ExpressFactor[0]));
                                } else if (i < 15 && i >= 9) {
                                    event.setFlowCapacityChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, capacity / 3600 * ExpressFactor[1]));
                                } else if (i < 19 && i >= 15) {
                                    event.setFlowCapacityChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, capacity / 3600 * ExpressFactor[2]));
                                } else if (i < 21 && i >= 19) {
                                    event.setFlowCapacityChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, capacity / 3600 * ExpressFactor[3]));
                                } else if (i >= 21) {
                                    event.setFlowCapacityChange(new ChangeValue(ChangeType.ABSOLUTE_IN_SI_UNITS, capacity / 3600 * ExpressFactor[4]));
                                }
                                event.addLink(link);
                                NetworkUtils.addNetworkChangeEvent(scenario.getNetwork(), event);
                            }
                        }
                    }


                }
            }


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

            // gondola pricing
				/*
				controler.addOverridingModule(new AbstractModule() {
					@Override
					public void install() {
						// We add a class which reacts on people who enter a link and lets it rain on them
						// if we are within a certain time window.
						// The class registers itself as an EventHandler and also produces events by itself.

						bind(ScoreEngine.class).asEagerSingleton();
						//bind(TaxiEngine.class).asEagerSingleton();
					}
				});

				controler.addOverridingModule( new AbstractModule(){
					@Override public void install() {

						this.bindScoringFunctionFactory().toInstance(new GondolaScoringFunctionFactory(scenario) ) ;
						//this.bindScoringFunctionFactory().toInstance(new TaxiScoringFunctionFactory(scenario) ) ;
					}

				});
				*/
            controler.run();
        }
    }

    private String getTimeBin(int hour) {
        if (hour < 6) {
            return "21-6";
        } else if (hour < 9) {
            return "6-9";
        } else if (hour < 15) {
            return "9-15";
        } else if (hour < 19) {
            return "15-19";
        } else if (hour < 21) {
            return "19-21";
        } else {
            return "21-6";
        }
    }



//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//

    //	}hg
    public static void main(String[] args) {
        RunFullModel r = new RunFullModel();
        try {
            r.run_MATSim(1,  "D:\\MATSim\\LA_large\\SignalSim\\lametro\\calibration\\new_pop\\config.xml"); // 2 should be 1st iteration
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
