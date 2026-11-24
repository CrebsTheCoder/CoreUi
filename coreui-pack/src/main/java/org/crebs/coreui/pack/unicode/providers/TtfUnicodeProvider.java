package org.crebs.coreui.pack.unicode.providers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.crebs.coreui.pack.unicode.AbstractUnicodeProvider;
import org.crebs.coreui.pack.unicode.UnicodeType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TtfUnicodeProvider extends AbstractUnicodeProvider {

    @JsonProperty("size")
    public final double size;

    @JsonProperty("oversample")
    public final double oversample;

    @JsonProperty("file")
    public final String file;

    @JsonProperty("skip")
    public final ArrayList<String> skip = new ArrayList<String>();

    @JsonProperty("shift")
    public ArrayList<Integer> shift;


    /**
     * Refer to https://minecraft.wiki/w/Font_definition for more info
     */
    public TtfUnicodeProvider(char unicodeChar, String namespacedTexturePath ,double size, double oversample){
        super(UnicodeType.TTF);
        this.size = size;
        this. oversample =  oversample;
        this.file = namespacedTexturePath;

    }


    public TtfUnicodeProvider addSkipCase(String value){
        skip.add(value);
        return this;
    }

    public void setShift(int int1, int int2){

        this.shift = new ArrayList<Integer>();
        this.shift.add(int1);
        this.shift.add(int2);

    }

    @Override
    public String getStringType() {
        return "ttf";
    }

    @Override
    public @NotNull String getFilePath() {
        return "";
    }
}
