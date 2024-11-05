package com.oussama.portfolio.ui.components.asciiimage

import android.graphics.Bitmap
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.view.animation.AnticipateOvershootInterpolator
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language
import kotlin.random.Random

/*@Language("AGSL")
val fragmentShader = """
        uniform float time;
        uniform float2 resolution;
        uniform shader contents;

        vec4 permute(vec4 x){return mod(((x*34.0)+1.0)*x, 289.0);}
        vec4 taylorInvSqrt(vec4 r){return 1.79284291400159 - 0.85373472095314 * r;}

        float snoise(vec3 v){
          const vec2  C = vec2(1.0/6.0, 1.0/3.0) ;
          const vec4  D = vec4(0.0, 0.5, 1.0, 2.0);

        // First corner
          vec3 i  = floor(v + dot(v, C.yyy) );
          vec3 x0 =   v - i + dot(i, C.xxx) ;

        // Other corners
          vec3 g = step(x0.yzx, x0.xyz);
          vec3 l = 1.0 - g;
          vec3 i1 = min( g.xyz, l.zxy );
          vec3 i2 = max( g.xyz, l.zxy );

          //  x0 = x0 - 0. + 0.0 * C
          vec3 x1 = x0 - i1 + 1.0 * C.xxx;
          vec3 x2 = x0 - i2 + 2.0 * C.xxx;
          vec3 x3 = x0 - 1. + 3.0 * C.xxx;

        // Permutations
          i = mod(i, 289.0 );
          vec4 p = permute( permute( permute(
                     i.z + vec4(0.0, i1.z, i2.z, 1.0 ))
                   + i.y + vec4(0.0, i1.y, i2.y, 1.0 ))
                   + i.x + vec4(0.0, i1.x, i2.x, 1.0 ));

        // Gradients
        // ( N*N points uniformly over a square, mapped onto an octahedron.)
          float n_ = 1.0/7.0; // N=7
          vec3  ns = n_ * D.wyz - D.xzx;

          vec4 j = p - 49.0 * floor(p * ns.z *ns.z);  //  mod(p,N*N)

          vec4 x_ = floor(j * ns.z);
          vec4 y_ = floor(j - 7.0 * x_ );    // mod(j,N)

          vec4 x = x_ *ns.x + ns.yyyy;
          vec4 y = y_ *ns.x + ns.yyyy;
          vec4 h = 1.0 - abs(x) - abs(y);

          vec4 b0 = vec4( x.xy, y.xy );
          vec4 b1 = vec4( x.zw, y.zw );

          vec4 s0 = floor(b0)*2.0 + 1.0;
          vec4 s1 = floor(b1)*2.0 + 1.0;
          vec4 sh = -step(h, vec4(0.0));

          vec4 a0 = b0.xzyw + s0.xzyw*sh.xxyy ;
          vec4 a1 = b1.xzyw + s1.xzyw*sh.zzww ;

          vec3 p0 = vec3(a0.xy,h.x);
          vec3 p1 = vec3(a0.zw,h.y);
          vec3 p2 = vec3(a1.xy,h.z);
          vec3 p3 = vec3(a1.zw,h.w);

        //Normalise gradients
          vec4 norm = taylorInvSqrt(vec4(dot(p0,p0), dot(p1,p1), dot(p2, p2), dot(p3,p3)));
          p0 *= norm.x;
          p1 *= norm.y;
          p2 *= norm.z;
          p3 *= norm.w;

        // Mix final noise value
          vec4 m = max(0.6 - vec4(dot(x0,x0), dot(x1,x1), dot(x2,x2), dot(x3,x3)), 0.0);
          m = m * m;
          return 42.0 * dot( m*m, vec4( dot(p0,x0), dot(p1,x1),
                                        dot(p2,x2), dot(p3,x3) ) );
        }

half4 main(in vec2 fragCoord) {
    // Normalized coordinates (0.0 to 1.0)
vec2 uv = fragCoord.xy / resolution.xy;
float noise = snoise(vec3(uv.x * 6, uv.y *6, time * 0.5));

noise *= exp(-length(abs(uv * 1.5)));
vec2 offset1 = vec2(noise * 0.02);
vec2 offset2 = vec2(0.02) / resolution.xy;
uv += offset1 - offset2;
    return contents.eval(uv * resolution.xy);
}
""".trimIndent()*/

