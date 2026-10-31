#version 330
#extension GL_ARB_separate_shader_objects : require
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};
layout(std140) uniform Projection { mat4 ProjMat; };
layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 0) out vec4 vertexColor;
layout(location = 1) out vec2 QuadCorner;
layout(location = 2) out vec2 GuiPosition;
void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    int cornerIndex = gl_VertexIndex % 4;
    QuadCorner = vec2(cornerIndex >= 2 ? 1.0 : 0.0, (cornerIndex == 1 || cornerIndex == 2) ? 1.0 : 0.0);
    GuiPosition = (ModelViewMat * vec4(Position, 1.0)).xy;
}


