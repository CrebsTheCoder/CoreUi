const int CORE_STRETCH_FIRST = 1;
const int CORE_STRETCH_LAST = 16;
const int CORE_MOVING_FIRST = 17;
const int CORE_MOVING_LAST = 24;
const float CORE_MOVING_TICKS = 4.0;
const float CORE_CLOCK_TICKS = 24000.0;

uniform sampler2D Sampler0;

layout(location = 4) out vec2 coreStretchPos;
layout(location = 5) flat out int coreStretchIndex;
layout(location = 6) out vec2 coreStretchSize;
layout(location = 7) flat out float corePopupOpacity;
layout(location = 8) flat out ivec2 coreSkinOrigin;

float corePopupFade(ivec4 data, int mode) {
    float start = float((data.g >> 1) | (data.b << 7));
    float elapsed = mod(GameTime * CORE_CLOCK_TICKS - start + CORE_CLOCK_TICKS, CORE_CLOCK_TICKS);
    if (elapsed > CORE_CLOCK_TICKS / 2.0) elapsed = 0.0;
    float progress = clamp(elapsed / 4.0, 0.0, 1.0);
    float eased = progress * progress * (3.0 - 2.0 * progress);
    float from = float(mode & 15) / 15.0;
    return mix(from, (mode & 16) == 0 ? 1.0 : 0.0, eased);
}

bool corePopupTextMarker() {
    ivec2 atlasSize = textureSize(Sampler0, 0);
    vec2 pixelPosition = UV0 * vec2(atlasSize);
    for (int x = -1; x <= 1; x += 2) {
        for (int y = -1; y <= 1; y += 2) {
            ivec2 texel = ivec2(floor(pixelPosition + vec2(x, y) * 0.25));
            if (any(lessThan(texel, ivec2(0))) || any(greaterThanEqual(texel, atlasSize))) continue;
            ivec4 marker = ivec4(texelFetch(Sampler0, texel, 0) * 255.0 + 0.5);
            if (marker.g == 239 && ((marker.r == 167 && (marker.a == 0 || marker.a == 1))
                    || (marker.r == 165 && marker.a == 1))) return true;
        }
    }
    return false;
}

bool coreGlyphCorner(out vec2 corner, out float layerOffset, out int kind, out ivec2 skinOrigin) {
    ivec2 atlasSize = textureSize(Sampler0, 0);
    vec2 pixelPosition = UV0 * vec2(atlasSize);
    corner = vec2(0.0);
    layerOffset = 0.0;
    kind = -1;
    skinOrigin = ivec2(0);
    for (int candidate = 0; candidate < 4; candidate++) {
        vec2 inward = vec2(candidate < 2 ? 0.25 : -0.25, candidate == 0 || candidate == 3 ? 0.25 : -0.25);
        ivec2 texel = ivec2(floor(pixelPosition + inward));
        if (texel.x < 0 || texel.y < 0 || texel.x >= atlasSize.x || texel.y >= atlasSize.y) {
            continue;
        }
        ivec4 marker = ivec4(texelFetch(Sampler0, texel, 0) * 255.0 + 0.5);
        if (marker.r == 252 && marker.g == 239 && marker.a == 1
                && (marker.b == 1 || marker.b == 2 || marker.b == 4 || marker.b == 8)) {
            kind = 2;
            corner = vec2(
                marker.b == 2 || marker.b == 8 ? 1.0 : 0.0,
                marker.b == 4 || marker.b == 8 ? 1.0 : 0.0
            );
            skinOrigin = texel - ivec2(corner * 21.0) + ivec2(1);
            return true;
        }
        if (marker.r == 253 && marker.g >= 195 && marker.g <= 239 && marker.a == 1) {
            kind = 0;
            layerOffset = float(239 - marker.g) * 9.0;
            corner = vec2(
                marker.b == 2 || marker.b == 8 ? 1.0 : 0.0,
                marker.b == 4 || marker.b == 8 ? 1.0 : 0.0
            );
            skinOrigin = texel - ivec2(corner * 17.0) + ivec2(1);
            return true;
        }
        if (marker.r == 254 && marker.a == 1 && marker.b < 8) {
            kind = 1;
            layerOffset = float(marker.g | ((marker.b & 4) << 6));
            corner = vec2(float(marker.b & 1), float((marker.b >> 1) & 1));
            skinOrigin = texel - ivec2(corner * 17.0) + ivec2(1);
            return true;
        }
    }
    return false;
}

