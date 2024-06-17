package GiKCzK.lab;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Renderer {

    public enum LineAlgo { NAIVE, BRESENHAM, BRESENHAM_INT; }

    private BufferedImage render;
    public final int h = 200;
    public final int w = 200;

    private String filename;
    private LineAlgo lineAlgo = LineAlgo.NAIVE;

    public Renderer(String filename) {
        render = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        this.filename = filename;
    }

    public void drawPoint(int x, int y) {
        int white = 255 | (255 << 8) | (255 << 16) | (255 << 24);
        render.setRGB(x, y, white);
    }

    public void drawLine(int x0, int y0, int x1, int y1, LineAlgo lineAlgo) {
        if(lineAlgo == LineAlgo.NAIVE) drawLineNaive(x0, y0, x1, y1);
        if(lineAlgo == LineAlgo.BRESENHAM) drawLineBresenham(x0, y0, x1, y1);
        if(lineAlgo == LineAlgo.BRESENHAM_INT) drawLineBresenhamInt(x0, y0, x1, y1);
    }


    public void drawLineNaive(int x0, int y0, int x1, int y1) {
        drawLineNaive(x0, y0, x1, y1, 0xffffffff);
    }

    public void drawLineNaive(int x0, int y0, int x1, int y1, int color) {

        // Najpierw odddzielnie obsługujemy przypadek pionowej lini, aby uniknąć przypadku dzielenia przez 0 i m = Inf
        // zakładamy przy tym, że można podać współrzędne y w dowolnej kolejności, niekoniecznie y0 < y1
        if(x0 == x1) {
            for(int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
                render.setRGB(x0, y, color);
            }
        }
        else {
            // jeśli punkty podane są w takiej kolejności, że x1 jest mniejsze od x0, to przed rysowaniem podmieniamy
            // punkty miejscami, aby iterować od x0 do x1. Czyli sprawdzamy koniec z początkiem.
            if(x1 < x0) {
                int temp = x0;
                x0 = x1;
                x1 = temp;
                temp = y0;
                y0 = y1;
                y1 = temp;
            }

            // rysujemy
            int dx = x1 - x0;
            int dy = y1 - y0;
            float m = dy / (float)dx;
            float step = 0.1f; // Dla uniknięcia przerywanej linii dla przypadków m >> 1 powinniśmy iterować subpixelowo:
            // dodawać do x mniej niż 1 pixel i wyznaczać y. Rozwiązuje to przypadek w którym dla stromej
            // linii w miejscu jednego x powinnismy narysowac kilka pikseli o roznym y.
            // Alternatywne podejście: porównać dx i dy i iterować po x lub po y.
            float y = y0;

            for(float x = x0; x <= x1; x = x + step) {
                y = y + m * step;
                render.setRGB(Math.round(x), Math.round(y), color);
            }
        }
    }

    public void drawLineBresenham(int x0, int y0, int x1, int y1) {
        drawLineBresenham(x0, y0, x1, y1, 0xffffffff);
    }

    public void drawLineBresenham(int x0, int y0, int x1, int y1, int color) {
        // wybieramy czy iterujemy po x czy po y
        int dx_abs =  Math.abs(x1-x0);
        int dy_abs = Math.abs(y1-y0);
        if(dy_abs <= dx_abs) {
            // sprawdzamy koniec z początkiem, ewentualnie podmieniamy, patrz drawLineNaive
            if(x1 < x0) {
                int temp = x0;
                x0 = x1;
                x1 = temp;
                temp = y0;
                y0 = y1;
                y1 = temp;
            }

            int dx = x1-x0;
            int dy = y1-y0;

            float derr = Math.abs(dy/(float)(dx));
            float err = 0;

            int y = y0;

            for (int x=x0; x<=x1; x++) {
                render.setRGB(x, y, color);
                err += derr;
                if (err > 0.5) {
                    y += (y1 > y0 ? 1 : -1);
                    err -= 1.;
                }
            }
        }
        // iterujemy po y
        else {
            // sprawdzamy koniec z początkiem, ewentualnie podmieniamy
            if(y1 < y0) {
                int temp = x0;
                x0 = x1;
                x1 = temp;
                temp = y0;
                y0 = y1;
                y1 = temp;
            }

            int dx = x1-x0;
            int dy = y1-y0;

            float derr = Math.abs(dx/(float)(dy));
            float err = 0;

            int x = x0;

            for (int y=y0; y<=y1; y++) {
                render.setRGB(x, y, color);
                err += derr;
                if (err > 0.5) {
                    x += (x1 > x0 ? 1 : -1);
                    err -= 1.;
                }
            }
        }
    }
    public void drawLineBresenhamInt(int x0, int y0, int x1, int y1) {
        drawLineBresenhamInt(x0, y0, x1, y1, 0xffffffff);
    }

    public void drawLineBresenhamInt(int x0, int y0, int x1, int y1, int color) {
        // wybieramy czy iterujemy po x czy po y
        int dx_abs =  Math.abs(x1-x0);
        int dy_abs = Math.abs(y1-y0);
        if(dy_abs <= dx_abs) {
            // sprawdzamy koniec z początkiem, ewentualnie podmieniamy, patrz drawLineNaive
            if(x1 < x0) {
                int temp = x0;
                x0 = x1;
                x1 = temp;
                temp = y0;
                y0 = y1;
                y1 = temp;
            }

            int dx = x1-x0;
            int dy = y1-y0;

            int derr = 2 * Math.abs(dy); // pomnożono przez 2 * dx aby uniknąć ułamka i float
            int err = 0;

            int y = y0;

            for (int x=x0; x<=x1; x++) {
                render.setRGB(x, y, color);
                err += derr;
                if (err > dx) {
                    y += (y1 > y0 ? 1 : -1);
                    err -= 2 * dx;
                }
            }
        }
        // iterujemy po y
        else {
            // sprawdzamy koniec z początkiem, ewentualnie podmieniamy
            if(y1 < y0) {
                int temp = x0;
                x0 = x1;
                x1 = temp;
                temp = y0;
                y0 = y1;
                y1 = temp;
            }

            int dx = x1-x0;
            int dy = y1-y0;

            int derr = 2 * Math.abs(dx);
            int err = 0;

            int x = x0;

            for (int y=y0; y<=y1; y++) {
                render.setRGB(x, y, color);
                err += derr;
                if (err > dy) {
                    x += (x1 > x0 ? 1 : -1);
                    err -= 2 * dy;
                }
            }
        }
    }


    public void save() throws IOException {
        File outputfile = new File(filename);
        render = Renderer.verticalFlip(render);
        ImageIO.write(render, "png", outputfile);
    }

    public void clear() {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int black = 0 | (0 << 8) | (0 << 16) | (255 << 24);
                render.setRGB(x, y, black);
            }
        }
    }

    public static BufferedImage verticalFlip(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage flippedImage = new BufferedImage(w, h, img.getColorModel().getTransparency());
        Graphics2D g = flippedImage.createGraphics();
        g.drawImage(img, 0, 0, w, h, 0, h, w, 0, null);
        g.dispose();
        return flippedImage;
    }
}
