#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:sample_lightmap.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform sampler2D Sampler2;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

flat out int coreGuiPass;
#moj_import <coreui:position_marker.glsl>
#moj_import <coreui:stretch_quads.glsl>

void main() {
    vec3 renderedPosition = Position;
    coreStretchIndex = -1;
    coreStretchPos = vec2(0.0);
    coreStretchSize = vec2(0.0);
    corePopupOpacity = 1.0;
    coreSkinOrigin = ivec2(0);
    coreGuiPass = abs(ProjMat[3][3] - 1.0) < 0.001 ? 1 : 0;
    if (coreGuiPass == 1) renderedPosition.xy += coreOffset(ivec4(round(Color * 255.0)));
    gl_Position = ProjMat * ModelViewMat * vec4(renderedPosition, 1.0);

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
    vertexColor = coreGuiPass == 1 ? Color : Color * sample_lightmap(Sampler2, UV2);
    texCoord0 = UV0;
}

