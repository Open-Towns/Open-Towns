package xaos.campaign;

import java.io.File;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xaos.Towns;
import xaos.main.Game;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Utils;
import xaos.utils.UtilsXML;


/**
 * 
 * Carga los datos de campañas y tiene un array de campañas
 * 
 */
public class CampaignManager {

	private static ArrayList<CampaignData> alCampaigns;


	private static void loadCampaigns () {
		alCampaigns = new ArrayList<CampaignData> ();

		// Cargar de fichero
		loadCampaignXML (Towns.getPropertiesString ("DATA_FOLDER") + "campaigns.xml", true); //$NON-NLS-1$ //$NON-NLS-2$

		// Mods
		File fUserFolder = new File (Game.getUserFolder ());
		if (!fUserFolder.exists () || !fUserFolder.isDirectory ()) {
			return;
		}

		ArrayList<String> alMods = Game.getModsLoaded ();
		if (alMods != null && alMods.size () > 0) {
			for (int i = 0; i < alMods.size (); i++) {
				String sModActionsPath = fUserFolder.getAbsolutePath () + System.getProperty ("file.separator") + Game.MODS_FOLDER1 + System.getProperty ("file.separator") + alMods.get (i) + System.getProperty ("file.separator") + Towns.getPropertiesString ("DATA_FOLDER") + "campaigns.xml"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				File fIni = new File (sModActionsPath);
				if (fIni.exists ()) {
					loadCampaignXML (sModActionsPath, false);
				}
			}
		}
	}


	private static void loadCampaignXML (String sXMLPath, boolean bLoadingMain) {
		try {
			Document doc = UtilsXML.loadXMLFile (sXMLPath);

			// Tenemos el documento XML parseado
			// Lo recorremos entero y vamos añadiendo los datos al array
			NodeList nodeList = doc.getDocumentElement ().getChildNodes ();
			Node node;
			String sID;
			for (int i = 0; i < nodeList.getLength (); i++) {
				node = nodeList.item (i);
				if (node.getNodeType () == Node.ELEMENT_NODE) {
					// Campaign
					// Obtenemos el ID, name y las campañas
					sID = UtilsXML.getChildValue (node.getChildNodes (), "id"); //$NON-NLS-1$

					int iIndex = -1;
					// Miramos si ya existe
					for (int c = 0; c < alCampaigns.size (); c++) {
						if (alCampaigns.get (c).getId ().equals (sID)) {
							iIndex = c;
							break;
						}
					}

					CampaignData campaignData;
					if (iIndex != -1 && !bLoadingMain) {
						campaignData = alCampaigns.get (iIndex);

						// Name
						String sAux = UtilsXML.getChildValue (node.getChildNodes (), "name"); //$NON-NLS-1$
						if (sAux != null) {
							campaignData.setName (sAux);
						}

						sAux = UtilsXML.getChildValue (node.getChildNodes (), "tutorial"); //$NON-NLS-1$
						if (sAux != null) {
							campaignData.setTutorial (sAux);
						}
					} else {
						campaignData = new CampaignData (sID);
						campaignData.setName (UtilsXML.getChildValue (node.getChildNodes (), "name")); //$NON-NLS-1$
						campaignData.setTutorial (UtilsXML.getChildValue (node.getChildNodes (), "tutorial")); //$NON-NLS-1$
					}

					// Missions
					campaignData.setMissions (loadMissions (node.getChildNodes (), campaignData, bLoadingMain));

					// Lo añadimos al array
					if (iIndex != -1 && !bLoadingMain) {
						alCampaigns.set (iIndex, campaignData);
					} else {
						alCampaigns.add (campaignData);
					}
				}
			}
		}
		catch (Exception e) {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("CampaignManager.0") + " [" + e.toString () + "]", "CampaignManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			Game.exit ();
		}
	}


