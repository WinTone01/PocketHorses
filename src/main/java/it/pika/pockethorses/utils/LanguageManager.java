package it.pika.pockethorses.utils;

import com.tchristofferson.configupdater.ConfigUpdater;
import it.pika.libs.config.Config;
import it.pika.pockethorses.Main;
import it.pika.pockethorses.enums.Language;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.util.List;

public class LanguageManager {

    @Getter
    private Language language;

    public void init() {
        createFiles();
        var configLanguage = Main.getConfigFile().getString("Options.Language");

        var languageFile = new File(Main.getInstance().getDataFolder() + File.separator + "Languages"
                + File.separator + "messages_%s.yml".formatted(configLanguage));
        if (!languageFile.exists()) {
            Main.getConsole().warning("Language not found: %s. Using 'en' instead.".formatted(configLanguage));
            language = Language.ENGLISH;
            return;
        }

        language = Language.fromFile(languageFile);
        Main.getConsole().info("Using language: %s".formatted(language.name()));
    }

    @SneakyThrows
    private void createFiles() {
        var folder = new File(Main.getInstance().getDataFolder(), "Languages");
        if (!folder.exists())
            folder.mkdir();

        var cs = new Config(Main.getInstance(), new File(Main.getInstance().getDataFolder()
                + File.separator + "Languages" + File.separator + "messages_cs.yml"), true);
        var de = new Config(Main.getInstance(), new File(Main.getInstance().getDataFolder()
                + File.separator + "Languages" + File.separator + "messages_de.yml"), true);
        var en = new Config(Main.getInstance(), new File(Main.getInstance().getDataFolder()
                + File.separator + "Languages" + File.separator + "messages_en.yml"), true);
        var es = new Config(Main.getInstance(), new File(Main.getInstance().getDataFolder()
                + File.separator + "Languages" + File.separator + "messages_es.yml"), true);
        var fr = new Config(Main.getInstance(), new File(Main.getInstance().getDataFolder()
                + File.separator + "Languages" + File.separator + "messages_fr.yml"), true);
        var hu = new Config(Main.getInstance(), new File(Main.getInstance().getDataFolder()
                + File.separator + "Languages" + File.separator + "messages_hu.yml"), true);
        var it = new Config(Main.getInstance(), new File(Main.getInstance().getDataFolder()
                + File.separator + "Languages" + File.separator + "messages_it.yml"), true);
        var nl = new Config(Main.getInstance(), new File(Main.getInstance().getDataFolder()
                + File.separator + "Languages" + File.separator + "messages_nl.yml"), true);
        var pt = new Config(Main.getInstance(), new File(Main.getInstance().getDataFolder()
                + File.separator + "Languages" + File.separator + "messages_pt.yml"), true);
        var zh = new Config(Main.getInstance(), new File(Main.getInstance().getDataFolder()
                + File.separator + "Languages" + File.separator + "messages_zh.yml"), true);

        ConfigUpdater.update(Main.getInstance(), "Languages/messages_cs.yml", cs.getFile());
        ConfigUpdater.update(Main.getInstance(), "Languages/messages_de.yml", de.getFile());
        ConfigUpdater.update(Main.getInstance(), "Languages/messages_en.yml", en.getFile());
        ConfigUpdater.update(Main.getInstance(), "Languages/messages_es.yml", es.getFile());
        ConfigUpdater.update(Main.getInstance(), "Languages/messages_fr.yml", fr.getFile());
        ConfigUpdater.update(Main.getInstance(), "Languages/messages_hu.yml", hu.getFile());
        ConfigUpdater.update(Main.getInstance(), "Languages/messages_it.yml", it.getFile());
        ConfigUpdater.update(Main.getInstance(), "Languages/messages_nl.yml", nl.getFile());
        ConfigUpdater.update(Main.getInstance(), "Languages/messages_pt.yml", pt.getFile());
        ConfigUpdater.update(Main.getInstance(), "Languages/messages_zh.yml", zh.getFile());

        cs.reload();
        de.reload();
        en.reload();
        es.reload();
        fr.reload();
        hu.reload();
        it.reload();
        nl.reload();
        pt.reload();
        zh.reload();
    }

    public List<String> getMainHelp() {
        return new Config(Main.getInstance(), language.getFile(), false).getStringList("help-message");
    }

    public List<String> getHorsesHelp() {
        return new Config(Main.getInstance(), language.getFile(), false).getStringList("horses-help-message");
    }

}
