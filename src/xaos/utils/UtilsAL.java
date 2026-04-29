package xaos.utils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import xaos.Towns;
import xaos.main.Game;

public final class UtilsAL {

    public final static String SOURCE_MUSIC_MAINMENU = "musicMM"; //$NON-NLS-1$
    public final static String SOURCE_MUSIC_INGAME = "musicIG"; //$NON-NLS-1$

    public final static String SOURCE_FX_CLICK = "fxclick"; //$NON-NLS-1$

//	public final static String SOURCE_FX_CHOP = 6;
    public final static String SOURCE_FX_MINE = "fxmine"; //$NON-NLS-1$
//	public final static String SOURCE_FX_DIG = 8;
    public final static String SOURCE_FX_EAT = "fxeat"; //$NON-NLS-1$

    public final static String SOURCE_FX_DEAD = "fxdead"; //$NON-NLS-1$

    public final static String SOURCE_FX_BUILDING = "fxbuilding"; //$NON-NLS-1$

    private static class AudioData {
        final int bufferId;
        final int sourceId;
        final boolean isMusic;

        AudioData(int bufferId, int sourceId, boolean isMusic) {
            this.bufferId = bufferId;
            this.sourceId = sourceId;
            this.isMusic = isMusic;
        }

        boolean isPlaying() {
            return AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
        }

        void play(boolean loop) {
            AL10.alSourcei(sourceId, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
            AL10.alSourcePlay(sourceId);
        }

        void stop() {
            AL10.alSourceStop(sourceId);
        }

        void setGain(float gain) {
            AL10.alSourcef(sourceId, AL10.AL_GAIN, gain);
        }
    }

    private static HashMap<String, AudioData> hmAudio;
    private static boolean openALON;
    private static long alDevice;
    private static long alContext;
    private static float musicVolume = 1.0f;
    private static float fxVolume = 1.0f;

