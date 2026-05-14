package xaos.panels.UI;

import java.util.ArrayList;

import xaos.actions.ActionPriorityManager;
import xaos.campaign.TutorialTrigger;
import xaos.data.CaravanData;
import xaos.data.CitizenGroupData;
import xaos.data.CitizenGroups;
import xaos.data.GlobalEventData;
import xaos.data.SoldierData;
import xaos.data.SoldierGroupData;
import xaos.data.SoldierGroups;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.CommandPanel;
import xaos.panels.ImagesPanel;
import xaos.panels.MainPanel;
import xaos.panels.MatsPanelData;
import xaos.panels.MessagesPanel;
import xaos.panels.MiniMapPanel;
import xaos.panels.TradePanel;
import xaos.panels.TypingPanel;
import xaos.panels.menus.ContextMenu;
import xaos.panels.menus.SmartMenu;
import xaos.platform.lwjgl3.input.Keyboard;
import xaos.stockpiles.Stockpile;
import xaos.tiles.Tile;
import xaos.tiles.entities.items.Container;
import xaos.tiles.entities.items.military.MilitaryItem;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.utils.Point3D;
import xaos.utils.UtilsAL;
import java.awt.Point;
import static xaos.panels.UI.UIPanelState.*;
import static xaos.panels.UI.UIPanel.*;
import xaos.utils.Messages;
import java.awt.Color;

