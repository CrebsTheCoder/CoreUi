package org.crebs.coreui.pack.unicode.providers;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UnihexSizeOverrides(
        @JsonProperty("from") String from,
        @JsonProperty("to") String to,
        @JsonProperty("left") double left,
        @JsonProperty("right") double right){


    public UnihexSizeOverrides(String from, String to, double left, double right){
        this.from = from;
        this.to = to;
        this.left = left;
        this.right = right;
    }

}
