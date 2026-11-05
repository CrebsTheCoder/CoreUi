package org.crebs.coreui.pack.unicode.providers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.crebs.coreui.pack.unicode.AbstractUnicodeProvider;
import org.crebs.coreui.pack.unicode.UnicodeType;

public class BitmapUnicodeProvider extends AbstractUnicodeProvider {

    @JsonProperty("ascent")
    public final double ascent;

    @JsonProperty("height")
    public final double height;

    @JsonProperty("file")
    public final String file;

    @JsonProperty("chars")
    public final String[] chars;


    /**
     * Refer to https://minecraft.wiki/w/Resource_pack#Fonts for more info
     */
    @JsonIgnore
    public BitmapUnicodeProvider(char unicodeChar, String namespacedTexturePath, double ascent, double height){
        super(UnicodeType.BITMAP);
        this.ascent = ascent;
        this.height = height;
        this.file = namespacedTexturePath;
        chars = new String[]{String.valueOf(unicodeChar)};
    }


    /** can be used to calculate a proper texture split for sprite sheets
    *    ArrayList<String> chars = new ArrayList<String>();
    *    ArrayList<String> returnChars = new ArrayList<String>();
    * <p>
    *   int w = spriteSheetSize.width / spriteSheetGridSize.width;
    *   int h = spriteSheetSize.height / spriteSheetGridSize.width;
    * <p>
    *
    *        for (int y = 0; y < h; y++) {
    *        String rowOfChars = "";
    * <p>
    *        for (int x = 0; x < w; x++) {
    *            rowOfChars = rowOfChars.concat(String.valueOf((char)currentUnicodeValue));
    *            returnChars.add(String.valueOf((char)currentUnicodeValue));
    *            currentUnicodeValue++;
    *        }
    * <p>
    *        chars.add(rowOfChars);
    *  }
    */
    @JsonIgnore
    public BitmapUnicodeProvider(String[] unicodeChars, String namespacedTexturePath, double ascent, double height){
        super(UnicodeType.BITMAP);
        this.ascent = ascent;
        this.height = height;
        chars = unicodeChars;
        this.file = namespacedTexturePath;
    }

    @JsonIgnore
    public String getStringType() {
        return "bitmap";
    }
}