	private static ArrayList<MissionData> loadMissions (NodeList nodeList, CampaignData campaignData, boolean bLoadingMain) throws Exception {
		ArrayList<MissionData> alMissions;
		if (!bLoadingMain) {
			// Cargamos la mision main
			alMissions = campaignData.getMissions ();
		} else {
			alMissions = new ArrayList<MissionData> ();
		}

		Node node;
		String sID;
		for (int i = 0; i < nodeList.getLength (); i++) {
			node = nodeList.item (i);
			if (node.getNodeType () == Node.ELEMENT_NODE && node.getNodeName ().equals ("mission")) { //$NON-NLS-1$
				sID = UtilsXML.getChildValue (node.getChildNodes (), "id"); //$NON-NLS-1$

				// Miramos si ya existe
				int iIndex = -1;
				for (int c = 0; c < alMissions.size (); c++) {
					if (alMissions.get (c).getId ().equals (sID)) {
						iIndex = c;
						break;
					}
				}

				MissionData missionData;
				if (iIndex != -1 && !bLoadingMain) {
					missionData = campaignData.getMissions ().get (iIndex);
					String sAux = UtilsXML.getChildValue (node.getChildNodes (), "name"); //$NON-NLS-1$
					if (sAux != null) {
						missionData.setName (sAux);
					}
					sAux = UtilsXML.getChildValue (node.getChildNodes (), "text"); //$NON-NLS-1$
					if (sAux != null) {
						missionData.setText (sAux);
					}
//					ArrayList<ObjectiveData> alObjectives = loadObjectives (node.getChildNodes ());
//					if (alObjectives != null && alObjectives.size () > 0) {
//						missionData.setObjectives (alObjectives);
//					}

					sAux = UtilsXML.getChildValue (node.getChildNodes (), "allowBury"); //$NON-NLS-1$
					if (sAux != null) {
						missionData.setAllowBury (sAux);
					}

					// Tutorial
					missionData.setTutorialFlows (loadTutorialFlows (node.getChildNodes ()));
					missionData.setTutorialFlowIndex (0);

					alMissions.set (iIndex, missionData);
				} else {
					missionData = new MissionData (sID);
					missionData.setName (UtilsXML.getChildValue (node.getChildNodes (), "name")); //$NON-NLS-1$
					missionData.setText (UtilsXML.getChildValue (node.getChildNodes (), "text")); //$NON-NLS-1$

//					missionData.setObjectives (loadObjectives (node.getChildNodes ()));

					missionData.setAllowBury (UtilsXML.getChildValue (node.getChildNodes (), "allowBury")); //$NON-NLS-1$

					missionData.setTutorialFlows (loadTutorialFlows (node.getChildNodes ()));
					missionData.setTutorialFlowIndex (0);

					alMissions.add (missionData);
				}
			}
		}

		return alMissions;
	}


//	private static ArrayList<ObjectiveData> loadObjectives (NodeList nodeList) throws Exception {
//		ArrayList<ObjectiveData> alObjectives = new ArrayList<ObjectiveData> ();
//
//		Node node;
//		for (int i = 0; i < nodeList.getLength (); i++) {
//			node = nodeList.item (i);
//			if (node.getNodeType () == Node.ELEMENT_NODE && node.getNodeName ().equals ("objective")) { //$NON-NLS-1$
//				ObjectiveData objectiveData = new ObjectiveData ();
//				objectiveData.setType (UtilsXML.getChildValue (node.getChildNodes (), "type")); //$NON-NLS-1$
//				objectiveData.setParam1 (UtilsXML.getChildValue (node.getChildNodes (), "param1")); //$NON-NLS-1$
//				objectiveData.setParam2 (UtilsXML.getChildValue (node.getChildNodes (), "param2")); //$NON-NLS-1$
//				alObjectives.add (objectiveData);
//			}
//		}
//
//		return alObjectives;
//	}


