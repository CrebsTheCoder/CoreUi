const ivec3 CORE_POSITION_PALETTE[5] = ivec3[5](
    ivec3(0, 0, 0),
    ivec3(48, 52, 49),
    ivec3(118, 121, 128),
    ivec3(181, 185, 182),
    ivec3(255, 255, 255)
);

bool corePositionMarker(ivec4 pixel) {
    return pixel.r == 166 && pixel.g == 239 && pixel.b >= 0 && pixel.b < 5;
}

vec4 corePositionPaint(vec4 pixel) {
    ivec4 marker = ivec4(round(pixel * 255.0));
    return corePositionMarker(marker) ? vec4(vec3(CORE_POSITION_PALETTE[marker.b]) / 255.0, pixel.a) : pixel;
}