    /**
     * Carga los ficheros de audio
     *
     * @return true si todo ok
     */
    private static boolean loadALData() {
        hmAudio = new HashMap<String, AudioData>();

        Properties propsAudio = new Properties();
        try {
            propsAudio.load(new FileInputStream("audio.ini")); //$NON-NLS-1$

            // Mods
            File fUserFolder = new File(Game.getUserFolder());
            if (fUserFolder.exists() && fUserFolder.isDirectory()) {
                ArrayList<String> alMods = Game.getModsLoaded();
                if (alMods != null && alMods.size() > 0) {
                    for (int i = 0; i < alMods.size(); i++) {
                        String sModAudioIniPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + alMods.get(i) + System.getProperty("file.separator") + "audio.ini";
                        File fIni = new File(sModAudioIniPath);
                        if (fIni.exists()) {
                            propsAudio.load(new FileInputStream(fIni));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Iterator<String> itKeys = propsAudio.stringPropertyNames().iterator();
        String sKey;
        while (itKeys.hasNext()) {
            sKey = itKeys.next();
            if (!loadAudio(sKey, propsAudio.getProperty(sKey))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Carga un fichero de audio en la hash
     *
     * @param sKey
     * @param sFile Fichero, si se le pasa null (o vacio) no pasa nada, mete
     * null en la hash
     * @return
     */
    private static boolean loadAudio(String sKey, String sFile) {
        String sFilePath = xaos.Towns.getPropertiesString("AUDIO_FOLDER") + sFile; //$NON-NLS-1$

        if (sFile == null || sFile.trim().length() == 0) {
            hmAudio.put(sKey, null);
            return true;
        }

        // Si el fichero no existe miraremos las carpetas de los mods activos
        File fAudio = new File(sFilePath);
        if (!fAudio.exists()) {
            // Mods
            File fUserFolder = new File(Game.getUserFolder());
            if (fUserFolder.exists() && fUserFolder.isDirectory()) {
                ArrayList<String> alMods = Game.getModsLoaded();
                if (alMods != null && alMods.size() > 0) {
                    for (int i = 0; i < alMods.size(); i++) {
                        String sModAudioPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + alMods.get(i) + System.getProperty("file.separator") + xaos.Towns.getPropertiesString("AUDIO_FOLDER") + sFile;
                        File fIni = new File(sModAudioPath);
                        if (fIni.exists()) {
                            sFilePath = sModAudioPath;
                            break;
                        }
                    }
                }
            }
        }

        // Cargar OGG con STB Vorbis
        try {
            byte[] fileBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(sFilePath));
            ByteBuffer fileBuffer = MemoryUtil.memAlloc(fileBytes.length);
            fileBuffer.put(fileBytes).flip();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer channelsBuf = stack.mallocInt(1);
                IntBuffer sampleRateBuf = stack.mallocInt(1);
                ShortBuffer pcm = STBVorbis.stb_vorbis_decode_memory(fileBuffer, channelsBuf, sampleRateBuf);
                MemoryUtil.memFree(fileBuffer);

                if (pcm == null) {
                    Log.log(Log.LEVEL_ERROR, Messages.getString("UtilsAL.5") + sFilePath + "]", "UtilsAL"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    return false;
                }

                int channels = channelsBuf.get(0);
                int sampleRate = sampleRateBuf.get(0);
                int format = channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;

                int bufferId = AL10.alGenBuffers();
                AL10.alBufferData(bufferId, format, pcm, sampleRate);
                MemoryUtil.memFree(pcm);

                int sourceId = AL10.alGenSources();
                AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferId);

                boolean isMusic = isMusic(sKey);
                AudioData audioData = new AudioData(bufferId, sourceId, isMusic);
                hmAudio.put(sKey, audioData);
            }
            return true;
        } catch (Exception e) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("UtilsAL.5") + sFilePath + "]", "UtilsAL"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return false;
        }
    }

    public static void initAL(int musicVolumeInt, int FXVolumeInt) {
        if (Game.isMusicON() || Game.isFXON()) {
            if (!isOpenALON()) {
                // Inicializar OpenAL
                alDevice = ALC10.alcOpenDevice((ByteBuffer) null);
                if (alDevice == 0) {
                    Log.log(Log.LEVEL_ERROR, "Failed to open OpenAL device", "UtilsAL"); //$NON-NLS-1$ //$NON-NLS-2$
                    Game.exit();
                    return;
                }
                ALCCapabilities deviceCaps = ALC.createCapabilities(alDevice);
                alContext = ALC10.alcCreateContext(alDevice, (IntBuffer) null);
                ALC10.alcMakeContextCurrent(alContext);
                AL.createCapabilities(deviceCaps);

                if (!loadALData()) {
                    Log.log(Log.LEVEL_ERROR, Messages.getString("UtilsAL.8"), "UtilsAL"); //$NON-NLS-1$ //$NON-NLS-2$
                    Game.exit();
                    return;
                }
                setOpenALON(true);
                setMusicVolume(musicVolumeInt);
                setFXVolume(FXVolumeInt);
            }
        }
    }

    public static void play(String sKey, int level) {
        if (level == Game.getWorld().getView().z) {
            play(sKey);
        }
    }

    public static void play(String sKey) {
        if (!Game.isMusicON() && !Game.isFXON()) return;
        if (hmAudio == null) return;
        AudioData audio = hmAudio.get(sKey);
        if (audio != null) {
            if (audio.isMusic) {
                if (!audio.isPlaying() && Game.isMusicON()) {
                    audio.play(true); // loop music
                }
            } else {
                if (Game.isFXON()) {
                    // Para FX: parar y reproducir de nuevo para permitir re-disparo
                    audio.stop();
                    audio.play(false);
                }
            }
        }
    }

    public static void stop(String sKey) {
        if (hmAudio == null) {
            return;
        }

        AudioData audio = hmAudio.get(sKey);
        if (audio != null && audio.isPlaying()) audio.stop();
    }

    public static void stopMusic() {
        if (hmAudio == null) {
            return;
        }

        for (Map.Entry<String, AudioData> entry : hmAudio.entrySet()) {
            if (entry.getValue() != null && isMusic(entry.getKey()) && entry.getValue().isPlaying()) {
                entry.getValue().stop();
            }
        }
    }

    public static void stopFX() {
        if (hmAudio == null) {
            return;
        }

        for (Map.Entry<String, AudioData> entry : hmAudio.entrySet()) {
            if (entry.getValue() != null && !isMusic(entry.getKey()) && entry.getValue().isPlaying()) {
                entry.getValue().stop();
            }
        }
    }

    public static void setMusicVolume(int iVolume) {
        musicVolume = 0.1f * iVolume;
        if (hmAudio == null) return;
        for (Map.Entry<String, AudioData> entry : hmAudio.entrySet()) {
            if (entry.getValue() != null && isMusic(entry.getKey())) {
                entry.getValue().setGain(musicVolume);
            }
        }
    }

    public static void setFXVolume(int iVolume) {
        fxVolume = 0.1f * iVolume;
        if (hmAudio == null) return;
        for (Map.Entry<String, AudioData> entry : hmAudio.entrySet()) {
            if (entry.getValue() != null && !isMusic(entry.getKey())) {
                entry.getValue().setGain(fxVolume);
            }
        }
    }

    public static void destroy() {
        if (hmAudio != null) {
            for (AudioData audio : hmAudio.values()) {
                if (audio != null) {
                    AL10.alDeleteSources(audio.sourceId);
                    AL10.alDeleteBuffers(audio.bufferId);
                }
            }
            hmAudio = null;
        }
        if (alContext != 0) {
            ALC10.alcMakeContextCurrent(0);
            ALC10.alcDestroyContext(alContext);
            alContext = 0;
        }
        if (alDevice != 0) {
            ALC10.alcCloseDevice(alDevice);
            alDevice = 0;
        }
    }

    private static boolean isMusic(String sKey) {
        return sKey != null && (sKey.equals(SOURCE_MUSIC_MAINMENU) || sKey.equals(SOURCE_MUSIC_INGAME));
    }

    public static void setOpenALON(boolean openALON) {
        UtilsAL.openALON = openALON;
    }

    public static boolean isOpenALON() {
        return openALON;
    }

    public static boolean exists(String fxKey) {
        if (hmAudio != null) {
            return hmAudio.containsKey(fxKey);
        } else {
            return true;
        }
    }

    public static void clearPropertiesAudio() {
        if (hmAudio != null) {
            stopFX();
            stopMusic();
            for (AudioData audio : hmAudio.values()) {
                if (audio != null) {
                    AL10.alDeleteSources(audio.sourceId);
                    AL10.alDeleteBuffers(audio.bufferId);
                }
            }
            hmAudio.clear();
            hmAudio = null;
            loadALData();
        }
    }
}
