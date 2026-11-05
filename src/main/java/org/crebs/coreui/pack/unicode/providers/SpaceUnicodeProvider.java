package org.crebs.coreui.pack.unicode.providers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.crebs.coreui.pack.unicode.AbstractUnicodeProvider;
import org.crebs.coreui.pack.unicode.UnicodeType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SpaceUnicodeProvider extends AbstractUnicodeProvider {
    @JsonProperty("advances")
    public final HashMap<String, Double> advances = new HashMap<>();


    /**
     * Refer to https://minecraft.wiki/w/Resource_pack#Fonts for more info
     * must be before chars definition, or it will not work
     */
    @JsonIgnore
    public SpaceUnicodeProvider(){
        super(UnicodeType.SPACE);

    }

    public SpaceUnicodeProvider addAdvancesCase(@NotNull String key, @NotNull double value){
        advances.put(key, value);
        return this;
    }

    @Override
    public String getStringType() {
        return "space";
    }

    @Override
    public @NotNull String getFilePath() {
        return "";
    }
}
