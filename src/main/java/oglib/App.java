package oglib;

import java.io.IOException;

import oglib.components.CompileException;
import oglib.components.CreateException;
import oglib.components.Program;
import oglib.game.GameState;
import oglib.gui.Simple2DBuffer;
import oglib.gui.WindowGL;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.IOException;
import java.nio.*;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class App {

    public static void main(String[] args) {
        var gameState = GameState.getGameState();
        var width = 300;
        var height = 300;
        var w = new WindowGL(width, height, "Drawing Program", gameState);

        try {
            var program = new Program("screen.vert", "screen.frag");
            var screen = new Simple2DBuffer(width, height);

            // x1 y1 x2 y2
            // Simple2DBuffer.drawLine(150, 150, 150, 299); // 1
            // Simple2DBuffer.drawLine(150, 150, 230, 299); // 2
            // Simple2DBuffer.drawLine(150, 150, 299, 299); // 3
            // Simple2DBuffer.drawLine(150, 150, 299, 230); // 4
            // Simple2DBuffer.drawLine(150, 150, 299, 150); // 5
            //// negative slopDDA
            // Simple2DBuffer.drawLine(150, 150, 299, 70); // 6
            // Simple2DBuffer.drawLine(150, 150, 299, 1); // 7 slope = -1
            // Simple2DBuffer.drawLine(150, 150, 230, 0); // 8
            // Simple2DBuffer.drawLine(150, 150, 150, 0); // 9
            //
            // Simple2DBuffer.drawLine(150, 150, 70, 0); // 10
            // Simple2DBuffer.drawLine(150, 150, 0, 0); // 11
            // Simple2DBuffer.drawLine(150, 150, 0, 70); // 12
            // Simple2DBuffer.drawLine(150, 150, 0, 150); // 13
            // Simple2DBuffer.drawLine(150, 150, 0, 230); // 14
            // Simple2DBuffer.drawLine(150, 150, 0, 299); // 15
            // Simple2DBuffer.drawLine(150, 150, 70, 299); // 16

            Simple2DBuffer.DDA(150, 0, 151, 299);

            // Simple2DBuffer.DDA(0, 0, 299, 299);
            // Simple2DBuffer.circle(150, 150, 0);
            // Simple2DBuffer.drawLine(151, 0, 150, 299);
            // Simple2DBuffer.drawLine(250, 250, 350, 0);

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