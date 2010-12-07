package tablist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.transitions.EffectsManager;
import org.jdesktop.animation.transitions.ScreenTransition;
import org.jdesktop.animation.transitions.TransitionTarget;
import org.jdesktop.animation.transitions.EffectsManager.TransitionType;

/**
 * Container to display animated tabs.  The display component should be
 * obtained by calling <code>getComponent()</code>.
 */
public class AnimatedTabPanel extends JPanel implements TransitionTarget {
    private static final int MAX_TAB_WIDTH = 205;
    private static final int MIN_TAB_WIDTH = 115;
    private static final int RIGHT_INSET = 3;
    
    private final List<Tab> tabList = new ArrayList<Tab>();
    private final JComponent parent;
    private final JButton moreButton;
    
    private final Animator animator;
    private final ScreenTransition transition;
    
    private int maxVisibleTabs;
    private int vizStartIdx = -1;
    private Tab selectedTab;
    private boolean tabRemoved;
    
    /**
     * Constructs an AnimatedTabPanel.
     */
    public AnimatedTabPanel() {
    	setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
    	setLayout(new GridBagLayout());
        setOpaque(false);
        
        // Wrap tab container in a parent component.  (JXLayer also works 
        // here.)  This is needed to work around a bug in the Animated
        // Transitions library.  To work correctly, the library requires the
        // bounds of the animated container relative to its parent to start
        // at location (0, 0).
        parent = new JPanel(new BorderLayout());
        parent.setOpaque(false);
        parent.add(this, BorderLayout.CENTER);
        
        // Create "more" button to list all tabs.
        moreButton = new JButton(new MoreAction());
        
        // Create animator and screen transition for this container.
        animator = new Animator(250);
        transition = new ScreenTransition(this, this, animator);
        
        // Add listener to adjust tab layout when container is resized. 
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Re-do tab layout when number of tabs changes.  This is NOT
                // animated because (1) this usually adjusts tab widths only, 
                // and (2) resize events can occur frequently.  
                if (calculateVisibleTabCount() != maxVisibleTabs) {
                    layoutTabs();
                    revalidate();
                    repaint();
                }
            }
        });
    }

    /**
     * Returns the display component.
     */
    public JComponent getComponent() {
        return parent;
    }
    
    /**
     * Adds a tab with the specified name.  This method starts an animation
     * to update the visible tabs.
     */
    public void addTab(String name) {
        // Create tab.
        Tab tab = new Tab(new SelectAction(name), new RemoveAction());
        
        // Set minimum and maximum widths.
        tab.setMinimumSize(new Dimension(MIN_TAB_WIDTH, tab.getMinimumSize().height));
        tab.setMaximumSize(new Dimension(MAX_TAB_WIDTH, tab.getMaximumSize().height));
        int tabWidth = Math.min(Math.max(tab.getPreferredSize().width, MIN_TAB_WIDTH), MAX_TAB_WIDTH);
        tab.setPreferredSize(new Dimension(tabWidth, tab.getPreferredSize().height));
        
        // Add tab to list.
        tabList.add(0, tab);
        
        // Select tab and start animation.
        setSelectedTab(tab);
        startAnimation(false);
    }
    
    /**
     * Removes the specified tab from the container.  This method starts an
     * animation to update the visible tabs.
     */
    public void removeTab(Tab tab) {
        // Select another tab if possible.
        if (tab == selectedTab) {
            int index = tabList.indexOf(tab);
            if (index < (tabList.size() - 1)) {
                setSelectedTab(tabList.get(index + 1));
            } else if (index > 0) {
                setSelectedTab(tabList.get(index - 1));
            }
        }
        
        // Remove tab.
        tabList.remove(tab);
        
        // Start animation.
        startAnimation(true);
    }
    
    /**
     * Returns the currently selected tab.
     */
    public Tab getSelectedTab() {
        for (Tab tab : tabList) {
            if (tab.isSelected()) {
                return tab;
            }
        }
        return null;
    }

    /**
     * Implements interface method to set up new tab layout.  This method is
     * called whenever the animation is started.
     */
    @Override
    public void setupNextScreen() {
        layoutTabs();
    }
    
    /**
     * Performs layout for visible tabs in the container.  The visible tabs
     * depends on the first visible tab, number of tabs, tab sizes, and 
     * container size.
     */
    private void layoutTabs() {
        // Get index of first visible tab.
        int oldStartIdx = vizStartIdx;
        
        // Remove all tabs and effects.
        removeAll();
        EffectsManager.clearAllEffects();
        
        // Add visible tabs to container.
        List<Tab> visibleTabs = getPendingVisibleTabs();
        for (int i = 0, size = visibleTabs.size(); i < size; i++) {
        	Tab tab = visibleTabs.get(i);
        	double weight = (i < (size - 1)) ? 0.0 : 1.0;
        	add(tab, new GridBagConstraints(i, 0, 1, 1, weight, weight,
        			GridBagConstraints.SOUTHWEST, GridBagConstraints.VERTICAL, new Insets(3,2,0,2), 0, 0));
        }
        
        // Add "more" button if some tabs not visible.
        if (visibleTabs.size() < getTabs().size()) {
        	add(moreButton, new GridBagConstraints(visibleTabs.size(), 0, 1, 1, 0.0, 0.0,
        			GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
        }
        
        // Set move-in effects for visible tabs.
        for (Tab tab : visibleTabs) {
            if (vizStartIdx == oldStartIdx) {
                // When tab removed, tabs slide in from the right.
                // When tab added, tabs slide in from the left.
                if (tabRemoved) {
                    EffectsManager.setEffect(tab, EffectsUtilities.createMoveInEffect(getWidth() - RIGHT_INSET, getHeight() / 2, true), TransitionType.APPEARING);
                } else {
                    EffectsManager.setEffect(tab, EffectsUtilities.createMoveInEffect(-MIN_TAB_WIDTH, 0, false), TransitionType.APPEARING);
                }
            } else if (vizStartIdx < oldStartIdx) {
                // New tabs slide in from the left.
                EffectsManager.setEffect(tab, EffectsUtilities.createMoveInEffect(-MIN_TAB_WIDTH, 0, false), TransitionType.APPEARING);
            } else {
                // New tabs slide in from the right.
                EffectsManager.setEffect(tab, EffectsUtilities.createMoveInEffect(getWidth() - RIGHT_INSET, getHeight() / 2, true), TransitionType.APPEARING);
            }
        }
        
        // Set move-out effects for all tabs.
        for (Tab tab : tabList) {
            if (vizStartIdx <= oldStartIdx) {
                // Old tabs slide out to the right.
                EffectsManager.setEffect(tab, EffectsUtilities.createMoveOutEffect(getWidth() - RIGHT_INSET, getHeight() / 2, true), TransitionType.DISAPPEARING);
            } else {
                // Old tabs slide out to the left.
                EffectsManager.setEffect(tab, EffectsUtilities.createMoveOutEffect(-MIN_TAB_WIDTH, 0, false), TransitionType.DISAPPEARING);
            }
        }
    }
    
    /**
     * Returns the tabs that *should* be visible, based on the currently
     * visible tabs and the currently selected tab.  This updates the state
     * and assumes the tabs it returns will become visible.
     * 
     * <p>The goal is to shift the minimum amount of distance possible, while
     * still keeping the selected tab in view.  If there is no selected tab,
     * this bumps everything to the left by one.</p>
     */
    private List<Tab> getPendingVisibleTabs() {
        // Calculate maximum visible tabs.
        maxVisibleTabs = calculateVisibleTabCount();
        
        // Determine tabs to display.
        List<Tab> tabs = getTabs();
        List<Tab> visibleTabs;
        if (maxVisibleTabs >= tabs.size()) {
            vizStartIdx = 0;
            visibleTabs = tabs;
        } else {        
            // Bump the start down from where it previously was
            // if there is now more room to display more tabs,
            // so that we display as many tabs as possible.
            if (tabs.size() - vizStartIdx < maxVisibleTabs) {
                vizStartIdx = tabs.size() - maxVisibleTabs;
            }
            visibleTabs = tabs.subList(vizStartIdx, vizStartIdx + maxVisibleTabs);
            
            // If we had a selection, make sure that we shift in the
            // appropriate distance to keep that selection in view.
            Tab selectedTab = getSelectedTab();
            if (selectedTab != null && !visibleTabs.contains(selectedTab)) {
                int selIdx = tabs.indexOf(selectedTab);
                if (vizStartIdx > selIdx) { // We have to shift left
                    vizStartIdx = selIdx;
                } else { // We have to shift right
                    vizStartIdx = selIdx - maxVisibleTabs + 1;
                }
                visibleTabs = tabs.subList(vizStartIdx, vizStartIdx + maxVisibleTabs);
            }
        }
        
        return visibleTabs;
    }
    
    /**
     * Calculates the number of visible tabs that will fit in the container.
     * This is based on the current container width and the minimum tab 
     * width.
     */
    private int calculateVisibleTabCount() {
        int visibleTabCount;
        
        // Calculate available width and maximum visible tabs.
        int totalWidth = getSize().width;
        int availWidth = Math.max(totalWidth, MIN_TAB_WIDTH);
        visibleTabCount = availWidth / (MIN_TAB_WIDTH + 4);
        
        // Adjust maximum tabs including "more" button if necessary.
        if (visibleTabCount < getTabs().size()) {
            int moreWidth = moreButton.getPreferredSize().width;
            availWidth = Math.max(totalWidth - moreWidth - RIGHT_INSET, MIN_TAB_WIDTH);
            visibleTabCount = availWidth / (MIN_TAB_WIDTH + 4);
        }
        
        return Math.max(visibleTabCount, 1);
    }
    
    /**
     * Selects the specified tab.
     */
    private void setSelectedTab(Tab tab) {
        if (selectedTab != null) {
            selectedTab.setSelected(false);
        }
        
        selectedTab = tab;
        
        if (selectedTab != null) {
            selectedTab.setSelected(true);
        }
    }
    
    /**
     * Returns an unmodifiable list of all tabs.
     */
    private List<Tab> getTabs() {
        return Collections.unmodifiableList(tabList);
    }
    
    /**
     * Creates a popup menu listing the available tabs.
     */
    private JPopupMenu createTabListPopup() {
        // Create drop-down list of tab names to select
        JPopupMenu popupMenu = new JPopupMenu();
        
        for (Tab tab : tabList) {
            popupMenu.add(new JMenuItem(tab.createSelectAction()));
        }
        
        return popupMenu;
    }
    
    /**
     * Starts the transition animation.  The specified indicator should be
     * true only if a tab is removed from the list.
     */
    private void startAnimation(boolean tabRemoved) {
        this.tabRemoved = tabRemoved;
        transition.start();
    }
    
    /**
     * Action to show popup list of all tabs.
     */
    private class MoreAction extends AbstractAction {

        public MoreAction() {
            super("More");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            JPopupMenu popupMenu = createTabListPopup();
            popupMenu.show(moreButton, 0, moreButton.getHeight());
        }
    }
    
    /**
     * Action to remove tab from list.
     */
    private class RemoveAction extends AbstractAction {

        public RemoveAction() {
            super("Close");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            removeTab((Tab) e.getSource());
        }
    }
    
    /**
     * Action to select tab.
     */
    private class SelectAction extends AbstractAction {

        public SelectAction(String name) {
            super(name);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            setSelectedTab((Tab) e.getSource());
            startAnimation(false);
        }
    }
}