public class UIPanelInputHandler {
    public static void mousePressed(int x, int y, int mouseButton) {
		int iPanel = isMouseOnAPanel(x, y);

		if (iPanel == MOUSE_NONE) {
			return;
		}

		/*
		 * TYPING PANEL (Si está activo ya no miraremos nada más)
		 */
		if (typingPanel != null) {
			if (iPanel == MOUSE_TYPING_PANEL_CLOSE || (mouseButton == 1 && iPanel == MOUSE_TYPING_PANEL)) {
				typingPanel = null;
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_TYPING_PANEL_CONFIRM) {
				closeTypingPanel();
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			}
			return;
		}

		/*
		 * IMAGES PANEL
		 */
		if (imagesPanel != null && ImagesPanel.isVisible()) {
			if (iPanel == MOUSE_IMAGES_PANEL_CLOSE || (mouseButton == 1 && iPanel == MOUSE_IMAGES_PANEL)) {
				ImagesPanel.setVisible(false);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_IMAGES_PANEL_NEXT) {
				if (ImagesPanel.nextFlow()) {
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_IMAGES_PANEL_PREVIOUS) {
				if (ImagesPanel.previousFlow()) {
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_IMAGES_PANEL_NEXT_MISSION) {
				if (ImagesPanel.nextMission()) {
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_TUTORIAL_ICON) {
				// Miramos también el botón (para hacer toggle)
				toggleTutorialPanel(false);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_IMAGES_PANEL) {
				return;
			}
		}

		/*
		 * PROFESSIONS PANEL
		 */
		if (isProfessionsPanelActive()) {
			if (iPanel == MOUSE_PROFESSIONS_PANEL_BUTTONS_CLOSE) {
				setProfessionsPanelActive(-1, false);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (professionsPanelMaxPages > 1 && iPanel == MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_UP) {
				if (professionsPanelPageIndex > 0) {
					professionsPanelPageIndex--;
				}
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (professionsPanelMaxPages > 1 && iPanel == MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_DOWN) {
				if ((professionsPanelPageIndex + 1) < professionsPanelMaxPages) {
					professionsPanelPageIndex++;
				}
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_PROFESSIONS_PANEL_BUTTONS_ITEMS) {
				// Ha clicado en un item, vamos a ver qué pasa
				if (menuProfessions == null) {
					return;
				}

				if (mouseButton == 1) { // Botón derecho (back al menú)
					if (menuProfessions.getParent() != null) {
						menuProfessions = menuProfessions.getParent();
						resizeProfessionsPanel(menuProfessions);
						recheckProfessionsPanelPages();
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					} else {
						setProfessionsPanelActive(-1, false);
					}
					return;
				}

				// Botón izquierdo
				Point p = isMouseOnProfessionsButtons(x, y);
				if (p != null && p.y < menuProfessions.getItems().size()) {
					SmartMenu menuAux = menuProfessions.getItems().get(p.y);
					if (menuAux.getType() == SmartMenu.TYPE_MENU) {
						menuProfessions = menuAux;
						resizeProfessionsPanel(menuProfessions);
						recheckProfessionsPanelPages();
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
						return;
					}
					if (menuAux.getCommand().equals(CommandPanel.COMMAND_BACK)) {
						// Back
						if (menuProfessions.getParent() != null) {
							menuProfessions = menuProfessions.getParent();
							resizeProfessionsPanel(menuProfessions);
							recheckProfessionsPanelPages();
							UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
						} else {
							setProfessionsPanelActive(-1, false);
						}
					} else {
						CommandPanel.executeCommand(menuAux.getCommand(), menuAux.getParameter(),
								menuAux.getParameter2(), menuAux.getDirectCoordinates(), null,
								SmartMenu.ICON_TYPE_ITEM);
						// Regeneramos el menu
						if (professionsPanelIsCitizen) {
							if (!ActionPriorityManager.regenerateProfessionsPanelMenu(menuProfessions,
									professionsPanelCitizenOrGroupIDActive)) {
								setProfessionsPanelActive(-1, false);
							}
						} else {
							if (!ActionPriorityManager.regenerateJobGroupPanelMenu(menuProfessions,
									professionsPanelCitizenOrGroupIDActive)) {
								setProfessionsPanelActive(-1, false);
							}
						}

						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					}

					return;
				}
			}

			if (iPanel == MOUSE_PROFESSIONS_PANEL) {
				if (mouseButton == 1) { // Botón derecho (cerramos o tiramos 1 atrás el menú)
					if (menuProfessions != null) {
						if (menuProfessions.getParent() != null) {
							menuProfessions = menuProfessions.getParent();
							resizePilePanel(menuProfessions);
							recheckProfessionsPanelPages();
						} else {
							setProfessionsPanelActive(-1, false);
						}
					} else {
						setProfessionsPanelActive(-1, false);
					}
				}
				return;
			}
		}

		/*
		 * PILE PANEL
		 */
		if (isPilePanelActive()) {
			if (iPanel == MOUSE_PILE_PANEL_BUTTONS_CLOSE) {
				setPilePanelActive(-1, false);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (pilePanelMaxPages > 1 && iPanel == MOUSE_PILE_PANEL_BUTTONS_SCROLL_UP) {
				if (pilePanelPageIndex > 0) {
					pilePanelPageIndex--;
				}
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (pilePanelMaxPages > 1 && iPanel == MOUSE_PILE_PANEL_BUTTONS_SCROLL_DOWN) {
				if ((pilePanelPageIndex + 1) < pilePanelMaxPages) {
					pilePanelPageIndex++;
				}
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_PILE_PANEL_BUTTONS_CONFIG_COPY) {
				if (menuPile == null) {
					return;
				}

				if (pilePanelIsContainer) {
					CommandPanel.executeCommand(CommandPanel.COMMAND_CONTAINER_COPY_TO_ALL, menuPile.getParameter(),
							null, null, null, -1);
				} else {
					CommandPanel.executeCommand(CommandPanel.COMMAND_STOCKPILE_COPY_TO_ALL, menuPile.getParameter(),
							null, null, null, -1);
				}
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK) {
				if (menuPile == null) {
					return;
				}

				if (pilePanelIsContainer) {
					Container container = Game.getWorld().getContainer(Integer.parseInt(menuPile.getParameter()));
					if (container != null) {
						container.setLockedToCopy(!container.isLockedToCopy());
						pilePanelIsLocked = container.isLockedToCopy();
					}
				} else {
					Stockpile pile = Stockpile.getStockpile(menuPile.getParameter());
					if (pile != null) {
						pile.setLockedToCopy(!pile.isLockedToCopy());
						pilePanelIsLocked = pile.isLockedToCopy();
					}
				}

				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_ALL) {
				if (menuPile == null) {
					return;
				}

				if (pilePanelIsContainer) {
					Container.lockUnlockAllConfigurations(true);

					Container container = Game.getWorld().getContainer(Integer.parseInt(menuPile.getParameter()));
					if (container != null) {
						pilePanelIsLocked = container.isLockedToCopy();
					}
				} else {
					Stockpile.lockUnlockAllConfigurations(true);

					Stockpile pile = Stockpile.getStockpile(menuPile.getParameter());
					if (pile != null) {
						pilePanelIsLocked = pile.isLockedToCopy();
					}
				}

				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_PILE_PANEL_BUTTONS_CONFIG_UNLOCK_ALL) {
				if (menuPile == null) {
					return;
				}

				if (pilePanelIsContainer) {
					Container.lockUnlockAllConfigurations(false);

					Container container = Game.getWorld().getContainer(Integer.parseInt(menuPile.getParameter()));
					if (container != null) {
						pilePanelIsLocked = container.isLockedToCopy();
					}
				} else {
					Stockpile.lockUnlockAllConfigurations(false);

					Stockpile pile = Stockpile.getStockpile(menuPile.getParameter());
					if (pile != null) {
						pilePanelIsLocked = pile.isLockedToCopy();
					}
				}

				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_PILE_PANEL_BUTTONS_ITEMS) {
				// Ha clicado en un item, vamos a ver qué pasa
				if (menuPile == null) {
					return;
				}

				if (mouseButton == 1) { // Botón derecho (back al menú)
					if (menuPile.getParent() != null) {
						menuPile = menuPile.getParent();
						resizePilePanel(menuPile);
						recheckPilePanelPages();
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					} else {
						setPilePanelActive(-1, false);
					}
					return;
				}

				// Botón izquierdo
				Point p = isMouseOnPileButtons(x, y);
				if (p != null && p.y < menuPile.getItems().size()) {
					SmartMenu menuAux = menuPile.getItems().get(p.y);
					if (menuAux.getType() == SmartMenu.TYPE_MENU) {
						menuPile = menuAux;
						resizePilePanel(menuPile);
						recheckPilePanelPages();
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
						return;
					}
					if (menuAux.getCommand().equals(CommandPanel.COMMAND_BACK)) {
						// Back
						if (menuPile.getParent() != null) {
							menuPile = menuPile.getParent();
							resizePilePanel(menuPile);
							recheckPilePanelPages();
							UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
						} else {
							setPilePanelActive(-1, false);
						}
					} else {
						CommandPanel.executeCommand(menuAux.getCommand(), menuAux.getParameter(),
								menuAux.getParameter2(), menuAux.getDirectCoordinates(), null,
								SmartMenu.ICON_TYPE_ITEM);
						// Regeneramos el menu
						if (isPilePanelIsContainer()) {
							// Container
							if (!Container.regenerateContainerPanelMenu(pilePanelPileContainerIDActive, menuPile)) {
								setPilePanelActive(-1, false);
							}
						} else {
							// Pila
							if (!Stockpile.regeneratePilePanelMenu(pilePanelPileContainerIDActive, menuPile)) {
								setPilePanelActive(-1, false);
							}
						}

						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					}

					return;
				}
			}

			if (iPanel == MOUSE_PILE_PANEL) {
				if (mouseButton == 1) { // Botón derecho (cerramos o tiramos 1 atrás el menú)
					if (menuPile != null) {
						if (menuPile.getParent() != null) {
							menuPile = menuPile.getParent();
							resizePilePanel(menuPile);
							recheckPilePanelPages();
						} else {
							setPilePanelActive(-1, false);
						}
					} else {
						setPilePanelActive(-1, false);
					}
				}
				return;
			}
		}

		/*
		 * MESSAGES PANEL
		 */
		if (isMessagesPanelActive()) {
			int iMessagesType = getMessagesPanelActive();
			if (iPanel == MOUSE_MESSAGES_PANEL_BUTTONS_CLOSE) {
				setMessagesPanelActive(-1);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_MESSAGES_PANEL_BUTTONS_ANNOUNCEMENT) {
				setMessagesPanelActive(MessagesPanel.TYPE_ANNOUNCEMENT);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_MESSAGES_PANEL_BUTTONS_COMBAT) {
				setMessagesPanelActive(MessagesPanel.TYPE_COMBAT);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_MESSAGES_PANEL_BUTTONS_HEROES) {
				setMessagesPanelActive(MessagesPanel.TYPE_HEROES);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_MESSAGES_PANEL_BUTTONS_SYSTEM) {
				setMessagesPanelActive(MessagesPanel.TYPE_SYSTEM);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_UP) {
				if (MessagesPanel.getPages(iMessagesType) > 1 && MessagesPanel.getPagesCurrent(iMessagesType) > 1) {
					MessagesPanel.pageUp(iMessagesType);
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_DOWN) {
				if (MessagesPanel.getPages(iMessagesType) > 1
						&& MessagesPanel.getPagesCurrent(iMessagesType) < MessagesPanel.getPages(iMessagesType)) {
					MessagesPanel.pageDown(iMessagesType);
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_MESSAGES_PANEL) {
				if (mouseButton == 1) { // Botón derecho (cerramos)
					setMessagesPanelActive(-1);
				} else {
					if (MessagesPanel.mousePressed(x, y, getMessagesPanelActive(),
							messagesPanelSubPanelPoint.x + tileMessagesPanel[3].getTileWidth(),
							messagesPanelSubPanelPoint.y + tileMessagesPanel[1].getTileHeight())) {
						setMessagesPanelActive(-1);
					}
				}
				return;
			}
		}

		/*
		 * MATS PANEL
		 */
		if (isMatsPanelActive()) {
			if (iPanel == MOUSE_MATS_PANEL_BUTTONS_CLOSE) {
				setMatsPanelActive(false);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_MATS_PANEL_BUTTONS_GROUPS) {
				Point p = isMouseOnMatsButtons(x, y);
				if (p != null) {
					setMatsPanelActive(p.y);
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_MATS_PANEL_BUTTONS_SCROLL_UP) {
				if (matsIndexPages[getMatsPanelActive()] > 0) {
					matsIndexPages[getMatsPanelActive()]--;
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_MATS_PANEL_BUTTONS_SCROLL_DOWN) {
				if (matsIndexPages[getMatsPanelActive()] < (matsNumPages[getMatsPanelActive()] - 1)) {
					matsIndexPages[getMatsPanelActive()]++;
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			}

			if (iPanel == MOUSE_MATS_PANEL) {
				if (mouseButton == 1) { // Botón derecho (cerramos)
					setMatsPanelActive(false);
				}
				return;
			}
		}

		/*
		 * LIVINGS PANEL
		 */
		if (isLivingsPanelActive()) {
			if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_CLOSE) {
				setLivingsPanelActive(LIVINGS_PANEL_TYPE_NONE, livingsPanelSoldiersGroupActive,
						livingsPanelCitizensGroupActive);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_UP) {
				if (!checkGroupsPanelEnabled(getLivingsPanelActive())) {
					if (livingsDataIndexPages[getLivingsPanelActive()] > 1) {
						livingsDataIndexPages[getLivingsPanelActive()]--;
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
						return;
					}
				} else {
					if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
						if (livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive] > 1) {
							livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive]--;
							UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
							return;
						}
					} else {
						if (livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive] > 1) {
							livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive]--;
							UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
							return;
						}
					}
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_DOWN) {
				int iNumLivings;
				ArrayList<Integer> alLivings = getLivings();
				if (alLivings != null) {
					iNumLivings = alLivings.size();
				} else {
					iNumLivings = 0;
				}
				if (iNumLivings > 0) {
					int iNumPages = (iNumLivings % LIVINGS_PANEL_MAX_ROWS == 0) ? iNumLivings / LIVINGS_PANEL_MAX_ROWS
							: (iNumLivings / LIVINGS_PANEL_MAX_ROWS) + 1;

					// Scrolls
					if (!checkGroupsPanelEnabled(getLivingsPanelActive())) {
						if (livingsDataIndexPages[getLivingsPanelActive()] < iNumPages) {
							livingsDataIndexPages[getLivingsPanelActive()]++;
							UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
							return;
						}
					} else {
						if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
							if (livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive] < iNumPages) {
								livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive]++;
								UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
								return;
							}
						} else {
							if (livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive] < iNumPages) {
								livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive]++;
								UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
								return;
							}
						}
					}
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP
					&& getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS
					&& livingsPanelCitizensGroupActive == -1) {
				Game.getWorld().substractRestrictHaulEquippingLevel();
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				// Tutorial flow
				Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_RESTRICTION,
						null);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN
					&& getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS
					&& livingsPanelCitizensGroupActive == -1) {
				Game.getWorld().addRestrictHaulEquippingLevel();
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				// Tutorial flow
				Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_RESTRICTION,
						null);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP
					&& getLivingsPanelActive() == LIVINGS_PANEL_TYPE_HEROES) {
				Game.getWorld().substractRestrictExploringLevel();
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				// Tutorial flow
				Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_RESTRICTION,
						null);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN
					&& getLivingsPanelActive() == LIVINGS_PANEL_TYPE_HEROES) {
				Game.getWorld().addRestrictExploringLevel();
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				// Tutorial flow
				Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_RESTRICTION,
						null);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_REMOVE) {
				if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS && livingsPanelSoldiersGroupActive != -1) {
					Point p = isMouseOnLivingsButtons(x, y);
					if (p != null) {
						ArrayList<Integer> alLivings = getLivings();
						int iIndex = getLivingsIndex();
						if (alLivings != null && p != null && (p.y + iIndex) >= 0
								&& (p.y + iIndex) < alLivings.size()) {
							LivingEntity le = World.getLivingEntityByID(alLivings.get((p.y + iIndex)));
							if (le != null) {
								Citizen soldier = (Citizen) le;
								soldier.getSoldierData().setState(SoldierData.STATE_GUARD, -1, le.getID());
								UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
								return;
							}
						}
					}
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_ADD) {
				if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS && livingsPanelSoldiersGroupActive == -1) {
					Point p = isMouseOnLivingsButtons(x, y);
					if (p != null) {
						ArrayList<Integer> alLivings = getLivings();
						int iIndex = getLivingsIndex();
						if (alLivings != null && p != null && (p.y + iIndex) >= 0
								&& (p.y + iIndex) < alLivings.size()) {
							LivingEntity le = World.getLivingEntityByID(alLivings.get((p.y + iIndex)));
							if (le != null) {
								Citizen soldier = (Citizen) le;
								if (Game.getCurrentState() == Game.STATE_CREATING_TASK) {
									Game.deleteCurrentTask();
								}
								ContextMenu menu = new ContextMenu();
								SmartMenu sm = new SmartMenu();

								SoldierGroupData sgd;
								for (int g = 0; g < SoldierGroups.MAX_GROUPS; g++) {
									// Añadir a grupos existentes
									sgd = Game.getWorld().getSoldierGroups().getGroup(g);
									if (soldier.getSoldierData().getState() != SoldierData.STATE_IN_A_GROUP
											|| soldier.getSoldierData().getGroup() != sgd.getId()) {
										sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, sgd.getName(), null,
												CommandPanel.COMMAND_SOLDIER_SET_STATE,
												Integer.toString(soldier.getID()),
												Integer.toString(SoldierData.STATE_IN_A_GROUP),
												new Point3D(sgd.getId(), -1, -1)));
									} else {
										sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, sgd.getName(), null, null, null,
												null, null, Color.GRAY));
									}
								}

								menu.setSmartMenu(sm);
								menu.setX(x + 16 + -menu.getWidth() / 2);
								menu.setY(y + 32);
								menu.resize();
								Game.setContextMenu(menu);
								UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
								// Tutorial flow
								Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT,
										TutorialTrigger.ICON_INT_LIVINGS_GROUPS, null);
								return;
							}
						}
					}
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_JOBS_GROUPS_ADDREMOVE) {
				if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
					Point p = isMouseOnLivingsButtons(x, y);
					if (p != null) {
						ArrayList<Integer> alLivings = getLivings();
						int iIndex = getLivingsIndex();
						if (alLivings != null && p != null && (p.y + iIndex) >= 0
								&& (p.y + iIndex) < alLivings.size()) {
							LivingEntity le = World.getLivingEntityByID(alLivings.get((p.y + iIndex)));
							if (le != null) {
								Citizen citizen = (Citizen) le;
								if (Game.getCurrentState() == Game.STATE_CREATING_TASK) {
									Game.deleteCurrentTask();
								}
								ContextMenu menu = new ContextMenu();
								SmartMenu sm = new SmartMenu();

								CitizenGroupData cgd;
								for (int g = 0; g < CitizenGroups.MAX_GROUPS; g++) {
									// Añadir a grupos existentes
									cgd = Game.getWorld().getCitizenGroups().getGroup(g);
									if (citizen.getCitizenData().getGroupID() != g) {
										sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM,
												Messages.getString("UIPanel.70") + cgd.getName(), null, //$NON-NLS-1$
												CommandPanel.COMMAND_CITIZEN_SET_JOB_GROUP,
												Integer.toString(citizen.getID()), Integer.toString(g), null,
												Color.GREEN));
									} else {
										sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM,
												Messages.getString("UIPanel.71") + cgd.getName(), null, //$NON-NLS-1$
												CommandPanel.COMMAND_CITIZEN_SET_JOB_GROUP,
												Integer.toString(citizen.getID()), Integer.toString(-1), null,
												Color.ORANGE));
									}
								}

								menu.setSmartMenu(sm);
								menu.setX(x + 16 + -menu.getWidth() / 2);
								menu.setY(y + 32);
								menu.resize();
								Game.setContextMenu(menu);
								UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
								// Tutorial flow
								Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT,
										TutorialTrigger.ICON_INT_LIVINGS_GROUPS, null);
								return;
							}
						}
					}
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SGROUP_NOGROUP) {
				livingsPanelSoldiersGroupActive = -1;
				createLivingsPanel(LIVINGS_PANEL_TYPE_SOLDIERS, -1, livingsPanelCitizensGroupActive);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SGROUP_GROUP) {
				Point p = isMouseOnLivingsButtons(x, y);
				if (p != null && p.y >= 0 && p.y < SoldierGroups.MAX_GROUPS) {
					livingsPanelSoldiersGroupActive = p.y;
					createLivingsPanel(LIVINGS_PANEL_TYPE_SOLDIERS, p.y, livingsPanelCitizensGroupActive);
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_CGROUP_NOGROUP) {
				livingsPanelCitizensGroupActive = -1;
				createLivingsPanel(LIVINGS_PANEL_TYPE_CITIZENS, livingsPanelSoldiersGroupActive, -1);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_CGROUP_GROUP) {
				Point p = isMouseOnLivingsButtons(x, y);
				if (p != null && p.y >= 0 && p.y < CitizenGroups.MAX_GROUPS) {
					livingsPanelCitizensGroupActive = p.y;
					createLivingsPanel(LIVINGS_PANEL_TYPE_CITIZENS, livingsPanelSoldiersGroupActive, p.y);
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_RENAME) {
				if (livingsPanelSoldiersGroupActive != -1) {
					typingPanel = new TypingPanel(renderWidth, renderHeight, Messages.getString("UIPanel.61"), //$NON-NLS-1$
							Game.getWorld().getSoldierGroups().getGroup(livingsPanelSoldiersGroupActive).getName(),
							TypingPanel.TYPE_RENAME_GROUP, livingsPanelSoldiersGroupActive);
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_RENAME) {
				if (livingsPanelCitizensGroupActive != -1) {
					typingPanel = new TypingPanel(renderWidth, renderHeight, Messages.getString("UIPanel.61"), //$NON-NLS-1$
							Game.getWorld().getCitizenGroups().getGroup(livingsPanelCitizensGroupActive).getName(),
							TypingPanel.TYPE_RENAME_JOB_GROUP, livingsPanelCitizensGroupActive);
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_GUARD) {
				if (livingsPanelSoldiersGroupActive != -1) {
					Game.getWorld().getSoldierGroups().getGroup(livingsPanelSoldiersGroupActive)
							.setState(SoldierGroupData.STATE_GUARD);
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_PATROL) {
				if (livingsPanelSoldiersGroupActive != -1) {
					Game.getWorld().getSoldierGroups().getGroup(livingsPanelSoldiersGroupActive)
							.setState(SoldierGroupData.STATE_PATROL);
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_BOSS) {
				if (livingsPanelSoldiersGroupActive != -1) {
					Game.getWorld().getSoldierGroups().getGroup(livingsPanelSoldiersGroupActive)
							.setState(SoldierGroupData.STATE_BOSS);
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_AUTOEQUIP) {
				if (livingsPanelSoldiersGroupActive != -1) {
					ArrayList<Integer> alSoldiers = Game.getWorld().getSoldierGroups()
							.getGroup(livingsPanelSoldiersGroupActive).getLivingIDs();
					LivingEntity le;
					for (int s = 0; s < alSoldiers.size(); s++) {
						le = World.getLivingEntityByID(alSoldiers.get(s));
						if (le != null) {
							CommandPanel.executeCommand(CommandPanel.COMMAND_AUTOEQUIP, Integer.toString(le.getID()),
									null, null, null, SmartMenu.ICON_TYPE_ITEM);
						}
					}
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_AUTOEQUIP) {
				ArrayList<Integer> alCitizens;
				if (livingsPanelCitizensGroupActive != -1) {
					alCitizens = Game.getWorld().getCitizenGroups().getGroup(livingsPanelCitizensGroupActive)
							.getLivingIDs();
				} else {
					alCitizens = Game.getWorld().getCitizenGroups().getCitizensWithoutGroup();
				}
				LivingEntity le;
				for (int s = 0; s < alCitizens.size(); s++) {
					le = World.getLivingEntityByID(alCitizens.get(s));
					if (le != null) {
						CommandPanel.executeCommand(CommandPanel.COMMAND_AUTOEQUIP, Integer.toString(le.getID()), null,
								null, null, SmartMenu.ICON_TYPE_ITEM);
					}
				}
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_DISBAND) {
				if (livingsPanelSoldiersGroupActive != -1) {
					ArrayList<Integer> alSoldiers = Game.getWorld().getSoldierGroups()
							.getGroup(livingsPanelSoldiersGroupActive).getLivingIDs();
					LivingEntity le;
					int iSize = alSoldiers.size();
					for (int s = 0; s < iSize; s++) {
						le = World.getLivingEntityByID(alSoldiers.get(0));
						if (le != null) {
							((Citizen) le).getSoldierData().setState(SoldierData.STATE_GUARD, -1, le.getID());
						}
					}
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_DISBAND) {
				ArrayList<Integer> alCitizens;
				if (livingsPanelCitizensGroupActive != -1) {
					alCitizens = Game.getWorld().getCitizenGroups().getGroup(livingsPanelCitizensGroupActive)
							.getLivingIDs();
				} else {
					alCitizens = Game.getWorld().getCitizenGroups().getCitizensWithoutGroup();
				}

				LivingEntity le;
				for (int s = (alCitizens.size() - 1); s >= 0; s--) {
					le = World.getLivingEntityByID(alCitizens.get(s));
					if (le != null) {
						CommandPanel.executeCommand(CommandPanel.COMMAND_CITIZEN_SET_JOB_GROUP,
								Integer.toString(le.getID()), Integer.toString(-1), null, null,
								SmartMenu.ICON_TYPE_ITEM);
					}
				}
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_CHANGE_JOBS) {
				setProfessionsPanelActive(livingsPanelCitizensGroupActive, false);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS || iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD
					|| iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY
					|| iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS
					|| iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET
					|| iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON
					|| iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS
					|| iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER
					|| iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN
					|| iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD
					|| iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL
					|| iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS
					|| iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP) {
				Point p = isMouseOnLivingsButtons(x, y);
				if (p != null) {
					ArrayList<Integer> alLivings = getLivings();
					int iIndex = getLivingsIndex();
					if (alLivings != null && p != null && (p.y + iIndex) >= 0 && (p.y + iIndex) < alLivings.size()) {
						LivingEntity le = World.getLivingEntityByID(alLivings.get((p.y + iIndex)));
						if (le != null) {
							if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS) {
								Game.getWorld().setView(le.getCoordinates());
								UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
								setLivingsPanelActive(LIVINGS_PANEL_TYPE_NONE, livingsPanelSoldiersGroupActive,
										livingsPanelCitizensGroupActive);

								// Tutorial flow
								Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT,
										TutorialTrigger.ICON_INT_LIVINGS_LIVINGS, null);
								return;
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS) {
								setProfessionsPanelActive(le.getID(), true);
								UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
								// Tutorial flow
								Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT,
										TutorialTrigger.ICON_INT_LIVINGS_JOBS, null);
								return;
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER) {
								CommandPanel.executeCommand(CommandPanel.COMMAND_CONVERT_TO_SOLDIER,
										Integer.toString(le.getID()), null, null, null, SmartMenu.ICON_TYPE_ITEM);
								UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
								// Tutorial flow
								Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT,
										TutorialTrigger.ICON_INT_LIVINGS_CONVERTSOLDIER, null);
								return;
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN) {
								CommandPanel.executeCommand(CommandPanel.COMMAND_CONVERT_TO_CIVILIAN,
										Integer.toString(le.getID()), null, null, null, SmartMenu.ICON_TYPE_ITEM);
								UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
								// Tutorial flow
								Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT,
										TutorialTrigger.ICON_INT_LIVINGS_CONVERTCIVILIAN, null);
								return;
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD) {
								CommandPanel.executeCommand(CommandPanel.COMMAND_SOLDIER_SET_STATE,
										Integer.toString(le.getID()), Integer.toString(SoldierData.STATE_GUARD),
										new Point3D(-1, -1, -1), null, SmartMenu.ICON_TYPE_ITEM);
								UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
								return;
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL) {
								CommandPanel.executeCommand(CommandPanel.COMMAND_SOLDIER_SET_STATE,
										Integer.toString(le.getID()), Integer.toString(SoldierData.STATE_PATROL),
										new Point3D(-1, -1, -1), null, SmartMenu.ICON_TYPE_ITEM);
								UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
								return;
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS) {
								CommandPanel.executeCommand(CommandPanel.COMMAND_SOLDIER_SET_STATE,
										Integer.toString(le.getID()), Integer.toString(SoldierData.STATE_BOSS_AROUND),
										new Point3D(-1, -1, -1), null, SmartMenu.ICON_TYPE_ITEM);
								UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
								return;
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD) {
								if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS
										|| getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
									MilitaryItem mi = le.getEquippedData().getHead();
									SmartMenu smUnequip = null;
									if (mi != null) {
										smUnequip = new SmartMenu(SmartMenu.TYPE_ITEM,
												Messages.getString("Citizen.20") //$NON-NLS-1$
														+ le.getEquippedData().getHead().getExtendedTilename(),
												null, CommandPanel.COMMAND_WEAR_OFF, null, null,
												new Point3D(le.getID(), MilitaryItem.LOCATION_HEAD, -1),
												le.getEquippedData().getHead().getItemTextColor());
									}
									UIPanel.createMilitaryContextMenu(smUnequip, MilitaryItem.LOCATION_HEAD, le, x, y);
									UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
									// Tutorial flow
									Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT,
											TutorialTrigger.ICON_INT_LIVINGS_BODY, null);
									return;
								}
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY) {
								if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS
										|| getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
									MilitaryItem mi = le.getEquippedData().getBody();
									SmartMenu smUnequip = null;
									if (mi != null) {
										smUnequip = new SmartMenu(SmartMenu.TYPE_ITEM,
												Messages.getString("Citizen.20") //$NON-NLS-1$
														+ le.getEquippedData().getBody().getExtendedTilename(),
												null, CommandPanel.COMMAND_WEAR_OFF, null, null,
												new Point3D(le.getID(), MilitaryItem.LOCATION_BODY, -1),
												le.getEquippedData().getBody().getItemTextColor());
									}
									createMilitaryContextMenu(smUnequip, MilitaryItem.LOCATION_BODY, le, x, y);
									UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
									// Tutorial flow
									Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT,
											TutorialTrigger.ICON_INT_LIVINGS_BODY, null);
									return;
								}
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS) {
								if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS
										|| getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
									MilitaryItem mi = le.getEquippedData().getLegs();
									SmartMenu smUnequip = null;
									if (mi != null) {
										smUnequip = new SmartMenu(SmartMenu.TYPE_ITEM,
												Messages.getString("Citizen.20") //$NON-NLS-1$
														+ le.getEquippedData().getLegs().getExtendedTilename(),
												null, CommandPanel.COMMAND_WEAR_OFF, null, null,
												new Point3D(le.getID(), MilitaryItem.LOCATION_LEGS, -1),
												le.getEquippedData().getLegs().getItemTextColor());
									}
									createMilitaryContextMenu(smUnequip, MilitaryItem.LOCATION_LEGS, le, x, y);
									UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
									// Tutorial flow
									Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT,
											TutorialTrigger.ICON_INT_LIVINGS_BODY, null);
									return;
								}
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET) {
								if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS
										|| getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
									MilitaryItem mi = le.getEquippedData().getFeet();
									SmartMenu smUnequip = null;
									if (mi != null) {
										smUnequip = new SmartMenu(SmartMenu.TYPE_ITEM,
												Messages.getString("Citizen.20") //$NON-NLS-1$
														+ le.getEquippedData().getFeet().getExtendedTilename(),
												null, CommandPanel.COMMAND_WEAR_OFF, null, null,
												new Point3D(le.getID(), MilitaryItem.LOCATION_FEET, -1),
												le.getEquippedData().getFeet().getItemTextColor());
									}
									createMilitaryContextMenu(smUnequip, MilitaryItem.LOCATION_FEET, le, x, y);
									UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
									// Tutorial flow
									Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT,
											TutorialTrigger.ICON_INT_LIVINGS_BODY, null);
									return;
								}
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON) {
								if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS
										|| getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
									MilitaryItem mi = le.getEquippedData().getWeapon();
									SmartMenu smUnequip = null;
									if (mi != null) {
										smUnequip = new SmartMenu(SmartMenu.TYPE_ITEM,
												Messages.getString("Citizen.20") //$NON-NLS-1$
														+ le.getEquippedData().getWeapon().getExtendedTilename(),
												null, CommandPanel.COMMAND_WEAR_OFF, null, null,
												new Point3D(le.getID(), MilitaryItem.LOCATION_WEAPON, -1),
												le.getEquippedData().getWeapon().getItemTextColor());
									}
									createMilitaryContextMenu(smUnequip, MilitaryItem.LOCATION_WEAPON, le, x, y);
									UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
									// Tutorial flow
									Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT,
											TutorialTrigger.ICON_INT_LIVINGS_BODY, null);
									return;
								}
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP) {
								if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS
										|| getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
									CommandPanel.executeCommand(CommandPanel.COMMAND_AUTOEQUIP,
											Integer.toString(le.getID()), null, null, null, SmartMenu.ICON_TYPE_ITEM);
									UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
									// Tutorial flow
									Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT,
											TutorialTrigger.ICON_INT_LIVINGS_AUTOEQUIP, null);
									return;
								}
							}
						}
					}
				}
			}

			if (iPanel == MOUSE_LIVINGS_PANEL) {
				if (mouseButton == 1) { // Botón derecho (cerramos)
					setLivingsPanelActive(LIVINGS_PANEL_TYPE_NONE, livingsPanelSoldiersGroupActive,
							livingsPanelCitizensGroupActive);
				}
				return;
			}
		}

		/*
		 * TRADE PANEL
		 */
		if (isTradePanelActive()) {
			if (iPanel == MOUSE_TRADE_PANEL_BUTTONS_CLOSE) {
				setTradePanelActive(false);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				return;
			}

			if (tradePanel != null) {
				CaravanData caravanData = Game.getWorld().getCurrentCaravanData();
				boolean bTrading = (caravanData != null && caravanData.getStatus() == CaravanData.STATUS_TRADING);

				if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_DOWN_CARAVAN) {
					tradePanel.scrollDownCaravan();
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				} else if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_UP_CARAVAN) {
					tradePanel.scrollUpCaravan();
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				} else if (iPanel == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_DOWN_CARAVAN) {
					tradePanel.scrollDownToBuyCaravan();
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				} else if (iPanel == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_UP_CARAVAN) {
					tradePanel.scrollUpToBuyCaravan();
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				} else if (iPanel == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_DOWN_TOWN) {
					tradePanel.scrollDownToSellTown();
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				} else if (iPanel == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_UP_TOWN) {
					tradePanel.scrollUpToSellTown();
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				} else if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_DOWN_TOWN) {
					tradePanel.scrollDownTown();
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				} else if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_UP_TOWN) {
					tradePanel.scrollUpTown();
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				} else if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_CARAVAN) {
					Point p = isMouseOnTradeButtons(x, y);
					if (p != null) {
						tradePanel.selectItemToBuy(p.y + tradePanel.getIndexButtonsCaravan());
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
						return;
					}
				} else if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN) {
					Point p = isMouseOnTradeButtons(x, y);
					if (p != null) {
						tradePanel.selectItemToNonBuy(p.y + tradePanel.getIndexButtonsToBuyCaravan());
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
						return;
					}
				} else if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_TOWN) {
					Point p = isMouseOnTradeButtons(x, y);
					if (p != null) {
						tradePanel.selectItemToSell(p.y + tradePanel.getIndexButtonsTown());
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
						return;
					}
				} else if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN) {
					Point p = isMouseOnTradeButtons(x, y);
					if (p != null) {
						tradePanel.selectItemToNonSell(p.y + tradePanel.getIndexButtonsToSellTown());
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
						return;
					}
				} else if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_CONFIRM) {
					if (tradePanel.isTransactionReady()) {
						if (Game.getWorld().getCurrentCaravanData() != null) {
							Game.getWorld().getCurrentCaravanData().confirmTrade();
							UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
							return;
						}
					}
				}
			}
			if (iPanel == MOUSE_TRADE_PANEL) {
				if (mouseButton == 1) { // Botón derecho (cerramos)
					setTradePanelActive(false);
				}
				return;
			}
		}

		/*
		 * PRIORITIES PANEL
		 */
		if (isPrioritiesPanelActive()) {
			if (iPanel == MOUSE_PRIORITIES_PANEL_ITEMS_DOWN) {
				Point p = isMouseOnPrioritiesItems(x, y);
				if (p != null) {
					if (p.x == MOUSE_PRIORITIES_PANEL_ITEMS_DOWN) {
						ActionPriorityManager.swapPriorities(p.y, p.y + 1);
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
						return;
					}
				}
			} else if (iPanel == MOUSE_PRIORITIES_PANEL_ITEMS_UP) {
				Point p = isMouseOnPrioritiesItems(x, y);
				if (p != null) {
					if (p.x == MOUSE_PRIORITIES_PANEL_ITEMS_UP) {
						ActionPriorityManager.swapPriorities(p.y, p.y - 1);
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
						return;
					}
				}
			} else if (iPanel == MOUSE_PRIORITIES_PANEL_ITEMS) {
				Point p = isMouseOnPrioritiesItems(x, y);
				if (p != null && p.y == (PRIORITIES_PANEL_NUM_ITEMS - 1)) {
					setPrioritiesPanelActive(false);
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_PRIORITIES_PANEL) {
				if (mouseButton == 1) { // Botón derecho (cerramos)
					setPrioritiesPanelActive(false);
				}
				return;
			}
		}

		/*
		 * PRODUCTION PANEL
		 */
		if (iPanel == MOUSE_PRODUCTION_OPENCLOSE) {
			setProductionPanelLocked(!isProductionPanelLocked());
			//  setProductionPanelActive (!isProductionPanelActive ());
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (isProductionPanelActive()) {
			if (iPanel == MOUSE_PRODUCTION_PANEL_ITEMS) {
				if (mouseButton == 1) { // Botón derecho (back al menú)
					if (productionPanelMenu.getParent() != null) {
						productionPanelMenu = productionPanelMenu.getParent();
						createProductionPanel(productionPanelMenu);
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					} else {
						setProductionPanelLocked(false);
					}
					return;
				}
				Point p = isMouseOnProductionItems(x, y);
				if (p != null) {
					if (p.x == MOUSE_PRODUCTION_PANEL_ITEMS) {
						SmartMenu smItem = productionPanelMenu.getItems().get(p.y);
						if (smItem.getType() == SmartMenu.TYPE_MENU) {
							productionPanelMenu = smItem;
							createProductionPanel(smItem);
						} else if (smItem.getType() == SmartMenu.TYPE_ITEM) {
							if (!smItem.getCommand().equalsIgnoreCase(CommandPanel.COMMAND_BACK)) {
								CommandPanel.executeCommand(smItem.getCommand(), smItem.getParameter(),
										smItem.getParameter2(), smItem.getDirectCoordinates(), smItem.getIcon(),
										smItem.getIconType());

								if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
										|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
									for (int rep = 0; rep < 99; rep++) {
										CommandPanel.executeCommand(smItem.getCommand(), smItem.getParameter(),
												smItem.getParameter2(), smItem.getDirectCoordinates(), smItem.getIcon(),
												smItem.getIconType());
									}
								} else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
										|| Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
									CommandPanel.executeCommand(smItem.getCommand(), smItem.getParameter(),
											smItem.getParameter2(), smItem.getDirectCoordinates(), smItem.getIcon(),
											smItem.getIconType());
									CommandPanel.executeCommand(smItem.getCommand(), smItem.getParameter(),
											smItem.getParameter2(), smItem.getDirectCoordinates(), smItem.getIcon(),
											smItem.getIconType());
									CommandPanel.executeCommand(smItem.getCommand(), smItem.getParameter(),
											smItem.getParameter2(), smItem.getDirectCoordinates(), smItem.getIcon(),
											smItem.getIconType());
									CommandPanel.executeCommand(smItem.getCommand(), smItem.getParameter(),
											smItem.getParameter2(), smItem.getDirectCoordinates(), smItem.getIcon(),
											smItem.getIconType());
								}
							} else {
								if (productionPanelMenu.getParent() != null) {
									productionPanelMenu = productionPanelMenu.getParent();
									createProductionPanel(productionPanelMenu);
								} else {
									setProductionPanelActive(false);
								}
							}
						}
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					}
				}
				return;
			} else if (iPanel == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR) {
				Point p = isMouseOnProductionItems(x, y);
				if (p != null) {
					if (p.x == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR) {
						SmartMenu smItem = productionPanelMenu.getItems().get(p.y);
						if (smItem.getType() == SmartMenu.TYPE_ITEM) {
							if (!smItem.getCommand().equalsIgnoreCase(CommandPanel.COMMAND_BACK)) {
								CommandPanel.executeCommand(smItem.getCommand(), smItem.getParameter(),
										smItem.getParameter2(), smItem.getDirectCoordinates(), smItem.getIcon(),
										smItem.getIconType());
								// Tutorial flow
								Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT,
										TutorialTrigger.ICON_INT_REGULAR_PLUS, null, smItem.getParameter());

								if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
										|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
									for (int rep = 0; rep < 99; rep++) {
										CommandPanel.executeCommand(smItem.getCommand(), smItem.getParameter(),
												smItem.getParameter2(), smItem.getDirectCoordinates(), smItem.getIcon(),
												smItem.getIconType());
									}
								} else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
										|| Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
									CommandPanel.executeCommand(smItem.getCommand(), smItem.getParameter(),
											smItem.getParameter2(), smItem.getDirectCoordinates(), smItem.getIcon(),
											smItem.getIconType());
									CommandPanel.executeCommand(smItem.getCommand(), smItem.getParameter(),
											smItem.getParameter2(), smItem.getDirectCoordinates(), smItem.getIcon(),
											smItem.getIconType());
									CommandPanel.executeCommand(smItem.getCommand(), smItem.getParameter(),
											smItem.getParameter2(), smItem.getDirectCoordinates(), smItem.getIcon(),
											smItem.getIconType());
									CommandPanel.executeCommand(smItem.getCommand(), smItem.getParameter(),
											smItem.getParameter2(), smItem.getDirectCoordinates(), smItem.getIcon(),
											smItem.getIconType());
								}
							}
						}
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					}
				}
			} else if (iPanel == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR) {
				Point p = isMouseOnProductionItems(x, y);
				if (p != null) {
					if (p.x == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR) {
						SmartMenu smItem = productionPanelMenu.getItems().get(p.y);
						if (smItem.getType() == SmartMenu.TYPE_ITEM) {
							if (!smItem.getCommand().equalsIgnoreCase(CommandPanel.COMMAND_BACK)) {
								Game.getWorld().getTaskManager().removeFromQueue(smItem.getParameter());
								// Tutorial flow
								Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT,
										TutorialTrigger.ICON_INT_REGULAR_MINUS, null, smItem.getParameter());

								if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
										|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
									for (int rep = 0; rep < 99; rep++) {
										Game.getWorld().getTaskManager().removeFromQueue(smItem.getParameter());
									}
								} else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
										|| Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
									Game.getWorld().getTaskManager().removeFromQueue(smItem.getParameter());
									Game.getWorld().getTaskManager().removeFromQueue(smItem.getParameter());
									Game.getWorld().getTaskManager().removeFromQueue(smItem.getParameter());
									Game.getWorld().getTaskManager().removeFromQueue(smItem.getParameter());
								}
							}
						}
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					}
				}
			} else if (iPanel == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED) {
				Point p = isMouseOnProductionItems(x, y);
				if (p != null) {
					if (p.x == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED) {
						SmartMenu smItem = productionPanelMenu.getItems().get(p.y);
						if (smItem.getType() == SmartMenu.TYPE_ITEM) {
							if (!smItem.getCommand().equalsIgnoreCase(CommandPanel.COMMAND_BACK)) {
								Game.getWorld().getTaskManager().addItemOnAutomatedQueue(smItem.getParameter());
								// Tutorial flow
								Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT,
										TutorialTrigger.ICON_INT_AUTOMATED_PLUS, null, smItem.getParameter());

								if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
										|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
									for (int rep = 0; rep < 99; rep++) {
										Game.getWorld().getTaskManager().addItemOnAutomatedQueue(smItem.getParameter());
									}
								} else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
										|| Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
									Game.getWorld().getTaskManager().addItemOnAutomatedQueue(smItem.getParameter());
									Game.getWorld().getTaskManager().addItemOnAutomatedQueue(smItem.getParameter());
									Game.getWorld().getTaskManager().addItemOnAutomatedQueue(smItem.getParameter());
									Game.getWorld().getTaskManager().addItemOnAutomatedQueue(smItem.getParameter());
								}
								UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
							}
						}
					}
				}
			} else if (iPanel == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED) {
				Point p = isMouseOnProductionItems(x, y);
				if (p != null) {
					if (p.x == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED) {
						SmartMenu smItem = productionPanelMenu.getItems().get(p.y);
						if (smItem.getType() == SmartMenu.TYPE_ITEM) {
							if (!smItem.getCommand().equalsIgnoreCase(CommandPanel.COMMAND_BACK)) {
								Game.getWorld().getTaskManager().removeItemOnAutomatedQueue(smItem.getParameter());
								// Tutorial flow
								Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT,
										TutorialTrigger.ICON_INT_AUTOMATED_MINUS, null, smItem.getParameter());

								if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
										|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
									for (int rep = 0; rep < 99; rep++) {
										Game.getWorld().getTaskManager()
												.removeItemOnAutomatedQueue(smItem.getParameter());
									}
								} else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
										|| Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
									Game.getWorld().getTaskManager().removeItemOnAutomatedQueue(smItem.getParameter());
									Game.getWorld().getTaskManager().removeItemOnAutomatedQueue(smItem.getParameter());
									Game.getWorld().getTaskManager().removeItemOnAutomatedQueue(smItem.getParameter());
									Game.getWorld().getTaskManager().removeItemOnAutomatedQueue(smItem.getParameter());
								}
								UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
							}
						}
					}
				}
			}
			if (iPanel == MOUSE_PRODUCTION_PANEL) {
				if (mouseButton == 1) { // Botón derecho (back al menú)
					if (productionPanelMenu.getParent() != null) {
						productionPanelMenu = productionPanelMenu.getParent();
						createProductionPanel(productionPanelMenu);
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					} else {
						setProductionPanelLocked(false);
					}
				}
				return;
			}
		}

		// BOTTOM
		if (iPanel == MOUSE_BOTTOM_OPENCLOSE) {
			setBottomMenuPanelLocked(!isBottomMenuPanelLocked());
			// setProductionPanelActive (!isProductionPanelActive ());
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (isBottomMenuPanelActive()) {
			if (iPanel == MOUSE_BOTTOM_LEFT_SCROLL) {
				if (bottomPanelItemIndex > 0) {
					bottomPanelItemIndex--;
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				}
				return;
			}
			if (iPanel == MOUSE_BOTTOM_RIGHT_SCROLL) {
				if ((bottomPanelItemIndex + BOTTOM_PANEL_NUM_ITEMS) < currentMenu.getItems().size()) {
					bottomPanelItemIndex++;
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				}
				return;
			}
			if (iPanel == MOUSE_BOTTOM_ITEMS) {
				int iItem = isMouseOnBottomItems(x, y);
				if (iItem != -1) {
					iItem = iItem + bottomPanelItemIndex;
					SmartMenu smItem = currentMenu.getItems().get(iItem);
					if (smItem.getType() == SmartMenu.TYPE_MENU && smItem.getItems() != null
							&& smItem.getItems().size() > 0) {
						// Activamos el subpanel de abajo
						bottomSubPanelMenu = smItem;
						createBottomSubPanel(smItem);
					} else if (smItem.getType() == SmartMenu.TYPE_ITEM) {
						CommandPanel.executeCommand(smItem.getCommand(), smItem.getParameter(), smItem.getParameter2(),
								smItem.getDirectCoordinates(), smItem.getIcon(), smItem.getIconType());
					}
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				}
				return;
			}
		}

		if (iPanel == MOUSE_BOTTOM_OPENCLOSE) {
			setBottomMenuPanelActive(!isBottomMenuPanelActive());
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
		}

		// MENU
		if (iPanel == MOUSE_MENU_OPENCLOSE) {
			setMenuPanelLocked(!isMenuPanelLocked());
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (isMenuPanelActive()) {
			if (iPanel == MOUSE_MENU_PANEL_ITEMS) {
				if (mouseButton == 1) { // Botón derecho (back al menú)
					if (menuPanelMenu.getParent() != null) {
						menuPanelMenu = menuPanelMenu.getParent();
						createMenuPanel(menuPanelMenu);
						createProductionPanel(productionPanelMenu);
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					} else {
						setMenuPanelLocked(false);
					}
					return;
				}
				int iItem = isMouseOnMenuItems(x, y);
				if (iItem != -1) {
					SmartMenu smItem = menuPanelMenu.getItems().get(iItem);
					if (smItem.getType() == SmartMenu.TYPE_MENU) {
						createMenuPanel(smItem);
						createProductionPanel(productionPanelMenu);
						menuPanelMenu = smItem;
					} else if (smItem.getType() == SmartMenu.TYPE_ITEM) {
						if (!smItem.getCommand().equalsIgnoreCase(CommandPanel.COMMAND_BACK)) {
							CommandPanel.executeCommand(smItem.getCommand(), smItem.getParameter(),
									smItem.getParameter2(), smItem.getDirectCoordinates(), smItem.getIcon(),
									smItem.getIconType());
						} else {
							if (menuPanelMenu.getParent() != null) {
								menuPanelMenu = menuPanelMenu.getParent();
								createMenuPanel(menuPanelMenu);
								createProductionPanel(productionPanelMenu);
							}
						}
					}
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				}
				return;
			}
			if (iPanel == MOUSE_MENU_PANEL) {
				if (mouseButton == 1) { // Botón derecho (back al menú)
					if (menuPanelMenu.getParent() != null) {
						menuPanelMenu = menuPanelMenu.getParent();
						createMenuPanel(menuPanelMenu);
						createProductionPanel(productionPanelMenu);
						UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
					} else {
						setMenuPanelLocked(false);
					}
				}
				return;
			}
		}

		if (iPanel == MOUSE_MENU_OPENCLOSE) {
			setMenuPanelActive(!isMenuPanelActive());
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
		}

		// BOTTOM submenu
		if (bottomSubPanelMenu != null && iPanel == MOUSE_BOTTOM_SUBITEMS) {
			// BOTTOM SUBPANEL
			if (mouseButton == 1) { // Botón derecho (back al menú)
				bottomSubPanelMenu = bottomSubPanelMenu.getParent();
				if (bottomSubPanelMenu != null) {
					if (bottomSubPanelMenu.getParent() == null) {
						bottomSubPanelMenu = null;
						createProductionPanel(productionPanelMenu);
					} else {
						createBottomSubPanel(bottomSubPanelMenu);
					}
					UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
				}
				return;
			}

			int iItem = isMouseOnBottomSubItems(x, y);
			if (iItem != -1) {
				SmartMenu smItem = bottomSubPanelMenu.getItems().get(iItem);
				if (smItem.getType() == SmartMenu.TYPE_MENU && smItem.getItems() != null
						&& smItem.getItems().size() > 0) {
					// Activamos el subpanel de abajo
					bottomSubPanelMenu = smItem;
					createBottomSubPanel(smItem);
				} else if (smItem.getType() == SmartMenu.TYPE_ITEM) {
					if (!smItem.getCommand().equalsIgnoreCase(CommandPanel.COMMAND_BACK)) {
						CommandPanel.executeCommand(smItem.getCommand(), smItem.getParameter(), smItem.getParameter2(),
								smItem.getDirectCoordinates(), smItem.getIcon(), smItem.getIconType());
						bottomSubPanelMenu = null;
						createProductionPanel(productionPanelMenu);
					} else {
						bottomSubPanelMenu = bottomSubPanelMenu.getParent();
						if (bottomSubPanelMenu != null) {
							if (bottomSubPanelMenu.getParent() == null) {
								bottomSubPanelMenu = null;
								createProductionPanel(productionPanelMenu);
							} else {
								createBottomSubPanel(bottomSubPanelMenu);
							}
						}
					}
				}
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			}
			return;
		}

		if (bottomSubPanelMenu != null && iPanel == MOUSE_BOTTOM_SUBPANEL) {
			// BOTTOM SUBPANEL
			if (mouseButton == 1) { // Botón derecho (back al menú)
				bottomSubPanelMenu = bottomSubPanelMenu.getParent();
				if (bottomSubPanelMenu != null) {
					if (bottomSubPanelMenu.getParent() == null) {
						bottomSubPanelMenu = null;
						createProductionPanel(productionPanelMenu);
					} else {
						createBottomSubPanel(bottomSubPanelMenu);
					}
				}
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			}
			return;
		}

		// ICONS
		if (iPanel == MOUSE_ICON_LEVEL_UP) {
			CommandPanel.executeCommand(CommandPanel.COMMAND_LEVEL_UP, null, null, null, null, 0);
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
				for (int rep = 0; rep < 99; rep++) {
					CommandPanel.executeCommand(CommandPanel.COMMAND_LEVEL_UP, null, null, null, null, 0);
				}
			} else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				CommandPanel.executeCommand(CommandPanel.COMMAND_LEVEL_UP, null, null, null, null, 0);
				CommandPanel.executeCommand(CommandPanel.COMMAND_LEVEL_UP, null, null, null, null, 0);
				CommandPanel.executeCommand(CommandPanel.COMMAND_LEVEL_UP, null, null, null, null, 0);
				CommandPanel.executeCommand(CommandPanel.COMMAND_LEVEL_UP, null, null, null, null, 0);
			}
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_LEVEL_DOWN) {
			CommandPanel.executeCommand(CommandPanel.COMMAND_LEVEL_DOWN, null, null, null, null, 0);
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
				for (int rep = 0; rep < 99; rep++) {
					CommandPanel.executeCommand(CommandPanel.COMMAND_LEVEL_DOWN, null, null, null, null, 0);
				}
			} else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				CommandPanel.executeCommand(CommandPanel.COMMAND_LEVEL_DOWN, null, null, null, null, 0);
				CommandPanel.executeCommand(CommandPanel.COMMAND_LEVEL_DOWN, null, null, null, null, 0);
				CommandPanel.executeCommand(CommandPanel.COMMAND_LEVEL_DOWN, null, null, null, null, 0);
				CommandPanel.executeCommand(CommandPanel.COMMAND_LEVEL_DOWN, null, null, null, null, 0);
			}
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_CITIZEN_PREVIOUS) {
			CommandPanel.executeCommand(CommandPanel.COMMAND_PREVIOUS_CITIZEN, null, null, null, null, 0);
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_CITIZEN_NEXT) {
			CommandPanel.executeCommand(CommandPanel.COMMAND_NEXT_CITIZEN, null, null, null, null, 0);
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_INFO_NUM_CITIZENS) {
			if (getLivingsPanelActive() != LIVINGS_PANEL_TYPE_CITIZENS) {
				setLivingsPanelActive(LIVINGS_PANEL_TYPE_CITIZENS, livingsPanelSoldiersGroupActive,
						livingsPanelCitizensGroupActive);

				Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_CITIZENS, null);
			} else {
				setLivingsPanelActive(LIVINGS_PANEL_TYPE_NONE, livingsPanelSoldiersGroupActive,
						livingsPanelCitizensGroupActive);
			}
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_SOLDIER_PREVIOUS) {
			CommandPanel.executeCommand(CommandPanel.COMMAND_PREVIOUS_SOLDIER, null, null, null, null, 0);
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_SOLDIER_NEXT) {
			CommandPanel.executeCommand(CommandPanel.COMMAND_NEXT_SOLDIER, null, null, null, null, 0);
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_INFO_NUM_SOLDIERS) {
			if (getLivingsPanelActive() != LIVINGS_PANEL_TYPE_SOLDIERS) {
				setLivingsPanelActive(LIVINGS_PANEL_TYPE_SOLDIERS, livingsPanelSoldiersGroupActive,
						livingsPanelCitizensGroupActive);

				Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_SOLDIERS, null);
			} else {
				setLivingsPanelActive(LIVINGS_PANEL_TYPE_NONE, livingsPanelSoldiersGroupActive,
						livingsPanelCitizensGroupActive);
			}
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_HERO_PREVIOUS) {
			CommandPanel.executeCommand(CommandPanel.COMMAND_PREVIOUS_HERO, null, null, null, null, 0);
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_HERO_NEXT) {
			CommandPanel.executeCommand(CommandPanel.COMMAND_NEXT_HERO, null, null, null, null, 0);
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_INFO_NUM_HEROES) {
			if (getLivingsPanelActive() != LIVINGS_PANEL_TYPE_HEROES) {
				setLivingsPanelActive(LIVINGS_PANEL_TYPE_HEROES, livingsPanelSoldiersGroupActive,
						livingsPanelCitizensGroupActive);

				Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_HEROES, null);
			} else {
				setLivingsPanelActive(LIVINGS_PANEL_TYPE_NONE, livingsPanelSoldiersGroupActive,
						livingsPanelCitizensGroupActive);
			}
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_INFO_CARAVAN) {
			setTradePanelActive(!isTradePanelActive());
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_PRIORITIES) {
			setPrioritiesPanelActive(!isPrioritiesPanelActive());
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_MATS) {
			setMatsPanelActive(!isMatsPanelActive());
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_MINIBLOCKS) {
			MainPanel.toggleMiniBlocks();
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_FLATMOUSE) {
			MainPanel.toggleFlatMouse();
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_3DMOUSE) {
			MainPanel.toggle3DMouse();
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_GRID) {
			MainPanel.toggleGrid();
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_SETTINGS) {
			CommandPanel.executeCommand(CommandPanel.COMMAND_EXIT_TO_MAIN_MENU, null, null, null, null, 0);
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_PAUSE_RESUME) {
			CommandPanel.executeCommand(CommandPanel.COMMAND_PAUSE, null, null, null, null, 0);
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_LOWER_SPEED) {
			if (World.SPEED > 1) {
				CommandPanel.executeCommand(CommandPanel.COMMAND_LOWER_SPEED, null, null, null, null, 0);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			}
			return;
		}
		if (iPanel == MOUSE_ICON_INCREASE_SPEED) {
			if (World.SPEED < World.SPEED_MAX) {
				CommandPanel.executeCommand(CommandPanel.COMMAND_INCREASE_SPEED, null, null, null, null, 0);
				UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			}
			return;
		}
		if (iPanel == MOUSE_TUTORIAL_ICON) {
			toggleTutorialPanel(false);
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}

		if (iPanel == MOUSE_MESSAGES_ICON_ANNOUNCEMENT) {
			if (getMessagesPanelActive() != MessagesPanel.TYPE_ANNOUNCEMENT) {
				setMessagesPanelActive(MessagesPanel.TYPE_ANNOUNCEMENT);
			} else {
				setMessagesPanelActive(-1);
			}
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}

		if (iPanel == MOUSE_MESSAGES_ICON_COMBAT) {
			if (getMessagesPanelActive() != MessagesPanel.TYPE_COMBAT) {
				setMessagesPanelActive(MessagesPanel.TYPE_COMBAT);
			} else {
				setMessagesPanelActive(-1);
			}
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}

		if (iPanel == MOUSE_MESSAGES_ICON_HEROES) {
			if (getMessagesPanelActive() != MessagesPanel.TYPE_HEROES) {
				setMessagesPanelActive(MessagesPanel.TYPE_HEROES);
			} else {
				setMessagesPanelActive(-1);
			}
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}

		if (iPanel == MOUSE_MESSAGES_ICON_SYSTEM) {
			if (getMessagesPanelActive() != MessagesPanel.TYPE_SYSTEM) {
				setMessagesPanelActive(MessagesPanel.TYPE_SYSTEM);
			} else {
				setMessagesPanelActive(-1);
			}
			UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
			return;
		}

		// MINIMAP
		if (iPanel == MOUSE_MINIMAP) {
			MiniMapPanel.mousePressed(x - minimapPanelX, y - minimapPanelY, mouseButton);
			return;
		}
	}
