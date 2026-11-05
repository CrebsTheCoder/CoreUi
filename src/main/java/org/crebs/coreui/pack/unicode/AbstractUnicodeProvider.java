package org.crebs.coreui.pack.unicode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.outmoded.packer.JsonTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public abstract class AbstractUnicodeProvider implements UnicodeProviderInterface, JsonTemplate {

    @JsonProperty("type")
    public final String type;
    @JsonProperty("filter")
    private HashMap<String, Object> filter = null;

    @JsonIgnore
    public AbstractUnicodeProvider(UnicodeType type){
        this.type = String.valueOf(type).toLowerCase();
    }

    public enum FilterType{
        UNICODE,
        JP
    }

    public void addCase(FilterType filterType, boolean value){
        if (filter == null)
            filter = new HashMap<>();

        filter.put((filterType == FilterType.UNICODE) ? "uniform" : "jp", value);
    }


}
