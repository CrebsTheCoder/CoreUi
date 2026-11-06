package org.crebs.coreui.pack.unicode.providers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.crebs.coreui.pack.unicode.AbstractUnicodeProvider;
import org.crebs.coreui.pack.unicode.UnicodeType;
import org.jetbrains.annotations.NotNull;


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
     * Refer to https://minecraft.wiki/w/Font_definition for more info
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
     * <p>
     *  NOTE: for some reason one font entry can take more than one character,
     *  minecraft handles this by splitting the texture across all the provided fonts.
     *  The equation for this is: length in pixels of texture/number of chars.
     *  For the vertical axis it's calculated by using the ascent value.
     *  I.e. if you have a texture that is 10x10, and your provide 4 chars with an ascent of 5
     *  you will get 4 glyphs in game that slit the texture into 4 5x5 pixel areas
     * <p>
     *  NOTE: I am not 100% sure this is completely accurate, so here is the official minecraft wiki definition.
     *  "The characters associated with each glyph in the texture file.
     *  The characters inside each string entry inside this list are mapped to the corresponding positions in the referenced file.
     *  The texture is divided evenly according to the number of strings in the list, and the length of the longest string in that list."
     *  <p>
     *  <a href="https://minecraft.wiki/w/Font_definition">minecraft.wiki/w/Font_definition</a>
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

    @Override
    public @NotNull String getFilePath() {
        return "";
    }
}
