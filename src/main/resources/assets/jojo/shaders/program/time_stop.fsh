#version 110
#define PI 3.1415926538

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;

uniform vec2 InSize;

uniform float TSEffectLength;
uniform float TSTicks;
uniform float TSLength;
uniform vec2 CenterScreenCoord;
uniform float FadeInLength;

uniform vec3 Gray;
uniform vec3 RedMatrix;
uniform vec3 GreenMatrix;
uniform vec3 BlueMatrix;
uniform vec3 Offset;
uniform vec3 ColorScale;
uniform float Saturation;

vec3 hue(float h) {
    float r = abs(h * 6.0 - 3.0) - 1.0;
    float g = 2.0 - abs(h * 6.0 - 2.0);
    float b = 2.0 - abs(h * 6.0 - 4.0);
    return clamp(vec3(r,g,b), 0.0, 1.0);
}

vec3 HSVtoRGB(vec3 hsv) {
    return ((hue(hsv.x) - 1.0) * hsv.y + 1.0) * hsv.z;
}

vec3 RGBtoHSV(vec3 rgb) {
    vec3 hsv = vec3(0.0);
    hsv.z = max(rgb.r, max(rgb.g, rgb.b));
    float min = min(rgb.r, min(rgb.g, rgb.b));
    float c = hsv.z - min;

    if (c != 0.0)
    {
        hsv.y = c / hsv.z;
        vec3 delta = (hsv.z - rgb) / c;
        delta.rgb -= delta.brg;
        delta.rg += vec2(2.0, 4.0);
        if (rgb.r >= hsv.z) {
            hsv.x = delta.b;
        } else if (rgb.g >= hsv.z) {
            hsv.x = delta.r;
        } else {
            hsv.x = delta.g;
        }
        hsv.x = fract(hsv.x / 6.0);
    }
    return hsv;
}

vec3 desaturate(vec3 rgb) { // copied desaturate shader
    float saturation = Saturation;
    float fadeIn = 1.0;
    
    float timeLeft = TSLength - TSTicks;
    if (FadeInLength > 0.0 && TSLength - TSTicks < FadeInLength) {
        fadeIn = max(timeLeft, 0.0) / FadeInLength;
        saturation = 1.0 - fadeIn * (1.0 - saturation);
    }
    
    // Color Matrix
    float RedValue = dot(rgb, mix(vec3(1.0, 0.0, 0.0), RedMatrix, fadeIn));
    float GreenValue = dot(rgb, mix(vec3(0.0, 1.0, 0.0), GreenMatrix, fadeIn));
    float BlueValue = dot(rgb, mix(vec3(0.0, 0.0, 1.0), BlueMatrix, fadeIn));
    vec3 OutColor = vec3(RedValue, GreenValue, BlueValue);

    // Offset & Scale
    OutColor = (OutColor * ColorScale) + Offset * fadeIn;

    // Saturation
    float Luma = dot(OutColor, Gray);
    vec3 Chroma = OutColor - Luma;
    OutColor = (Chroma * saturation) + Luma;

    return OutColor;
}

void main() {
    vec3 rgb = texture2D(DiffuseSampler, texCoord).rgb;
    if (TSEffectLength > 0.0) {
	    float TSEffectTiming = TSTicks / TSEffectLength;
	    if (TSLength < 0.0 || TSEffectTiming <= 0.0) {
	        gl_FragColor = vec4(rgb, 1.0);
	    }
	    else if (TSLength >= 100.0 && TSEffectTiming < 1.0) { // the circle
	        float effectRadiusWorld;
	        if (TSEffectTiming < 0.8) {
	            effectRadiusWorld = TSEffectTiming * 1.25;
	        }
	        else {
	            effectRadiusWorld = (1.0 - TSEffectTiming) * 5.0;
	        }
	        
	        float sizeMax = max(InSize.x, InSize.y) * 2.0;
	        vec2 sizeCorr = InSize / sizeMax;
	        vec2 texCoordCorr = texCoord * sizeCorr;
	        vec2 centerCoord = CenterScreenCoord * sizeCorr;
	        
	        float distFromCenter = distance(texCoordCorr, centerCoord);
	        if (distFromCenter < effectRadiusWorld) {
	            float distortionAmount = 1.0;
	            float f = 1.0 - distortionAmount * distFromCenter / effectRadiusWorld;
	            vec2 newCoord = CenterScreenCoord + (texCoordCorr - centerCoord) * f / sizeCorr;
	            vec3 rgbNew = texture2D(DiffuseSampler, newCoord).rgb;
	            
	            vec3 hsv = RGBtoHSV(rgbNew);
	            
	            hsv.x = fract(hsv.x + TSEffectTiming * 0.5 + 0.25);
	            hsv.z = 0.4 + 0.4 * (1.0 - hsv.z);
	            
	            gl_FragColor = vec4(HSVtoRGB(hsv), 1.0);
	        }
	        
	        else {
	            if (TSEffectTiming < 0.8) {
	                gl_FragColor = vec4(rgb, 1.0);
	            }
	            else {
	                gl_FragColor = vec4(desaturate(rgb), 1.0);
	            }
	        }
	    }
	    else {
	        gl_FragColor = vec4(desaturate(rgb), 1.0);
	    }
    }
    else {
        gl_FragColor = vec4(desaturate(rgb), 1.0);
    }
}
