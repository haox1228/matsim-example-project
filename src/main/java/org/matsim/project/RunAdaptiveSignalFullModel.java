package org.matsim.project;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;
import org.matsim.contrib.signals.controller.laemmerFix.LaemmerConfigGroup;
import org.matsim.contrib.signals.controller.laemmerFix.LaemmerConfigGroup.Regime;
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


public class RunAdaptiveSignalFullModel {

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




    public void run_MATSim(int k, String path) throws Exception, IOException{

//		double ExpressFactor []= new double [5];
//		double ArterialFactor []= new double [5];
//		double ExpressFactor1 []= new double [5];
//		double ArterialFactor1 []= new double [5];
////

//		double speedfactor [][] = new double [5][24];
        //double speedfactor [] = {0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8};
//		double speedfactor [] = {0.913819888,0.907529595,0.905589126,0.908653448,0.920459311,0.906343947,0.738518096,0.650561535,0.630794439,0.678957403,0.724268665,0.750880124,0.753074063,0.734076102,0.685970047,0.638554922,0.613693656,0.595692115,0.626505016,0.711358501,0.799820253,0.898303461,0.912174085,0.911181505};
        double speedfactor [] = {0.913819888,0.907529595,0.905589126,0.908653448,0.920459311,0.906343947,0.738518096,0.661577977,0.626169987,0.669341615,0.698480649,0.702863113,0.679543179,0.637576004,0.569647807,0.526635806,0.52234041,0.539521992,0.626505016,0.711358501,0.799820253,0.898303461,0.912174085,0.911181505};
        double speedfactor_wknd [] = {0.99940158281,0.99708319426,0.99554179196,0.99238514130,0.98963242224,0.9906198106,0.97447750699,0.94228266235,0.92388133387,0.90811304100,0.88140867405,0.83981867959,0.8160764777,0.78060529898,0.74882934637,0.70524961476,0.73241775503,0.76926529329,0.83458252921,0.88820070912,0.90447765659,0.96620438939,0.97916012147,0.97580898523};
        //		try {
//			BufferedReader reader = new BufferedReader(new FileReader("C:\\MATSim\\calibration\\speed\\factor.csv"));
//			reader.readLine();
//			String line = null;
//			int count = 0;
//	        while((line=reader.readLine())!=null){
//	            String item[] = line.split(",");
//	            for(int i = 0; i < item.length; i++) {
//	            	speedfactor[count][i] = Double.parseDouble(item[i]);
//	            	System.out.println(speedfactor[count][i]);
//	            }
//	            count++;
//	        }
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

        //double ExpressFactor []= {0.8, 0.8, 0.8, 0.8, 0.8}; // 0 iter
        //double ExpressFactor []= {0.62128898,0.66397437,0.66980107,0.78181944,1.28130222}; // 1st iter
        double ExpressFactor []= {0.443784647,0.462896104,0.488964576,0.451651106,1.627747433}; //2nd iter
        //double ExpressFactor []= {0.77203356,0.604663736,1.0166032,1.826581032,0.484916064};
        //double ExpressFactor []= {0.769495702,0.611477229,1.033875646,1.823944406,0.486589862};
        //double ExpressFactor []= {0.744481127,0.515295794,1.293328122,4.23754805,0.369681752};
        double ArterialFactor [] = {0.8, 0.8, 0.8, 0.8, 0.8};



        //double ExpressFactor []= {0.510231578, 0.477674912, 0.550571519, 2.076690108, 1.116185162}; // capacity
        //double ArterialFactor []= {0.510231578, 0.477674912, 0.550571519, 2.076690108, 1.116185162}; // capacity
        //double ExpressFactor1 []= {0.8,0.8,0.8,0.8,0.8};// speed
        //double ArterialFactor1 []= {0.8,0.8,0.8,0.8,0.8}; // speed

//		try{
//			BufferedReader reader = new BufferedReader(new FileReader("C:\\MATSim\\calibration\\" + String.valueOf(k)
//					+ "\\theta_up_down" + String.valueOf(k) + ".csv"));
//			String line=null;
//			int i = 0;
//			double [][] param = new double [20][2];
//
//			while((line=reader.readLine())!=null){
//				String temp [] = line.split(",");
//				param[i][0]=Double.parseDouble(temp[0]);
//				param[i][1]=Double.parseDouble(temp[1]);
//				i+=1;
//			}
//			System.out.println("Theta loaded");
//			reader.close();
//
//			for(int col = 0; col < 2; col++){
//				for(int j=0;j<5;j++ ){
//					ExpressFactor[j]=param[j][col];
//					ArterialFactor[j]=param[j+5][col];
//					ExpressFactor1[j]=param[j+10][col];
//					ArterialFactor1[j]=param[j+15][col];
//					System.out.println(ExpressFactor[j]+","+ArterialFactor[j]+","+ExpressFactor1[j]+","+ArterialFactor1[j]);
//					//System.out.println(ExpressFactor[j]+","+ArterialFactor[j]);
//
//				}
        Config config = ConfigUtils.loadConfig(path);
        config.network().setTimeVariantNetwork(true);
        config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
        //config.controler().setOutputDirectory("C:\\MATSim\\calibration\\" + k);

        Scenario scenario = ScenarioUtils.loadScenario(config) ;
        SignalSystemsConfigGroup signalsConfigGroup = ConfigUtils.addOrGetModule(config,
                SignalSystemsConfigGroup.GROUP_NAME, SignalSystemsConfigGroup.class);
        signalsConfigGroup.setUseSignalSystems(true);
        if (signalsConfigGroup.isUseSignalSystems()) {
            scenario.addScenarioElement(SignalsData.ELEMENT_NAME, new SignalsDataLoader(config).loadSignalsData());
        }
        //Network network = scenario.getNetwork();
        LaemmerConfigGroup laemmerConfigGroup = ConfigUtils.addOrGetModule(config,
                LaemmerConfigGroup.GROUP_NAME, LaemmerConfigGroup.class);
        laemmerConfigGroup.setActiveRegime(Regime.COMBINED);
        laemmerConfigGroup.setDesiredCycleTime(90);
        laemmerConfigGroup.setMinGreenTime(10);
        Controler controler = new Controler( scenario ) ;
//				controler.addOverridingModule(new SwissRailRaptorModule());
        Signals.configure(controler);
        for ( Link link : scenario.getNetwork().getLinks().values() ) {
            double speed = link.getFreespeed() ;
            //final double threshold = 5./3.6;
            double capacity = link.getCapacity() ;
            Set<String> linkType = link.getAllowedModes();

            if(linkType.contains("car")){
                if ( speed > 33 ) {
                    for (int day = 0; day < k; day++) {
                        for(int i = 0; i < 24; i++){
                            int timeindex = day * 24 + i;
                            NetworkChangeEvent event = new NetworkChangeEvent(timeindex*3600.) ;
                            if(day % 7 == 5 || day % 7 == 6) {
                                event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, speed*speedfactor_wknd[i] ));
                            }else {
                                event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, speed*speedfactor[i] ));
                            }

                            if(i < 6) {
                                event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ExpressFactor[4] ));
                            }else if(i < 9 && i >= 6) {
                                event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ExpressFactor[0] ));
                            }else if(i < 15 && i >= 9) {
                                event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ExpressFactor[1] ));
                            }else if(i < 19 && i >= 15) {
                                event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ExpressFactor[2] ));
                            }else if(i < 21 && i >= 19) {
                                event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ExpressFactor[3] ));
                            }else if(i >= 21) {
                                event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ExpressFactor[4] ));
                            }
                            event.addLink(link);
                            NetworkUtils.addNetworkChangeEvent(scenario.getNetwork(),event);
                        }
                    }
