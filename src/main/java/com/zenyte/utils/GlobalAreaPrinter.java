package com.zenyte.utils;

import com.zenyte.GameEngine;
import com.zenyte.game.world.region.Area;
import com.zenyte.game.world.region.GlobalAreaManager;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Kris | 15/04/2019 14:09
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class GlobalAreaPrinter implements MapPrinter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalAreaPrinter.class);

    public static final void main(final String[] args) throws IOException {
        GameEngine.main(new String[] {});
        final java.util.ArrayList<java.util.concurrent.Callable<java.lang.Void>> list = new ArrayList<Callable<Void>>();
        for (int i = 0; i < 4; i++) {
            final int plane = i;
            list.add(() -> {
                new GlobalAreaPrinter().load(plane);
                return null;
            });
        }
        ForkJoinPool.commonPool().invokeAll(list);
        System.exit(-1);
    }

    @Override
    public String path(final int plane) {
        return "data/map/produced global areas image " + plane + ".png";
    }

    @Override
    public void draw(final Graphics2D graphics, final int plane) throws IOException {
        log.info("Drawing map image");
        final java.util.ArrayList<java.awt.Color> list = new ArrayList<Color>();
        for (float x = 0; x < 360; x++) {
            Color c = Color.getHSBColor(x / 360, 1, 1);
            list.add(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100));
        }
        Collections.shuffle(list);
        int colI = 0;
        final java.util.HashMap<com.zenyte.game.world.region.Area, java.util.ArrayList<java.awt.Polygon>> ps = new HashMap<Area, ArrayList<Polygon>>();
        for (Area area : GlobalAreaManager.getAllAreas()) {
            for (final com.zenyte.game.world.region.RSPolygon polygon : area.getPolygons()) {
                if (!polygon.getPlanes().contains(plane)) continue;
                final int[][] points = polygon.getPoints();
                final int[] xPoints = new int[points.length];
                final int[] yPoints = new int[points.length];
                for (int i = 0; i < points.length; i++) {
                    xPoints[i] = getX(points[i][0]);
                    yPoints[i] = getY(points[i][1]);
                }
                final java.awt.Polygon poly = new Polygon(xPoints, yPoints, points.length);
                if (!ps.containsKey(area)) {
                    ps.put(area, new ArrayList<>());
                }
                ps.get(area).add(poly);
            }
        }
        for (final java.util.Map.Entry<com.zenyte.game.world.region.Area, java.util.ArrayList<java.awt.Polygon>> polygon : ps.entrySet()) {
            for (final java.awt.Polygon poly : polygon.getValue()) {
                graphics.setColor(list.get(colI++));
                graphics.fillPolygon(poly);
            }
        }
        for (final java.util.Map.Entry<com.zenyte.game.world.region.Area, java.util.ArrayList<java.awt.Polygon>> polygon : ps.entrySet()) {
            for (final java.awt.Polygon poly : polygon.getValue()) {
                final java.awt.FontMetrics metrics = graphics.getFontMetrics();
                final java.awt.geom.Rectangle2D rect = poly.getBounds2D();
                final double rectX = rect.getX();
                final double rectY = rect.getY();
                // Determine the X coordinate for the text
                int x = (int) (rectX + (rect.getWidth() - metrics.stringWidth(polygon.getKey().name())) / 2.0F);
                // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
                int y = (int) (rectY + ((rect.getHeight() - metrics.getHeight()) / 2) + metrics.getAscent());
                // Draw the String
                graphics.setColor(Color.white);
                graphics.drawString(polygon.getKey().name(), x, y);
            }
        }
    }
}
