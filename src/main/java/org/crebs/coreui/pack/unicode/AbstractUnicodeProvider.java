package org.crebs.coreui.pack.unicode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public abstract class AbstractUnicodeProvider implements UnicodeProviderInterface{

    @JsonProperty("type")
    public final String type;
    @JsonProperty("filter")
    private final HashMap<String, Object> filter = new HashMap<>();

    @JsonIgnore
    public AbstractUnicodeProvider(UnicodeType type){
        this.type = String.valueOf(type).toLowerCase();
    }

    public enum FilterType{
        UNICODE,
        JP
    }

    public void addCase(FilterType filterType, boolean value){
        filter.put((filterType == FilterType.UNICODE) ? "uniform" : "jp", value);
    }


}
