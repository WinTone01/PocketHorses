package it.pika.pockethorses.enums;

import it.pika.pockethorses.Main;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;

@AllArgsConstructor
public enum Language {

    ENGLISH(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_en.yml")),
    CZECH(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_cs.yml")),
    DUTCH(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_nl.yml")),
    FRENCH(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_fr.yml")),
    GERMAN(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_de.yml")),
    HUNGARIAN(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_hu.yml")),
    ITALIAN(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_it.yml")),
    PORTUGUESE(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_pt.yml")),
    SPANISH(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_es.yml")),
    CHINESE(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_zh.yml")),
    TURKISH(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_tr.yml"));

    @Getter
    private final File file;

    public static Language fromFile(File file) {
        for (Language value : values()) {
            if (!value.getFile().getName().equalsIgnoreCase(file.getName()))
                continue;

            return value;
        }

        return ENGLISH;
    }

}