	private static ArrayList<TutorialFlow> loadTutorialFlows (NodeList nodeList) throws Exception {
		ArrayList<TutorialFlow> alFlows = new ArrayList<TutorialFlow> ();

		Node node, nodeFlow;
		for (int i = 0; i < nodeList.getLength (); i++) {
			node = nodeList.item (i);
			if (node.getNodeType () == Node.ELEMENT_NODE && node.getNodeName ().equals ("tutorialFlow")) { //$NON-NLS-1$
				// Tenemos el tag de flows, ahora vamos cargando 1 a 1
				NodeList nodeListFlows = node.getChildNodes ();
				if (nodeListFlows != null && nodeListFlows.getLength () > 0) {
					for (int j = 0; j < nodeListFlows.getLength (); j++) {
						nodeFlow = nodeListFlows.item (j);
						if (nodeFlow.getNodeType () == Node.ELEMENT_NODE && nodeFlow.getNodeName ().equals ("flow")) { //$NON-NLS-1$
							// Tenemos un flow, lo metemos en la lista
							TutorialFlow flow = new TutorialFlow ();
							flow.setTitle (UtilsXML.getChildValue (nodeFlow.getChildNodes (), "title")); //$NON-NLS-1$
							flow.setTexts (UtilsXML.getChildValues (nodeFlow.getChildNodes (), "text")); //$NON-NLS-1$
							flow.setImage (UtilsXML.getChildValue (nodeFlow.getChildNodes (), "image")); //$NON-NLS-1$
							flow.setTile (UtilsXML.getChildValue (nodeFlow.getChildNodes (), "tile")); //$NON-NLS-1$
							flow.setStartEvents (Utils.getArray (UtilsXML.getChildValue (nodeFlow.getChildNodes (), "startEvents"))); //$NON-NLS-1$
							flow.setNextMission (UtilsXML.getChildValue (nodeFlow.getChildNodes (), "nextMission")); //$NON-NLS-1$
							flow.setBlinkItems (UtilsXML.getChildValue (nodeFlow.getChildNodes (), "blinkItems")); //$NON-NLS-1$
							flow.setBlinkPiles (Utils.getArray (UtilsXML.getChildValue (nodeFlow.getChildNodes (), "blinkPiles"))); //$NON-NLS-1$
							flow.setBlinkBottom (Utils.getArray (UtilsXML.getChildValue (nodeFlow.getChildNodes (), "blinkBottom"))); //$NON-NLS-1$
							flow.setBlinkRight (Utils.getArray (UtilsXML.getChildValue (nodeFlow.getChildNodes (), "blinkRight"))); //$NON-NLS-1$
							loadBlinkProduction (flow, nodeFlow.getChildNodes ());
							flow.setBlinkMinis (Utils.getArray (UtilsXML.getChildValue (nodeFlow.getChildNodes (), "blinkMinis"))); //$NON-NLS-1$

							// Cargamos los triggers
							flow.setOrderedTriggers (UtilsXML.getChildValue (nodeFlow.getChildNodes (), "orderedTriggers")); //$NON-NLS-1$
							flow.setTriggers (loadTutorialTriggers (nodeFlow.getChildNodes ()));

							// Añadimos el flow
							alFlows.add (flow);
						}
					}
				}

				// Flow cargado
//				System.out.println ("Flow cargado, num flows: " + alFlows.size ());
//				for (int f = 0; f < alFlows.size (); f++) {
//					TutorialFlow flow = alFlows.get (f);
//					System.out.println ("\nFlow " + f);
//					if (flow.getBlinkItems () != null) {
//						System.out.println ("blink items: " + flow.getBlinkItems ().size ());
//					}
//					if (flow.getBlinkBottom () != null) {
//						System.out.println ("blink bottom: " + flow.getBlinkBottom ().size ());
//					}
//					if (flow.getTriggers () != null) {
//						System.out.println ("Image triggers: " + flow.getTriggers ().size ());
//						
//						for (int f2 = 0; f2 < flow.getTriggers ().size (); f2++) {
//							TutorialTrigger trigger = flow.getTriggers ().get (f2);
//							System.out.println (trigger.getType () + "-> " + trigger.getParam1 () + ", " + trigger.getParam2 ());
//						}
//					}
//				}
				break;
			}
		}

		return alFlows;
	}
		

