import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import java.io.File;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private class Tile {
        int x;
        int y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    int boardWidth;
    int boardHeight;
    int tileSize = 25;

    // Snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;

    // Food
    Tile food;
    Random random;

    // Game logic
    int velocityX;
    int velocityY;
    Timer gameLoop;
    boolean gameOver = false;

    // New features
    int level = 1;
    int score = 0;

    JButton restartButton;

    SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.black);
        setLayout(null); // Allow absolute positioning
        addKeyListener(this);
        setFocusable(true);

        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<>();

        food = new Tile(10, 10);
        random = new Random();
        placeFood();

        velocityX = 1;
        velocityY = 0;

        gameLoop = new Timer(100, this);
        gameLoop.start();

        // Restart button
        restartButton = new JButton("Restart");
        restartButton.setBounds(boardWidth / 2 - 50, boardHeight / 2 + 30, 100, 40); // Position and size
        restartButton.setBackground(Color.black);
        restartButton.setForeground(Color.white);
        restartButton.setFont(new Font("Arial", Font.BOLD, 16));
        restartButton.setFocusPainted(false);
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> restartGame());
        this.add(restartButton);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Gradient background
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, Color.black, 0, boardHeight, Color.black);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, boardWidth, boardHeight);

        // Grid Lines
        g2d.setColor(Color.black);
        for (int i = 0; i < boardWidth / tileSize; i++) {
            g2d.drawLine(i * tileSize, 0, i * tileSize, boardHeight);
            g2d.drawLine(0, i * tileSize, boardWidth, i * tileSize);
        }

        // Food
        g2d.setColor(Color.red);
        g2d.fillRoundRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize, 10, 10);

        // Snake Head
        g2d.setColor(Color.cyan);
        g2d.fillRoundRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize, 10, 10);

        // Snake Body
        for (Tile snakePart : snakeBody) {
            g2d.setColor(Color.green);
            g2d.fillRoundRect(snakePart.x * tileSize, snakePart.y * tileSize, tileSize, tileSize, 10, 10);
        }

        // Score and Level
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(Color.white);
        g2d.drawString("Score: " + score, 10, 20);
        g2d.drawString("Level: " + level, 10, 40);

        if (gameOver) {
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            g2d.setColor(Color.red);
            g2d.drawString("Game Over!", boardWidth / 2 - 100, boardHeight / 2 - 20);
            restartButton.setVisible(true);
        } else {
            restartButton.setVisible(false);
        }
    }

    public void placeFood() {
        food.x = random.nextInt(boardWidth / tileSize);
        food.y = random.nextInt(boardHeight / tileSize);
    }

    public void move() {
        if (collision(snakeHead, food)) {
            snakeBody.add(new Tile(food.x, food.y));
            placeFood();
            score++;
            playSound("eat.wav");

            if (score % 10 == 0) {
                level++;
                gameLoop.setDelay(Math.max(50, gameLoop.getDelay() - 10));
            }
        }

        for (int i = snakeBody.size() - 1; i >= 0; i--) {
            Tile snakePart = snakeBody.get(i);
            if (i == 0) {
                snakePart.x = snakeHead.x;
                snakePart.y = snakeHead.y;
            } else {
                Tile prevSnakePart = snakeBody.get(i - 1);
                snakePart.x = prevSnakePart.x;
                snakePart.y = prevSnakePart.y;
            }
        }

        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        for (Tile snakePart : snakeBody) {
            if (collision(snakeHead, snakePart)) {
                gameOver = true;
                playSound("gameover.wav");
            }
        }

        if (snakeHead.x < 0 || snakeHead.x >= boardWidth / tileSize || snakeHead.y < 0 || snakeHead.y >= boardHeight / tileSize) {
            gameOver = true;
            playSound("gameover.wav");
        }
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (velocityY != 1) {
                    velocityX = 0;
                    velocityY = -1;
                }
                break;
            case KeyEvent.VK_DOWN:
                if (velocityY != -1) {
                    velocityX = 0;
                    velocityY = 1;
                }
                break;
            case KeyEvent.VK_LEFT:
                if (velocityX != 1) {
                    velocityX = -1;
                    velocityY = 0;
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (velocityX != -1) {
                    velocityX = 1;
                    velocityY = 0;
                }
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public void playSound(String soundFile) {
        try {
            File soundPath = new File(soundFile);
            if (soundPath.exists()) {
                Clip clip = AudioSystem.getClip();
                clip.open(AudioSystem.getAudioInputStream(soundPath));
                clip.start();
            } else {
                System.out.println("Can't find file");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void restartGame() {
        snakeHead = new Tile(5, 5);
        snakeBody.clear();
        placeFood();
        velocityX = 1;
        velocityY = 0;
        score = 0;
        level = 1;
        gameOver = false;
        gameLoop.setDelay(100);
        repaint();
    }
}
