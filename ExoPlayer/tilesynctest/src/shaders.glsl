//VERTEX SHADER
attribute vec3 vPosition;
attribute vec2 texCoord;
varying vec2 texCoordinates;
void main() {
	texCoordinates = texCoord;
	gl_Position = vec4(vPosition.x,vPosition.y,vPosition.z,1f);
}



//FRAGMENT SHADER
#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform vec3 vColor;
uniform samplerExternalOES s_texture[4];
varying vec2 texCoordinates;
void main(){
	gl_FragColor = texture2D(s_texture[0],texCoordinates);
}
