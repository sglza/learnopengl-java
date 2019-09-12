#version 300 es
layout (location = 0) in vec3 position;

uniform vec4 offset;

void main()
{
    gl_Position = vec4(position, 1.0) + offset;
}