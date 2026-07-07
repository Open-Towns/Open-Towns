/**
 * Copyright 2014 by lastdigitofpi.com
 *
 * Be inspired by this source but please don't just copy it ;)
 */
package xaos.property;

import xaos.property.wrapper.IntegerPropertyWrapper;
import xaos.property.wrapper.BooleanPropertyWrapper;
import xaos.property.wrapper.StringPropertyWrapper;

/**
 * Holds all possible settings for the main towns properties file
 *
 * @author Florian Frankenberger
 */
public class MainProperties {

    private MainProperties() { /* utility class */ }

    public static Property<Integer> WINDOW_WIDTH = new Property<Integer>(PropertyFile.PROPERTY_FILE_MAIN, "WINDOW_WIDTH", IntegerPropertyWrapper.INSTANCE);
    public static Property<Integer> WINDOW_HEIGHT = new Property<Integer>(PropertyFile.PROPERTY_FILE_MAIN, "WINDOW_HEIGHT", IntegerPropertyWrapper.INSTANCE);
    public static Property<Boolean> FULLSCREEN = new Property<Boolean>(PropertyFile.PROPERTY_FILE_MAIN, "FULLSCREEN", BooleanPropertyWrapper.INSTANCE);

    public static Property<Boolean> MUSIC = new Property<Boolean>(PropertyFile.PROPERTY_FILE_MAIN, "MUSIC", BooleanPropertyWrapper.INSTANCE);
    public static Property<Integer> VOLUME_MUSIC = new Property<Integer>(PropertyFile.PROPERTY_FILE_MAIN, "VOLUME_MUSIC", IntegerPropertyWrapper.INSTANCE);

    public static Property<Boolean> FX = new Property<Boolean>(PropertyFile.PROPERTY_FILE_MAIN, "FX", BooleanPropertyWrapper.INSTANCE);
    public static Property<Integer> VOLUME_FX = new Property<Integer>(PropertyFile.PROPERTY_FILE_MAIN, "VOLUME_FX", IntegerPropertyWrapper.INSTANCE);

    public static Property<Integer> AUTOSAVE_DAYS = new Property<Integer>(PropertyFile.PROPERTY_FILE_MAIN, "AUTOSAVE_DAYS", IntegerPropertyWrapper.INSTANCE);
    public static Property<Boolean> MOUSE_SCROLL = new Property<Boolean>(PropertyFile.PROPERTY_FILE_MAIN, "MOUSE_SCROLL", BooleanPropertyWrapper.INSTANCE);
    public static Property<Boolean> MOUSE_SCROLL_EARS = new Property<Boolean>(PropertyFile.PROPERTY_FILE_MAIN, "MOUSE_SCROLL_EARS", BooleanPropertyWrapper.INSTANCE);
    public static Property<Boolean> MOUSE_2D_CUBES = new Property<Boolean>(PropertyFile.PROPERTY_FILE_MAIN, "MOUSE_2D_CUBES", BooleanPropertyWrapper.INSTANCE);
    public static Property<Boolean> ZOOM_ON_CURSOR = new Property<Boolean>(PropertyFile.PROPERTY_FILE_MAIN, "ZOOM_ON_CURSOR", BooleanPropertyWrapper.INSTANCE);
    public static Property<Boolean> DISABLED_ITEMS = new Property<Boolean>(PropertyFile.PROPERTY_FILE_MAIN, "DISABLED_ITEMS", BooleanPropertyWrapper.INSTANCE);
    public static Property<Boolean> DISABLED_GODS = new Property<Boolean>(PropertyFile.PROPERTY_FILE_MAIN, "DISABLED_GODS", BooleanPropertyWrapper.INSTANCE);
    public static Property<Boolean> PAUSE_START = new Property<Boolean>(PropertyFile.PROPERTY_FILE_MAIN, "PAUSE_START", BooleanPropertyWrapper.INSTANCE);
    public static Property<Integer> SIEGES = new Property<Integer>(PropertyFile.PROPERTY_FILE_MAIN, "SIEGES", IntegerPropertyWrapper.INSTANCE);
    public static Property<Boolean> SIEGE_PAUSE = new Property<Boolean>(PropertyFile.PROPERTY_FILE_MAIN, "SIEGE_PAUSE", BooleanPropertyWrapper.INSTANCE);
    public static Property<Boolean> CARAVAN_PAUSE = new Property<Boolean>(PropertyFile.PROPERTY_FILE_MAIN, "CARAVAN_PAUSE", BooleanPropertyWrapper.INSTANCE);
    public static Property<Boolean> ALLOW_BURY = new Property<Boolean>(PropertyFile.PROPERTY_FILE_MAIN, "ALLOW_BURY", BooleanPropertyWrapper.INSTANCE);
    public static Property<Integer> PATHFINDING_LEVEL = new Property<Integer>(PropertyFile.PROPERTY_FILE_MAIN, "PATHFINDING_LEVEL", IntegerPropertyWrapper.INSTANCE);
    public static Property<String> MODS = new Property<String>(PropertyFile.PROPERTY_FILE_MAIN, "MODS", StringPropertyWrapper.INSTANCE);
    public static Property<String> SERVERS = new Property<String>(PropertyFile.PROPERTY_FILE_MAIN, "SERVERS", StringPropertyWrapper.INSTANCE);

}
