package xaos.panels.info;

import java.awt.Color;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import xaos.campaign.CampaignManager;
import xaos.campaign.MissionData;
import xaos.main.Game;
import xaos.utils.ColorGL;
import xaos.utils.Messages;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;

public final class MissionPanel extends InfoPanel {

/*
    private MissionData missionData;
    private ArrayList<String> alText;
    private ArrayList<Boolean> alCenter;
    private ArrayList<ColorGL> alColor;

    public MissionPanel(String sCampaignID, String sMissionID) {
        super();
        setMissionData(CampaignManager.getMission(sCampaignID, sMissionID));
    }

    public void setMissionData(MissionData missionData) {
        this.missionData = missionData;

        if (missionData == null) {
            return;
        }

        setMissionText(Messages.getString("MissionPanel.0") + missionData.getName() + "]", true, Color.WHITE); //$NON-NLS-1$ //$NON-NLS-2$
        addMissionText(new String());
        if (missionData.getText() != null) {
            addMissionText(missionData.getText());
            addMissionText(new String());
            addMissionText(new String());
        }

        if (missionData.getObjectives() != null && missionData.getObjectives().size() > 0) {
            addMissionText(Messages.getString("MissionPanel.2"), true, Color.WHITE); //$NON-NLS-1$
            for (int i = 0; i < missionData.getObjectives().size(); i++) {
                addMissionText(missionData.getObjectives().get(i).toString());
            }
        }
    }

    private void setMissionText(String text, boolean center, Color color) {
        alText = null;
        alCenter = null;
        alColor = null;
        addMissionText(text, center, color);
    }

    private void addMissionText(String text) {
        addMissionText(text, false, Color.GRAY);
    }

    /**
     * Añade un texto en un array de Strings, partido segun el ancho del panel
     *
     * @param text Texto
     *
    private void addMissionText(String text, boolean center, Color color) {
        if (alText == null) {
            alText = new ArrayList<>();
            alCenter = new ArrayList<>();
            alColor = new ArrayList<>();
        }

        int iMaxChars = UtilFont.getMaxCharsByWidth(text, getWidth() - UtilFont.MAX_WIDTH);
        if (text.length() > iMaxChars) {
            alText.add(new String(text.substring(0, iMaxChars)));
            alCenter.add(Boolean.valueOf(center));
            alColor.add(new ColorGL(color));
            addMissionText(text.substring(iMaxChars).trim());
        } else {
            alText.add(text);
            alCenter.add(Boolean.valueOf(center));
            alColor.add(new ColorGL(color));
        }
    }

    public MissionData getMissionData() {
        return missionData;
    }

    public void render() {
        super.render();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        UtilsGL.glBegin(GL11.GL_QUADS);
        for (int i = 0; i < alText.size(); i++) {
            if (alCenter.get(i)) {
                UtilsGL.drawString(alText.get(i), getX() + getWidth() / 2 - (UtilFont.getWidth(alText.get(i)) / 2), getY() + UtilFont.MAX_HEIGHT / 2 + (i * (UtilFont.MAX_HEIGHT + UtilFont.MAX_HEIGHT / 2)), alColor.get(i));
            } else {
                UtilsGL.drawString(alText.get(i), getX() + UtilFont.MAX_WIDTH / 2, getY() + UtilFont.MAX_HEIGHT / 2 + (i * (UtilFont.MAX_HEIGHT + UtilFont.MAX_HEIGHT / 2)), alColor.get(i));
            }
        }
        UtilsGL.glEnd();
    }
*/
}
