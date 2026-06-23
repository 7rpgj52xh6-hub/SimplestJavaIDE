package simplestJavaIDEpackage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;
import javax.swing.Icon;

/**
 * Crisp, theme-coloured toolbar icons drawn with Java2D (no bitmaps, no extra
 * dependency, sharp at any scale).
 *
 * @author Daniel Trageser
 */
public final class Icons {

  private static final int SIZE = 18;
  private static final Color NEUTRAL = new Color(0xC9CDD4);

  private Icons() {}

  public static Icon play() {
    return icon(
        NEUTRAL,
        g -> {
          g.setColor(Theme.RUN_GREEN);
          Path2D p = new Path2D.Float();
          p.moveTo(5, 3.5);
          p.lineTo(15, 9);
          p.lineTo(5, 14.5);
          p.closePath();
          g.fill(p);
        });
  }

  public static Icon stop() {
    return icon(
        NEUTRAL,
        g -> {
          g.setColor(Theme.STOP_RED);
          g.fill(new RoundRectangle2D.Float(4.5f, 4.5f, 9, 9, 3, 3));
        });
  }

  public static Icon save() {
    return icon(
        NEUTRAL,
        g -> {
          g.draw(new RoundRectangle2D.Float(3.5f, 3.5f, 11, 11, 3, 3));
          g.drawLine(6, 3, 6, 7); // shutter
          g.drawLine(12, 3, 12, 7);
          g.drawLine(6, 7, 12, 7);
          g.fillRect(6, 10, 6, 4); // label
        });
  }

  public static Icon zoomIn() {
    return magnifier(true);
  }

  public static Icon zoomOut() {
    return magnifier(false);
  }

  public static Icon imports() {
    return icon(
        NEUTRAL,
        g -> {
          // a tray (open box) with an arrow pointing down into it
          g.drawLine(3, 10, 3, 15);
          g.drawLine(3, 15, 15, 15);
          g.drawLine(15, 15, 15, 10);
          g.drawLine(9, 2, 9, 11);
          Path2D head = new Path2D.Float();
          head.moveTo(6, 8);
          head.lineTo(9, 11.5);
          head.lineTo(12, 8);
          g.draw(head);
        });
  }

  public static Icon classMarker() {
    return icon(
        Theme.ACCENT,
        g -> {
          g.setColor(Theme.ACCENT);
          g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 14f));
          g.drawString("{}", 2, 14);
        });
  }

  public static Icon bug() {
    return icon(
        NEUTRAL,
        g -> {
          g.draw(new Ellipse2D.Float(6, 5, 6, 10)); // body
          g.drawLine(9, 5, 9, 15); // centre line
          g.drawLine(6, 7, 3, 5); // legs left
          g.drawLine(6, 10, 3, 10);
          g.drawLine(6, 13, 3, 15);
          g.drawLine(12, 7, 15, 5); // legs right
          g.drawLine(12, 10, 15, 10);
          g.drawLine(12, 13, 15, 15);
          g.drawLine(7, 5, 5, 2); // antennae
          g.drawLine(11, 5, 13, 2);
        });
  }

  public static Icon enter() {
    return icon(
        NEUTRAL,
        g -> {
          g.setColor(Theme.ACCENT);
          g.drawLine(14, 4, 14, 11); // down stroke
          g.drawLine(14, 11, 6, 11); // left stroke
          Path2D head = new Path2D.Float();
          head.moveTo(9, 8);
          head.lineTo(5.5, 11);
          head.lineTo(9, 14);
          g.draw(head);
        });
  }

  public static Icon help() {
    return icon(
        NEUTRAL,
        g -> {
          g.draw(new Ellipse2D.Float(3, 3, 12, 12));
          g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 10f));
          g.drawString("?", 7, 12);
        });
  }

  private static Icon magnifier(boolean plus) {
    return icon(
        NEUTRAL,
        g -> {
          g.draw(new Ellipse2D.Float(3, 3, 9, 9));
          g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
          g.drawLine(12, 12, 16, 16);
          g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
          g.drawLine(5, 7, 10, 7);
          if (plus) {
            g.drawLine(7, 5, 7, 9);
          }
        });
  }

  private static Icon icon(Color stroke, Consumer<Graphics2D> painter) {
    return new Icon() {
      @Override
      public void paintIcon(Component c, Graphics g0, int x, int y) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(x, y);
        g.setColor(stroke);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        painter.accept(g);
        g.dispose();
      }

      @Override
      public int getIconWidth() {
        return SIZE;
      }

      @Override
      public int getIconHeight() {
        return SIZE;
      }
    };
  }
}
