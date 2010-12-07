package tablist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Application to demonstrate animated tabs in a horizontal panel.
 */
public class AnimationMain {

    JPanel windowPanel = new JPanel();
    
    JPanel topPanel = new JPanel();
    JButton addButton = new JButton();
    AnimatedTabPanel tabPanel = new AnimatedTabPanel();
    
    JPanel contentPanel = new JPanel();
    
    private int tabCount = 0;
    
    /**
     * Constructs the demo content.
     */
    private AnimationMain() {
        windowPanel.setLayout(new BorderLayout());
        
        topPanel.setLayout(new BorderLayout());
        topPanel.setPreferredSize(new Dimension(570, 36));
        
        addButton.setText("Add Tab");
        addButton.setPreferredSize(new Dimension(120, 23));
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabPanel.addTab("Tab " + tabCount++);
            }
        });
        
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setPreferredSize(new Dimension(570, 120));
        
        windowPanel.add(topPanel, BorderLayout.NORTH);
        windowPanel.add(contentPanel, BorderLayout.CENTER);
        topPanel.add(addButton, BorderLayout.WEST);
        topPanel.add(tabPanel.getComponent(), BorderLayout.CENTER);
    }
    
    /**
     * Displays the demo window.
     */
    private void display() {
        JFrame frame = new JFrame("Animated Tab List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.add(windowPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    /**
     * Main application method.
     * @param args
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        AnimationMain demo = new AnimationMain();
        demo.display();
    }
}
