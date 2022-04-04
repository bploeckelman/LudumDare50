package lando.systems.ld50.audio;

import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import lando.systems.ld50.Main;
import lando.systems.ld50.assets.Assets;

public class AudioManager implements Disposable {

    public MutableFloat soundVolume;
    public MutableFloat musicVolume;

    public static boolean isMusicMuted;
    public static boolean isSoundMuted;

    // none should not have a sound
    public enum Sounds {
        none,
        chaching,
        cheer,
        click,
        collapse,
        earth,
        explode,
        goodKarma,
        badKarma,
        helicopter,
        laser,
        rumble,
        screamFemale,
        screamMale,
        thud,
        houseImpact
    }

    public enum Musics {
        none,
        main,
        introMusic,
        outroMusic,
        mainTheme
    }

    public ObjectMap<Sounds, SoundContainer> sounds = new ObjectMap<>();
    public ObjectMap<Musics, Music> musics = new ObjectMap<>();

    public Music currentMusic;
    public Musics eCurrentMusic;
    public Music oldCurrentMusic;

    private final Assets assets;
    private final TweenManager tween;

    public AudioManager(Main game) {
        this.assets = game.assets;
        this.tween = game.tween;

        putSound(Sounds.chaching, assets.chachingSound);
        putSound(Sounds.click, assets.click1Sound);
        putSound(Sounds.earth, assets.earthSound);
        putSound(Sounds.cheer, assets.cheer1Sound);
        putSound(Sounds.cheer, assets.cheer2Sound);
        putSound(Sounds.collapse, assets.collapse1Sound);
        putSound(Sounds.explode, assets.explode1Sound);
        putSound(Sounds.explode, assets.explode2Sound);
        putSound(Sounds.goodKarma, assets.harp1Sound);
        putSound(Sounds.badKarma, assets.guitar1Sound);
        putSound(Sounds.helicopter, assets.helicopter1Sound);
        putSound(Sounds.helicopter, assets.helicopter2Sound);
        putSound(Sounds.laser, assets.laser1Sound);
        putSound(Sounds.laser, assets.laser2Sound);
        putSound(Sounds.rumble, assets.rumble1Sound);
        putSound(Sounds.rumble, assets.rumble2Sound);
        putSound(Sounds.rumble, assets.rumble3Sound);
        putSound(Sounds.rumble, assets.rumble4Sound);
        putSound(Sounds.screamFemale, assets.screamFemaleSound);
        putSound(Sounds.screamFemale, assets.screamFemale2Sound);
        putSound(Sounds.screamFemale, assets.screamFemale3Sound);
        putSound(Sounds.screamFemale, assets.screamMale1Sound);
        putSound(Sounds.screamFemale, assets.screamMale2Sound);
        putSound(Sounds.screamFemale, assets.screamMale3Sound);
        putSound(Sounds.thud, assets.thud1Sound);
        putSound(Sounds.thud, assets.thud2Sound);
        putSound(Sounds.houseImpact, assets.houseImpact1);
        putSound(Sounds.houseImpact, assets.houseImpact2);
        putSound(Sounds.houseImpact, assets.houseImpact3);
//        putSound(Sounds.houseImpact, assets.houseImpact4);
//        putSound(Sounds.houseImpact, assets.houseImpact5);
        putSound(Sounds.houseImpact, assets.explode1Sound);
        putSound(Sounds.houseImpact, assets.explode2Sound);
        putSound(Sounds.houseImpact, assets.explode3Sound);
        putSound(Sounds.houseImpact, assets.explode4Sound);
//        putSound(Sounds., assets.);
//        putSound(Sounds., assets.);

        musics.put(Musics.main, assets.mainMusic);
        musics.put(Musics.mainTheme, assets.mainTheme);
        musics.put(Musics.introMusic, assets.introMusic);
        musics.put(Musics.outroMusic, assets.outroMusic);

        musicVolume = new MutableFloat(.5f); 
        soundVolume = new MutableFloat(.5f);

        isMusicMuted = false;
        isSoundMuted = false;
//        musicVolume = new MutableFloat(Prefs.getFloat(Prefs.Name.music_volume_float));
//        soundVolume = new MutableFloat(Prefs.getFloat(Prefs.Name.sound_volume_float));
//
//        isMusicMuted = Prefs.getBool(Prefs.Name.music_muted_bool);
//        isSoundMuted = Prefs.getBool(Prefs.Name.sound_muted_bool);

    }

