package oglib.gui;

import static com.google.common.base.Preconditions.checkArgument;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import oglib.components.Color;

public class Simple2DBuffer {

    private float[] vertices = new float[] {
            // positions // colors // texture coords
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, // top right
            1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, // bottom right
            -1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, // bottom left
            -1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f // top left
    };
    private byte[] indices = new byte[] { 0, 1, 3, 1, 2, 3 };

    final private int vao;
    final private int vbo;
    final private int ebo;
    final private int textureId;
    private static int width; // was final and static
    final private int height;
    final private ByteBuffer screenBuffer;
    private static byte[] screen; // was final
    final private byte[] blankScreen;
    private static boolean updated = false; // added static
    final private static int posLocation = 0;
    final private static int colorLocation = 1;
    final private static int uvLocation = 2;

    public Simple2DBuffer(int width, int height) {
        checkArgument(width > 0, "Width must be positive");
        checkArgument(height > 0, "Height must be positive");

        this.width = width;
        this.height = height;
        screen = new byte[this.width * this.height * 3];
        blankScreen = new byte[this.width * this.height * 3];

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        var vboBuffer = ByteBuffer.allocateDirect(vertices.length << 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vboBuffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vboBuffer, GL_STATIC_DRAW);
        // Position Attribute
        // size = elements on a row (8) * float size (4)
        glVertexAttribPointer(posLocation, 3, GL_FLOAT, false, 8 * 4, 0);
        glEnableVertexAttribArray(posLocation);

        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        var eboBuffer = ByteBuffer.allocateDirect(indices.length).order(ByteOrder.nativeOrder());
        eboBuffer.put(indices).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, eboBuffer, GL_STATIC_DRAW);

        // Color Attribute
        // size = elements on a row (8) * float size (4)
        // offset = position vertex elements (3) * float size (4)
        glVertexAttribPointer(colorLocation, 3, GL_FLOAT, false, 8 * 4, 3 * 4);
        glEnableVertexAttribArray(colorLocation);

