#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

flat in int coreGuiPass;
#moj_import <coreui:position_marker.glsl>
#moj_import <coreui:quad_paint.glsl>

void main() {
    if (coreGuiPass == 1) {
        if (coreStretchIndex == -2) discard;
        if (coreStretchIndex >= 0) {
            coreDrawQuad();
            return;
        }
    }
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}

