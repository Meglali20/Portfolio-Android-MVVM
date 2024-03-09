precision mediump float;

uniform vec2 u_resolution;
uniform int isTouching;
uniform sampler2D u_texture0;
uniform float startRangeX;
uniform float endRangeX;
uniform float startRangeY;
uniform float endRangeY;
uniform float initialAlpha;
uniform float rangeSkippingValue;

float character(float n, vec2 p)
{
    p = floor(p * vec2(4.0, -4.0) + 2.5);
    if (clamp(p.x, 0.0, 4.0) == p.x)
    {
        if (clamp(p.y, 0.0, 4.0) == p.y)
        {
            if (int(mod(n / exp2(p.x + 5.0 * p.y), 2.0)) == 1) return 1.0;
        }
    }
    return 0.0;
}
vec3 invertColor(vec3 color) {
    return vec3(1.0 - color.r, 1.0 - color.g, 1.0 - color.b);
}

bool isBlack(vec3 color) {
    return color.r == 0.0 && color.g == 0.0 && color.b == 0.0;
}
bool isInsideRange(vec2 uv, float startRangeX, float endRangeX, float startRangeY, float endRangeY, vec2 u_resolution, float rangeModifier) {
    // Checks if is inside a square
    float scaledStartRangeX = startRangeX + rangeModifier;
    float scaledEndRangeX = endRangeX - rangeModifier;
    float scaledStartRangeY = startRangeY + rangeModifier;
    float scaledEndRangeY = endRangeY - rangeModifier;

    return (uv.x > scaledStartRangeX / u_resolution.x && uv.x < scaledEndRangeX / u_resolution.x &&
    uv.y > scaledStartRangeY / u_resolution.y && uv.y < scaledEndRangeY / u_resolution.y);
}
bool isInsideCircle(vec2 uv, vec2 center, float radius, vec2 u_resolution) {
    vec2 normalizedUV = uv * u_resolution / max(u_resolution.x, u_resolution.y);
    vec2 normalizedCenter = center / max(u_resolution.x, u_resolution.y);
    float distanceSquared = dot(normalizedUV - normalizedCenter, normalizedUV - normalizedCenter);
    float radiusSquared = (radius / max(u_resolution.x, u_resolution.y)) * (radius / max(u_resolution.x, u_resolution.y));
    return distanceSquared < radiusSquared;
}
float calculateAlphaFalloff(vec2 uv, vec2 u_resolution) {
    float distToLeft = uv.x;
    float distToRight = u_resolution.x - uv.x;
    float distToTop = uv.y;
    float distToBottom = u_resolution.y - uv.y;

    float edgeThreshold = 0.2; // Adjust the edge threshold here

    // Normalize distances
    float maxDist = max(u_resolution.x, u_resolution.y);
    distToLeft /= maxDist;
    distToRight /= maxDist;
    distToTop /= maxDist;
    distToBottom /= maxDist;

    float alpha = initialAlpha;

    // Check if the pixel is closer to the left or right side
    float minHorizontalDist = min(distToLeft, distToRight);
    alpha *= smoothstep(0.0, edgeThreshold, minHorizontalDist);

    // Check if the pixel is closer to the top or bottom side
    float minVerticalDist = min(distToTop, distToBottom);
    alpha *= smoothstep(0.0, edgeThreshold, minVerticalDist);

    return alpha;
}
void main() {
    vec2 pix = gl_FragCoord.xy;
    vec2 uv = pix / u_resolution.xy;
    uv.y = 1.0 - uv.y; // Flip the texture vertically

    vec3 col = texture2D(u_texture0, uv).rgb;
    float gray = 0.3 * col.r + 0.59 * col.g + 0.11 * col.b;

    int n = 4096;

    // limited character set
    if (gray > 0.2) n = 65600;    // :
    if (gray > 0.3) n = 163153;   // *
    if (gray > 0.4) n = 15255086; // o
    if (gray > 0.5) n = 13121101; // &
    if (gray > 0.6) n = 15252014; // 8
    if (gray > 0.7) n = 13195790; // @
    if (gray > 0.8) n = 11512810; // #

    float charsize = 4.8;
    float alpha = 1.0;
    vec2 p = mod(pix / charsize, charsize / 2.0) - vec2(charsize / 4.0);

    float rangeModifier = endRangeY - startRangeY;
    bool insideRange = isInsideCircle(uv, vec2((startRangeX + endRangeX) / 2.0, (startRangeY + endRangeY) / 2.0), (endRangeX - startRangeX) / 2.0, u_resolution);

    if (insideRange) {
        vec2 center = vec2((startRangeX + endRangeX) / 2.0, (startRangeY + endRangeY) / 2.0);
        float distance = distance(uv * u_resolution, center);



        // Additional logic for changing characters gradually
        float heatArea = clamp(1. - distance / (u_resolution.x * 0.5), 0.0, 1.0);
        float bwFactor = 1.0;
        float blenderDivider = (u_resolution.x * 0.1);
        if (isTouching == 1) {
            blenderDivider = (u_resolution.x * 0.22);
        } else {
            blenderDivider = (u_resolution.x * 0.11);
        }

        if (heatArea < 0.2) {
            n = 4096;
        } else if (heatArea < 0.3) {
            n = 4096;
        } else if (heatArea < 0.4) {
            blenderDivider = (u_resolution.x * 0.45);
        } else if (heatArea < 0.5) {
            blenderDivider = (u_resolution.x * 0.4);
        } else if (heatArea < 0.6) {
            blenderDivider = (u_resolution.x * 0.4);
        }
        bwFactor = 1.2;
        // Adjust the smoothstep range for a smoother blending
        float blendAmount = smoothstep(0.1, 1.0, distance / blenderDivider);

        // Gradual blending from the center towards the edges
        col = mix(col, vec3(character(float(n), p)), blendAmount);
        if (heatArea < 0.2) {
            col = vec3(character(float(n), p));
        } else if (heatArea < rangeSkippingValue * 0.9) {
            col = (col * bwFactor) * character(float(n), p);
        }

    } else {
        col = vec3(character(float(n), p));
    }

    alpha = calculateAlphaFalloff(pix, u_resolution);
    gl_FragColor = vec4(col, alpha);
}