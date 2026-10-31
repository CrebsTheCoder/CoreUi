#version 330
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};
layout(std140) uniform Projection { mat4 ProjMat; };
in vec3 Position;
in vec4 Color;
out vec4 vertexColor;
out vec2 QuadCorner;
out vec2 GuiPosition;
void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    int cornerIndex = gl_VertexID % 4;
    QuadCorner = vec2(cornerIndex >= 2 ? 1.0 : 0.0, (cornerIndex == 1 || cornerIndex == 2) ? 1.0 : 0.0);
    GuiPosition = (ModelViewMat * vec4(Position, 1.0)).xy;
}