@Language("AGSL")
val ASCIIFragmentShader = """

     uniform vec2 uResolution;
     uniform int isTouching;
     uniform float startRangeX;
     uniform float endRangeX;
     uniform float startRangeY;
     uniform float endRangeY;
     uniform float initialAlpha;
     uniform float rangeSkippingValue;
     uniform shader image;
     
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
     bool isInsideRange(vec2 uv, float startRangeX, float endRangeX, float startRangeY, float endRangeY, vec2 uResolution, float rangeModifier) {
         // Checks if is inside a square
         float scaledStartRangeX = startRangeX + rangeModifier;
         float scaledEndRangeX = endRangeX - rangeModifier;
         float scaledStartRangeY = startRangeY + rangeModifier;
         float scaledEndRangeY = endRangeY - rangeModifier;

         return (uv.x > scaledStartRangeX / uResolution.x && uv.x < scaledEndRangeX / uResolution.x &&
         uv.y > scaledStartRangeY / uResolution.y && uv.y < scaledEndRangeY / uResolution.y);
     }
     bool isInsideCircle(vec2 uv, vec2 center, float radius, vec2 uResolution) {
         vec2 normalizedUV = uv * uResolution / max(uResolution.x, uResolution.y);
         vec2 normalizedCenter = center / max(uResolution.x, uResolution.y);
         float distanceSquared = dot(normalizedUV - normalizedCenter, normalizedUV - normalizedCenter);
         float radiusSquared = (radius / max(uResolution.x, uResolution.y)) * (radius / max(uResolution.x, uResolution.y));
         return distanceSquared < radiusSquared;
     }
     float calculateAlphaFalloff(vec2 uv, vec2 uResolution) {
         float distToLeft = uv.x;
         float distToRight = uResolution.x - uv.x;
         float distToTop = uv.y;
         float distToBottom = uResolution.y - uv.y;

         float edgeThreshold = 0.2; // Adjust the edge threshold here

         // Normalize distances
         float maxDist = max(uResolution.x, uResolution.y);
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
     half4 main(float2 fragCoord) {
         vec2 pix = fragCoord;
         vec2 uv = pix / uResolution.xy;
    
         vec3 col = image.eval(fragCoord).rgb;
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
         bool insideRange = isInsideCircle(uv, vec2((startRangeX + endRangeX) / 2.0, (startRangeY + endRangeY) / 2.0), (endRangeX - startRangeX) / 2.0, uResolution);

         if (insideRange) {
             vec2 center = vec2((startRangeX + endRangeX) / 2.0, (startRangeY + endRangeY) / 2.0);
             float distance = distance(uv * uResolution, center);



             // Additional logic for changing characters gradually
             float heatArea = clamp(1. - distance / (uResolution.x * 0.5), 0.0, 1.0);
             float bwFactor = 1.0;
             float blenderDivider = (uResolution.x * 0.1);
             if (isTouching == 1) {
                 blenderDivider = (uResolution.x * 0.22);
             } else {
                 blenderDivider = (uResolution.x * 0.11);
             }

             if (heatArea < 0.2) {
                 n = 4096;
             } else if (heatArea < 0.3) {
                 n = 4096;
             } else if (heatArea < 0.4) {
                 blenderDivider = (uResolution.x * 0.45);
             } else if (heatArea < 0.5) {
                 blenderDivider = (uResolution.x * 0.4);
             } else if (heatArea < 0.6) {
                 blenderDivider = (uResolution.x * 0.4);
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

         alpha = calculateAlphaFalloff(pix, uResolution);
         /*
        **AGSL and Premultiplied Alpha
        When dealing with transparent colors, there are two (common) possible representations: straight (unassociated) alpha and premultiplied (associated) alpha. In ASGL the color returned by the main function is expected to be premultiplied. AGSL's use of premultiplied alpha implies:
         If your AGSL shader will return transparent colors, be sure to multiply the RGB by A. The resulting color should be [R*A, G*A, B*A, A], not [R, G, B, A].
         */
         return vec4(col*alpha, alpha);
     }
 """.trimIndent()

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AsciiImage(bitmap: Bitmap) {
    val shader = remember { RuntimeShader(ASCIIFragmentShader) }
    val maxTouchSize = 250f
    val touchSize = maxTouchSize / 2
    val minRangeSkippingValue = 0.75f
    val maxRangeSkippingValue = 0.9f
    var speedFactorX = 5f
    var speedFactorY = 5f
    var isTouching by remember { mutableStateOf(false) }
    var startRangeX by remember { mutableFloatStateOf(touchSize) }
    var endRangeX by remember { mutableFloatStateOf(-touchSize) }
    var startRangeY by remember { mutableFloatStateOf(0f) }
    var endRangeY by remember { mutableFloatStateOf(touchSize * 2) }
    var previousEndRangeX by remember { mutableFloatStateOf(0f) }
    var previousEndRangeY by remember { mutableFloatStateOf(0f) }
    val alphaAnimation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var size = IntSize(0, 0)
    //val transition = updateTransition(targetState = isTouching, label = "")
    //val toX by transition.animateFloat ( transitionSpec = { tween(2000) }, label = "toX", targetValueByState = {_isTouching -> if(_isTouching) 0f else 0f} )
    val xAnimation = remember { Animatable(0f) }
    val yAnimation = remember { Animatable(0f) }
    val touchAnimation = remember { Animatable(touchSize) }
    val rangeSkippingAnimation = remember { Animatable(maxRangeSkippingValue) }

    LaunchedEffect(Unit) {
        scope.launch {
            while (true) {
                if (!isTouching) {
                    if (endRangeX >= size.width || startRangeX <= 0) {
                        speedFactorX = -speedFactorX
                        speedFactorX += Random.nextFloat() * 0.2f - 0.1f
                    }

                    if (endRangeY >= size.height || startRangeY <= 0) {
                        speedFactorY = -speedFactorY
                        speedFactorY += Random.nextFloat() * 0.2f - 0.1f
                    }

                    startRangeX += speedFactorX
                    endRangeX += speedFactorX
                    startRangeY += speedFactorY
                    endRangeY += speedFactorY
                }
                delay(10)
            }
        }
        alphaAnimation.animateTo(
            1f,
            animationSpec = tween(durationMillis = 2000)
        )
    }
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "",
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
            .height(500.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    // ACTION_DOWN here
                    val down = awaitFirstDown()
                    down.consume()
                    isTouching = true
                    if (previousEndRangeX == 0f || previousEndRangeY == 0f) {
                        scope.launch {
                            xAnimation.snapTo(endRangeX - touchSize)
                            xAnimation.animateTo(
                                down.position.x,
                                animationSpec = tween(
                                    durationMillis = 600,
                                    easing = {
                                        AnticipateOvershootInterpolator().getInterpolation(it)
                                    })
                            )
                        }
                        scope.launch {
                            yAnimation.snapTo(endRangeY)
                            yAnimation.animateTo(
                                down.position.y,
                                animationSpec = tween(
                                    durationMillis = 600,
                                    easing = {
                                        AnticipateOvershootInterpolator().getInterpolation(it)
                                    })
                            )
                        }
                        scope.launch {
                            rangeSkippingAnimation.animateTo(
                                minRangeSkippingValue,
                                animationSpec = tween(
                                    durationMillis = 600,
                                    easing = {
                                        AnticipateOvershootInterpolator().getInterpolation(it)
                                    })
                            )
                        }
                    }
                    do {
                        val event: PointerEvent = awaitPointerEvent()
                        // ACTION_MOVE loop
                        event.changes.forEach { change: PointerInputChange ->
                            //if(xAnimation.isRunning || yAnimation.isRunning) return@forEach
                            change.consume()
                            startRangeX = change.position.x - touchSize
                            endRangeX = change.position.x + touchSize
                            startRangeY = change.position.y - touchSize * 2
                            endRangeY = change.position.y
                            if (xAnimation.isRunning) scope.launch { xAnimation.snapTo(endRangeX) }
                            if (yAnimation.isRunning) scope.launch { yAnimation.snapTo(endRangeY) }
                            previousEndRangeX = endRangeX
                            previousEndRangeY = endRangeY
                        }
                    } while (event.changes.any { it.pressed })
                    //ACTION_UP
                    isTouching = false
                    previousEndRangeX = 0f
                    previousEndRangeY = 0f
                    scope.launch {
                        touchAnimation.animateTo(
                            maxTouchSize / 2,
                            animationSpec = tween(
                                durationMillis = 600,
                                easing = {
                                    AnticipateOvershootInterpolator().getInterpolation(it)
                                })
                        )
                    }
                    scope.launch {
                        xAnimation.snapTo(
                            startRangeX + touchSize
                        )
                    }
                    scope.launch {
                        yAnimation.snapTo(
                            startRangeY + touchSize / 2
                        )
                    }
                    scope.launch {
                        rangeSkippingAnimation.animateTo(
                            maxRangeSkippingValue,
                            animationSpec = tween(
                                durationMillis = 600,
                                easing = {
                                    AnticipateOvershootInterpolator().getInterpolation(it)
                                })
                        )
                    }
                }
            }
            .onSizeChanged {
                size = it
                shader.setFloatUniform(
                    "uResolution",
                    it.width.toFloat(),
                    it.height.toFloat()
                )
                shader.setFloatUniform("startRangeX", startRangeX)
                shader.setFloatUniform("endRangeX", endRangeX)
                shader.setFloatUniform("startRangeY", startRangeY)
                shader.setFloatUniform("endRangeY", endRangeY)
                shader.setFloatUniform("initialAlpha", alphaAnimation.value)
                shader.setFloatUniform("rangeSkippingValue", rangeSkippingAnimation.value)
            }
            .graphicsLayer {
                clip = true
                if (xAnimation.isRunning) {
                    startRangeX = xAnimation.value - touchSize
                    endRangeX = xAnimation.value + touchSize
                }
                shader.setFloatUniform("startRangeX", startRangeX)
                shader.setFloatUniform("endRangeX", endRangeX)

                if (yAnimation.isRunning) {
                    startRangeY = (yAnimation.value) - touchSize * 2
                    endRangeY = yAnimation.value
                }
                shader.setFloatUniform("startRangeY", startRangeY)
                shader.setFloatUniform("endRangeY", endRangeY)
                shader.setFloatUniform("initialAlpha", alphaAnimation.value)
                shader.setFloatUniform("rangeSkippingValue", rangeSkippingAnimation.value)
                shader.setIntUniform("isTouching", if (isTouching) 1 else 0)
                renderEffect = RenderEffect
                    .createRuntimeShaderEffect(shader, "image")
                    .asComposeRenderEffect()
            }
    )
}