    public void update(float dt) {
//        Gdx.app.log("volume", String.valueOf(musicVolume.floatValue()));
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume.floatValue());
            currentMusic.play();
        }

        if (oldCurrentMusic != null) {
            oldCurrentMusic.setVolume(musicVolume.floatValue());
        }
    }

    @Override
    public void dispose() {
        Sounds[] allSounds = Sounds.values();
        for (Sounds sound : allSounds) {
            if (sounds.get(sound) != null) {
                sounds.get(sound).dispose();
            }
        }
        Musics[] allMusics = Musics.values();
        for (Musics music : allMusics) {
            if (musics.get(music) != null) {
                musics.get(music).dispose();
            }
        }
        currentMusic = null;
    }

    public void putSound(Sounds soundType, Sound sound) {
        SoundContainer soundCont = sounds.get(soundType);
        if (soundCont == null) {
            soundCont = new SoundContainer();
        }

        soundCont.addSound(sound);
        sounds.put(soundType, soundCont);
    }

    public long playSound(Sounds soundOption) {
        if (isSoundMuted || soundOption == Sounds.none) return -1;
        return playSound(soundOption, soundVolume.floatValue());
    }

    public long playSound(Sounds soundOption, float volume) {
        volume = volume * soundVolume.floatValue();
        if (isSoundMuted || soundOption == Sounds.none) return -1;

        SoundContainer soundCont = sounds.get(soundOption);
        if (soundCont == null) {
            // Gdx.app.log("NoSound", "No sound found for " + soundOption.toString());
            return 0;
        }

        Sound s = soundCont.getSound();
        return (s != null) ? s.play(volume) : 0;
    }

    public long playSound(Sounds soundOption, float volume, float pitch, float pan) {
        volume = volume * soundVolume.floatValue();
        if (isSoundMuted || soundOption == Sounds.none) return -1;

        SoundContainer soundCont = sounds.get(soundOption);
        if (soundCont == null) {
            // Gdx.app.log("NoSound", "No sound found for " + soundOption.toString());
            return 0;
        }

        Sound s = soundCont.getSound();
        return (s != null) ? s.play(volume, pitch, pan) : 0;
    }

    public long playDirectionalSoundFromVector(Sounds soundOption, Vector2 vector, float viewportWidth) {
        if (isSoundMuted || soundOption == Sounds.none) return -1;

        SoundContainer soundCont = sounds.get(soundOption);
        if (soundCont == null) {
            // Gdx.app.log("NoSound", "No sound found for " + soundOption.toString());
            return 0;
        }

        Sound s = soundCont.getSound();
        float midWidth = viewportWidth / 2f;
        float pan = -1 * (midWidth - vector.x) / midWidth;
        Gdx.app.log("pan: ", String.valueOf(pan));

        return (s != null) ? s.play(soundVolume.floatValue(), 1f, pan) : 0;
    }

    public void stopSound(Sounds soundOption) {
        SoundContainer soundCont = sounds.get(soundOption);
        if (soundCont != null) {
            soundCont.stopSound();
        }
    }

    public void stopAllSounds() {
        for (SoundContainer soundCont : sounds.values()) {
            if (soundCont != null) {
                soundCont.stopSound();
            }
        }
    }

    public Music playMusic(Musics musicOptions) {
        return playMusic(musicOptions, true);
    }

    public Music playMusic(Musics musicOptions, boolean playImmediately) {
        return playMusic(musicOptions, playImmediately, true);
    }

    public Music playMusic(Musics musicOptions, boolean playImmediately, boolean looping) {
        if (playImmediately) {
            if (currentMusic != null && currentMusic.isPlaying()) {
                currentMusic.stop();
            }
            // fade in out streams
            currentMusic = startMusic(musicOptions, looping);
        } else {
            if (currentMusic == null || !currentMusic.isPlaying()) {
                currentMusic = startMusic(musicOptions, looping);
            } else {
                currentMusic.setLooping(false);
                currentMusic.setOnCompletionListener(music -> {
                    currentMusic = startMusic(musicOptions, looping);
                });
            }
        }
        return currentMusic;
    }

    private Music startMusic(Musics musicOptions, boolean looping) {
        Music music = musics.get(musicOptions);
        if (music != null) {
            music.setVolume(musicVolume.floatValue());
            music.setLooping(looping);
            music.play();
        }
        return music;
    }

    public void fadeMusic(Musics musicOption) {
        if (eCurrentMusic == musicOption) return;

    }

    public void stopMusic() {
        for (Music music : musics.values()) {
            if (music != null) music.stop();
        }
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }

    public void setMusicVolume(float level) {
        //Prefs.put(Prefs.Name.music_volume_float, level);
        if (isMusicMuted)
            musicVolume.setValue(0f);
        else
            musicVolume.setValue(level);
    }
    public void setSoundVolume(float level) {
        //Prefs.put(Prefs.Name.sound_volume_float, level);
        if (isSoundMuted)
            soundVolume.setValue(0f);
        else
            soundVolume.setValue(level);
    }
//
//    public void setMusicMuted(boolean isMuted) {
//        Prefs.put(Prefs.Name.music_muted_bool, isMuted);
//        isMusicMuted = isMuted;
//        musicVolume.setValue(isMuted ? 0f : Prefs.getFloat(Prefs.Name.music_volume_float));
//    }
//
//    public void setSoundMuted(boolean isMuted) {
//        Prefs.put(Prefs.Name.sound_muted_bool, isMuted);
//        isSoundMuted = isMuted;
//        soundVolume.setValue(isMuted ? 0f : Prefs.getFloat(Prefs.Name.sound_volume_float));
//    }

}

class SoundContainer {
    public Array<Sound> sounds;
    public Sound currentSound;

    public SoundContainer() {
        sounds = new Array<Sound>();
    }

    public void addSound(Sound s) {
        if (!sounds.contains(s, false)) {
            sounds.add(s);
        }
    }

    public Sound getSound() {
        if (sounds.size > 0) {
            int randIndex = MathUtils.random(0, sounds.size - 1);
            Sound s = sounds.get(randIndex);
            currentSound = s;
            return s;
        } else {
            // Gdx.app.log("No sounds found!");
            return null;
        }
    }

    public void stopSound() {
        if (currentSound != null) {
            currentSound.stop();
        }
    }

    public void dispose() {
        if (currentSound != null) {
            currentSound.dispose();
        }
    }
}