	private static void loadBlinkProduction (TutorialFlow flow, NodeList nodelist) throws Exception {
		ArrayList<String> alBlinks = Utils.getArray (UtilsXML.getChildValue (nodelist, "blinkProduction")); //$NON-NLS-1$

		if (alBlinks != null) {
//			// Pillamos los parámetros
//			Node node = UtilsXML.getChild (nodelist, "blinkProduction"); //$NON-NLS-1$
//			boolean bRegPlus = false;
//			boolean bRegMinus = false;
//			boolean bAutPlus = false;
//			boolean bAutMinus = false;
//			if (node != null) {
//				NamedNodeMap nnm = node.getAttributes ();
//				if (nnm != null) {
//					if (nnm.getNamedItem ("regularPlus") != null) {
//						bRegPlus = Boolean.parseBoolean (nnm.getNamedItem ("regularPlus").getNodeValue ());
//					}
//					if (nnm.getNamedItem ("regularMinus") != null) {
//						bRegMinus = Boolean.parseBoolean (nnm.getNamedItem ("regularMinus").getNodeValue ());
//					}
//					if (nnm.getNamedItem ("automatedPlus") != null) {
//						bAutPlus = Boolean.parseBoolean (nnm.getNamedItem ("automatedPlus").getNodeValue ());
//					}
//					if (nnm.getNamedItem ("automatedMinus") != null) {
//						bAutMinus = Boolean.parseBoolean (nnm.getNamedItem ("automatedMinus").getNodeValue ());
//					}
//				}
//			}
//
//			flow.setBlinkProduction (alBlinks, bRegPlus, bRegMinus, bAutPlus, bAutMinus);
			flow.setBlinkProduction (alBlinks);
		}
	}


	private static ArrayList<TutorialTrigger> loadTutorialTriggers (NodeList nodeList) throws Exception {
		ArrayList<TutorialTrigger> alTriggers = new ArrayList<TutorialTrigger> ();

		Node node, nodeTrigger;
		for (int i = 0; i < nodeList.getLength (); i++) {
			node = nodeList.item (i);
			if (node.getNodeType () == Node.ELEMENT_NODE && node.getNodeName ().equals ("triggers")) { //$NON-NLS-1$
				// Lista de triggers
				NodeList nodeListTriggers = node.getChildNodes ();
				if (nodeListTriggers != null && nodeListTriggers.getLength () > 0) {
					for (int j = 0; j < nodeListTriggers.getLength (); j++) {
						nodeTrigger = nodeListTriggers.item (j);
						if (nodeTrigger.getNodeType () == Node.ELEMENT_NODE && nodeTrigger.getNodeName ().equals ("trigger")) { //$NON-NLS-1$
							// Tenemos un trigger, lo cargamos
							TutorialTrigger trigger = new TutorialTrigger ();
							trigger.setType (UtilsXML.getChildValue (nodeTrigger.getChildNodes (), "type")); //$NON-NLS-1$
							trigger.setParam1 (UtilsXML.getChildValue (nodeTrigger.getChildNodes (), "param1")); //$NON-NLS-1$
							trigger.setParam2 (UtilsXML.getChildValue (nodeTrigger.getChildNodes (), "param2")); //$NON-NLS-1$
							trigger.setParamXYZ (UtilsXML.getChildValue (nodeTrigger.getChildNodes (), "paramXYZ")); //$NON-NLS-1$
							alTriggers.add (trigger);
						}
					}
				}

				break;
			}
		}

		return alTriggers;
	}


	public static ArrayList<CampaignData> getCampaigns () {
		if (alCampaigns == null) {
			loadCampaigns ();
		}

		return alCampaigns;
	}


	public static MissionData getMission (String campaignID, String missionID) {
		if (campaignID == null || missionID == null) {
			return null;
		}

		if (alCampaigns == null) {
			loadCampaigns ();
		}

		CampaignData campaignData;
		MissionData missionData;
		for (int i = 0; i < alCampaigns.size (); i++) {
			campaignData = alCampaigns.get (i);
			if (!campaignData.getId ().equals (campaignID)) {
				continue;
			}

			for (int j = 0; j < campaignData.getMissions ().size (); j++) {
				missionData = campaignData.getMissions ().get (j);
				if (missionData.getId ().equals (missionID)) {
					return missionData;
				}
			}

		}
		return null;
	}


	public static void clear () {
		alCampaigns = null;
	}
}
