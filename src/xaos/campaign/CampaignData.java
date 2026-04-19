package xaos.campaign;

import java.util.ArrayList;


/**
 * 
 * Aquí se guardan los datos de una campaña. ID, Nombre, misiones que la componen, ...
 * 
 */
public class CampaignData {

	private String id;
	private String name;
	private boolean tutorial;

	private ArrayList<MissionData> missions;


	public CampaignData (String sID) {
		setId (sID);
		setMissions (new ArrayList<MissionData> ());
	}


	public void setId (String id) {
		this.id = id;
	}


	public String getId () {
		return id;
	}


	public void setName (String name) {
		this.name = name;
	}


	public String getName () {
		return name;
	}


	public void setTutorial (String sTutorial) {
		setTutorial (Boolean.parseBoolean (sTutorial));
	}


	public void setTutorial (boolean tutorial) {
		this.tutorial = tutorial;
	}


	public boolean isTutorial () {
		return tutorial;
	}


	public void setMissions (ArrayList<MissionData> missions) {
		this.missions = missions;
	}


	public ArrayList<MissionData> getMissions () {
		return missions;
	}
}
