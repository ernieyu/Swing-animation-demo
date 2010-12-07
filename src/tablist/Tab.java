package tablist;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Tab component.
 */
public class Tab extends JPanel {
    private static final Color SELECTED_BACKGROUND = Color.GRAY;
    private static final Color UNSELECTED_BACKGROUND = Color.LIGHT_GRAY;

    private final Action mainAction;
    private final Action closeAction;
    
    private JLabel nameLabel = new JLabel();
    private JButton closeButton = new CloseButton();
    
    private boolean selected;
    
    /**
     * Constructs a tab with the specified main and close actions.
     */
    public Tab(Action mainAction, Action closeAction) {
        this.mainAction = mainAction;
        this.closeAction = closeAction;
        
        setBackground(UNSELECTED_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        setLayout(new GridBagLayout());
        
        nameLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        nameLabel.setText((String) mainAction.getValue(Action.NAME));
        nameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                select();
            }
        });
        
        add(nameLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
        		GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
        add(closeButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
        		GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
    }
    
    /**
     * Creates a new Action that can be used to select the tab.
     */
    public Action createSelectAction() {
        return new AbstractAction(nameLabel.getText()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                select();
            }
        };
    }
    
    /**
     * Returns an indicator that determines whether the tab is selected.
     */
    public boolean isSelected() {
        return selected;
    }
    
    /**
     * Sets an indicator that determines whether the tab is selected.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        setBackground(selected ? SELECTED_BACKGROUND : UNSELECTED_BACKGROUND);
    }
    
    /**
     * Fires the select action for the tab. 
     */
    private void select() {
        ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "select");
        mainAction.actionPerformed(evt);
    }
    
    /**
     * Fires the close action for the tab.
     */
    private void close() {
        ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "close");
        closeAction.actionPerformed(evt);
    }
    
    /**
     * Close button.
     */
    private class CloseButton extends JButton {
        
        public CloseButton() {
            setContentAreaFilled(false);
            setFont(getFont().deriveFont(Font.BOLD));
            setMargin(new Insets(3,3,3,3));
            setIcon(new CloseIcon(Color.BLACK, 6));
            setRolloverIcon(new CloseIcon(Color.RED, 6));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    setCursor(Cursor.getDefaultCursor());
                }
                
                @Override
                public void mouseClicked(MouseEvent e) {
                    close();
                }
            });
        }
    }
    
    /**
     * Close icon.
     */
    private static class CloseIcon implements Icon {
        private static final float SIZE_TO_THICKNESS = 6.0f;
        
        private final Color color;
        private final int size;

        public CloseIcon(Color color, int size) {
            this.color = color;
            this.size = size;
        }

        @Override
        public int getIconHeight() {
            return this.size;
        }

        @Override
        public int getIconWidth() {
            return this.size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // Create graphics.
            Graphics2D g2d = (Graphics2D) g.create();
            
            // Set graphics to use anti-aliasing for smoothness.
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Set line color and thickness.
            float thickness = Math.max(this.size / SIZE_TO_THICKNESS, 1.0f);
            g2d.setColor(this.color);
            g2d.setStroke(new BasicStroke(thickness));

            // Create shape.
            Shape backSlash = new Line2D.Double(0, 0, this.size, this.size);
            Shape slash = new Line2D.Double(0, this.size, this.size, 0);
            
            // Draw shape at specified position.
            g2d.translate(x, y);
            g2d.draw(backSlash);
            g2d.draw(slash);

            // Dispose graphics.
            g2d.dispose();
        }
    }
}
