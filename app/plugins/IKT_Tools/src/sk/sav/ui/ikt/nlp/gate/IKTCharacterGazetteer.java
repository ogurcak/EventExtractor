/*
 *  IKTGazetteer.java
 *
 * Copyright (c) 2000-2012, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 3, 29 June 2007.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  stevo, 15/11/2013
 *
 * For details on the configuration options, see the user guide:
 * http://gate.ac.uk/cgi-bin/userguide/sec:creole-model:config
 */

package sk.sav.ui.ikt.nlp.gate;

import gate.AnnotationSet;
import gate.Corpus;
import gate.DataStore;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.ANNIEConstants;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import gate.util.GateException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import sk.sav.ui.ikt.nlp.gazetteer.character.CharacterGazetteer;
import sk.sav.ui.ikt.nlp.gazetteer.character.CharacterGazetteer.Representation;

/**
 * This class is the implementation of the resource IKT TOOLS.
 */
@CreoleResource(name = "IKT Character Gazetteer", comment = "Character gazetteer by IKT Group")
public class IKTCharacterGazetteer extends AbstractLanguageAnalyser implements
		ProcessingResource {

	private static final long serialVersionUID = 8060875525401881969L;

	private static final Logger log = Logger
			.getLogger(IKTCharacterGazetteer.class);

	public static void printUsage() {
		System.out
				.println("usage: "
						+ IKTCharacterGazetteer.class.getSimpleName()
						+ " -gateHome <dir> -cfg <config file> -dataStoreDir <dir> -corpusName <string> -outputASName <string>");
	}

	public static void main(String[] args) throws GateException,
			MalformedURLException {

		String gateHome = null;
		String cfgFile = null;
		String dataStoreDir = null;
		String corpusName = null;
		String outputASName = null;

		for (int i = 0; i < args.length; i++) {
			if (args[i].matches("(?i)-gateHome") && i + 1 < args.length) {
				i++;
				gateHome = args[i];
				continue;
			}
			if (args[i].matches("(?i)-cfg") && i + 1 < args.length) {
				i++;
				cfgFile = args[i];
				continue;
			}
			if (args[i].matches("(?i)-dataStoreDir") && i + 1 < args.length) {
				i++;
				dataStoreDir = new File(args[i]).getAbsolutePath();
				if (!dataStoreDir.matches("[a-z]+://")) {
					dataStoreDir = "file://" + dataStoreDir;
				}
				continue;
			}
			if (args[i].matches("(?i)-corpusName") && i + 1 < args.length) {
				i++;
				corpusName = args[i];
				continue;
			}
			if (args[i].matches("(?i)-outputASName") && i + 1 < args.length) {
				i++;
				outputASName = args[i];
				continue;
			}
		}

		if (gateHome == null || cfgFile == null || dataStoreDir == null
				|| corpusName == null || outputASName == null) {
			printUsage();
			System.exit(0);
		}

		log.info("GATE initialization...");
		System.setProperty("gate.home", gateHome);
		Gate.init();
		log.info("GATE initialized!");

		log.info("Initializing Annotowatch...");

		Gate.getCreoleRegister().registerDirectories(
				new File("./").toURI().toURL());

		FeatureMap params = Factory.newFeatureMap();
		params.put("cfgFile", cfgFile);
		ProcessingResource tokenGazetteer = (ProcessingResource) Factory
				.createResource(IKTCharacterGazetteer.class.getName(), params);
		tokenGazetteer.setParameterValue("outputASName", outputASName);
		log.info(IKTCharacterGazetteer.class.getSimpleName() + " initialized!");

		log.info("Opening data store: " + dataStoreDir);
		DataStore dataStore = Factory.openDataStore(
				"gate.persist.SerialDataStore", dataStoreDir);
		dataStore.open();
		log.info("Data store opened!");

		log.info("Opening corpus: " + corpusName);
		List<?> corpusIds = dataStore.getLrIds("gate.corpora.SerialCorpusImpl");
		Corpus corpus = null;
		for (Object corpusId : corpusIds) {
			if (dataStore.getLrName(corpusId).equals(corpusName)) {
				FeatureMap fm = Factory.newFeatureMap();
				fm.put(DataStore.DATASTORE_FEATURE_NAME, dataStore);
				fm.put(DataStore.LR_ID_FEATURE_NAME, corpusId);
				corpus = (Corpus) Factory.createResource(
						"gate.corpora.SerialCorpusImpl", fm);
				break;
			}
		}
		if (corpus != null) {
			log.info("Corpus opened!");
		} else {
			log.error("No such corpus: " + corpusName);
			System.exit(0);
		}

		log.info("Creating analyser...");
		SerialAnalyserController ctrl = (SerialAnalyserController) Factory
				.createResource(gate.creole.SerialAnalyserController.class
						.getName());

		log.info("Adding " + IKTCharacterGazetteer.class.getSimpleName()
				+ " to the analyser...");
		ctrl.add(tokenGazetteer);

		log.info("Setting corpus for the analyser...");
		ctrl.setCorpus(corpus);

		log.info("Executing analyser...");
		ctrl.execute();
		log.info("Analyser is done!");

		log.info("Syncing corpus...");
		dataStore.sync(corpus);

		log.info("Closing data store...");
		dataStore.close();
		log.info("Finished!");
	}

	private URL cfgFile;
	private Boolean caseSensitive;
	private CharacterGazetteer gazetteer;
	private Representation treeRepresentation;
	private HashSet<String> listClasses;
	private HashMap<Integer, String> classMap;
	private String outputASName;
	private AnnotationSet outputAS;

	@Override
	public Resource init() throws ResourceInstantiationException {

		gazetteer = new CharacterGazetteer(treeRepresentation,
				isCaseSensitive());
		listClasses = new HashSet<String>();
		classMap = new HashMap<Integer, String>();

		try {
			BufferedReader cfgIn = new BufferedReader(new InputStreamReader(
					cfgFile.openStream()));

			int lastClassID = -1;
			String listsDefLine = null;
			while ((listsDefLine = cfgIn.readLine()) != null) {

				listsDefLine = listsDefLine.trim();
				int semicpos = listsDefLine.indexOf(':');

				URL listURL = new URL(cfgFile.toString().replaceFirst("[^/]*$",
						"")
						+ listsDefLine.substring(0, semicpos));
				String listClass = listsDefLine.substring(semicpos + 1);

				int classID = -1;
				if (listClasses.add(listClass)) {
					classID = ++lastClassID;
					classMap.put(classID, listClass);
				} else {
					for (int k : classMap.keySet()) {
						if (classMap.get(k).equals(listClass)) {
							classID = k;
							break;
						}
					}
					if (classID == -1) {
						throw new RuntimeException();
					}
				}
				BufferedReader listReader = new BufferedReader(
						new InputStreamReader(listURL.openStream()));
				String listLine = null;
				while ((listLine = listReader.readLine()) != null) {
					gazetteer.insert(listLine.trim(), classID);
				}
			}

			cfgIn.close();

		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage());
			throw new ResourceInstantiationException(e.getMessage());
		}

		return this;
	}

	@Override
	public void reInit() throws ResourceInstantiationException {
		super.reInit();
		init();
	}

	@CreoleParameter(defaultValue = "")
	public void setCfgFile(URL cfgFile) {
		this.cfgFile = cfgFile;
	}

	public URL getCfgFile() {
		return this.cfgFile;
	}

	@CreoleParameter(defaultValue = "CHILDSIBLING")
	public void setTreeRepresentation(Representation treeRepresentation) {
		this.treeRepresentation = treeRepresentation;
	}

	public Representation getTreeRepresentation() {
		return this.treeRepresentation;
	}

	@CreoleParameter(defaultValue = "true")
	public void setCaseSensitive(Boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public Boolean getCaseSensitive() {
		return this.caseSensitive;
	}

	public Boolean isCaseSensitive() {
		return this.caseSensitive;
	}

	@RunTime
	@CreoleParameter(defaultValue = "")
	public void setOutputASName(String outputASName) {
		this.outputASName = outputASName;
	}

	public String getOutputASName() {
		return outputASName;
	}

	@Override
	public void execute() throws ExecutionException {

		if (document == null) {
			throw new ExecutionException("No document to process!");
		}

		outputAS = document.getAnnotations(outputASName);
		outputAS.clear();

		String text = document.getContent().toString();
		if (!isCaseSensitive()) {
			text = text.toLowerCase();
		}

		List<int[]> matches = gazetteer.find(text);

		for (int[] match : matches) {
			for (int i = 2; i < match.length; i++) {
				try {
					String clazz = classMap.get(match[i]);
					int semicpos = clazz.indexOf(':');
					String clazz_major = (semicpos != -1) ? clazz.substring(0,
							semicpos) : clazz;
					String clazz_minor = (semicpos != -1) ? clazz.substring(
							semicpos + 1, clazz.length()) : null;
					FeatureMap features = Factory.newFeatureMap();
					features.put(ANNIEConstants.LOOKUP_MAJOR_TYPE_FEATURE_NAME,
							clazz_major);
					if (clazz_minor != null && !clazz_minor.isEmpty()) {
						features.put(
								ANNIEConstants.LOOKUP_MINOR_TYPE_FEATURE_NAME,
								clazz_minor);
					}
					outputAS.add((long) match[0], (long) match[1],
							ANNIEConstants.LOOKUP_ANNOTATION_TYPE, features);
				} catch (Exception e) {
					e.printStackTrace();
					log.error(e.getMessage());
					throw new ExecutionException(e);
				}
			}
		}
	}
} // class IKTGazetteer
