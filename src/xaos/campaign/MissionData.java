package xaos.campaign;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import xaos.main.Game;
import xaos.utils.Messages;


/**
 * 
 * Aquí se guardan los datos de una misión. ID, Nombre, objetivos, ...
 * 
 */
public class MissionData implements Externalizable {
	private static final long serialVersionUID = -1688348947848596698L;

	private String id;
	private String name;
	private String text;
	private boolean allowBury;

	// Tutorial
	private ArrayList<TutorialFlow> tutorialFlows;
	private int tutorialFlowIndex;


	public MissionData () {
	}


	public MissionData (String sID) {
		setId (sID);
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


	public void setText (String text) {
		this.text = text;
	}


	public String getText () {
		return text;
	}


//	public void setObjectives (ArrayList<ObjectiveData> objectives) {
//		this.objectives = objectives;
//	}
//
//
//	public ArrayList<ObjectiveData> getObjectives () {
//		return objectives;
//	}


	public void setAllowBury (boolean allowBury) {
		this.allowBury = allowBury;
	}


	public void setAllowBury (String sAllowBury) {
		if (sAllowBury == null || sAllowBury.trim ().length () == 0) {
			setAllowBury (true);
		} else {
			setAllowBury (!sAllowBury.equalsIgnoreCase ("FALSE")); //$NON-NLS-1$
		}
	}


	public boolean isAllowBury () {
		return allowBury;
	}


	public void setTutorialFlows (ArrayList<TutorialFlow> flows) {
		this.tutorialFlows = flows;
	}


	public ArrayList<TutorialFlow> getTutorialFlows () {
		return tutorialFlows;
	}


	public void setTutorialFlowIndex (int tutorialFlowIndex) {
		this.tutorialFlowIndex = tutorialFlowIndex;
	}


	public int getTutorialFlowIndex () {
		return tutorialFlowIndex;
	}


	public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
		id = (String) in.readObject ();
		name = (String) in.readObject ();
		text = (String) in.readObject ();
		allowBury = in.readBoolean ();

		tutorialFlows = (ArrayList<TutorialFlow>) in.readObject ();
		tutorialFlowIndex = in.readInt ();
	}


	public void writeExternal (ObjectOutput out) throws IOException {
		if (Game.SAVE_MISSION) {
			out.writeObject (id);
			out.writeObject (name);
			out.writeObject (text);
			out.writeBoolean (allowBury);

			out.writeObject (tutorialFlows);
			out.writeInt (tutorialFlowIndex);
		} else {
			out.writeObject ("xcnid"); //$NON-NLS-1$
			out.writeObject ("xcnname"); //$NON-NLS-1$
			out.writeObject ("xcntext"); //$NON-NLS-1$
			out.writeBoolean (false);

			out.writeObject (new ArrayList<TutorialFlow> ());
			out.writeInt (0);
		}
	}
}
