import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ErDiagramRenderer {

    private static final Color BG_TOP = new Color(244, 248, 255);
    private static final Color BG_BOTTOM = new Color(234, 241, 250);
    private static final Color CARD = new Color(255, 255, 255);
    private static final Color BORDER = new Color(197, 210, 225);
    private static final Color TEXT = new Color(30, 42, 56);
    private static final Color MUTED = new Color(95, 110, 125);
    private static final Color HEADER = new Color(44, 104, 179);

    private static final int WIDTH = 3200;
    private static final int HEIGHT = 1400;

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 46);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 22);
    private static final Font TABLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font ROW_FONT = new Font("Consolas", Font.PLAIN, 18);
    private static final Font REL_FONT = new Font("Segoe UI", Font.BOLD, 17);

    private static final class TableDef {
        final String name;
        final int x;
        final int y;
        final int w;
        final List<String> rows;

        TableDef(String name, int x, int y, int w, List<String> rows) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.w = w;
            this.rows = rows;
        }

        int h() {
            return 56 + rows.size() * 34 + 10;
        }

        int centerY() {
            return y + h() / 2;
        }

        int centerX() {
            return x + w / 2;
        }

        int left() {
            return x;
        }

        int right() {
            return x + w;
        }

        int top() {
            return y;
        }

        int bottom() {
            return y + h();
        }
    }

    private static final class Relation {
        final int[] xs;
        final int[] ys;
        final String leftCard;
        final String rightCard;
        final String label;
        final Color color;
        final int labelX;
        final int labelY;

        Relation(int[] xs, int[] ys, String leftCard, String rightCard, String label, Color color, int labelX, int labelY) {
            this.xs = xs;
            this.ys = ys;
            this.leftCard = leftCard;
            this.rightCard = rightCard;
            this.label = label;
            this.color = color;
            this.labelX = labelX;
            this.labelY = labelY;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            paintBackground(g);
            paintHeader(g);

            TableDef users = new TableDef(
                    "users", 80, 120, 620, List.of(
                    "id : BIGINT [PK, AI]",
                    "username : VARCHAR(50) [UK]",
                    "email : VARCHAR(100) [UK]",
                    "password : VARCHAR(255)",
                    "enabled : BIT(1)",
                    "created_at : DATETIME"
            ));
            TableDef cart = new TableDef(
                    "cart", 780, 120, 520, List.of(
                    "id : BIGINT [PK, AI]",
                    "user_id : BIGINT [FK, UK]",
                    "total_amount : DECIMAL(12,2)",
                    "created_at : DATETIME",
                    "updated_at : DATETIME"
            ));
            TableDef orders = new TableDef(
                    "orders", 1380, 120, 560, List.of(
                    "id : BIGINT [PK, AI]",
                    "user_id : BIGINT [FK]",
                    "total_amount : DECIMAL(12,2)",
                    "payment_method : VARCHAR(40)",
                    "status : VARCHAR(40)",
                    "created_at : DATETIME"
            ));
            TableDef categories = new TableDef(
                    "categories", 2020, 120, 520, List.of(
                    "id : BIGINT [PK, AI]",
                    "name : VARCHAR(80) [UK]",
                    "enabled : BIT(1)",
                    "created_at : DATETIME"
            ));
            TableDef products = new TableDef(
                    "products", 2620, 120, 520, List.of(
                    "id : BIGINT [PK, AI]",
                    "name : VARCHAR(120)",
                    "description : VARCHAR(500)",
                    "price : DECIMAL(12,2)",
                    "stock_quantity : INT",
                    "status : VARCHAR(30)",
                    "enabled : BIT(1)",
                    "category_id : BIGINT [FK]",
                    "created_at : DATETIME",
                    "updated_at : DATETIME"
            ));
            TableDef roles = new TableDef(
                    "roles", 80, 840, 460, List.of(
                    "id : BIGINT [PK, AI]",
                    "name : VARCHAR(20) [UK]"
            ));
            TableDef userRoles = new TableDef(
                    "user_roles", 620, 840, 560, List.of(
                    "user_id : BIGINT [PK, FK]",
                    "role_id : BIGINT [PK, FK]"
            ));
            TableDef cartItems = new TableDef(
                    "cart_items", 1260, 840, 620, List.of(
                    "id : BIGINT [PK, AI]",
                    "cart_id : BIGINT [FK]",
                    "product_id : BIGINT [FK]",
                    "quantity : INT",
                    "unit_price : DECIMAL(12,2)",
                    "line_total : DECIMAL(12,2)",
                    "created_at : DATETIME",
                    "UNIQUE(cart_id, product_id)"
            ));
            TableDef orderItems = new TableDef(
                    "order_items", 2200, 840, 620, List.of(
                    "id : BIGINT [PK, AI]",
                    "order_id : BIGINT [FK]",
                    "product_id : BIGINT [FK]",
                    "quantity : INT",
                    "unit_price : DECIMAL(12,2)",
                    "line_total : DECIMAL(12,2)"
            ));

            List<TableDef> tables = List.of(
                    users, cart, orders, categories, products, roles, userRoles, cartItems, orderItems
            );
            for (TableDef t : tables) {
                drawTable(g, t);
            }

            List<Relation> relations = new ArrayList<>();
            relations.add(new Relation(
                    new int[]{users.right(), cart.left()},
                    new int[]{users.y + 86, cart.y + 86},
                    "1", "1", "fk_cart_user", new Color(0, 132, 255), 708, 112
            ));
            relations.add(new Relation(
                    new int[]{users.right(), orders.left()},
                    new int[]{users.y + 146, orders.y + 146},
                    "1", "N", "fk_orders_user", new Color(53, 108, 191), 700, 178
            ));
            relations.add(new Relation(
                    new int[]{users.centerX(), users.centerX(), userRoles.left()},
                    new int[]{users.bottom(), userRoles.y - 42, userRoles.y - 42},
                    "1", "N", "fk_user_roles_user", new Color(0, 158, 129), 182, 806
            ));
            relations.add(new Relation(
                    new int[]{roles.right(), userRoles.left()},
                    new int[]{roles.y + 90, userRoles.y + 90},
                    "1", "N", "fk_user_roles_role", new Color(0, 144, 112), 430, 936
            ));
            relations.add(new Relation(
                    new int[]{categories.right(), products.left()},
                    new int[]{categories.y + 130, products.y + 130},
                    "1", "N", "fk_products_category", new Color(195, 95, 0), 2546, 164
            ));
            relations.add(new Relation(
                    new int[]{cart.centerX(), cart.centerX(), cartItems.left()},
                    new int[]{cart.bottom(), cartItems.y - 52, cartItems.y - 52},
                    "1", "N", "fk_cart_items_cart", new Color(173, 65, 162), 1046, 794
            ));
            relations.add(new Relation(
                    new int[]{products.centerX(), products.centerX(), cartItems.right()},
                    new int[]{products.bottom(), cartItems.y - 92, cartItems.y - 92},
                    "1", "N", "fk_cart_items_product", new Color(111, 72, 194), 2844, 778
            ));
            relations.add(new Relation(
                    new int[]{orders.centerX(), orders.centerX(), orderItems.left()},
                    new int[]{orders.bottom(), orderItems.y - 48, orderItems.y - 48},
                    "1", "N", "fk_order_items_order", new Color(193, 49, 67), 1710, 802
            ));
            relations.add(new Relation(
                    new int[]{products.centerX(), products.centerX(), orderItems.centerX()},
                    new int[]{products.bottom(), orderItems.y - 92, orderItems.y - 92},
                    "1", "N", "fk_order_items_product", new Color(136, 36, 142), 2844, 732
            ));

            for (Relation rel : relations) {
                drawRelation(g, rel);
            }

            g.setFont(SUBTITLE_FONT);
            g.setColor(MUTED);
            g.drawString("PK = Primary Key, FK = Foreign Key, UK = Unique Key, AI = Auto Increment", 80, HEIGHT - 42);
        } finally {
            g.dispose();
        }

        File outDir = new File("docs");
        if (!outDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            outDir.mkdirs();
        }
        ImageIO.write(img, "png", new File(outDir, "er-diagram.png"));
    }

    private static void paintBackground(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, BG_TOP, 0, HEIGHT, BG_BOTTOM));
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private static void paintHeader(Graphics2D g) {
        g.setFont(TITLE_FONT);
        g.setColor(TEXT);
        g.drawString("Secure Shopping Cart - ER Diagram", 80, 66);

        g.setFont(SUBTITLE_FONT);
        g.setColor(MUTED);
        g.drawString("Database: secure_cart_db", 82, 98);
    }

    private static void drawTable(Graphics2D g, TableDef t) {
        int h = t.h();
        int arc = 24;

        g.setColor(new Color(20, 28, 38, 24));
        g.fillRoundRect(t.x + 5, t.y + 6, t.w, h, arc, arc);

        g.setColor(CARD);
        g.fill(new RoundRectangle2D.Double(t.x, t.y, t.w, h, arc, arc));

        g.setColor(BORDER);
        g.setStroke(new BasicStroke(1.8f));
        g.draw(new RoundRectangle2D.Double(t.x, t.y, t.w, h, arc, arc));

        g.setColor(HEADER);
        g.fillRoundRect(t.x, t.y, t.w, 56, arc, arc);
        g.fillRect(t.x, t.y + 28, t.w, 28);

        g.setColor(Color.WHITE);
        g.setFont(TABLE_FONT);
        FontMetrics fm = g.getFontMetrics();
        int titleW = fm.stringWidth(t.name);
        g.drawString(t.name, t.x + (t.w - titleW) / 2, t.y + 36);

        g.setFont(ROW_FONT);
        for (int i = 0; i < t.rows.size(); i++) {
            int rowY = t.y + 56 + (i * 34);
            if (i % 2 == 1) {
                g.setColor(new Color(247, 250, 255));
                g.fillRect(t.x + 1, rowY, t.w - 2, 34);
            }
            g.setColor(new Color(229, 236, 245));
            g.drawLine(t.x + 1, rowY, t.x + t.w - 2, rowY);
            g.setColor(TEXT);
            g.drawString(t.rows.get(i), t.x + 16, rowY + 23);
        }
    }

    private static void drawRelation(Graphics2D g, Relation rel) {
        g.setColor(rel.color);
        g.setStroke(new BasicStroke(3.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawPolyline(rel.xs, rel.ys, rel.xs.length);

        int sx = rel.xs[0];
        int sy = rel.ys[0];
        int ex = rel.xs[rel.xs.length - 1];
        int ey = rel.ys[rel.ys.length - 1];
        int px = rel.xs[rel.xs.length - 2];
        int py = rel.ys[rel.ys.length - 2];

        g.fillOval(sx - 5, sy - 5, 10, 10);
        drawArrow(g, px, py, ex, ey, rel.color);

        g.setFont(REL_FONT);
        g.setColor(new Color(20, 38, 58));
        g.drawString(rel.leftCard, sx + 8, sy - 8);
        g.drawString(rel.rightCard, ex - 20, ey - 8);    }

    private static void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2, Color color) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) {
            return;
        }
        double ux = dx / len;
        double uy = dy / len;
        int size = 13;
        int baseX = (int) Math.round(x2 - ux * size);
        int baseY = (int) Math.round(y2 - uy * size);
        int nx = (int) Math.round(-uy * 7);
        int ny = (int) Math.round(ux * 7);

        Polygon p = new Polygon();
        p.addPoint(x2, y2);
        p.addPoint(baseX + nx, baseY + ny);
        p.addPoint(baseX - nx, baseY - ny);

        g.setColor(color);
        g.fillPolygon(p);
    }
}