        // UV Attribute
        // size = elements on a row (8) * float size (4)
        // offset = (position vertex elements (3) + color vertex elements (3)) * float
        // size (4)
        glVertexAttribPointer(uvLocation, 2, GL_FLOAT, false, 8 * 4, (3 + 3) * 4);
        glEnableVertexAttribArray(uvLocation);

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        screenBuffer = ByteBuffer.allocateDirect(this.width * this.height * 3).order(ByteOrder.nativeOrder());

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean value) {
        this.updated = value;
    }

    /**
     * Sets the pixel at x,y to the given color.
     */
    public void set(int x, int y, Color color) {
        final var idx = (y * width + x) * 3;
        screen[idx] = color.red;
        screen[idx + 1] = color.green;
        screen[idx + 2] = color.blue;
        updated = true;
    }

    public static void set(int x, int y, int red, int green, int blue) {
        checkArgument(red >= 0 && red <= 255, "Red channel must have values between 0 and 255");
        checkArgument(green >= 0 && green <= 255, "Green channel must have values between 0 and 255");
        checkArgument(blue >= 0 && blue <= 255, "Blue channel must have values between 0 and 255");

        final var idx = (y * width + x) * 3;
        screen[idx] = (byte) (0xFF & red);
        screen[idx + 1] = (byte) (0xFF & green);
        screen[idx + 2] = (byte) (0xFF & blue);
        updated = true;
    }

    public void set(int idx, int red, int green, int blue) {
        checkArgument(red >= 0 && red <= 255, "Red channel must have values between 0 and 255");
        checkArgument(green >= 0 && green <= 255, "Green channel must have values between 0 and 255");
        checkArgument(blue >= 0 && blue <= 255, "Blue channel must have values between 0 and 255");

        var realIdx = idx * 3;

        screen[realIdx] = (byte) (0xFF & red);
        screen[realIdx + 1] = (byte) (0xFF & green);
        screen[realIdx + 2] = (byte) (0xFF & blue);
        updated = true;
    }

    public void clear() {
        System.arraycopy(blankScreen, 0, screen, 0, width * height);
        updated = true;
    }

    public void draw() {
        glBindVertexArray(vao);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBindTexture(GL_TEXTURE_2D, textureId);
        if (updated) {
            screenBuffer.clear();
            screenBuffer.put(screen).flip();
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, screenBuffer);
            updated = false;
        }
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, 0);
    }

    /**
     * Esta función utiliza el algoritmo de Bresenham para trazar una línea, el
     * algorítmo está diseñado para recibir la coordenada de dos puntos donde x1 <
     * x2 & y1 < y2 y se dibuja la línea de izquierda a derecha. Para solucionar
     * esto se implementó una condición que revise el orden de los puntos, y de ser
     * incorrecto se invierten.
     * 
     * Cuando la pendiente es mayor a 0 y menor a 1 se tiene que decidir si el
     * siguiente punto a dibujar se hará como (X + 1, Y) o (X + 1, Y + 1), esto es
     * porque la coordenada X estará cambiando constantemente mientras que la
     * coordenada Y solo incrementará de manera intermitente.
     * 
     * Esta dinámica cambia según la pendiente de la línea, el algoritmo trabaja
     * diferente dependiendo de la pendiente de la línea que se esté dibujando.
     * Cuando la pendiente es mayor a 1 o menor a -1 la variable que cambia
     * constantemente es Y mientras que X incrementará de manera intermitente, por
     * lo que el error se calcula con respecto a Y en lugar de X.
     */
    public static void myDrawLine(int x1, int y1, int x2, int y2) {

        var width = 299;
        int m_new;
        int slope_error_new;

        float dx = x2 - x1;
        float dy = y2 - y1;
        float slope = dy / dx;

        if ((x1 > width) || (x2 > width)) {
            System.out.println("invalid line");
        } else {

            if (x1 > x2) {
                var tempX = x2;
                x2 = x1;
                x1 = tempX;

                var tempY = y2;
                y2 = y1;
                y1 = tempY;
            }

            if (slope >= 0 && slope <= 1) {
                m_new = 2 * (y2 - y1);
                slope_error_new = m_new - (x2 - x1);
                for (int x = x1, y = y1; x <= x2; x++) {
                    set(x, y, 255, 255, 255);
                    slope_error_new += m_new;
                    if (slope_error_new >= 0) {
                        y++;
                        slope_error_new -= 2 * (x2 - x1);
                    }
                }
            } else if (slope > 1) {
                m_new = 2 * (x2 - x1);
                slope_error_new = m_new - (y2 - y1);
                for (int x = x1, y = y1; y <= y2; y++) {
                    set(x, y, 255, 255, 255);
                    slope_error_new += m_new;

                    if (slope_error_new >= 0) {
                        x++;
                        slope_error_new -= 2 * (y2 - y1);
                    }
                }
            } else if (slope < 0 && slope >= -1) {
                m_new = -1 * (2 * (y2 - y1));
                slope_error_new = m_new - (x2 - x1);
                for (int x = x1, y = y1; x <= x2; x++) {
                    set(x, y, 255, 255, 255);
                    slope_error_new += m_new;
                    if (slope_error_new >= 0) {
                        y--;
                        slope_error_new -= 2 * (x2 - x1);
                    }
                }
            } else if (slope < -1) {
                m_new = -1 * (2 * (x2 - x1));
                slope_error_new = m_new - (y2 - y1);
                for (int x = x1, y = y1; y >= y2; y--) {
                    set(x, y, 255, 255, 255);
                    slope_error_new += m_new;
                    if (slope_error_new <= 0) {
                        x++;
                        slope_error_new -= 2 * (y2 - y1);
                    }
                }
            }
        }
    }

    /***
     * Esta es la función que utiliza el algorítmo DDA, y funciona correctamente en
     * .todos los escenarios.
     * 
     * Este algoritmo calcula el número de pixeles que se tienen que trazar, lo cual
     * dependerá de la pendiente y el tamaño que tenga la línea Además se calcula el
     * incremento promedio que existe por cada pixel de la línea, debido a que no se
     * puede dibujar pixeles en posiciones flotantes este incremento se suma a la
     * coordenada X y Y, redondeandolo al entero más cercano.
     * 
     * A diferencia del algorítmo de Bresenham, aquí se incrementa tanto X como Y
     * simultaneamente sin importar si el número incrementa al ser redondeado al
     * entero más cercano o no.
     */
    public static void drawLine(int x1, int y1, int x2, int y2) {

        int dx = x2 - x1;
        int dy = y2 - y1;

        int steps = Math.abs(dx) > Math.abs(dy) ? Math.abs(dx) : Math.abs(dy);

        float Xinc = dx / (float) steps;
        float Yinc = dy / (float) steps;

        float X = x1;
        float Y = y1;

        for (int i = 0; i <= steps; i++) {
            set(Math.round(X), Math.round(Y), 255, 255, 255);
            X += Xinc;
            Y += Yinc;
        }
    }

    /**
     * EL algorítmo de Bresenham para trazar un círculo trata de aprovechar la
     * simetría del círculo, dividiendo los 360 grados en 8 partes iguales de 45
     * grados. Así que para cada punto que se coloca en el primer octante, este se
     * vera reflejado respectivamente en cada uno de los otros 7 sectores del
     * círculo.
     * 
     * El siguiente paso es conocer la dirección hacia la que será dibujado el
     * siguiente pixel, para eso se calcula un parámetro d y se revisa si este es
     * igual a 0, de ser así el siguiente punto se moverá hacia abajo como
     * 
     * (x + 1, y - 1)
     * 
     * De otra forma el siguiente punto se moverá de forma horizontal creciendo en
     * el eje x. El algorítmo se repite hasta haber completado de dibujar un
     * octante, y por consecuente habiendo dibujado todo el círculo.
     * 
     */
    public static void drawCircle(int xc, int yc, int r) {
        var x = 0;
        var y = r;
        var d = 3 - 2 * r;
        circle(xc, yc, x, y);
        while (y >= x) {
            x++;
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
            circle(xc, yc, x, y);
        }
    }

    public static void circle(int xc, int yc, int x, int y) {
        set(xc + x, yc + y, 255, 255, 255);
        set(xc - x, yc + y, 255, 255, 255);
        set(xc + x, yc - y, 255, 255, 255);
        set(xc - x, yc - y, 255, 255, 255);
        set(xc + y, yc + x, 255, 255, 255);
        set(xc - y, yc + x, 255, 255, 255);
        set(xc + y, yc - x, 255, 255, 255);
        set(xc - y, yc - x, 255, 255, 255);
    }
}