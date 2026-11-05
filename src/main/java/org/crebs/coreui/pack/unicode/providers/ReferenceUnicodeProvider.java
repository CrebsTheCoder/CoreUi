package org.crebs.coreui.pack.unicode.providers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.crebs.coreui.pack.unicode.AbstractUnicodeProvider;
import org.crebs.coreui.pack.unicode.UnicodeType;
import org.jetbrains.annotations.NotNull;

public class ReferenceUnicodeProvider extends AbstractUnicodeProvider {
    @JsonProperty("id")
    public final String id;

    /**
     * Refer to https://minecraft.wiki/w/Resource_pack#Fonts for more info
     */
    public ReferenceUnicodeProvider(String namespacedIdToFont){
        super(UnicodeType.REFERENCE);
        this.id = namespacedIdToFont;
    }


    @Override
    public String getStringType() {
        return "reference";
    }

    @Override
    public @NotNull String getFilePath() {
        return "";
    }
}
