import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

class HappyBall extends JPanel implements ActionListener, KeyListener {

    private final int WIDTH = 400;
    private final int HEIGHT = 600;
    private final int GROUND_HEIGHT = 100;
    private final int BALL_SIZE = 22;
    private final int PIPE_WIDTH = 70;
    private final int GAP_HEIGHT = 160;
    private final double GRAVITY = 0.5;
    private final double JUMP = -8;

    private int ballX = 100;
    private int ballY = HEIGHT / 2;
    private double velocity = 0;

    private static class PipePair {
        int x, gapY;
        boolean passed = false;
        PipePair(int x, int gapY) { this.x = x; this.gapY = gapY; }
    }
    private final ArrayList<PipePair> pipes = new ArrayList<>();
    private final Random rand = new Random();

    private final ArrayList<Rectangle> clouds = new ArrayList<>();

    private boolean started = false;
    private boolean gameOver = false;
    private int score = 0;

    private final Timer timer = new Timer(1000 / 60, this);

    public HappyBall() {
        JFrame frame = new JFrame("Happy Ball");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.add(this);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addKeyListener(this);
        setBackground(new Color(135, 206, 250));

        for (int i = 0; i < 5; i++)
            clouds.add(new Rectangle(rand.nextInt(WIDTH), rand.nextInt(200),
                    60 + rand.nextInt(40), 30 + rand.nextInt(15)));

        timer.start();
    }

    private void addPipe(int x) {
        int minGapY = 100;
        int maxGapY = HEIGHT - GROUND_HEIGHT - 200;
        int gapY = minGapY + rand.nextInt(maxGapY - minGapY);
        pipes.add(new PipePair(x, gapY));
    }

    private void resetGame() {
        ballY = HEIGHT / 2;
        velocity = 0;
        score = 0;
        pipes.clear();

        int startX = WIDTH + 100;
        for (int i = 0; i < 4; i++)
            addPipe(startX + i * (160 + rand.nextInt(40)));

        gameOver = false;
        started = true;
        timer.start();
    }

    private void jump() {
        if (gameOver) resetGame();
        else if (!started) resetGame();
        velocity = JUMP;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        for (Rectangle c : clouds) {
            c.x -= 1;
            if (c.x + c.width < 0) c.x = WIDTH + rand.nextInt(100);
        }

        if (started) {

            for (PipePair p : pipes) p.x -= 3;

           
            if (pipes.isEmpty() || pipes.get(pipes.size() - 1).x + PIPE_WIDTH < WIDTH - 160) {
                int lastX = pipes.isEmpty() ? WIDTH : pipes.get(pipes.size() - 1).x;
                addPipe(lastX + 160 + rand.nextInt(40));
            }

           
            Iterator<PipePair> it = pipes.iterator();
            while (it.hasNext()) {
                PipePair p = it.next();
                if (p.x + PIPE_WIDTH < 0) it.remove();
            }

           
            velocity += GRAVITY;
            ballY += (int) velocity;

           
            Rectangle ballRect = new Rectangle(ballX, ballY, BALL_SIZE, BALL_SIZE);
            for (PipePair p : pipes) {
                Rectangle top = new Rectangle(p.x, 0, PIPE_WIDTH, p.gapY);
                Rectangle bottom = new Rectangle(p.x, p.gapY + GAP_HEIGHT, PIPE_WIDTH,
                        HEIGHT - p.gapY - GAP_HEIGHT - GROUND_HEIGHT);
                if (ballRect.intersects(top) || ballRect.intersects(bottom)) gameOver = true;

                
                if (!p.passed && p.x + PIPE_WIDTH < ballX) {
                    score++;
                    p.passed = true;
                }
            }

            if (ballY + BALL_SIZE > HEIGHT - GROUND_HEIGHT || ballY < 0) gameOver = true;
            if (gameOver) timer.stop();
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

       
        GradientPaint sky = new GradientPaint(0, 0, new Color(135, 206, 250),
                                              0, HEIGHT, new Color(176, 224, 230));
        g2.setPaint(sky);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        
        g2.setColor(new Color(255, 255, 255, 200));
        for (Rectangle c : clouds)
            g2.fillRoundRect(c.x, c.y, c.width, c.height, 20, 20);

        
        g2.setColor(new Color(222, 184, 135));
        g2.fillRect(0, HEIGHT - GROUND_HEIGHT, WIDTH, GROUND_HEIGHT);
        g2.setColor(new Color(210, 170, 120));
        for (int i = 0; i < WIDTH; i += 20)
            g2.fillRect(i, HEIGHT - GROUND_HEIGHT + (i % 40 == 0 ? 0 : 5), 20, 10);

        
        for (PipePair p : pipes) {
            GradientPaint pipeColor = new GradientPaint(p.x, 0, new Color(0, 200, 0),
                                                        p.x + PIPE_WIDTH, 0, new Color(0, 120, 0));
            g2.setPaint(pipeColor);
            g2.fillRect(p.x, 0, PIPE_WIDTH, p.gapY);
            g2.fillRect(p.x, p.gapY + GAP_HEIGHT, PIPE_WIDTH,
                        HEIGHT - p.gapY - GAP_HEIGHT - GROUND_HEIGHT);
        }

        
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillOval(ballX + 3, ballY + 3, BALL_SIZE, BALL_SIZE);
        g2.setColor(new Color(255, 70, 70));
        g2.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);
        g2.setColor(Color.WHITE);
        g2.fillOval(ballX + 5, ballY + 5, 6, 6);
        g2.setColor(Color.BLACK);
        g2.drawOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

        
        g2.setFont(new Font("MV Boli", Font.BOLD, 24));
        g2.setColor(Color.BLACK);
        g2.drawString("Score: " + score, WIDTH / 2 - 50, 40);

        
        if (!started) {
            g2.setFont(new Font("MV Boli", Font.BOLD, 22));
            g2.drawString("Press SPACE to start", WIDTH / 2 - 130, HEIGHT / 2);
        }

        
        if (gameOver) {
            
            Font bigFont = new Font("MV Boli", Font.BOLD, 80);
            g2.setFont(bigFont);
            g2.setColor(new Color(255, 255, 255, 40));
            FontMetrics fm = g2.getFontMetrics(bigFont);
            String bgName = "ATULYA JHA";
            int x = (WIDTH - fm.stringWidth(bgName)) / 2;
            int y = HEIGHT / 2 + fm.getAscent() / 2;
            g2.drawString(bgName, x, y);

            
            g2.setFont(new Font("MV Boli", Font.BOLD, 32));
            g2.setColor(Color.BLACK);
            g2.drawString("Game Over!", WIDTH / 2 - 100, HEIGHT / 2 - 30);
            g2.setFont(new Font("MV Boli", Font.PLAIN, 20));
            g2.drawString("Press SPACE to restart", WIDTH / 2 - 115, HEIGHT / 2 + 15);

            
            g2.setFont(new Font("MV Boli", Font.PLAIN, 16));
            g2.setColor(Color.BLACK);
            String signature = "Atulya Jha";
            g2.drawString(signature, WIDTH / 2 - g2.getFontMetrics().stringWidth(signature)/2,
                          HEIGHT / 2 + 50);

            
            g2.setFont(new Font("MV Boli", Font.PLAIN, 14));
            g2.setColor(Color.BLACK);
            String lang = "Java";
            g2.drawString(lang, WIDTH / 2 - g2.getFontMetrics().stringWidth(lang)/2,
                          HEIGHT / 2 + 70);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) jump();
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HappyBall::new);
    }
}
