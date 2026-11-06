package org.crebs.coreui.pack.unicode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.outmoded.packer.JsonTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static net.outmoded.packer.PackerUtils.splitNamespaceId;


public class UnicodeFileRegister implements JsonTemplate {

    @JsonProperty("providers")
    private final ArrayList<AbstractUnicodeProvider> providers;

    private final String fullWritePath;

    /**
     * Refer to <a href="https://minecraft.wiki/w/Font_definition">...</a> for more info
     * @param namespacedWritePath should be "namespace:folder/custom.json"
     *
     */
    @JsonIgnore
    public UnicodeFileRegister(String namespacedWritePath) {
        fullWritePath = "assets/"+splitNamespaceId(namespacedWritePath)[0]+"/font/"+splitNamespaceId(namespacedWritePath)[1];
        providers = new ArrayList<>();
    }

    public UnicodeFileRegister addUnicodeProvider(AbstractUnicodeProvider unicodeProvider){
        providers.add(unicodeProvider);
        return this;
    }


    @Override
    public @NotNull String getFilePath() {
        return fullWritePath;
    }
}