//							{
//								NetworkChangeEvent event = new NetworkChangeEvent(0.*3600.) ;
//								//event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  threshold/10 ));
//								event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ExpressFactor[4] ));
//								event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, speed*ExpressFactor1[4] ));
//								event.addLink(link);
//								NetworkUtils.addNetworkChangeEvent(scenario.getNetwork(),event);
//							}
//							{
//								NetworkChangeEvent event = new NetworkChangeEvent(6.*3600.) ;
//								//event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  threshold/10 ));
//								event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ExpressFactor[0] ));
//								event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, speed*ExpressFactor1[0] ));
//								event.addLink(link);
//								NetworkUtils.addNetworkChangeEvent(scenario.getNetwork(),event);
//							}
//							{
//								NetworkChangeEvent event = new NetworkChangeEvent(9.*3600.) ;
//								//event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  threshold/10 ));
//								event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ExpressFactor[1] ));
//								event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, speed*ExpressFactor1[1] ));
//								event.addLink(link);
//								NetworkUtils.addNetworkChangeEvent(scenario.getNetwork(),event);
//							}
//							{
//								NetworkChangeEvent event = new NetworkChangeEvent(15.*3600.) ;
//								//event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  threshold/10 ));
//								event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ExpressFactor[2] ));
//								event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, speed*ExpressFactor1[2] ));
//								event.addLink(link);
//								NetworkUtils.addNetworkChangeEvent(scenario.getNetwork(),event);
//							}
//							{
//								NetworkChangeEvent event = new NetworkChangeEvent(19.*3600.) ;
//								//event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  threshold/10 ));
//								event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ExpressFactor[3] ));
//								event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, speed*ExpressFactor1[3] ));
//								event.addLink(link);
//								NetworkUtils.addNetworkChangeEvent(scenario.getNetwork(),event);
//							}
//							{
//								NetworkChangeEvent event = new NetworkChangeEvent(21.*3600.) ;
//								//event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  threshold/10 ));
//								event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ExpressFactor[4] ));
//								event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, speed*ExpressFactor1[4] ));
//								event.addLink(link);
//								NetworkUtils.addNetworkChangeEvent(scenario.getNetwork(),event);
//							}

                }
						/*
						else{
							{
								NetworkChangeEvent event = new NetworkChangeEvent(0.*3600.) ;
								//event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  threshold/10 ));
								event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ArterialFactor[4] ));
								event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, speed*ArterialFactor1[4] ));
								event.addLink(link);
								NetworkUtils.addNetworkChangeEvent(scenario.getNetwork(),event);
							}
							{
								NetworkChangeEvent event = new NetworkChangeEvent(6.*3600.) ;
								//event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  threshold/10 ));
								event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ArterialFactor[0] ));
								event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, speed*ArterialFactor1[0] ));
								event.addLink(link);
								NetworkUtils.addNetworkChangeEvent(scenario.getNetwork(),event);
							}
							{
								NetworkChangeEvent event = new NetworkChangeEvent(9.*3600.) ;
								//event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  threshold/10 ));
								event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ArterialFactor[1] ));
								event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, speed*ArterialFactor1[1] ));
								event.addLink(link);
								NetworkUtils.addNetworkChangeEvent(scenario.getNetwork(),event);
							}
							{
								NetworkChangeEvent event = new NetworkChangeEvent(15.*3600.) ;
								//event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  threshold/10 ));
								event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ArterialFactor[2] ));
								event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, speed*ArterialFactor1[2] ));
								event.addLink(link);
								NetworkUtils.addNetworkChangeEvent(scenario.getNetwork(),event);
							}
							{
								NetworkChangeEvent event = new NetworkChangeEvent(19.*3600.) ;
								//event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  threshold/10 ));
								event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ArterialFactor[3] ));
								event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, speed*ArterialFactor1[3] ));
								event.addLink(link);
								NetworkUtils.addNetworkChangeEvent(scenario.getNetwork(),event);
							}
							{
								NetworkChangeEvent event = new NetworkChangeEvent(21.*3600.) ;
								//event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  threshold/10 ));
								event.setFlowCapacityChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, capacity/3600*ArterialFactor[4] ));
								event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS, speed*ArterialFactor1[4] ));
								event.addLink(link);
								NetworkUtils.addNetworkChangeEvent(scenario.getNetwork(),event);
							}

						}*/
            }
        }


        // ---
//				controler.addOverridingModule(new SwissRailRaptorModule());
//				controler.addOverridingModule(new SBBTransitModule());
//				controler.configureQSimComponents(components -> {
//					SBBTransitEngineQSimModule.configure(components);
//				});
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



//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//

    //	}hg
    public static void main(String[] args) {
        RunAdaptiveSignalFullModel r = new RunAdaptiveSignalFullModel();
        try {
            r.run_MATSim(1,  "D:\\MATSim\\LA_large\\SignalSim\\lametro\\fullmodel\\input\\config_fareless_transit.xml"); // 2 should be 1st iteration
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
