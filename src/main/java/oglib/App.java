package oglib;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import java.io.IOException;

import oglib.components.CompileException;
import oglib.components.CreateException;
import oglib.components.Program;
import oglib.game.GameState;
import oglib.gui.Simple2DBuffer;
import oglib.gui.WindowGL;

public class App {

    public static void main(String[] args) {
        var gameState = GameState.getGameState();
        var width = 300;
        var height = 300;
        var w = new WindowGL(width, height, "Drawing Program", gameState);

        try {
            var program = new Program("screen.vert", "screen.frag");
            var screen = new Simple2DBuffer(width, height);

            Simple2DBuffer.drawLine(150, 150, 150, 299); // 1
            Simple2DBuffer.drawLine(150, 150, 230, 299); // 2
            Simple2DBuffer.drawLine(150, 150, 299, 299); // 3
            Simple2DBuffer.drawLine(150, 150, 299, 230); // 4
            Simple2DBuffer.drawLine(150, 150, 299, 150); // 5
            // negative slope
            Simple2DBuffer.drawLine(150, 150, 299, 70); // 6
            Simple2DBuffer.drawLine(150, 150, 299, 1); // 7 slope = -1
            Simple2DBuffer.drawLine(150, 150, 230, 0); // 8
            Simple2DBuffer.drawLine(150, 150, 150, 0); // 9

            Simple2DBuffer.drawLine(150, 150, 70, 0); // 10
            Simple2DBuffer.drawLine(150, 150, 0, 0); // 11
            Simple2DBuffer.drawLine(150, 150, 0, 70); // 12
            Simple2DBuffer.drawLine(150, 150, 0, 150); // 13
            // negative slope
            Simple2DBuffer.drawLine(150, 150, 0, 230); // 14
            Simple2DBuffer.drawLine(150, 150, 0, 299); // 15
            Simple2DBuffer.drawLine(150, 150, 70, 299); // 16

            Simple2DBuffer.drawCircle(150, 150, 50);
            Simple2DBuffer.drawCircle(150, 150, 100);
            Simple2DBuffer.drawCircle(150, 150, 148);

            while (!w.windowShouldClose()) {
                glClearColor(0f, 0f, 0f, 1.0f);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                program.use();
                screen.draw();
                w.swapBuffers();
                w.pollEvents();
            }
            w.destroy();
        } catch (IOException | CreateException | CompileException e) {
            e.printStackTrace();
        }

    }
}