Towns patch notes
=====

Version v15
-----
(not released yet)

- Add: Information text on the contextual menu about the lock/unlock status on the piles/containers
- Add: Two buttons on the pile/container configuration panel to lock/unlock all the configurations
- Add: Living entities (townies, enemies, animals, ...) won't "move" like crazy childs to the enemies when fight
- Add: New mayan walls (https://dl.dropboxusercontent.com/u/19115973/mayan.PNG)
- Fix: Servers timeout response is set to 2 seconds

-----

Version v14e
-----
(released: 1st september 2014)

- Add: A way to copy piles/containers configuration to other piles/containers
- Change: Normalized sound files
- Fix: Glitch opening a pile/container configuration panel when other one is already opened

-----

Version v14d
-----
(released: 18th july 2014)

- Add: New image based tutorials
- Add: Terraforming
- Add: Floating red numbers when a living takes damage
- Add: Right click over the typing panel now closes it
- Add: ESC key now closes any panel (IE. Townies panel, priorities panel, ...)
- Add: The possibility to load a saved game when you start a game (used on the tutorials)
- Add: A way to concatenate campaign/missions once you complete the current one
- Add: The regular mouse now shows the pointed area number
- Change: The townies level restriction for hauling now is also used when they perform tasks
- Change: Manual equipping of townies now shows a warning in front of the items that are on restricted levels
- Change: Scaffolds now decay after some time
- Fix: Townies now will use already built and unplaced items before the construction of new ones
- Fix: If a texture can't be loaded with the current library the game will use the old method
- Fix: Images from mods are loaded again
- Fix: Layed eggs Skootenbeetens are now feeded with blue radishes (like the non-layed ones)
- Fix: The chest image when rotated now is correct
- Performance: Seed generation when you start a new game (3-15 times faster)
- Performance: Monster generation when you start a new game (20% faster)

- Modders: New `<startingPoint>` tag on the `map_gen.xml`, inside the `<init>` tag. It's the spawning point of citizens (IE. 100,100)
- Modders: New `<tutorialFlow>` tag on the [campaigns.xml][]. Instructions and explanation of the new tags are on top of the .xml
- Modders: New `<tutorial>` tag on the [campaigns.xml][], under the `<campaign>` tag. It can be true or false (default). It tells the game in what menu should be placed the campaign
- Modders: New `<maxAgeTerrain>` on the [items.xml][]. It cointains the REAL terrain the item will turn when it dies. Used on the terraforming.

-----

Version v14c
-----

- Add: Moebius, the new guy
- Change: Update to LWJGL 2.9.1
- Change: Now fullscreen mode is stored in the towns.ini property, so the game will restart in fullscreen if you leave it in fullscreen.
- Change: Messages icon won't start blinking directly when a new game ist created just because of the Towns version information
- Fix: An item over a zone that is replaced because an action now maintains the locked status
- Fix: OSX is now supported out of the box

-----

Version v14b
-----

- Add: Snoats now need to eat wheat
- Add: Soldier level added to the tooltip in the soldiers panel
- Change: Wild cactus won't die after some time (30 days)
- Change: Removed the non-sense unlock option for certain items
- Change: Skootenbeetens now eat blue radishes instead of wheat
- Change: "Dense wind" event has been renamed to "Heavy wind"
- Fix: Issue with the minimum population required for events (used in the whisperdeath event)
- Fix: Now it's not possible to click on some trade panel buttons when the caravan is trading
- Fix: A lock is set to the burner actions to avoid all citizens to try to burn things at the same time
- Fix: The heroes with room prerequisite on a higher layer now also check for the required items
- Fix: Crash when you are on the last layer and you have activated the 2D mouse

- Modders: New tag `<canBeUnlocked>` on [items.xml][]. Possible values are false and true (default). If false the unlock option won't appear when right click an item

-----

Version v14a
-----

- Change: We set a minimum number of population (65) needed in order to spawn the whisperdead event
- Fix: A bug in the "whisperdeath" event. Also we tuned up the number of zombies per grave from 1d2 to 1d2-1
- Fix: Bone wall (block) and carving bench now aren't free to build
- Fix: Rotated graphic for the Bone bed was pointing to an empty/bad area
- Fix: Removed the tooltips on the invisible list of items when a merchant is trading
- Fix: Graphic for the stealing wolf was pointing to an empty/bad area

- Modders: New `<minPopulation>` tag on the [events.xml][] file. It stores the minimum number (not a dice) of townies needed in order to spawn the event (default is 0)

-----

Version v14
-----

- Add: Events
- Add: Item rotation (right click / rotate for already built items. For items you are building, hit the key 'F' to select which direction it should face
- Add: Townies will carry the newly created items (wood when chop, manufactured items, ...) to a nearby stockpile/container before performing another task
- Add: Locked items can be unlocked using the contextual menu
- Add: Soldiers level up
- Add: Page number on the splitted menus
- Add: New white fence, bone wall and bone roof
- Add: Cave dungeon entrances from the edges (underground sieges can spawn from there)
- Add: New audio files (caravan, immigrants and hero arrivals, shrine, events)
- Add: An option to hide the user interface (default key: U)
- Add: An option to take a screenshot (default key: F8)
- Add: Some heroes need specific items in their rooms in order to join your town
- Add: Average happiness needed to receive immigrants added to the UI
- Typo: 'human remains' for 'human corpse' on the shrine action names
- Change: New 3D mouse, now the user will select easily the cell he is pointing at
- Change: New 2D mouse, now some cubes will appear to show when the mouse is on air (toggable from the game options)
- Change: New graphics for zombies, direwolves, weretiger and golems
- Change: Smaller clock item
- Change: Bone torch now is build on the carving bench
- Change: Underground sieges spawning is limited to the edges
- Change: Mod loader now unloads the main menu texture and loads the modded one (if exists)
- Change: Heroes wounded won't go explore again until they recover the full health
- Typo: Verbs used when shooting arrows changed from "stick" to "shoot"
- Typo: Taunting trunk "headpunch" verb changed to "headbutt"
- Typo: "Hanging lamps" instead of "Hanged lamps"
- Fix: Heroes stop their path if they see a better item to equip
- Fix: Stuck trading items on merchant transactions
- Fix: Menu splitting on some circunstances, showing different menu sizes
- Fix: Caravans leaving the town when there are still transactions in place
- Fix: Bug on the autoproduction panel with actions that can use several benches (fire, low fire, stove...)
- Fix: Now the server functions doesn't allow some special characters
- Fix: The Nuxcrown level 3 happiness is fixed

- Modders: The internal mod loader now allows the modification and deletion of the stock panel tags ([matspanel.xml][])
- Modders: New [events.xml][] file. The instructions of how to script it are inside the file.
- Modders: New [gods.xml][] file. The instructions of how to script it are inside the file.
- Modders: New entries on the audio.ini file.
- Modders: `<wait>` tags on [actions.xml][] now accept audio IDs without a loop counter.
- Modders: New LOSPCT tag on the [effects.xml][]. It modifies the line of sight of the livings.
- Modders: New tag `<addGodStatus>` on [actions.xml][]. Used to increase or decrease the god happiness (status) when townies perform actions.
- Modders: New tag `<maxAgeNeedsItems>` on [items.xml][]. The item with this tag won't die until it has all those items around.
- Modders: New tag `<maxAgeNeedsItemsRadius>` on [items.xml][]. Used with `<maxAgeNeedsItems>`. Is the radius where the game checks.
- Modders: New tag `<effectsPrerequisite>` on [effects.xml][]. Is a list of effects needed on the living in order to spawn the current effect.
- Modders: New tag `<freeRoomItems>` on [heroes.xml][]. Is a list of items needed by an hero on his room to join the town.

Note: There are modding features for the gods but they are not included on v14

-----

Version v13a
-----
(release date: may 1st 2013)

- Fix: Control key issue when buying/selling items to the caravan
- Fix: Heroes near death won't go aid friends
- Fix: Heroes won't go aid other heroes of other parties
- Fix: Sacrificing animals to the shrine now works properly

-----

Version v13
-----
(release date: april 30th 2013)

- Add: Server support for buried maps (townsmods.net server added by default)
- Add: You can add text to some items, and bury them!
- Add: New writeable wooden signs
- Add: New items, books, scrolls and a shrine, an item to revive custom died heroes
- Add: Sieges have a chance to come from underground
- Add: Death messages for heroes
- Add: Heroes choose companions based on their morale. Companions will explore together
- Add: Languages now have internal mod loader support
- Add: Mods folder tip on the mods menu
- Add: Sheep are added to the butcher-for-bones menu
- Add: Average happiness added to the UI
- Add: New happiness system
- Add: Trading panel now shows the remaining trades
- Add: The control key can be used like the shift key on several panels. It will add and subtract in amounts of 100
- Add: Wild snoat and a snoat farm
- Change: Cyclop graphics
- Change: Raise and lower speed icons are now disabled when you set the min/max speed
- Change: Bamboo trap damage has been reduced to half
- Change: The option to pause the game when a caravan comes, now also pauses the game when the caravan is ready to trade
- Balance: Fire bow stats are slightly increased
- Balance: Eat a fish now fills a bit less
- Balance: Fishing set happiness cooldown has been increased
- Balance: Monsters' level boosted in lower dungeons
- Typo: Bite/bites verbs swapped on the Exploding sprouts and bats
- Typo: "it's tiny size" changed to "its tiny size" (iron short sword description)
- Typo: Spanish name for the low and burnt cooking fires
- Typo: Some 'accesible' words instead of 'accessible'
- Fix: Allies combat log colors now are correct
- Fix: Planting flowers ID in the right menu
- Fix: Lag when you have many non-available orders, has been reduced
- Fix: Werepig and throwing rock glitches
- Fix: Now the system detects all the low and burnt cooking fires to renew them
- Fix: The removal of items from containers doesn't have the "Unknown task" text anymore

- Modders: New `<text>` tag on the [items.xml][]. Possible values are true and false (default). Used to set an item to be writeable (tombs, books, scrolls, signs, ...)
- Modders: New `<lock>` tag on the queues of [actions.xml][]. Possible values are item IDs. Used to lock a bench when the action is assigned
- Modders: New `<reviveHeroes>` tag on the queues of [actions.xml][]. It doesn't require a value. Used to reset the dead status of the custom heroes
- Modders: New `<workCounterPCT>` and `<idleCounterPCT>` on [livingentities.xml][]. Value should be a number or a dice. Used on citizens, for the new happiness system
- Modders: New `<deleteCoins>` and `<deleteCoinsPCT>` tags on the queues of the [actions.xml][]. Possible values are a dice or a single number. Used to remove coins from the world
- Modders: New submenu on the decorations/outdoor for the statues and poles

-----

Version v12b
-----
(release date: april 16th 2013)

- Add: New monster, the evil turtle
- Add: New option to pause the game when a caravan comes
- Change: Werepig graphics now are isometric
- Change: Pathfinding now adds a bit more importance to the Z level
- Change: Removing an item with the contextual menu now reveals the cell under
- Change: Now the game doesn't crash if for some reason a saved game file is not found
- Fix: Following livings glitches and starts to wander off screen
- Fix: Mod loader issue when a mod adds new terrain types
- Fix: Buried architect's tables and hanged lamps are not sold by the merchants anymore

-----

Version v12a
-----
(release date: april 11th 2013)

- Add: An option to tune the CPU level used on pathfinding
- Add: Wolfy, queen of werewolves monster
- Add: New graphic for the shorn sheeps
- Add: Job group name on the townies tooltips and when right click on them
- Add: New icon for the special decorative items
- Change: Removed the butchering of reindeers from the production panel
- Change: Removed the message spam when the taunting trunk casts effects
- Typo: "Hobgoblin Khopesh" changed to "Hobgoblin khopesh"
- Typo: "Raw reindeer steak" changed to "Raw snoat steak"
- Typo: Spanish name for the Nature aid skill
- Typo: Spanish description for the candle items
- Typo: "Shorn sheep" instead of "Skinned sheep"
- Fix: Buried harps, buried purple thrones and buried graves are not sold by the merchants anymore
- Fix: Cloth and leather armors have been included to the stock panel
- Fix: Shadows over stockpiles and zones on the camera level
- Fix: Crash when you scroll down levels past the bottom layer
- Fix: Issue rendering cells when you load a non-v12 saved game that contains vertical ladders
- Fix: Glitch on the red long flags that face south-west
- Fix: Heroes avoiding the level restriction when they don't have new cells to explore

-----

Version v12
-----
(9th april 2013)

- Add: Bury system!
- Add: Integrated mod loader!
- Add: A way to restrict the hauling and autoequipping by layer
- Add: A way to restrict the heroes exploring layers
- Add: Townies happiness is showed when you right click on them
- Add: Elven names for the elf heroes
- Add: New living type, allies. Used on the spawned livings (sadonkeleton, ...). They can use doors.
- Add: Description of items in the in-game tooltip
- Change: Ladders are not needed anymore to climb 1-level underground cells
- Change: Sorted military items on the equip menus
- Change: Terrain cells on the camera level now are brighter
- Change: Boat caravan now brings animals in a cage
- Change: Plant trap won't hurt animals
- Change: Vertical ladder now have connections
- Change: Cooking time on the low cooking fire and the normal fire swapped
- Change: Heroes dead sound
- Change: Game speed now can be increased up to 5
- Change: Cyclops now have a daunting scream effect. Is casted when the cyclop is near death, it makes townies to flee
- Change: Softened the hard level sieges. Added a new "harder" level
- Change: Replaced items by scripting now allow the placement over piles and zones
- Change: Loading a wrong saved game won't end in a game crash
- Change: Anvil is now on the workshops group
- Change: Containers and walls now can be built in areas
- Content: New walls and a dome roof
- Content: Sheeps
- Content: Cloth armors
- Content: New animals in a cage items
- Content: New heroes, Vechs and The Herbalist
- Typo: Delete scaffolds typo, "Spiderite set" spanish typo
- Typo: "It's essence..." by "Its essence..." on some weapon descriptions
- Typo: "crush" by "crushes" on some weapon verbs
- Fix: Placement of patrol points on air is no longer allowed
- Fix: Issue on the actions that doesn't allow to mix moveTerrain's and pick's
- Fix: Harvest orders never done if the townie starts a battle while performing it
- Fix: Huge lag if no haulers and some idle townies in the town
- Fix: Containers now can be sold to merchants
- Fix: Improved the way townies build walls to avoid lost orders
- Fix: More stable manual launching from towns.command in paths where a space is found
- Fix: Crash "un-hauling" items from a stockpile
- Fix: Glitch on "La Pedrera" walls
- Fix: Stuck citizens after a robbery siege move some containers

- Modders: New property "id" on the `menuXXX.xml` files. Used to give a way to modded xmls to delete tags
- Modders: New property "id" on the `gen_XXX.xml` files. Used to give a way to modded xmls to delete or modify tags
- Modders: New "delete" and "deleteContent" properties on the submenus for the modded `menuXXX.xml` files. Possible values are true and false. Used to remove the original content of the menus
- Modders: New "delete" property on the items for the modded `menuXXX.xml` files. Possible values are true and false. Used to remove the original content of the menus
- Modders: New "delete" property on the `gen_XXX.xml` files. Possible values are true and false. Used to remove the original content of the `gen_XXX.xml` files
- Modders: New `<delete>` tag property on almost all the data xml files. It should contain a priority "id". Used to remove original data
- Modders: Removed the `<rangedCharges>` tag on the [items.xml][]
- Modders: The `<follow>` tag now accepts comma separated values
- Modders: New `<castTrigger>` tag on the effects. Possible values are: ALWAYS, HITTED, ENEMIES_IN_LOS, NEAR_DEATH and NOT_MAX_HP
- Modders: New `<flee>` tag on the effects. Possible values are true and false (default). It makes the citizen/hero to flee when they receive it
- Modders: New `<createItemByType>` on the [actions.xml][]. This way you can create a random item (used on the new treasure chests)
- Modders: New `<bury>` and `<buryLocked>` tags on the items. Possible values are true and false (default). Used to indicate the items that will be buried and the status when unbury them
- Modders: New `<buryItem>` and `<buryItemPCT>` tag on the items. Possible values are comma separated item IDs and percentages. Used to know what item (just one) will spawn when unbury
- Modders: New `<buryLivings>` and `<buryLivingsPCT>` tags on the items. Possible values are comma separated living IDs and spawn percentages. Used to know what livings will spawn when unbury
- Modders: New `<buryDestroyItem>` tag on the items. Possible values are true and false (default). Used to know if the unburied item do not spawn items
- Modders: New tag `<allowBury>` on the [gen_map.xml][] files. Possible values are true (default) and false. Used to prevent the bury system in a map. Used on the tutorials.
- Modders: New `<rangedOneShoot>` tag on ranged items. Possible values are true and false (default). Used on the ranged weapons monster heads

-----

Version v11a
-----

- Change: Reduced the heroes appearing rate
- Change: All the roofs but the broken one and the straw one will block fluids
- Change: Cooking a fish now takes a bit longer
- Fix: Health points being reset to a living entity base stats while receive any effect
- Fix: Snow birds missing graphics for older save games

-----

Version v11
-----

- Add: Citizen jobs and groups
- Add: Light items
- Add: Configurable FX and music volume
- Add: Fishing dock and fishing set items, used for fishing
- Add: Food variety bonus
- Add: "Interior" roofs
- Add: Possibility to set a square area for certain actions instead of a row (IE. planting wheat, building scaffolds)
- Add: New customizable priorities (move_to_caravan, build_buildings and feed_animals)
- Add: Toggable 3D mouse. Useful to build things on other layers than the current one
- Add: New walls and, a new wooden road item, a well and a pond
- Add: Ogre, werewolf, snickers, fire head and ghoul isometric graphics
- Add: New living, the Snoat! (Snow goat). It replaces the old reindeers and does
- Add: Flat blocks for the wheat and all the roofs
- Add: Chance to idle livings to move more than 1 cell
- Add: Animated living entities even when they do not move (birds, brownie bar rider, ghosts and fire head)
- Add: Enemies stats when you right click on them
- Add: New damage types and resistances to weapons, armors and monsters
- Change: Spanish names for the armor sets
- Change: Almost all the effects but the direct ones will not notify with a message when a living receives it
- Change: Military items are not displayed on the livings panel if the living have the graphic change effect (IE. Citizen turned to a pig)
- Change: Townies will stop the "Move to caravan" and the "Autoequip" tasks to eat if necessary
- Change: Big sieges will not stuck the citizens when search a path
- Change: Menu item "Delete" by "Delete scaffolds"
- Change: Dynamic pile/container configuration panel
- Change: Items and livings now are shadowed if they stand in a dark cell
- Change: Reduced drop percentage to heads and bones
- Change: Green block and green road now needs green color instead of yellow flowers to be built
- Change: Wooden block and Wooden moss block removed from the utilities type
- Change: Increased the difficulty of the last dungeon
- Change: Bone carver item has been removed from the bone armor prerequisites (still in the menu for save compatibility)
- Change: Now you can set the maximum stock you want in the "burn items" actions
- Change: Military prices increased a bit. Spider bow value reduced.
- Remove: Snow birds
- Fix: Empty group names no longer allowed
- Fix: Mini issues/typos in the xmls
- Fix: Citizens no longer will change their mining point after other citizens ends their mine tasks
- Fix: Crash when a citizen decides to drop an item when he just died (over a stockpile)
- Fix: Incorrect coins ammount was being displayed when loading a saved game (The issue didn't affect the real coins ammount)
- Fix: Occasions when a citizen has two rooms while changing personal room owner
- Fix: Tooltips in a soldier panel when the soldier list is empty
- Fix: Citizens won't move locked items on certain conditions
- Fix: Only 1 auto-production item was placed on the queue on some circumstances
- Fix: Freeze when a wounded citizen uses a 2nd level dormitory bed
- Fix: Projectiles data are loaded properly when you load a game (this avoids the "null sticks null" messages)
- Fix: Equipment menus are no longer partly rendered out of the screen

- Modders: New `<animatedWhenIdle>` tag on the [livingentities.xml][]. Possible values are true or false (default). Used on birds, brownie bat rider, ghosts and fire head
- Modders: New `<translucent>` tag on the [items.xml][]. Possible values are true or false (default). Used on windows and glass walls
- Modders: New `<lightRadius>` tag on the [items.xml][]. It allows a numerical value (default 0). Used on items that produce light
- Modders: New `<lightRed>`, `<lightGreen>` and `<lightBlue>` tag on the [items.xml][]. It allows the values FULL, HALF or NONE (default). Used on items that produce light
- Modders: New `<inverted>` tag on the [actions.xml][]. Possible valueas are true or false (default). Used on the "burn items" actions. This way the stock number acts as a maximum
- Modders: Property "COLOR" removed from the graphics.ini

-----

Version v10b
-----

- Change: Purged human remains can be used to create graves and tombs
- Fix: Crash loading a save that contains "destroy flour" tasks
- Fix: Issue with buildings under construction after the load of a saved game

-----

Version v10a
-----

- Add: Samurai helmet
- Change: Removed the maxAge from the wild wheat, wild sugar canes and wild bamboo. This way wild plants will not disappear
- Change: Golden haste helmet is now considered a special armor instead of a golden one
- Change: Market roof now uses animal hides to be built (instead of the old blue color)
- Change: Iron bars now allow fluids
- Change: Flood gates now has a glue tag and can also serve as a floor flood door
- Fix: Position of some items in the stock panel
- Fix: Crash when a citizen decides to drop an item when he just died

-----

Version v10
-----

- Add: Fluid blocking, fluid elevator and fluid allowed items
- Add: Evaporation of fluids
- Add: Wind effect to plants
- Add: Citizens sleeping under a roof will sleep "faster" and they will endure more time during their day
- Add: Town coins on the top
- Add: Flower gathering on the production menu
- Add: New stockpile and container management panel
- Add: New effect applied to food that gives a boost to the citizens' happiness
- Add: New items: Floodgate, fluids elevator, bamboo items, sugarcane items, sugar and cake
- Add: New helmets: Straw hat, wooden mask, golden mask, mayan mask, samurai mask and kerchiefs
- Add: New walls: 3 new stone walls, a new log wall, a wheat wall and a bamboo wall
- Add: New mountains map
- Change: Improved way of droping items to avoid large paths on some cases
- Change: Saved game is stored in a temporary file until the game is completelly saved. This avoids the loss of your progress if the save fails
- Change: Little rest bonus for townies sleeping underground
- Change: Flour burner removed from the menu
- Change: Linux launcher on Steam warns you if the game can't find Java (thanks to Vince D. for testing)
- Change: Brownie village area now can be tilled
- Change: Unifallow feed food changed to cactus fruits
- Change: Human remains will stop ghost spawning after 3 day
- Change: Autoequip now randomise the item to be equipped in order to avoid mass movement to the same item
- Typo: "Wild kootenbeeten" instead of "Wild skootenbeeten"
- Typo: "green hat" instead of "Green hat"
- Fix: Main menu issue when the size of the saved games is equals to the game window height
- Fix: Random freeze if the market zone contain walls and a coming caravan decide to move over a wall
- Fix: Underground terrain glitches when loading a saved game
- Fix: Citizens won't skip the chopping tasks when a tree keeper is attacked
- Fix: Falling items won't destroy the ground ones
- Fix: Townies now take into account items in containers when you order them to build an already built item

- Modders: New `<blockFluids>` tag on [items.xml][]. It allows true/false values. Default is false. (Used on the floodgate item and some walls)
- Modders: New `<allowFluids>` tag on [items.xml][]. It allows true/false values. Default is false. (Used on the 2 stone arcs)
- Modders: New `<fluidsElevator>` tag on [items.xml][]. It allows true/false values. Default is false. (Used on the fluids elevator item)
- Modders: New `<maxAgeNeedsWater>` tag on [items.xml][]. It allows true/false values. Default is false. (Used on the bamboo cans)
- Modders: New `<maxAgeNeedsWaterRadius>` tag on [items.xml][]. It allows a numerical value. (Used on the bamboo cans)
- Modders: New `<happy>` tag on [effects.xml][]. It allows a number or a dice (IE. 1d8). (Used on teh cake item)

-----

Version v9b
-----

- Change: Added 0's in front of the hours and minutes of the saved games
- Fix: Hauling issues

-----

Version v9a
-----

- Fix: Removed the creation of wild animals in the farms
- Fix: Wild animals won't turn into non-wild ones when you use them to obtain resources (IE. eggs, milk, ...)
- Fix: Stuck citizens when they are trying to perform a task with some non-accessible points

-----

Version v9
-----

- Add: Farm animals (IE. cows, pigs, ...) now need food to stay alive. Townies will bring them food when available
- Add: New savegame functions. From now on all the future builds should be savegame compatible with the old ones. This also will allow us to debug easily savegames from users
- Add: Multiple saves per map
- Add: Keybindings and new shortcuts
- Add: Military items now have a small % to have prefix/suffix when are manufactured by townies
- Add: Holding shift when you sell items to caravans will make it sell in stacks of 10
- Add: Added administration privileges to the game launcher on Windows. This should fix launch issues with some users
- Add: Isometric yetis and froggies
- Add: New bunny hat
- Change: The savegame folder will be .towns/save/ (it will not use the build name as a folder to help the future savegame compatibility)
- Change: Context menus are displayed over the "Mission completed" text
- Change: Townies will not haul from a stockpile to a barrel anymore
- Change: Options are saved every time you make a change on the main menu. The "save options" item has been removed
- Change: The mill building has been removed by a new item
- Performance: Improved hauling and containers access functions. That should remove the lag spikes on bigger towns
- Performance: Improvement in the pathfinding. It will avoid the big increase of memory usage and some lag peaks
- Typo: Gold armor spanish translation
- Typo: "Name's weapon brokes" changed to "Name's weapon broke"
- Typo: Hapiness -> Happiness in the Townies list
- Glitch: Market roof fixed
- Fix: Fluids now fall when you mine the cell under them
- Fix: Some roof priorities changed to "wall construction" priority
- Fix: Glitch with livings walking over road items
- Fix: Scripted sounds (IE. chop) are only played if the camera is in the level where the source is
- Fix: Buildings transparency now works properly
- Fix: Removing fluids with the contextual menu now changes the visibility of the cells below
- Fix: Livings on the fog of war doesn't receive effects
- Fix: If an item dissapears during a task (IE. the low fire extinguishes during the cooking) townies will search for other possible items to use (IE. a stove)

- Modders: `<prerequisite>` and `<prerequisiteFriendly>` on [buildings.xml][] now accepts comma separated values
- Modders: New `<foodNeeded>` tag on [livingentities.xml][]. It contains comma separated items. Used to indicate the possible items a friendly needs to be feed
- Modders: New `<foodNeededTurns>` tag on [livingentities.xml][]. It contains a numerical value. Used to set the number of turns where a living starts to be hungry
- Modders: New `<foodNeededDieTurns>` tag on [livingentities.xml][]. It contains a numerical value. Used to set the number of turns that a living can survive when hungry

-----

Version v8a
-----

- Fix: Crash starting the game if both FX and music are turned off

-----

Version v8
-----

- Add: New audio.ini file to script sounds to any action
- Change: Removed the `<stackable>` flag from statues and added to them the `<decoration.outdoors>` type
- Change: Numbers on the production panel now have a black border
- Change: Livings now are capable to move to a cell in other layer in emergencies
- Fix: Typo with the low cooking fire
- Fix: Soldier siege chance protection
- Fix: Trade panel can be closed with right click even if the caravan is not in place
- Fix: `<move>` actions now doesn't check on containers (this avoid stuck citizens over barrels)
- Modders: New `<fxDead>` tag to livings to script there the audio file you want to be played when a living dies
- Modders: New 'fx' and 'fxTurns' attributes to the `<wait>` tag on [actions.xml][]. This way you can script the sound to be used when townies perform actions and the cadence

-----

Version v7
-----

- Add: Cooking fire now transforms to low cooking fire and then burnt cooking fire and can be renewed at each stage
- Change: "Remove scaffold" button graphic
- Fix: Some jungle tiles names
- Fix: Glitch near some sand blocks
- Fix: Living robbery sieges doesn't make the victim drop items
- Fix: "Game has been paused" message do not appear when you receive a siege and have the pause option turned off
- Fix: Glitch with edge menus when you close a game and start a new one
- Fix: Non stackable items now don't appear in the container management menus

-----

Version v6
-----

- Add: Robbery sieges! (with a few new enemies)
- Add: Multi-monster sieges
- Add: Guard soldiers that belongs to a group move to their barracks zone when idle
- Add: Arrangements to the soundtrack
- Add: Credits option in the main menu
- Add: New option to set the pause when a siege starts
- Change: Options menu now doesn't close when click on an option
- Change: Sieges now can be set as off, easy, normal, hard or insane
- Change: The chance of a siege to appear now is based on several factors, one of them is the number of soldiers in the town
- Change: The phrase that warns you a task could not be completed
- Change: Citizens hauling items now take a break to eat/sleep if necessary
- Change: Reduced a bit the pathfinding iterations to free a bit the CPU
- Change: Improved the search items function (now it takes 20% less CPU time)
- Change: Reduced starting coins
- Change: Wooden snake sword and The trickster doesn't spam messages anymore
- Change: Brownie bat rider and Fire head now evade traps
- Change: Seeds removed from the "rawfood" type
- Change: Jungle door removed from the "furniture" type
- Fix: Issue with the display of tooltips/buttons in the livings panel
- Fix: Mini glitch with some terrains
- Fix: Mini glitch near pines and normal trees
- Fix: Glitch with some flatten tiles (cactus and some trees)
- Fix: Double bonus is no longer applied when a living equips some special military stuff
- Fix: Turning ON the music while playing no longer plays the main menu music
- Fix: Low performance on some machines when the "Flatten all blocks" option is enabled
- Fix: Flame attack now works with ranged weapons
- Fix: Saving issues solved
- Fix: Repeating an order (with the Shift key) now also works with the dig commands
- Fix: Wood detailer prerequisites are now ok
- Fix: Stuck citizens over barrels with the eat task
- Fix: Item `<habitat>` is not check when a caravan sells it
- Modders: New tags `<steal>` for enemy livings, it accepts items or types as values
- Modders: New tags `<stealLivings>` for enemy livings, it accepts livings IDs
- Modders: New tag `<startingPointID>` for the `<heightSeed>` tag

-----

Version 0.60a
-----

- Add: Strange forces have awaken in the forests
- Add: Caprontos decorative architect table
- Add: New option to allow the mouse scroll while hovering the edge buttons
- Change: Divine armor heal effect and head crushed stun effect are modified to avoid the message spamming
- Change: Stun effect name changed to "Stun attack" to avoid confusion
- Change: Roof (25%)/underground (15%) time bonus when citizens use benches to take into account the multileveling
- Change: "The lightning" will now look differently, shoot lightnings and have a new effect attached to
- Fix: Glitches near the fruit trees
- Fix: Low performance on some machines when the "Flatten blocks near mouse" option is enabled
- Fix: Military items inside containers are not carried to the caravan
- Fix: In some cases already died livings doesn't dissapear
- Fix: Typo in spanish with the wooden armor/weapon types
- Fix: Jungle mushroom will now always produce mushrooms when harvested
- Fix: Ancient wall now have a flat graphic
- Fix: Glitch in the bones caravan
- Fix: Special armors are now shown in the correct stock menu
- Fix: Road tiles connects with any other road tiles

-----

Version 0.60
-----

- Add: Caravans!
- Add: UI changed!
- Add: Soldier groups!
- Add: Shadows
- Add: New stock panel
- Add: New option in main menu to turn sieges off
- Add: Options in-game
- Add: Click on a message now gets you to the living that produced it (not just the point where the event occurs)
- Add: Added the funtion to move back and forth through heroes
- Add: Shift + click on level up/down now moves the camera 5 levels
- Add: Shift + click on the production panel icons now increases/reduces the ammounts by 5
- Add: Flatten blocks near the cursor option in the UI
- Change: Siege enemies now choose random citizens when target
- Change: The game map now is saved (and loaded) secuent

[campaigns.xml]: ./src/data/campaigns.xml
[items.xml]: ./src/data/items.xml
[events.xml]: ./src/data/events.xml
[matspanel.xml]: ./src/data/matspanel.xml
[gods.xml]: ./src/data/gods.xml
[actions.xml]: ./src/data/actions.xml
[effects.xml]: ./src/data/effects.xml
[heroes.xml]: ./src/data/heroes.xml
[livingentities.xml]: ./src/data/livingentities.xml
[buildings.xml]: ./src/data/buildings.xml