vec2 coreOffset(ivec4 data) {
    vec2 corner;
    float layerOffset;
    int kind;
    ivec2 skinOrigin;
    bool marked = coreGlyphCorner(corner, layerOffset, kind, skinOrigin);
    if (marked) coreStretchIndex = -2;
    if (marked && kind == 0 && data.a >= CORE_STRETCH_FIRST && data.a <= CORE_STRETCH_LAST) {
        int width = data.r | ((data.g & 1) << 8);
        int height = ((data.g >> 1) & 127) | ((data.b & 3) << 7);
        vec2 target = vec2(float(width), float(height));
        int yOffset = (data.b >> 4) & 15;
        coreStretchPos = corner * target;
        coreStretchSize = target;
        coreStretchIndex = 0;
        coreSkinOrigin = skinOrigin;
        return corner * (target - vec2(8.0)) - vec2(1.0) + vec2(0.0, float(yOffset) - layerOffset);
    }
    if (marked && kind == 2 && data.a >= CORE_MOVING_FIRST && data.a <= CORE_MOVING_LAST) {
        int state = data.a - CORE_MOVING_FIRST;
        bool slider = state == 3 || state == 4 || state == 5 || state == 7;
        vec2 target = slider ? vec2(10.0, 18.0) : vec2(14.0, 20.0);
        coreStretchPos = corner * target;
        coreStretchSize = target;
        coreStretchIndex = 64;
        coreSkinOrigin = skinOrigin;
        float start = float(data.r | ((data.g & 127) << 8));
        float elapsed = mod(GameTime * CORE_CLOCK_TICKS - start + CORE_CLOCK_TICKS, CORE_CLOCK_TICKS);
        if (elapsed > CORE_CLOCK_TICKS / 2.0) {
            elapsed = 0.0;
        }
        float remaining = 1.0 - clamp(elapsed / CORE_MOVING_TICKS, 0.0, 1.0);
        float distance = float(data.b - 128) * remaining * remaining * remaining;
        return corner * (target - vec2(8.0))
                + ((data.g & 128) != 0 ? vec2(0.0, distance) : vec2(distance, 0.0)) - vec2(1.0);
    }
    if (marked && kind == 1 && data.a >= 26 && data.a <= 249) {
        int popupPaint = (data.a - 26) >> 5;
        vec2 target = vec2(float(data.r | ((data.g & 1) << 8)), popupPaint == 6 ? 22.0 : 24.0);
        coreStretchPos = corner * target;
        coreStretchSize = target;
        coreStretchIndex = 0;
        coreSkinOrigin = skinOrigin;
        corePopupOpacity = corePopupFade(data, (data.a - 26) & 31);
        return corner * (target - vec2(8.0)) - vec2(1.0) - vec2(0.0, layerOffset + 12.0 * (1.0 - corePopupOpacity));
    }
    if (marked) return vec2(0.0);
    if ((data.a == 255 || data.a == 25 || data.a >= 58 && data.a <= 121) && corePopupTextMarker()) {
        coreStretchIndex = -2;
        if (data.a == 25) {
            coreStretchIndex = 65;
            return vec2(-1.0, -1.0 - float(data.r | (data.g << 8)));
        }
        if (data.a >= 58 && data.a <= 121) {
            coreStretchIndex = 65;
            corePopupOpacity = corePopupFade(data, (data.a - 58) & 31);
            return vec2(-1.0, -1.0 - float(data.r | ((data.g & 1) << 8)) - 12.0 * (1.0 - corePopupOpacity));
        }
        return vec2(0.0);
    }
    if (data.a == 25) {
        return vec2(-1.0, -1.0 - float(data.r | (data.g << 8)));
    }
    return vec2(0.0);
}
