package org.crebs.coreui.pack.unicode.providers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.crebs.coreui.pack.unicode.AbstractUnicodeProvider;
import org.crebs.coreui.pack.unicode.UnicodeType;

import java.util.ArrayList;

public class UnihexUnicodeProvider extends AbstractUnicodeProvider {

    @JsonProperty("hex_file")
    public final String file;

    @JsonProperty("size_overrides")
    public ArrayList<UnihexSizeOverrides> sizeOverrides;


    /**
     * Refer to https://minecraft.wiki/w/Resource_pack#Fonts for more info
     */
    @JsonIgnore
    public UnihexUnicodeProvider(String namespacedZipPath){
        super(UnicodeType.UNIHEX);
        this.file = namespacedZipPath;


    }

    public UnihexUnicodeProvider addSizeOverridesCase(UnihexSizeOverrides sizeOverrides){
        if (this.sizeOverrides == null)
            this.sizeOverrides = new ArrayList<>();

        this.sizeOverrides.add(sizeOverrides);
        return this;
    }


    @Override
    public String getStringType() {
        return "unihex";
    }


}