public static boolean keyPressed(int tecla) {
		if (tecla == Keyboard.KEY_ESCAPE) {
			if (imagesPanel != null && ImagesPanel.isVisible()) {
				ImagesPanel.setVisible(false);
				return true;
			} else if (isPrioritiesPanelActive()) {
				setPrioritiesPanelActive(false);
				return true;
			} else if (isTradePanelActive()) {
				setTradePanelActive(false);
				return true;
			} else if (isProfessionsPanelActive()) {
				setProfessionsPanelActive(-1, false);
				return true;
			} else if (isLivingsPanelActive()) {
				setLivingsPanelActive(LIVINGS_PANEL_TYPE_NONE, livingsPanelSoldiersGroupActive,
						livingsPanelCitizensGroupActive);
				return true;
			} else if (isMatsPanelActive()) {
				setMatsPanelActive(false);
				return true;
			} else if (isMessagesPanelActive()) {
				setMessagesPanelActive(-1);
				return true;
			} else if (isPilePanelActive()) {
				setPilePanelActive(-1, false);
				return true;
			}
		}

		return false;
	}
public static int isMouseOnAPanel(int x, int y) {
		return isMouseOnAPanel(x, y, false);
	}

	/**
	 * Indica si el ratón está en algún panel. Retorna un código según el panel
	 * 
	 * @param x
	 * @param y
	 * @param doEdgeMenusStuff. Setea el delay a 0 si el mouse está en uno de los
	 *                          paneles laterales, también abre/cierra menus y tal
	 * @return
	 */
	public static int isMouseOnAPanel(int x, int y, boolean doEdgeMenusStuff) {
		/*
		 * TYPING PANEL (Si está activo ya no miraremos nada más)
		 */
		if (typingPanel != null) {
			if (isMouseOnTypingPanel(x, y)) {
				if (isMouseOnAnIcon(x, y, TypingPanel.getCloseButtonPoint(), tileButtonClose, tileButtonCloseAlpha)) {
					return MOUSE_TYPING_PANEL_CLOSE;
				} else if (isMouseOnAnIcon(x, y, TypingPanel.getConfirmPoint(), TypingPanel.getTileConfirm(),
						TypingPanel.getTileConfirmAlpha())) {
					return MOUSE_TYPING_PANEL_CONFIRM;
				}

				return MOUSE_TYPING_PANEL;
			}

			return MOUSE_NONE;
		}

		/*
		 * IMAGES PANEL
		 */
		if (imagesPanel != null && ImagesPanel.isVisible()) {
			if (isMouseOnImagesPanel(x, y)) {
				if (isMouseOnAnIcon(x, y, ImagesPanel.getCloseButtonPoint(), tileButtonClose, tileButtonCloseAlpha)) {
					return MOUSE_IMAGES_PANEL_CLOSE;
				} else if (isMouseOnAnIcon(x, y, ImagesPanel.getPreviousImagePoint(), ImagesPanel.getTilePrevious())) {
					return MOUSE_IMAGES_PANEL_PREVIOUS;
				} else if (isMouseOnAnIcon(x, y, ImagesPanel.getNextImagePoint(), ImagesPanel.getTileNext())) {
					return MOUSE_IMAGES_PANEL_NEXT;
				} else if (isMouseOnAnIcon(x, y, ImagesPanel.getNextMissionPoint(), ImagesPanel.getTileNextMission())) {
					return MOUSE_IMAGES_PANEL_NEXT_MISSION;
				}

				return MOUSE_IMAGES_PANEL;
			}

			// Miramos también el botón (para hacer toggle)
			// if (isMouseOnAnIcon (x, y, iconTutorialPoint, tileBottomItem,
			// tileBottomItemAlpha)) {
			// return MOUSE_TUTORIAL_ICON;
			// }
			//
			// return MOUSE_NONE;
		}

		/*
		 * PROFESSIONS PANEL
		 */
		if (isProfessionsPanelActive()) {
			if (isMouseOnProfessionsPanel(x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(true, true, true);
				}

				Point p = isMouseOnProfessionsButtons(x, y);
				if (p != null) {
					return p.x;
				}

				return MOUSE_PROFESSIONS_PANEL;
			}
		}

		/*
		 * PILE PANEL
		 */
		if (isPilePanelActive()) {
			if (isMouseOnPilePanel(x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(true, true, true);
				}

				Point p = isMouseOnPileButtons(x, y);
				if (p != null) {
					return p.x;
				}

				return MOUSE_PILE_PANEL;
			}
		}

		/*
		 * MESSAGES PANEL
		 */
		if (isMessagesPanelActive()) {
			if (isMouseOnMessagesPanel(x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(true, true, true);
				}

				Point p = isMouseOnMessagesButtons(x, y);
				if (p != null) {
					return p.x;
				}

				return MOUSE_MESSAGES_PANEL;
			}
		}

		/*
		 * MATS PANEL
		 */
		if (isMatsPanelActive()) {
			if (isMouseOnMatsPanel(x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(true, true, true);
				}

				Point p = isMouseOnMatsButtons(x, y);
				if (p != null) {
					return p.x;
				}

				return MOUSE_MATS_PANEL;
			}
		}

		/*
		 * LIVINGS PANEL
		 */
		if (isLivingsPanelActive()) {
			if (isMouseOnLivingsPanel(x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(true, true, true);
				}

				Point p = isMouseOnLivingsButtons(x, y);
				if (p != null) {
					return p.x;
				}

				return MOUSE_LIVINGS_PANEL;
			}
		}

		/*
		 * TRADE PANEL
		 */
		if (isTradePanelActive()) {
			if (isMouseOnTradePanel(x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(true, true, true);
				}

				Point p = isMouseOnTradeButtons(x, y);
				if (p != null) {
					return p.x;
				}

				return MOUSE_TRADE_PANEL;
			}
		}

		/*
		 * PRIORITIES PANEL
		 */
		if (isPrioritiesPanelActive()) {
			if (isMouseOnPrioritiesPanel(x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(true, true, true);
				}

				Point p = isMouseOnPrioritiesItems(x, y);
				if (p != null) {
					return p.x;
				}
				return MOUSE_PRIORITIES_PANEL;
			}
		}

		/*
		 * PRODUCTION PANEL
		 */
		if (isProductionPanelActive()) {
			
			if (isMouseOnAnIcon(x, y, tileOpenCloseProductionPanelPoint, tileOpenProductionPanelON,
					tileOpenProductionPanelONAlpha)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(true, true, false);
					delayTime = 0;
				}
				return MOUSE_PRODUCTION_OPENCLOSE;
			}
			if (isMouseOnProductionPanel(x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(true, true, false);
					delayTime = 0;
				}

				Point p = isMouseOnProductionItems(x, y);
				if (p != null) {
					return p.x;
				}

				return MOUSE_PRODUCTION_PANEL;
			}
			if (doEdgeMenusStuff) {
				if (delayTime > (Game.FPS_INGAME / 8) * 6) {
					if (!isProductionPanelLocked() && !isMouseOnAnIcon(x, y, tileOpenCloseProductionPanelPoint,
							tileOpenProductionPanel, tileOpenProductionPanelAlpha)) {
						delayTime = 0;
						setProductionPanelActive(false);
					}
				}
			}
		} else {
			if (doEdgeMenusStuff) {
				
				if (isMouseOnAnIcon(x, y, tileOpenCloseProductionPanelPoint, tileOpenProductionPanel,
						tileOpenProductionPanelAlpha)) {
					setProductionPanelActive(true);

					// Cerramos los menús no locked
					closeNonLockedMenus(true, true, false);
					delayTime = 0;
					return MOUSE_PRODUCTION_OPENCLOSE;
				}
			}
		}

		// BOTTOM
		if (isBottomMenuPanelActive()) {
			if (isMouseOnBottomLeftScroll(x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(false, true, true);
					delayTime = 0;
				}
				return MOUSE_BOTTOM_LEFT_SCROLL;
			}
			if (isMouseOnBottomRightScroll(x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(false, true, true);
					delayTime = 0;
				}
				return MOUSE_BOTTOM_RIGHT_SCROLL;
			}
			if (isMouseOnBottomItems(x, y) != -1) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(false, true, true);
					delayTime = 0;
				}
				return MOUSE_BOTTOM_ITEMS;
			}
			if (isMouseOnBottomPanel(x, y)) { // Este check tiene que ir detrás de los items, ya que los items están
												// encima
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(false, true, true);
					delayTime = 0;
				}
				return MOUSE_BOTTOM_PANEL;
			}
			if (isMouseOnAnIcon(x, y, tileOpenCloseBottomMenuPoint, tileOpenBottomMenuON, tileOpenBottomMenuONAlpha)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(false, true, true);
					delayTime = 0;
				}
				return MOUSE_BOTTOM_OPENCLOSE;
			}

			if (bottomSubPanelMenu != null) {
				// BOTTOM SUBPANEL
				if (isMouseOnBottomSubItems(x, y) != -1) {
					if (doEdgeMenusStuff) {
						// Cerramos los menús no locked
						closeNonLockedMenus(false, true, true);
						delayTime = 0;
					}
					return MOUSE_BOTTOM_SUBITEMS;
				}
				if (isMouseOnBottomSubPanel(x, y)) {
					if (doEdgeMenusStuff) {
						// Cerramos los menús no locked
						closeNonLockedMenus(false, true, true);
						delayTime = 0;
					}
					return MOUSE_BOTTOM_SUBPANEL;
				}
			}

			if (doEdgeMenusStuff) {
				if (delayTime > (Game.FPS_INGAME / 8) * 6) {
					if (!isBottomMenuPanelLocked() && !isMouseOnAnIcon(x, y, tileOpenCloseBottomMenuPoint,
							tileOpenBottomMenu, tileOpenBottomMenuAlpha)) {
						delayTime = 0;
						setBottomMenuPanelActive(false);
					}
				}
			}
		} else {
			if (doEdgeMenusStuff) {
				if (isMouseOnAnIcon(x, y, tileOpenCloseBottomMenuPoint, tileOpenBottomMenu, tileOpenBottomMenuAlpha)) {
					setBottomMenuPanelActive(true);

					// Cerramos los menús no locked
					closeNonLockedMenus(false, true, true);
					delayTime = 0;
					return MOUSE_BOTTOM_OPENCLOSE;
				}
			}
		}

		// MENU (right)
		if (isMenuPanelActive()) {
			if (isMouseOnMenuItems(x, y) != -1) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(true, false, true);
					delayTime = 0;
				}
				return MOUSE_MENU_PANEL_ITEMS;
			}
			if (isMouseOnMenuPanel(x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(true, false, true);
					delayTime = 0;
				}
				return MOUSE_MENU_PANEL;
			}
			if (isMouseOnAnIcon(x, y, tileOpenCloseRightMenuPoint, tileOpenRightMenuON, tileOpenRightMenuONAlpha)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus(true, false, true);
					delayTime = 0;
				}
				return MOUSE_MENU_OPENCLOSE;
			}

			if (doEdgeMenusStuff) {
				if (delayTime > (Game.FPS_INGAME / 8) * 6) {
					if (!isMenuPanelLocked() && !isMouseOnAnIcon(x, y, tileOpenCloseRightMenuPoint, tileOpenRightMenu,
							tileOpenRightMenuAlpha)) {
						delayTime = 0;
						setMenuPanelActive(false);
					}
				}
			}
		} else {
			if (doEdgeMenusStuff) {
				if (isMouseOnAnIcon(x, y, tileOpenCloseRightMenuPoint, tileOpenRightMenu, tileOpenRightMenuAlpha)) {
					setMenuPanelActive(true);

					// Cerramos los menús no locked
					closeNonLockedMenus(true, false, true);
					delayTime = 0;
					return MOUSE_MENU_OPENCLOSE;
				}
			}
		}

		// MINI ICONS
		if (isMouseOnAnIcon(x, y, iconLevelUpPoint, tileIconLevelUp, tileIconLevelUpAlpha)) {
			return MOUSE_ICON_LEVEL_UP;
		}
		if (isMouseOnAnIcon(x, y, iconLevelPoint, tileIconLevel, tileIconLevelAlpha)) {
			return MOUSE_ICON_LEVEL;
		}
		if (isMouseOnAnIcon(x, y, iconLevelDownPoint, tileIconLevelDown, tileIconLevelDownAlpha)) {
			return MOUSE_ICON_LEVEL_DOWN;
		}
		if (isMouseOnAnIcon(x, y, iconCitizenPreviousPoint, tileIconCitizenPrevious, tileIconPreviousMiniAlpha)) {
			return MOUSE_ICON_CITIZEN_PREVIOUS;
		}
		if (isMouseOnAnIcon(x, y, iconCitizenNextPoint, tileIconCitizenNext, tileIconNextMiniAlpha)) {
			return MOUSE_ICON_CITIZEN_NEXT;
		}
		if (isMouseOnAnIcon(x, y, iconSoldierPreviousPoint, tileIconSoldierPrevious, tileIconPreviousMiniAlpha)) {
			return MOUSE_ICON_SOLDIER_PREVIOUS;
		}
		if (isMouseOnAnIcon(x, y, iconSoldierNextPoint, tileIconSoldierNext, tileIconNextMiniAlpha)) {
			return MOUSE_ICON_SOLDIER_NEXT;
		}
		if (isMouseOnAnIcon(x, y, iconHeroPreviousPoint, tileIconHeroPrevious, tileIconPreviousMiniAlpha)) {
			return MOUSE_ICON_HERO_PREVIOUS;
		}
		if (isMouseOnAnIcon(x, y, iconHeroNextPoint, tileIconHeroNext, tileIconNextMiniAlpha)) {
			return MOUSE_ICON_HERO_NEXT;
		}

		// Messages
		if (isMouseOnAnIcon(x, y, messageIconPoints[0], messageTiles[0], messageTilesAlpha.get(0))) {
			return MOUSE_MESSAGES_ICON_ANNOUNCEMENT;
		}
		if (isMouseOnAnIcon(x, y, messageIconPoints[1], messageTiles[1], messageTilesAlpha.get(1))) {
			return MOUSE_MESSAGES_ICON_COMBAT;
		}
		if (isMouseOnAnIcon(x, y, messageIconPoints[2], messageTiles[2], messageTilesAlpha.get(2))) {
			return MOUSE_MESSAGES_ICON_HEROES;
		}
		if (isMouseOnAnIcon(x, y, messageIconPoints[3], messageTiles[3], messageTilesAlpha.get(3))) {
			return MOUSE_MESSAGES_ICON_SYSTEM;
		}

		// Events
		if (isMouseOnAnIcon(x, y, iconEventsPoint, GlobalEventData.getIcon())) {
			return MOUSE_EVENTS_ICON;
		}

		// Tutorial
		if (isMouseOnAnIcon(x, y, iconTutorialPoint, tileIconTutorial)) {
			return MOUSE_TUTORIAL_ICON;
		}

		// Gods
		// if (TownsProperties.GODS_ACTIVATED) {
		// if (isMouseOnAnIcon (x, y, iconGodsPoint, tileIconGods)) {
		// return MOUSE_GODS_ICON;
		// }
		// }

		// Backgrounds
		if (isMouseOnAnIcon(x, y, iconNumCitizensBackgroundPoint, tileBottomItem, tileBottomItemAlpha)) {
			return MOUSE_INFO_NUM_CITIZENS;
		}
		if (isMouseOnAnIcon(x, y, iconNumSoldiersBackgroundPoint, tileBottomItem, tileBottomItemAlpha)) {
			return MOUSE_INFO_NUM_SOLDIERS;
		}
		if (isMouseOnAnIcon(x, y, iconNumHeroesBackgroundPoint, tileBottomItem, tileBottomItemAlpha)) {
			return MOUSE_INFO_NUM_HEROES;
		}
		if (isMouseOnAnIcon(x, y, iconCaravanBackgroundPoint, tileBottomItem, tileBottomItemAlpha)) {
			return MOUSE_INFO_CARAVAN;
		}

		// ICONS
		if (isMouseOnAnIcon(x, y, iconPrioritiesPoint, tileIconPriorities, tileIconPrioritiesAlpha)) {
			return MOUSE_ICON_PRIORITIES;
		}
		if (isMouseOnAnIcon(x, y, iconMatsPoint, tileIconMats, tileIconMatsAlpha)) {
			return MOUSE_ICON_MATS;
		}
		if (isMouseOnAnIcon(x, y, iconMiniblocksPoint, tileIconMiniblocks, tileIconMiniblocksAlpha)) {
			return MOUSE_ICON_MINIBLOCKS;
		}
		if (isMouseOnAnIcon(x, y, iconFlatMousePoint, tileIconFlatMouse, tileIconFlatMouseAlpha)) {
			return MOUSE_ICON_FLATMOUSE;
		}
		if (isMouseOnAnIcon(x, y, icon3DMousePoint, tileIcon3DMouse, tileIcon3DMouseAlpha)) {
			return MOUSE_ICON_3DMOUSE;
		}
		if (isMouseOnAnIcon(x, y, iconGridPoint, tileIconGrid, tileIconGridAlpha)) {
			return MOUSE_ICON_GRID;
		}
		if (isMouseOnAnIcon(x, y, iconSettingsPoint, tileIconSettings, tileIconSettingsAlpha)) {
			return MOUSE_ICON_SETTINGS;
		}
		if (isMouseOnAnIcon(x, y, iconPauseResumePoint, tileIconPause, tileIconPauseResumeAlpha)) {
			return MOUSE_ICON_PAUSE_RESUME;
		}
		if (isMouseOnAnIcon(x, y, iconLowerSpeedPoint, tileIconLowerSpeed, tileIconLowerSpeedAlpha)) {
			return MOUSE_ICON_LOWER_SPEED;
		}
		if (isMouseOnAnIcon(x, y, iconIncreaseSpeedPoint, tileIconIncreaseSpeed, tileIconIncreaseSpeedAlpha)) {
			return MOUSE_ICON_INCREASE_SPEED;
		}
		if (Game.getCurrentMissionData() != null && Game.getCurrentMissionData().getTutorialFlows().size() > 0) {
			if (isMouseOnAnIcon(x, y, iconTutorialPoint, tileBottomItem, tileBottomItemAlpha)) {
				return MOUSE_TUTORIAL_ICON;
			}
		}

		// DATE
		if (isMouseOnDatePanel(x, y)) {
			return MOUSE_DATEPANEL;
		}

		// INFO
		if (isMouseOnInfoPanel(x, y)) {
			return MOUSE_INFOPANEL;
		}

		// MINIMAP
		if (isMouseOnMinimap(x, y)) {
			return MOUSE_MINIMAP;
		}

		return MOUSE_NONE;
	}

	public static boolean isMouseOnAnIcon(int x, int y, Point point, Tile tile) {
		if ((y >= point.y && y < (point.y + tile.getTileHeight()))
				&& (x >= point.x && x < (point.x + tile.getTileWidth()))) {
			return true;
		}

		return false;
	}

	public static boolean isMouseOnAnIcon(int x, int y, Point point, Tile tile, boolean[][] alpha) {
		if ((y >= point.y && y < (point.y + tile.getTileHeight()))
				&& (x >= point.x && x < (point.x + tile.getTileWidth()))) {
			return !alpha[x - point.x][y - point.y];
		}

		return false;
	}

	public static boolean isMouseCloseToOpenCloseBottomIcon(int x, int y) {
		return isMouseCloseToIcon(x, y, tileOpenCloseBottomMenuPoint, tileOpenBottomMenu, CLOSE_PIXELS);
	}

	public static boolean isMouseCloseToOpenCloseMenuIcon(int x, int y) {
		return isMouseCloseToIcon(x, y, tileOpenCloseRightMenuPoint, tileOpenRightMenu, CLOSE_PIXELS);
	}

	public static boolean isMouseCloseToOpenCloseProductionIcon(int x, int y) {
		return isMouseCloseToIcon(x, y, tileOpenCloseProductionPanelPoint, tileOpenProductionPanel, CLOSE_PIXELS);
	}

	public static boolean isMouseCloseToIcon(int x, int y, Point point, Tile tile, int closeFactor) {
		if ((y >= (point.y - closeFactor) && y < (point.y + tile.getTileHeight() + closeFactor))
				&& (x >= (point.x - closeFactor) && x < (point.x + tile.getTileWidth() + closeFactor))) {
			return true;
		}

		return false;
	}

	public static boolean isMouseOnBottomPanel(int x, int y) {
		if (y >= bottomPanelY && y < (bottomPanelY + BOTTOM_PANEL_HEIGHT)) {
			// Dentro del panel "virtual", miramos los paneles internos con sus
			// transparencias

			if (x >= bottomPanelX && x < (bottomPanelX + BOTTOM_PANEL_WIDTH)) {
				return (!tileBottomPanelAlpha[x - bottomPanelX][y - bottomPanelY]);
			}
		}

		return false;
	}

	public static boolean isMouseOnBottomLeftScroll(int x, int y) {
		if ((y >= bottomPanelY && y < (bottomPanelY + BOTTOM_PANEL_HEIGHT))
				&& (x >= bottomPanelLeftScrollX && x < (bottomPanelLeftScrollX + BOTTOM_PANEL_SCROLL_WIDTH))) {
			return !tileBottomScrollLeftAlpha[x - bottomPanelLeftScrollX][y - bottomPanelY];
		}

		return false;
	}

	public static boolean isMouseOnBottomRightScroll(int x, int y) {
		if ((y >= bottomPanelY && y < (bottomPanelY + BOTTOM_PANEL_HEIGHT))
				&& (x >= bottomPanelRightScrollX && x < (bottomPanelRightScrollX + BOTTOM_PANEL_SCROLL_WIDTH))) {
			return !tileBottomScrollRightAlpha[x - bottomPanelRightScrollX][y - bottomPanelY];
		}

		return false;
	}

	/**
	 * Indica si el mouse está en un item, devuelve el número del mismo o -1 en caso
	 * de no estar
	 * 
	 * @param x
	 * @param y
	 * @return devuelve el número del item o -1 en caso de no estar
	 */
	public static int isMouseOnBottomItems(int x, int y) {
		if (y >= bottomPanelY && y < (bottomPanelY + BOTTOM_PANEL_HEIGHT)) {
			Point point;
			for (int i = 0; i < BOTTOM_PANEL_NUM_ITEMS; i++) {
				point = bottomPanelItemsPosition.get(i);
				if (x >= point.x && x < (point.x + BOTTOM_ITEM_WIDTH)) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						return i;
					}
				}
			}
		}

		return -1;
	}

	public static boolean isMouseOnBottomSubPanel(int x, int y) {
		if (x >= bottomSubPanelPoint.x && x < (bottomSubPanelPoint.x + BOTTOM_SUBPANEL_WIDTH)
				&& y >= bottomSubPanelPoint.y && y < (bottomSubPanelPoint.y + BOTTOM_SUBPANEL_HEIGHT)) {
			return true;
		}

		return false;
	}

	/**
	 * Indica si el mouse está en un item del submenu de abajo, devuelve el número
	 * del mismo o -1 en caso de no estar
	 * 
	 * @param x
	 * @param y
	 * @return devuelve el número del item o -1 en caso de no estar
	 */
	public static int isMouseOnBottomSubItems(int x, int y) {
		if (bottomSubPanelMenu != null && y >= bottomSubPanelPoint.y
				&& y < (bottomSubPanelPoint.y + BOTTOM_SUBPANEL_HEIGHT) && x >= bottomSubPanelPoint.x
				&& x < (bottomSubPanelPoint.x + BOTTOM_SUBPANEL_WIDTH)) {
			Point point;
			bucle1: for (int y1 = 0; y1 < BOTTOM_SUBPANEL_NUM_ITEMS_Y; y1++) {
				for (int x1 = 0; x1 < BOTTOM_SUBPANEL_NUM_ITEMS_X; x1++) {
					int i = (y1 * BOTTOM_SUBPANEL_NUM_ITEMS_X) + x1;
					if (i >= bottomSubPanelMenu.getItems().size()) {
						break bucle1;
					}
					point = bottomSubPanelItemsPosition.get(i);
					if (x >= point.x && x < (point.x + BOTTOM_SUBITEM_WIDTH) && y >= point.y
							&& y < (point.y + BOTTOM_SUBITEM_HEIGHT)) {
						if (!tileBottomSubItemAlpha[x - point.x][y - point.y]) {
							return i;
						}
					}
				}
			}
		}

		return -1;
	}

	public static boolean isMouseOnDatePanel(int x, int y) {
		if ((y >= datePanelPoint.y && y < (datePanelPoint.y + tileDatePanel.getTileHeight()))
				&& (x >= datePanelPoint.x && x < (datePanelPoint.x + tileDatePanel.getTileWidth()))) {
			return !tileDatePanelAlpha[x - datePanelPoint.x][y - datePanelPoint.y];
		}

		return false;
	}

	public static boolean isMouseOnInfoPanel(int x, int y) {
		if ((y >= infoPanelPoint.y && y < (infoPanelPoint.y + tileInfoPanel.getTileHeight()))
				&& (x >= infoPanelPoint.x && x < (infoPanelPoint.x + tileInfoPanel.getTileWidth()))) {
			return !tileInfoPanelAlpha[x - infoPanelPoint.x][y - infoPanelPoint.y];
		}

		return false;
	}

	public static boolean isMouseOnProductionPanel(int x, int y) {
		return ((y >= productionPanelPoint.y && y < (productionPanelPoint.y + PRODUCTION_PANEL_HEIGHT))
				&& (x >= productionPanelPoint.x && x < (productionPanelPoint.x + PRODUCTION_PANEL_WIDTH)));
	}

	public static boolean isMouseOnImagesPanel(int x, int y) {
		return ((y >= ImagesPanel.getPanelPoint().y && y < (ImagesPanel.getPanelPoint().y + ImagesPanel.HEIGHT))
				&& (x >= ImagesPanel.getPanelPoint().x && x < (ImagesPanel.getPanelPoint().x + ImagesPanel.WIDTH)));
	}

	public static boolean isMouseOnTypingPanel(int x, int y) {
		return ((y >= TypingPanel.getPanelPoint().y && y < (TypingPanel.getPanelPoint().y + TypingPanel.HEIGHT))
				&& (x >= TypingPanel.getPanelPoint().x && x < (TypingPanel.getPanelPoint().x + TypingPanel.WIDTH)));
	}

	public static boolean isMouseOnMessagesPanel(int x, int y) {
		return ((y >= messagesPanelPoint.y && y < (messagesPanelPoint.y + MESSAGES_PANEL_HEIGHT))
				&& (x >= messagesPanelPoint.x && x < (messagesPanelPoint.x + MESSAGES_PANEL_WIDTH)));
	}

	public static boolean isMouseOnMatsPanel(int x, int y) {
		return ((y >= matsPanelPoint.y && y < (matsPanelPoint.y + MATS_PANEL_HEIGHT))
				&& (x >= matsPanelPoint.x && x < (matsPanelPoint.x + MATS_PANEL_WIDTH)));
	}

	public static boolean isMouseOnProfessionsPanel(int x, int y) {
		return ((y >= professionsPanelPoint.y && y < (professionsPanelPoint.y + PROFESSIONS_PANEL_HEIGHT))
				&& (x >= professionsPanelPoint.x && x < (professionsPanelPoint.x + PROFESSIONS_PANEL_WIDTH)));
	}

	public static boolean isMouseOnPilePanel(int x, int y) {
		return ((y >= pilePanelPoint.y && y < (pilePanelPoint.y + PILE_PANEL_HEIGHT))
				&& (x >= pilePanelPoint.x && x < (pilePanelPoint.x + PILE_PANEL_WIDTH)));
	}

	public static boolean isMouseOnLivingsPanel(int x, int y) {
		return ((y >= livingsPanelPoint.y && y < (livingsPanelPoint.y + LIVINGS_PANEL_HEIGHT))
				&& (x >= livingsPanelPoint.x && x < (livingsPanelPoint.x + LIVINGS_PANEL_WIDTH)));
	}

	public static boolean isMouseOnTradePanel(int x, int y) {
		return ((y >= tradePanelPoint.y && y < (tradePanelPoint.y + TRADE_PANEL_HEIGHT))
				&& (x >= tradePanelPoint.x && x < (tradePanelPoint.x + TRADE_PANEL_WIDTH)));
	}

	public static boolean isMouseOnPrioritiesPanel(int x, int y) {
		return ((y >= prioritiesPanelPoint.y && y < (prioritiesPanelPoint.y + PRIORITIES_PANEL_HEIGHT))
				&& (x >= prioritiesPanelPoint.x && x < (prioritiesPanelPoint.x + PRIORITIES_PANEL_WIDTH)));
	}

	public static boolean isMouseOnMinimap(int x, int y) {
		if (y >= minimapPanelY && y < (minimapPanelY + MINIMAP_PANEL_HEIGHT) && x >= minimapPanelX
				&& x < (minimapPanelX + MINIMAP_PANEL_WIDTH)) {
			if (MiniMapPanel.isMouseOver(x - minimapPanelX, y - minimapPanelY)) {
				return true;
			}

			return !tileMinimapPanelAlpha[x - minimapPanelX][y - minimapPanelY];
		}

		return false;
	}

	public static boolean isMouseOnMenuPanel(int x, int y) {
		if (x >= menuPanelPoint.x && x < (menuPanelPoint.x + MENU_PANEL_WIDTH) && y >= menuPanelPoint.y
				&& y < (menuPanelPoint.y + MENU_PANEL_HEIGHT)) {
			return true;
		}

		return false;
	}

	public static  int isMouseOnMenuItems(int x, int y) {
		if (y >= menuPanelPoint.y && y < (menuPanelPoint.y + MENU_PANEL_HEIGHT) && x >= menuPanelPoint.x
				&& x < (menuPanelPoint.x + MENU_PANEL_WIDTH)) {
			Point point;
			bucle1: for (int y1 = 0; y1 < MENU_PANEL_NUM_ITEMS_Y; y1++) {
				for (int x1 = 0; x1 < MENU_PANEL_NUM_ITEMS_X; x1++) {
					int i = (y1 * MENU_PANEL_NUM_ITEMS_X) + x1;
					if (i >= menuPanelMenu.getItems().size()) {
						break bucle1;
					}
					point = menuPanelItemsPosition.get(i);
					if (x >= point.x && x < (point.x + MENU_ITEM_WIDTH) && y >= point.y
							&& y < (point.y + MENU_ITEM_HEIGHT)) {
						if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
							return i;
						}
					}
				}
			}
		}

		return -1;
	}

	/**
	 * Indica si el mouse está en un item (o en los +/-) del panel de producción
	 * 
	 * @param x
	 * @param y
	 * @return Un punto, X es el MOUSE_ID y Y indica la posición del item en el
	 *         array correspondiente
	 */
	public static  Point isMouseOnProductionItems(int x, int y) {
		if (y >= productionPanelPoint.y && y < (productionPanelPoint.y + PRODUCTION_PANEL_HEIGHT)
				&& x >= productionPanelPoint.x && x < (productionPanelPoint.x + PRODUCTION_PANEL_WIDTH)) {
			Point point;
			bucle1: for (int y1 = 0; y1 < PRODUCTION_PANEL_NUM_ITEMS_Y; y1++) {
				for (int x1 = 0; x1 < PRODUCTION_PANEL_NUM_ITEMS_X; x1++) {
					int i = (y1 * PRODUCTION_PANEL_NUM_ITEMS_X) + x1;
					if (i >= productionPanelMenu.getItems().size()) {
						break bucle1;
					}
					point = productionPanelItemsPosition.get(i);
					if (x >= point.x && x < (point.x + PRODUCTION_PANEL_ITEM_WIDTH) && y >= point.y
							&& y < (point.y + PRODUCTION_PANEL_ITEM_HEIGHT)) {
						if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
							MOUSE_PRODUCTION_PANEL_ITEMS_POINT.y = i;
							return MOUSE_PRODUCTION_PANEL_ITEMS_POINT;
						}
					}
					point = productionPanelItemsPlusRegularPosition.get(i);
					if (point.x != -1) {
						if (x >= point.x && x < (point.x + ICON_WIDTH) && y >= point.y && y < (point.y + ICON_HEIGHT)) {
							if (!tileProductionPanelPlusIconAlpha[x - point.x][y - point.y]) {
								MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR_POINT.y = i;
								return MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR_POINT;
							}
						}
						point = productionPanelItemsMinusRegularPosition.get(i);
						if (x >= point.x && x < (point.x + ICON_WIDTH) && y >= point.y && y < (point.y + ICON_HEIGHT)) {
							if (!tileProductionPanelMinusIconAlpha[x - point.x][y - point.y]) {
								MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR_POINT.y = i;
								return MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR_POINT;
							}
						}
						point = productionPanelItemsPlusAutomatedPosition.get(i);
						if (x >= point.x && x < (point.x + ICON_WIDTH) && y >= point.y && y < (point.y + ICON_HEIGHT)) {
							if (!tileProductionPanelPlusIconAlpha[x - point.x][y - point.y]) {
								MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED_POINT.y = i;
								return MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED_POINT;
							}
						}
						point = productionPanelItemsMinusAutomatedPosition.get(i);
						if (x >= point.x && x < (point.x + ICON_WIDTH) && y >= point.y && y < (point.y + ICON_HEIGHT)) {
							if (!tileProductionPanelMinusIconAlpha[x - point.x][y - point.y]) {
								MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED_POINT.y = i;
								return MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED_POINT;
							}
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Indica si el mouse está en un item (o en los up/down) del panel de
	 * prioridades
	 * 
	 * @param x
	 * @param y
	 * @return Un punto, X es el MOUSE_ID y Y indica la posición del item en el
	 *         array correspondiente
	 */
	public static Point isMouseOnPrioritiesItems(int x, int y) {
		if (typingPanel != null) {
			return null;
		}

		if (y >= prioritiesPanelPoint.y && y < (prioritiesPanelPoint.y + PRIORITIES_PANEL_HEIGHT)
				&& x >= prioritiesPanelPoint.x && x < (prioritiesPanelPoint.x + PRIORITIES_PANEL_WIDTH)) {
			Point point;
			for (int i = 0; i < PRIORITIES_PANEL_NUM_ITEMS; i++) {
				point = prioritiesPanelItemsPosition.get(i);
				if (x >= point.x && x < (point.x + PRIORITIES_PANEL_ITEM_SIZE) && y >= point.y
						&& y < (point.y + PRIORITIES_PANEL_ITEM_SIZE)) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_PRIORITIES_PANEL_ITEMS_POINT.y = i;
						return MOUSE_PRIORITIES_PANEL_ITEMS_POINT;
					}
				}
				point = prioritiesPanelItemsUpPosition.get(i);
				if (x >= point.x && x < (point.x + ICON_WIDTH) && y >= point.y && y < (point.y + ICON_HEIGHT)) {
					if (!tilePrioritiesPanelUpIconAlpha[x - point.x][y - point.y]) {
						MOUSE_PRIORITIES_PANEL_ITEMS_UP_POINT.y = i;
						return MOUSE_PRIORITIES_PANEL_ITEMS_UP_POINT;
					}
				}
				point = prioritiesPanelItemsDownPosition.get(i);
				if (x >= point.x && x < (point.x + ICON_WIDTH) && y >= point.y && y < (point.y + ICON_HEIGHT)) {
					if (!tilePrioritiesPanelDownIconAlpha[x - point.x][y - point.y]) {
						MOUSE_PRIORITIES_PANEL_ITEMS_DOWN_POINT.y = i;
						return MOUSE_PRIORITIES_PANEL_ITEMS_DOWN_POINT;
					}
				}
			}
		}

		return null;
	}

	public static Point isMouseOnMessagesButtons(int x, int y) {
		if (typingPanel != null) {
			return null;
		}

		Point point;
		// Close button
		point = messagesPanelClosePoint;
		if (x >= point.x && x < (point.x + tileButtonClose.getTileWidth()) && y >= point.y
				&& y < (point.y + tileButtonClose.getTileHeight())) {
			if (!tileButtonCloseAlpha[x - point.x][y - point.y]) {
				return MOUSE_MESSAGES_PANEL_BUTTONS_CLOSE_POINT;
			}
		}

		// Types
		if (isMouseOnAnIcon(x, y, messagePanelIconPoints[MessagesPanel.TYPE_ANNOUNCEMENT],
				messagePanelTiles[MessagesPanel.TYPE_ANNOUNCEMENT],
				messagePanelTilesAlpha.get(MessagesPanel.TYPE_ANNOUNCEMENT))) {
			return MOUSE_MESSAGES_PANEL_BUTTONS_ANNOUNCEMENT_POINT;
		}
		if (isMouseOnAnIcon(x, y, messagePanelIconPoints[MessagesPanel.TYPE_COMBAT],
				messagePanelTiles[MessagesPanel.TYPE_COMBAT], messagePanelTilesAlpha.get(MessagesPanel.TYPE_COMBAT))) {
			return MOUSE_MESSAGES_PANEL_BUTTONS_COMBAT_POINT;
		}
		if (isMouseOnAnIcon(x, y, messagePanelIconPoints[MessagesPanel.TYPE_HEROES],
				messagePanelTiles[MessagesPanel.TYPE_HEROES], messagePanelTilesAlpha.get(MessagesPanel.TYPE_HEROES))) {
			return MOUSE_MESSAGES_PANEL_BUTTONS_HEROES_POINT;
		}
		if (isMouseOnAnIcon(x, y, messagePanelIconPoints[MessagesPanel.TYPE_SYSTEM],
				messagePanelTiles[MessagesPanel.TYPE_SYSTEM], messagePanelTilesAlpha.get(MessagesPanel.TYPE_SYSTEM))) {
			return MOUSE_MESSAGES_PANEL_BUTTONS_SYSTEM_POINT;
		}

		// Scrolls
		if (isMouseOnAnIcon(x, y, messagePanelIconScrollUpPoint, tileScrollUp, tileScrollUpButtonAlpha)) {
			return MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_UP_POINT;
		}
		if (isMouseOnAnIcon(x, y, messagePanelIconScrollDownPoint, tileScrollDown, tileScrollDownButtonAlpha)) {
			return MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_DOWN_POINT;
		}

		return null;
	}

	/**
	 * Indica si el mouse está en un item (o en los up/down) del panel de trade
	 * 
	 * @param x
	 * @param y
	 * @return Un punto, X es el MOUSE_ID y Y indica la posición del item en el
	 *         array correspondiente
	 */
	public static Point isMouseOnTradeButtons(int x, int y) {
		if (typingPanel != null) {
			return null;
		}

		Point point;
		// Close button
		point = tradePanelClosePoint;
		if (x >= point.x && x < (point.x + tileButtonClose.getTileWidth()) && y >= point.y
				&& y < (point.y + tileButtonClose.getTileHeight())) {
			if (!tileButtonCloseAlpha[x - point.x][y - point.y]) {
				return MOUSE_TRADE_PANEL_BUTTONS_CLOSE_POINT;
			}
		}

		if (tradePanel != null && y >= tradePanelPoint.y && y < (tradePanelPoint.y + TRADE_PANEL_HEIGHT)
				&& x >= tradePanelPoint.x && x < (tradePanelPoint.x + TRADE_PANEL_WIDTH)) {
			// Scroll up caravan
			point = tradePanel.getScrollUpCaravanPoint();
			if (x >= point.x && x < (point.x + tileScrollUp.getTileWidth()) && y >= point.y
					&& y < (point.y + tileScrollUp.getTileHeight())) {
				if (!tileScrollUpButtonAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_CARAVAN_UP_POINT;
				}
			}
			// Scroll down caravan
			point = tradePanel.getScrollDownCaravanPoint();
			if (x >= point.x && x < (point.x + tileScrollDown.getTileWidth()) && y >= point.y
					&& y < (point.y + tileScrollDown.getTileHeight())) {
				if (!tileScrollDownButtonAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_CARAVAN_DOWN_POINT;
				}
			}

			// Scroll up caravan to-buy
			point = tradePanel.getScrollUpCaravanToBuyPoint();
			if (x >= point.x && x < (point.x + tileScrollUp.getTileWidth()) && y >= point.y
					&& y < (point.y + tileScrollUp.getTileHeight())) {
				if (!tileScrollUpButtonAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN_UP_POINT;
				}
			}
			// Scroll down caravan to-buy
			point = tradePanel.getScrollDownCaravanToBuyPoint();
			if (x >= point.x && x < (point.x + tileScrollDown.getTileWidth()) && y >= point.y
					&& y < (point.y + tileScrollDown.getTileHeight())) {
				if (!tileScrollDownButtonAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN_DOWN_POINT;
				}
			}

			// Scroll up town to-sell
			point = tradePanel.getScrollUpTownToSellPoint();
			if (x >= point.x && x < (point.x + tileScrollUp.getTileWidth()) && y >= point.y
					&& y < (point.y + tileScrollUp.getTileHeight())) {
				if (!tileScrollUpButtonAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN_UP_POINT;
				}
			}
			// Scroll down town to-sell
			point = tradePanel.getScrollDownTownToSellPoint();
			if (x >= point.x && x < (point.x + tileScrollDown.getTileWidth()) && y >= point.y
					&& y < (point.y + tileScrollDown.getTileHeight())) {
				if (!tileScrollDownButtonAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN_DOWN_POINT;
				}
			}

			// Scroll up town
			point = tradePanel.getScrollUpTownPoint();
			if (x >= point.x && x < (point.x + tileScrollUp.getTileWidth()) && y >= point.y
					&& y < (point.y + tileScrollUp.getTileHeight())) {
				if (!tileScrollUpButtonAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_TOWN_UP_POINT;
				}
			}
			// Scroll down town
			point = tradePanel.getScrollDownTownPoint();
			if (x >= point.x && x < (point.x + tileScrollDown.getTileWidth()) && y >= point.y
					&& y < (point.y + tileScrollDown.getTileHeight())) {
				if (!tileScrollDownButtonAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_TOWN_DOWN_POINT;
				}
			}

			// Confirm
			point = tradePanel.getConfirmPoint();
			if (x >= point.x && x < (point.x + TradePanel.tileTradeConfirm.getTileWidth()) && y >= point.y
					&& y < (point.y + TradePanel.tileTradeConfirm.getTileHeight())) {
				if (!TradePanel.tileTradeConfirmAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_CONFIRM_POINT;
				}
			}

			// Caravan buttons
			for (int i = 0; i < tradePanel.getAlButtonPointsCaravan().size(); i++) {
				point = tradePanel.getAlButtonPointsCaravan().get(i);
				if (x >= point.x && x < (point.x + TRADE_PANEL_BUTTON_WIDTH) && y >= point.y
						&& y < (point.y + TRADE_PANEL_BUTTON_HEIGHT)) {
					if (!TradePanel.tileTradeButtonAlpha[x - point.x][y - point.y]) {
						MOUSE_TRADE_PANEL_BUTTONS_CARAVAN_POINT.y = i;
						return MOUSE_TRADE_PANEL_BUTTONS_CARAVAN_POINT;
					}
				}
			}

			// Caravan buttons to-buy
			for (int i = 0; i < tradePanel.getAlButtonPointsCaravanToBuy().size(); i++) {
				point = tradePanel.getAlButtonPointsCaravanToBuy().get(i);
				if (x >= point.x && x < (point.x + TRADE_PANEL_BUTTON_WIDTH) && y >= point.y
						&& y < (point.y + TRADE_PANEL_BUTTON_HEIGHT)) {
					if (!TradePanel.tileTradeButtonAlpha[x - point.x][y - point.y]) {
						MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN_POINT.y = i;
						return MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN_POINT;
					}
				}
			}

			// Town buttons to-sell
			for (int i = 0; i < tradePanel.getAlButtonPointsTownToSell().size(); i++) {
				point = tradePanel.getAlButtonPointsTownToSell().get(i);
				if (x >= point.x && x < (point.x + TRADE_PANEL_BUTTON_WIDTH) && y >= point.y
						&& y < (point.y + TRADE_PANEL_BUTTON_HEIGHT)) {
					if (!TradePanel.tileTradeButtonAlpha[x - point.x][y - point.y]) {
						MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN_POINT.y = i;
						return MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN_POINT;
					}
				}
			}

			// Town buttons
			for (int i = 0; i < tradePanel.getAlButtonPointsTown().size(); i++) {
				point = tradePanel.getAlButtonPointsTown().get(i);
				if (x >= point.x && x < (point.x + TRADE_PANEL_BUTTON_WIDTH) && y >= point.y
						&& y < (point.y + TRADE_PANEL_BUTTON_HEIGHT)) {
					if (!TradePanel.tileTradeButtonAlpha[x - point.x][y - point.y]) {
						MOUSE_TRADE_PANEL_BUTTONS_TOWN_POINT.y = i;
						return MOUSE_TRADE_PANEL_BUTTONS_TOWN_POINT;
					}
				}
			}

			// Buy icon
			point = tradePanel.getBuyIconPoint();
			if (x >= point.x && x < (point.x + TradePanel.tileTradeBuy.getTileWidth()) && y >= point.y
					&& y < (point.y + TradePanel.tileTradeBuy.getTileHeight())) {
				return MOUSE_TRADE_PANEL_ICON_BUY_POINT;
			}

			// Sell icon
			point = tradePanel.getSellIconPoint();
			if (x >= point.x && x < (point.x + TradePanel.tileTradeSell.getTileWidth()) && y >= point.y
					&& y < (point.y + TradePanel.tileTradeSell.getTileHeight())) {
				return MOUSE_TRADE_PANEL_ICON_SELL_POINT;
			}
		}

		return null;
	}

	/**
	 * Indica si el mouse está en un item (o en los up/down) del panel de pila
	 * 
	 * @param x
	 * @param y
	 * @return Un punto, X es el MOUSE_ID y Y indica la posición del item en el
	 *         array correspondiente
	 */
	public static Point isMouseOnPileButtons(int x, int y) {
		if (typingPanel != null) {
			return null;
		}

		Point point;
		// Close button
		point = pilePanelClosePoint;
		if (x >= point.x && x < (point.x + tileButtonClose.getTileWidth()) && y >= point.y
				&& y < (point.y + tileButtonClose.getTileHeight())) {
			if (!tileButtonCloseAlpha[x - point.x][y - point.y]) {
				return MOUSE_PILE_PANEL_BUTTONS_CLOSE_POINT;
			}
		}

		// Scrolls
		point = pilePanelIconScrollUpPoint;
		if (x >= point.x && x < (point.x + tileScrollUp.getTileWidth()) && y >= point.y
				&& y < (point.y + tileScrollUp.getTileHeight())) {
			if (!tileScrollUpButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_PILE_PANEL_BUTTONS_SCROLL_UP_POINT;
			}
		}
		point = pilePanelIconScrollDownPoint;
		if (x >= point.x && x < (point.x + tileScrollDown.getTileWidth()) && y >= point.y
				&& y < (point.y + tileScrollDown.getTileHeight())) {
			if (!tileScrollDownButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_PILE_PANEL_BUTTONS_SCROLL_DOWN_POINT;
			}
		}

		// Configuration buttons
		point = pilePanelIconConfigCopyPoint;
		if (x >= point.x && x < (point.x + tileConfigCopy.getTileWidth()) && y >= point.y
				&& y < (point.y + tileConfigCopy.getTileHeight())) {
			if (!tileConfigCopyButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_PILE_PANEL_BUTTONS_CONFIG_COPY_POINT;
			}
		}
		point = pilePanelIconConfigLockPoint;
		if (x >= point.x && x < (point.x + tileConfigLock.getTileWidth()) && y >= point.y
				&& y < (point.y + tileConfigLock.getTileHeight())) {
			if (!tileConfigLockButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_POINT;
			}
		}
		point = pilePanelIconConfigLockAllPoint;
		if (x >= point.x && x < (point.x + tileConfigLockAll.getTileWidth()) && y >= point.y
				&& y < (point.y + tileConfigLockAll.getTileHeight())) {
			if (!tileConfigLockAllButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_ALL_POINT;
			}
		}
		point = pilePanelIconConfigUnlockAllPoint;
		if (x >= point.x && x < (point.x + tileConfigUnlockAll.getTileWidth()) && y >= point.y
				&& y < (point.y + tileConfigUnlockAll.getTileHeight())) {
			if (!tileConfigUnlockAllButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_PILE_PANEL_BUTTONS_CONFIG_UNLOCK_ALL_POINT;
			}
		}

		// Items
		if (menuPile != null) {
			int iFirstIndex = pilePanelPageIndex * PILE_PANEL_MAX_ITEMS_PER_PAGE;
			int iMin = Math.min(menuPile.getItems().size() - iFirstIndex, PILE_PANEL_MAX_ITEMS_PER_PAGE);
			for (int i = 0; i < iMin; i++) {
				point = pilePanelItemPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth()) && y >= point.y
						&& y < (point.y + tileBottomItem.getTileHeight())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_PILE_PANEL_BUTTONS_ITEMS_POINT.y = i + iFirstIndex;
						return MOUSE_PILE_PANEL_BUTTONS_ITEMS_POINT;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Indica si el mouse está en un item (o en los up/down) del panel de
	 * profesiones
	 * 
	 * @param x
	 * @param y
	 * @return Un punto, X es el MOUSE_ID y Y indica la posición del item en el
	 *         array correspondiente
	 */
	public static Point isMouseOnProfessionsButtons(int x, int y) {
		if (typingPanel != null) {
			return null;
		}

		Point point;
		// Close button
		point = professionsPanelClosePoint;
		if (x >= point.x && x < (point.x + tileButtonClose.getTileWidth()) && y >= point.y
				&& y < (point.y + tileButtonClose.getTileHeight())) {
			if (!tileButtonCloseAlpha[x - point.x][y - point.y]) {
				return MOUSE_PROFESSIONS_PANEL_BUTTONS_CLOSE_POINT;
			}
		}

		// Scrolls
		point = professionsPanelIconScrollUpPoint;
		if (x >= point.x && x < (point.x + tileScrollUp.getTileWidth()) && y >= point.y
				&& y < (point.y + tileScrollUp.getTileHeight())) {
			if (!tileScrollUpButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_UP_POINT;
			}
		}
		point = professionsPanelIconScrollDownPoint;
		if (x >= point.x && x < (point.x + tileScrollDown.getTileWidth()) && y >= point.y
				&& y < (point.y + tileScrollDown.getTileHeight())) {
			if (!tileScrollDownButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_DOWN_POINT;
			}
		}

		// Items
		if (menuProfessions != null) {
			int iFirstIndex = professionsPanelPageIndex * PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE;
			int iMin = Math.min(menuProfessions.getItems().size() - iFirstIndex, PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE);
			for (int i = 0; i < iMin; i++) {
				point = professionsPanelItemPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth()) && y >= point.y
						&& y < (point.y + tileBottomItem.getTileHeight())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_PROFESSIONS_PANEL_BUTTONS_ITEMS_POINT.y = i + iFirstIndex;
						return MOUSE_PROFESSIONS_PANEL_BUTTONS_ITEMS_POINT;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Indica si el mouse está en un item (o en los up/down) del panel de trade
	 * 
	 * @param x
	 * @param y
	 * @return Un punto, X es el MOUSE_ID y Y indica la posición del item en el
	 *         array correspondiente
	 */
	public static Point isMouseOnMatsButtons(int x, int y) {
		if (typingPanel != null) {
			return null;
		}

		Point point;
		// Close button
		point = matsPanelClosePoint;
		if (x >= point.x && x < (point.x + tileButtonClose.getTileWidth()) && y >= point.y
				&& y < (point.y + tileButtonClose.getTileHeight())) {
			if (!tileButtonCloseAlpha[x - point.x][y - point.y]) {
				return MOUSE_MATS_PANEL_BUTTONS_CLOSE_POINT;
			}
		}

		// Scrolls
		point = matsPanelIconScrollUpPoint;
		if (x >= point.x && x < (point.x + tileScrollUp.getTileWidth()) && y >= point.y
				&& y < (point.y + tileScrollUp.getTileHeight())) {
			if (!tileScrollUpButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_MATS_PANEL_BUTTONS_SCROLL_UP_POINT;
			}
		}
		point = matsPanelIconScrollDownPoint;
		if (x >= point.x && x < (point.x + tileScrollDown.getTileWidth()) && y >= point.y
				&& y < (point.y + tileScrollDown.getTileHeight())) {
			if (!tileScrollDownButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_MATS_PANEL_BUTTONS_SCROLL_DOWN_POINT;
			}
		}

		// Groups
		for (int i = 0; i < MatsPanelData.numGroups; i++) {
			point = matsPanelIconPoints[i];
			if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth()) && y >= point.y
					&& y < (point.y + tileBottomItem.getTileHeight())) {
				if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
					MOUSE_MATS_PANEL_BUTTONS_GROUPS_POINT.y = i;
					return MOUSE_MATS_PANEL_BUTTONS_GROUPS_POINT;
				}
			}
		}

		// Items
		if (getMatsPanelActive() != -1) {
			int iFirstIndex = matsIndexPages[getMatsPanelActive()] * MATS_PANEL_MAX_ITEMS_PER_PAGE;
			int iMin = Math.min(MatsPanelData.tileGroups.get(getMatsPanelActive()).size() - iFirstIndex,
					MATS_PANEL_MAX_ITEMS_PER_PAGE);
			for (int i = 0; i < iMin; i++) {
				point = matsPanelItemPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth()) && y >= point.y
						&& y < (point.y + tileBottomItem.getTileHeight())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_MATS_PANEL_BUTTONS_ITEMS_POINT.y = i + iFirstIndex;
						return MOUSE_MATS_PANEL_BUTTONS_ITEMS_POINT;
					}
				}
			}
		}

		return null;
	}

	public static Point isMouseOnLivingsButtons(int x, int y) {
		if (typingPanel != null) {
			return null;
		}

		Point point;
		// Close button
		point = livingsPanelClosePoint;
		if (x >= point.x && x < (point.x + tileButtonClose.getTileWidth()) && y >= point.y
				&& y < (point.y + tileButtonClose.getTileHeight())) {
			if (!tileButtonCloseAlpha[x - point.x][y - point.y]) {
				return MOUSE_LIVINGS_PANEL_BUTTONS_CLOSE_POINT;
			}
		}

		// Scrolls
		point = livingsPanelIconScrollUpPoint;
		if (x >= point.x && x < (point.x + tileScrollUp.getTileWidth()) && y >= point.y
				&& y < (point.y + tileScrollUp.getTileHeight())) {
			if (!tileScrollUpButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_UP_POINT;
			}
		}
		point = livingsPanelIconScrollDownPoint;
		if (x >= point.x && x < (point.x + tileScrollDown.getTileWidth()) && y >= point.y
				&& y < (point.y + tileScrollDown.getTileHeight())) {
			if (!tileScrollDownButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_DOWN_POINT;
			}
		}

		if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
			// Groups subpanel
			point = livingsGroupPanelFirstIconPoint;
			if (x >= point.x && x < (point.x + tileLivingsNoGroup.getTileWidth()) && y >= point.y
					&& y < (point.y + tileLivingsNoGroup.getTileHeight())) {
				if (!tileLivingsNoGroupAlpha[x - point.x][y - point.y]) {
					return MOUSE_LIVINGS_PANEL_SGROUP_NOGROUP_POINT;
				}
			}

			// Los 10 grupos
			for (int i = 0; i < SoldierGroups.MAX_GROUPS; i++) {
				point = new Point(livingsGroupPanelFirstIconPoint.x,
						livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation);
				if (x >= point.x && x < (point.x + tileLivingsGroup.getTileWidth()) && y >= point.y
						&& y < (point.y + tileLivingsGroup.getTileHeight())) {
					if (!tileLivingsGroupAlpha[x - point.x][y - point.y]) {
						MOUSE_LIVINGS_PANEL_SGROUP_GROUP_POINT.y = i;
						return MOUSE_LIVINGS_PANEL_SGROUP_GROUP_POINT;
					}
				}
			}
		} else if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
			// Groups subpanel
			point = livingsGroupPanelFirstIconPoint;
			if (x >= point.x && x < (point.x + tileLivingsNoJobGroup.getTileWidth()) && y >= point.y
					&& y < (point.y + tileLivingsNoJobGroup.getTileHeight())) {
				if (!tileLivingsNoJobGroupAlpha[x - point.x][y - point.y]) {
					return MOUSE_LIVINGS_PANEL_CGROUP_NOGROUP_POINT;
				}
			}

			// Los 10 grupos
			for (int i = 0; i < SoldierGroups.MAX_GROUPS; i++) {
				point = new Point(livingsGroupPanelFirstIconPoint.x,
						livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation);
				if (x >= point.x && x < (point.x + tileLivingsJobGroup.getTileWidth()) && y >= point.y
						&& y < (point.y + tileLivingsJobGroup.getTileHeight())) {
					if (!tileLivingsJobGroupAlpha[x - point.x][y - point.y]) {
						MOUSE_LIVINGS_PANEL_CGROUP_GROUP_POINT.y = i;
						return MOUSE_LIVINGS_PANEL_CGROUP_GROUP_POINT;
					}
				}
			}
		}

		if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
			// Single group
			if (livingsPanelCitizensGroupActive != -1) {
				point = livingsSingleGroupRenamePoint;
				if (x >= point.x && x < (point.x + tileLivingsSingleJobGroupRename.getTileWidth()) && y >= point.y
						&& y < (point.y + tileLivingsSingleJobGroupRename.getTileHeight())) {
					if (!tileLivingsSingleJobGroupRenameAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_RENAME_POINT;
					}
				}
				point = livingsSingleGroupAutoequipPoint;
				if (x >= point.x && x < (point.x + tileLivingsRowAutoequip.getTileWidth()) && y >= point.y
						&& y < (point.y + tileLivingsRowAutoequip.getTileHeight())) {
					if (!tileLivingsRowAutoequipAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_AUTOEQUIP_POINT;
					}
				}
				point = livingsSingleGroupChangeJobsPoint;
				if (x >= point.x && x < (point.x + tileLivingsSingleGroupChangeJobs.getTileWidth()) && y >= point.y
						&& y < (point.y + tileLivingsSingleGroupChangeJobs.getTileHeight())) {
					if (!tileLivingsSingleGroupChangeJobsAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_CHANGE_JOBS_POINT;
					}
				}
				point = livingsSingleGroupDisbandPoint;
				if (x >= point.x && x < (point.x + tileLivingsSingleJobGroupDisband.getTileWidth()) && y >= point.y
						&& y < (point.y + tileLivingsSingleJobGroupDisband.getTileHeight())) {
					if (!tileLivingsSingleJobGroupDisbandAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_DISBAND_POINT;
					}
				}
			} else {
				// Restrict
				point = livingsPanelIconRestrictUpPoint;
				if (x >= point.x && x < (point.x + tileIconLevelUp.getTileWidth()) && y >= point.y
						&& y < (point.y + tileIconLevelUp.getTileHeight())) {
					if (!tileIconLevelUpAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP_POINT;
					}
				}
				point = livingsPanelIconRestrictDownPoint;
				if (x >= point.x && x < (point.x + tileIconLevelDown.getTileWidth()) && y >= point.y
						&& y < (point.y + tileIconLevelDown.getTileHeight())) {
					if (!tileIconLevelDownAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN_POINT;
					}
				}
			}
		} else if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
			// Single group
			if (livingsPanelSoldiersGroupActive != -1) {
				point = livingsSingleGroupRenamePoint;
				if (x >= point.x && x < (point.x + tileLivingsSingleGroupRename.getTileWidth()) && y >= point.y
						&& y < (point.y + tileLivingsSingleGroupRename.getTileHeight())) {
					if (!tileLivingsSingleGroupRenameAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_RENAME_POINT;
					}
				}
				point = livingsSingleGroupGuardPoint;
				if (x >= point.x && x < (point.x + tileLivingsSingleGroupGuard.getTileWidth()) && y >= point.y
						&& y < (point.y + tileLivingsSingleGroupGuard.getTileHeight())) {
					if (!tileLivingsSingleGroupGuardAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_GUARD_POINT;
					}
				}
				point = livingsSingleGroupPatrolPoint;
				if (x >= point.x && x < (point.x + tileLivingsSingleGroupPatrol.getTileWidth()) && y >= point.y
						&& y < (point.y + tileLivingsSingleGroupPatrol.getTileHeight())) {
					if (!tileLivingsSingleGroupPatrolAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_PATROL_POINT;
					}
				}
				point = livingsSingleGroupBossPoint;
				if (x >= point.x && x < (point.x + tileLivingsSingleGroupBoss.getTileWidth()) && y >= point.y
						&& y < (point.y + tileLivingsSingleGroupBoss.getTileHeight())) {
					if (!tileLivingsSingleGroupBossAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_BOSS_POINT;
					}
				}
				point = livingsSingleGroupAutoequipPoint;
				if (x >= point.x && x < (point.x + tileLivingsRowAutoequip.getTileWidth()) && y >= point.y
						&& y < (point.y + tileLivingsRowAutoequip.getTileHeight())) {
					if (!tileLivingsRowAutoequipAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_AUTOEQUIP_POINT;
					}
				}
				point = livingsSingleGroupDisbandPoint;
				if (x >= point.x && x < (point.x + tileLivingsSingleGroupDisband.getTileWidth()) && y >= point.y
						&& y < (point.y + tileLivingsSingleGroupDisband.getTileHeight())) {
					if (!tileLivingsSingleGroupDisbandAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_DISBAND_POINT;
					}
				}
			}
		} else {
			// Heroes
			// Restrict
			point = livingsPanelIconRestrictUpPoint;
			if (x >= point.x && x < (point.x + tileIconLevelUp.getTileWidth()) && y >= point.y
					&& y < (point.y + tileIconLevelUp.getTileHeight())) {
				if (!tileIconLevelUpAlpha[x - point.x][y - point.y]) {
					return MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP_POINT;
				}
			}
			point = livingsPanelIconRestrictDownPoint;
			if (x >= point.x && x < (point.x + tileIconLevelDown.getTileWidth()) && y >= point.y
					&& y < (point.y + tileIconLevelDown.getTileHeight())) {
				if (!tileIconLevelDownAlpha[x - point.x][y - point.y]) {
					return MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN_POINT;
				}
			}
		}

		// Living, equipment, civ/soldier converts, ...
		ArrayList<Integer> alLivings = getLivings();
		int iNumLivings;
		if (alLivings != null) {
			iNumLivings = alLivings.size();
		} else {
			iNumLivings = 0;
		}

		if (iNumLivings > 0) {
			int iNumPages = (iNumLivings % LIVINGS_PANEL_MAX_ROWS == 0) ? iNumLivings / LIVINGS_PANEL_MAX_ROWS
					: (iNumLivings / LIVINGS_PANEL_MAX_ROWS) + 1;
			int iIndexPage;
			boolean bNoGroupsPanel = !checkGroupsPanelEnabled(getLivingsPanelActive());
			if (bNoGroupsPanel) {
				iIndexPage = livingsDataIndexPages[getLivingsPanelActive()];
			} else {
				if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
					iIndexPage = livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive];
				} else {
					iIndexPage = livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive];
				}
			}
			if (iIndexPage > iNumPages) {
				if (bNoGroupsPanel) {
					livingsDataIndexPages[getLivingsPanelActive()] = iNumPages;
				} else {
					if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
						livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive] = iNumPages;
					} else {
						livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive] = iNumPages;
					}
				}
				iIndexPage = iNumPages;
			} else if (iIndexPage < 1) {
				if (bNoGroupsPanel) {
					livingsDataIndexPages[getLivingsPanelActive()] = 1;
				} else {
					if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
						livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive] = 1;
					} else {
						livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive] = 1;
					}
				}
				iIndexPage = 1;
			}

			int iMaxRows = Math.min(iNumLivings - ((iIndexPage - 1) * LIVINGS_PANEL_MAX_ROWS),
					livingsPanelRowPoints.length);
			iMaxRows = Math.min(iMaxRows, LIVINGS_PANEL_MAX_ROWS);

			for (int i = 0; i < iMaxRows; i++) {
				// Living
				point = livingsPanelRowPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth()) && y >= point.y
						&& y < (point.y + tileBottomItem.getTileHeight())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_POINT.y = i;
						return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_POINT;
					}
				}

				// Equipment
				point = livingsPanelRowHeadPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth()) && y >= point.y
						&& y < (point.y + tileBottomItem.getTileHeight())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD_POINT.y = i;
						return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD_POINT;
					}
				}
				point = livingsPanelRowBodyPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth()) && y >= point.y
						&& y < (point.y + tileBottomItem.getTileHeight())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY_POINT.y = i;
						return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY_POINT;
					}
				}
				point = livingsPanelRowLegsPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth()) && y >= point.y
						&& y < (point.y + tileBottomItem.getTileHeight())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS_POINT.y = i;
						return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS_POINT;
					}
				}
				point = livingsPanelRowFeetPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth()) && y >= point.y
						&& y < (point.y + tileBottomItem.getTileHeight())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET_POINT.y = i;
						return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET_POINT;
					}
				}
				point = livingsPanelRowWeaponPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth()) && y >= point.y
						&& y < (point.y + tileBottomItem.getTileHeight())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON_POINT.y = i;
						return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON_POINT;
					}
				}

				// Autoequip
				if (getLivingsPanelActive() != LIVINGS_PANEL_TYPE_HEROES) {
					point = livingsPanelRowAutoequipPoints[i];
					if (x >= point.x && x < (point.x + tileLivingsRowAutoequip.getTileWidth()) && y >= point.y
							&& y < (point.y + tileLivingsRowAutoequip.getTileHeight())) {
						if (!tileLivingsRowAutoequipAlpha[x - point.x][y - point.y]) {
							MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP_POINT.y = i;
							return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP_POINT;
						}
					}
				}

				// Civ/soldier converts
				if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
					point = livingsPanelRowConvertCivilianSoldierPoints[i];
					if (x >= point.x && x < (point.x + tileLivingsRowConvertSoldier.getTileWidth()) && y >= point.y
							&& y < (point.y + tileLivingsRowConvertSoldier.getTileHeight())) {
						if (!tileLivingsRowConvertSoldierAlpha[x - point.x][y - point.y]) {
							MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_POINT.y = i;
							return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_POINT;
						}
					}
					if (livingsPanelCitizensGroupActive == -1) {
						point = livingsPanelRowProfessionPoints[i];
						if (x >= point.x && x < (point.x + tileLivingsRowProfession.getTileWidth()) && y >= point.y
								&& y < (point.y + tileLivingsRowProfession.getTileHeight())) {
							if (!tileLivingsRowProfessionAlpha[x - point.x][y - point.y]) {
								MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS_POINT.y = i;
								return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS_POINT;
							}
						}
					}
					point = livingsPanelRowJobsGroupsPoints[i];
					if (x >= point.x && x < (point.x + tileLivingsRowJobsGroups.getTileWidth()) && y >= point.y
							&& y < (point.y + tileLivingsRowJobsGroups.getTileHeight())) {
						if (!tileLivingsRowJobsGroupsAlpha[x - point.x][y - point.y]) {
							MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_JOBS_GROUPS_ADDREMOVE_POINT.y = i;
							return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_JOBS_GROUPS_ADDREMOVE_POINT;
						}
					}
				} else if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
					point = livingsPanelRowConvertCivilianSoldierPoints[i];
					if (x >= point.x && x < (point.x + tileLivingsRowConvertCivilian.getTileWidth()) && y >= point.y
							&& y < (point.y + tileLivingsRowConvertCivilian.getTileHeight())) {
						if (!tileLivingsRowConvertCivilianAlpha[x - point.x][y - point.y]) {
							MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN_POINT.y = i;
							return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN_POINT;
						}
					}

					if (livingsPanelSoldiersGroupActive == -1) {
						point = livingsPanelRowConvertSoldierGuardPoints[i];
						if (x >= point.x && x < (point.x + tileLivingsRowConvertSoldierGuard.getTileWidth())
								&& y >= point.y && y < (point.y + tileLivingsRowConvertSoldierGuard.getTileHeight())) {
							if (!tileLivingsRowConvertSoldierGuardAlpha[x - point.x][y - point.y]) {
								MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD_POINT.y = i;
								return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD_POINT;
							}
						}
						point = livingsPanelRowConvertSoldierPatrolPoints[i];
						if (x >= point.x && x < (point.x + tileLivingsRowConvertSoldierPatrol.getTileWidth())
								&& y >= point.y && y < (point.y + tileLivingsRowConvertSoldierPatrol.getTileHeight())) {
							if (!tileLivingsRowConvertSoldierPatrolAlpha[x - point.x][y - point.y]) {
								MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL_POINT.y = i;
								return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL_POINT;
							}
						}
						point = livingsPanelRowConvertSoldierBossPoints[i];
						if (x >= point.x && x < (point.x + tileLivingsRowConvertSoldierBoss.getTileWidth())
								&& y >= point.y && y < (point.y + tileLivingsRowConvertSoldierBoss.getTileHeight())) {
							if (!tileLivingsRowConvertSoldierBossAlpha[x - point.x][y - point.y]) {
								MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS_POINT.y = i;
								return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS_POINT;
							}
						}
						point = livingsPanelRowGroupPoints[i];
						if (x >= point.x && x < (point.x + tileLivingsRowGroupAdd.getTileWidth()) && y >= point.y
								&& y < (point.y + tileLivingsRowGroupAdd.getTileHeight())) {
							if (!tileLivingsRowGroupAddAlpha[x - point.x][y - point.y]) {
								MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_ADD_POINT.y = i;
								return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_ADD_POINT;
							}
						}
					} else {
						point = livingsPanelRowGroupPoints[i];
						if (x >= point.x && x < (point.x + tileLivingsRowGroupRemove.getTileWidth()) && y >= point.y
								&& y < (point.y + tileLivingsRowGroupRemove.getTileHeight())) {
							if (!tileLivingsRowGroupRemoveAlpha[x - point.x][y - point.y]) {
								MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_REMOVE_POINT.y = i;
								return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_REMOVE_POINT;
							}
						}
					}
				}
			}
		}

		return null;
	}

}
