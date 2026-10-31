#ifndef CORE_LOCATION
#define CORE_LOCATION(n)
#endif

CORE_LOCATION(4) in vec2 coreStretchPos;
CORE_LOCATION(5) flat in int coreStretchIndex;
CORE_LOCATION(6) in vec2 coreStretchSize;
CORE_LOCATION(7) flat in float corePopupOpacity;
CORE_LOCATION(8) flat in ivec2 coreSkinOrigin;

float coreSkinAxis(float position, float size) {
    float border = min(2.0, size * 0.5);
    if (position < border) return position * 2.0 / border;
    if (position >= size - border) return 16.0 - (size - position) * 2.0 / border;
    return 2.0 + (position - border) * 12.0 / (size - border * 2.0);
}

void coreDrawQuad() {
    if (corePopupOpacity <= 0.0) discard;
    vec4 paint;
    if (coreStretchIndex == 66) {
        paint = corePositionPaint(texture(Sampler0, texCoord0)) * vec4(vertexColor.rgb, 1.0);
        if (paint.a < 0.1) discard;
    } else if (coreStretchIndex == 65) {
        paint = texture(Sampler0, texCoord0);
        ivec4 marker = ivec4(round(paint * 255.0));
        if (marker.r == 167 && marker.g == 239 && marker.b == 0 && marker.a == 1) discard;
    } else {
        vec2 source = coreStretchIndex == 64 ? coreStretchPos
                : vec2(coreSkinAxis(coreStretchPos.x, coreStretchSize.x), coreSkinAxis(coreStretchPos.y, coreStretchSize.y));
        ivec2 maximum = coreStretchIndex == 64 ? ivec2(coreStretchSize) - ivec2(1) : ivec2(15);
        ivec2 pixel = clamp(ivec2(floor(source)), ivec2(0), maximum);
        paint = texelFetch(Sampler0, coreSkinOrigin + pixel, 0);
    }
    paint.a *= corePopupOpacity;
    if (paint.a <= 0.0) discard;
#ifdef OIT_ALPHA_ONLY
    executeAlphaOnlyPhase(gl_FragCoord.z, paint.a * ColorModulator.a);
#else
    fragColor = paint * ColorModulator;
#endif
}

