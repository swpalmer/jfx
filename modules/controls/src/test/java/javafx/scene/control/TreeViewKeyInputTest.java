/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javafx.scene.control;

import com.sun.javafx.tk.Toolkit;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import java.util.List;
import com.sun.javafx.Utils;
import com.sun.javafx.scene.control.behavior.TreeViewAnchorRetriever;
import com.sun.javafx.scene.control.infrastructure.KeyEventFirer;
import com.sun.javafx.scene.control.infrastructure.KeyModifier;
import com.sun.javafx.scene.control.infrastructure.StageLoader;
import com.sun.javafx.scene.control.infrastructure.VirtualFlowTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotSame;

//@Ignore("Disabling tests as they fail with OOM in continuous builds")
public class TreeViewKeyInputTest {
    private TreeView<String> treeView;
    private MultipleSelectionModel<TreeItem<String>> sm;
    private FocusModel<TreeItem<String>> fm;
    
    private KeyEventFirer keyboard;
    private StageLoader stageLoader;
    
    private final TreeItem<String> root = new TreeItem<String>("Root");                     // 0
        private final TreeItem<String> child1 = new TreeItem<String>("Child 1");            // 1
        private final TreeItem<String> child2 = new TreeItem<String>("Child 2");            // 2
        private final TreeItem<String> child3 = new TreeItem<String>("Child 3");            // 3
            private final TreeItem<String> subchild1 = new TreeItem<String>("Subchild 1");  // 4
            private final TreeItem<String> subchild2 = new TreeItem<String>("Subchild 2");  // 5
            private final TreeItem<String> subchild3 = new TreeItem<String>("Subchild 3");  // 6
        private final TreeItem<String> child4 = new TreeItem<String>("Child 4");            // 7
        private final TreeItem<String> child5 = new TreeItem<String>("Child 5");            // 8
        private final TreeItem<String> child6 = new TreeItem<String>("Child 6");            // 9
        private final TreeItem<String> child7 = new TreeItem<String>("Child 7");            // 10
        private final TreeItem<String> child8 = new TreeItem<String>("Child 8");            // 11
        private final TreeItem<String> child9 = new TreeItem<String>("Child 9");            // 12
        private final TreeItem<String> child10 = new TreeItem<String>("Child 10");          // 13

    @Before public void setup() {
        // reset tree structure
        root.getChildren().clear();
        root.setExpanded(true);
        root.getChildren().setAll(child1, child2, child3, child4, child5, child6, child7, child8, child9, child10 );
        child1.getChildren().clear();
        child1.setExpanded(false);
        child2.getChildren().clear();
        child2.setExpanded(false);
        child3.getChildren().clear();
        child3.setExpanded(true);
        child3.getChildren().setAll(subchild1, subchild2, subchild3);
        child4.getChildren().clear();
        child4.setExpanded(false);
        child5.getChildren().clear();
        child5.setExpanded(false);
        child6.getChildren().clear();
        child6.setExpanded(false);
        child7.getChildren().clear();
        child7.setExpanded(false);
        child8.getChildren().clear();
        child8.setExpanded(false);
        child9.getChildren().clear();
        child9.setExpanded(false);
        child10.getChildren().clear();
        child10.setExpanded(false);
        
        // recreate treeview and gather models
        treeView = new TreeView<String>();
        treeView.setRoot(root);
        sm = treeView.getSelectionModel();
        sm.setSelectionMode(SelectionMode.MULTIPLE);
        sm.clearAndSelect(0);
        fm = treeView.getFocusModel();

        // set up keyboard event firer
        keyboard = new KeyEventFirer(treeView);
        
        // create a simple UI that will be shown (to send the keyboard events to)
        stageLoader = new StageLoader(treeView);
        stageLoader.getStage().show();
    }
    
    @After public void tearDown() {
        treeView.getSkin().dispose();
        stageLoader.dispose();
    }
    
    
    /***************************************************************************
     * Util methods
     **************************************************************************/
    
    private String debug() {
        StringBuilder sb = new StringBuilder("Selected Indices: [");
        
        List<Integer> indices = sm.getSelectedIndices();
        for (Integer index : indices) {
            sb.append(index);
            sb.append(", ");
        }
        
        sb.append("] \nFocus: " + fm.getFocusedIndex());
        sb.append(" \nAnchor: " + getAnchor());
        return sb.toString();
    }
    
    // Returns true if ALL indices are selected
    private boolean isSelected(int... indices) {
        for (int index : indices) {
            if (! sm.isSelected(index)) return false;
        }
        return true;
    }
    
    // Returns true if ALL indices are NOT selected
    private boolean isNotSelected(int... indices) {
        for (int index : indices) {
            if (sm.isSelected(index)) return false;
        }
        return true;
    }
    
    private int getAnchor() {
        return TreeViewAnchorRetriever.getAnchor(treeView);
    }
    
    private boolean isAnchor(int index) {
        return getAnchor() == index;
    }
    
    private int getItemCount() {
        return root.getChildren().size() + child3.getChildren().size();
    }
    
    
    /***************************************************************************
     * General tests
     **************************************************************************/    
    
    @Test public void testInitialState() {
        assertTrue(sm.isSelected(0));
        assertEquals(1, sm.getSelectedIndices().size());
        assertEquals(1, sm.getSelectedItems().size());
    }
    
    /***************************************************************************
     * Tests for row-based single selection
     **************************************************************************/
    
    @Test public void testDownArrowChangesSelection() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress();
        assertFalse(sm.isSelected(0));
        assertTrue(sm.isSelected(1));
    }
    
    @Test public void testDownArrowDoesNotChangeSelectionWhenAtLastIndex() {
        int endIndex = getItemCount();
        sm.clearAndSelect(endIndex);
        assertTrue(debug(), sm.isSelected(endIndex));
        keyboard.doDownArrowPress();
        assertTrue(sm.isSelected(endIndex));
    }
    
    @Test public void testUpArrowDoesNotChangeSelectionWhenAt0Index() {
        sm.clearAndSelect(0);
        keyboard.doUpArrowPress();
        testInitialState();
    }
    
    @Test public void testUpArrowChangesSelection() {
        sm.clearAndSelect(1);
        keyboard.doUpArrowPress();
        assertFalse(sm.isSelected(1));
        assertTrue(sm.isSelected(0));
    }
    
    @Test public void testLeftArrowDoesNotChangeState() {
        keyboard.doLeftArrowPress();
        testInitialState();
    }
    
    // test 19
    @Test public void testCtrlDownMovesFocusButLeavesSelectionAlone() {
        assertTrue(fm.isFocused(0));
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());
        assertTrue(fm.isFocused(1));
        assertTrue(sm.isSelected(0));
        assertFalse(sm.isSelected(1));
    }
    
    // test 20
    @Test public void testCtrlUpDoesNotMoveFocus() {
        assertTrue(fm.isFocused(0));
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());
        assertTrue(fm.isFocused(0));
        assertTrue(sm.isSelected(0));
    }
    
    // test 21
    @Test public void testCtrlLeftDoesNotMoveFocus() {
        assertTrue(fm.isFocused(0));
        keyboard.doLeftArrowPress(KeyModifier.getShortcutKey());
        assertTrue(fm.isFocused(0));
        assertTrue(sm.isSelected(0));
    }
    
    // test 22
    @Test public void testCtrlRightDoesNotMoveFocus() {
        assertTrue(fm.isFocused(0));
        keyboard.doRightArrowPress(KeyModifier.getShortcutKey());
        assertTrue(fm.isFocused(0));
        assertTrue(sm.isSelected(0));
    }
    
    // test 23
    @Test public void testCtrlUpMovesFocus() {
        sm.clearAndSelect(1);
        assertTrue(fm.isFocused(1));
        assertTrue(sm.isSelected(1));
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());
        assertTrue(fm.isFocused(0));
        assertTrue(sm.isSelected(1));
    }
    
    // test 24
    @Test public void testCtrlDownDoesNotMoveFocusWhenAtLastIndex() {
        int endIndex = getItemCount();
        sm.clearAndSelect(endIndex);
        assertTrue(fm.isFocused(endIndex));
        assertTrue(sm.isSelected(endIndex));
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());
        assertTrue(fm.isFocused(endIndex));
        assertTrue(sm.isSelected(endIndex));
    }
    
    // test 25
    @Test public void testCtrlDownArrowWithSpaceChangesAnchor() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // select 2
        assertTrue(isSelected(0, 2));
        assertTrue(isNotSelected(1));
        assertTrue(isAnchor(2));
    }
    
    // test 26
    @Test public void testCtrlUpArrowWithSpaceChangesAnchor() {
        sm.clearAndSelect(2);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 0
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // select 0
        assertTrue(isSelected(0, 2));
        assertTrue(isNotSelected(1));
        assertTrue(isAnchor(0));
    }
    
    // test 44
    @Test public void testHomeKey() {
        sm.clearAndSelect(3);
        keyboard.doKeyPress(KeyCode.HOME);
        assertTrue(isSelected(0));
        assertTrue(isNotSelected(1,2,3));
    }
    
    // test 45
    @Test public void testEndKey() {
        sm.clearAndSelect(3); 
        keyboard.doKeyPress(KeyCode.END);
        assertTrue(debug(), isSelected(getItemCount()));
        assertTrue(isNotSelected(1,2,3));
    }
    
    // test 53
    @Test public void testCtrlHome() {
        sm.clearAndSelect(5);
        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.getShortcutKey());
        assertTrue(isSelected(5));
        assertTrue(fm.isFocused(0));
    }
    
    // test 54
    @Test public void testCtrlEnd() {
        sm.clearAndSelect(5);
        keyboard.doKeyPress(KeyCode.END, KeyModifier.getShortcutKey());
        assertTrue(isSelected(5));
        assertTrue(fm.isFocused(getItemCount()));
    }
    
    // test 68
    @Test public void testCtrlSpaceToClearSelection() {
        sm.clearAndSelect(5);
        assertTrue(isSelected(5));
        assertTrue(fm.isFocused(5));
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));
        assertTrue(isNotSelected(5));
        assertTrue(debug(), fm.isFocused(5));
        assertTrue(isAnchor(5));
    }
    
    
    
    /***************************************************************************
     * Tests for row-based multiple selection
     **************************************************************************/
    
    @Test public void testShiftDownArrowIncreasesSelection() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.SHIFT);
        assertTrue(sm.isSelected(0));
        assertTrue(sm.isSelected(1));
    }
    
    @Test public void testShiftDownArrowDoesNotChangeSelectionWhenAtLastIndex() {
        int endIndex = getItemCount() - 1;
        sm.clearAndSelect(endIndex);
        assertTrue(sm.isSelected(endIndex));
        keyboard.doDownArrowPress(KeyModifier.SHIFT);
        assertTrue(sm.isSelected(endIndex));
    }
    
    @Test public void testShiftUpArrowIncreasesSelection() {
        sm.clearAndSelect(1);
        keyboard.doUpArrowPress(KeyModifier.SHIFT);
        assertTrue(sm.isSelected(0));
        assertTrue(sm.isSelected(1));
    }
    
    @Test public void testShiftUpArrowWhenAt0Index() {
        sm.clearAndSelect(0);
        keyboard.doUpArrowPress(KeyModifier.SHIFT);
        assertTrue(sm.isSelected(0));
    }
    
    @Test public void testShiftLeftArrowWhenAt0Index() {
        sm.clearAndSelect(0);
        keyboard.doLeftArrowPress(KeyModifier.SHIFT);
        assertTrue(sm.isSelected(0));
        assertFalse(sm.isSelected(1));
    }
    
    @Test public void testShiftRightArrowWhenAt0Index() {
        sm.clearAndSelect(0);
        keyboard.doRightArrowPress(KeyModifier.SHIFT);
        assertTrue(sm.isSelected(0));
        assertFalse(sm.isSelected(1));
    }
    
    @Test public void testShiftDownTwiceThenShiftUp() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.SHIFT);
        keyboard.doDownArrowPress(KeyModifier.SHIFT);
        keyboard.doUpArrowPress(KeyModifier.SHIFT);
        assertTrue(debug(), sm.isSelected(0));
        assertTrue(sm.isSelected(1));
        assertFalse(sm.isSelected(2));
    }
    
    @Test public void testShiftUpTwiceThenShiftDownFrom0Index() {
        sm.clearAndSelect(0);
        keyboard.doUpArrowPress(KeyModifier.SHIFT);
        keyboard.doUpArrowPress(KeyModifier.SHIFT);
        keyboard.doDownArrowPress(KeyModifier.SHIFT);
        assertTrue(sm.isSelected(0));
        assertTrue(sm.isSelected(1));
        assertFalse(sm.isSelected(2));
    }
    
    @Test public void testShiftLeftTwiceThenShiftRight() {
        sm.clearAndSelect(0);
        keyboard.doLeftArrowPress(KeyModifier.SHIFT);
        keyboard.doLeftArrowPress(KeyModifier.SHIFT);
        keyboard.doRightArrowPress(KeyModifier.SHIFT);
        assertTrue(sm.isSelected(0));
        assertFalse(sm.isSelected(1));
        assertFalse(sm.isSelected(2));
    }
    
    @Test public void testShiftRightTwiceThenShiftLeft() {
        sm.clearAndSelect(0);
        keyboard.doRightArrowPress(KeyModifier.SHIFT);
        keyboard.doRightArrowPress(KeyModifier.SHIFT);
        keyboard.doLeftArrowPress(KeyModifier.SHIFT);
        assertTrue(sm.isSelected(0));
        assertFalse(sm.isSelected(1));
        assertFalse(sm.isSelected(2));
    }
    
    @Test public void testShiftUpTwiceThenShiftDown() {
        sm.clearAndSelect(2);
        keyboard.doUpArrowPress(KeyModifier.SHIFT);
        keyboard.doUpArrowPress(KeyModifier.SHIFT);
        keyboard.doDownArrowPress(KeyModifier.SHIFT);
        assertFalse(sm.isSelected(0));
        assertTrue(sm.isSelected(1));
        assertTrue(sm.isSelected(2));
        assertFalse(sm.isSelected(3));
    }
    
    // test 18 from Jindra's testcases.rtf file
    @Test public void testShiftDownTwiceThenShiftUpWhenAtLastIndex() {
        int endIndex = getItemCount();
        sm.clearAndSelect(endIndex);
        keyboard.doDownArrowPress(KeyModifier.SHIFT);
        keyboard.doDownArrowPress(KeyModifier.SHIFT);
        keyboard.doUpArrowPress(KeyModifier.SHIFT);
        assertTrue(sm.isSelected(endIndex));
        assertTrue(sm.isSelected(endIndex - 1));
        assertFalse(sm.isSelected(endIndex - 2));
    }
    
    // test 27
    @Test public void testCtrlDownArrowWithSpaceChangesAnchor_extended() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // select 2
        
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 0
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // deselect 0
        assertTrue(isSelected(2));
        assertTrue(isNotSelected(0, 1));
        assertTrue(isAnchor(0));
    }
    
    // test 28
    @Test public void testCtrlUpArrowWithSpaceChangesAnchor_extended() {
        sm.clearAndSelect(2);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 0
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // select 0
        
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // deselect 2
        assertTrue(isSelected(0));
        assertTrue(isNotSelected(1, 2));
        assertTrue(isAnchor(2));
    }
    
    // test 29
    @Test public void testCtrlDownArrowWithSpaceChangesAnchor_extended2() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // select 2
        
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 3
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 4
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // select 4
        assertTrue(isSelected(0, 2, 4));
        assertTrue(isNotSelected(1, 3, 5));
        assertTrue(isAnchor(4));
    }
    
    // test 30
    @Test public void testCtrlUpArrowWithSpaceChangesAnchor_extended2() {
        sm.clearAndSelect(4);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 3
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // select 2
        
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 0
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // select 0
        assertTrue(isSelected(0, 2, 4));
        assertTrue(isNotSelected(1, 3));
        assertTrue(isAnchor(0));
    }
    
    // test 31
    @Test public void testCtrlDownArrowThenShiftSpaceToSelectRange() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.SHIFT);  // select 0,1,2
        assertTrue(isSelected(0, 1, 2));
        assertTrue(isNotSelected(3));
        assertTrue(isAnchor(0));
    }
    
    // test 32
    @Test public void testCtrlUpArrowThenShiftSpaceToSelectRange() {
        sm.clearAndSelect(2);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 0
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.SHIFT);  // select 0,1,2
        assertTrue(isSelected(0, 1, 2));
        assertTrue(isNotSelected(3));
        assertTrue(debug(), isAnchor(2));
    }
    
    // test 33
    @Test public void testCtrlDownArrowThenSpaceToChangeSelection() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // select 2, keeping 0 selected
        assertTrue(isSelected(0, 2));
        assertTrue(isNotSelected(1, 3));
        assertTrue(isAnchor(2));
        
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 3
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 4
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.SHIFT);  // select 2,3,4
        assertTrue(isSelected(2, 3, 4));
        assertTrue(isNotSelected(0, 1));
        assertTrue(isAnchor(2));
    }
    
    // test 34
    @Test public void testCtrlUpArrowThenSpaceToChangeSelection() {
        sm.clearAndSelect(4);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 3
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // select 2, keeping 4 selected
        assertTrue(isSelected(2, 4));
        assertTrue(isNotSelected(0, 1, 3));
        assertTrue(isAnchor(2));
        
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 0
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.SHIFT);  // select 0,1,2
        assertTrue(isSelected(0, 1, 2));
        assertTrue(isNotSelected(3, 4));
        assertTrue(debug(), isAnchor(2));
    }
    
    // test 35
    @Test public void testCtrlDownTwiceThenShiftDown() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doKeyPress(KeyCode.DOWN, KeyModifier.SHIFT);  // select 0,1,2,3
        assertTrue(isSelected(0, 1, 2, 3));
    }
    
    // test 36
    @Test public void testCtrlUpTwiceThenShiftDown() {
        sm.clearAndSelect(3);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 0
        keyboard.doKeyPress(KeyCode.DOWN, KeyModifier.SHIFT);  // select 1,2,3
        assertTrue(isSelected(1, 2, 3));
        assertTrue(isNotSelected(0));
    }
    
    // test 37
    @Test public void testCtrlDownThriceThenShiftUp() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 3
        keyboard.doKeyPress(KeyCode.UP, KeyModifier.SHIFT);  // select 0,1,2
        assertTrue(isSelected(0, 1, 2));
        assertTrue(isNotSelected(3, 4));
    }
    
    // test 38
    @Test public void testCtrlUpTwiceThenShiftUp() {
        sm.clearAndSelect(3);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doKeyPress(KeyCode.UP, KeyModifier.SHIFT);  // select 0,1,2,3
        assertTrue(isSelected(0, 1, 2, 3));
        assertTrue(isNotSelected(4));
    }
    
    // test 39
    @Test public void testCtrlDownTwiceThenSpace_extended() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // select 0,2
        assertTrue(isSelected(0, 2));
        assertTrue(isNotSelected(1, 3));
        assertTrue(isAnchor(2));
        
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 3
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 4
        keyboard.doDownArrowPress(KeyModifier.SHIFT);   // select 2,3,4,5
        assertTrue(isSelected(2, 3, 4, 5));
        assertTrue(isNotSelected(0, 1));
        assertTrue(isAnchor(2));
    }
    
    // test 40
    @Test public void testCtrlUpTwiceThenSpace_extended() {
        sm.clearAndSelect(5);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 4
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 3
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // select 3,5
        assertTrue(isSelected(3,5));
        assertTrue(isNotSelected(0,1,2,4));
        assertTrue(isAnchor(3));
        
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 0
        keyboard.doDownArrowPress(KeyModifier.SHIFT);   // select 1,2,3
        assertTrue(isSelected(1,2,3));
        assertTrue(isNotSelected(0,4,5));
        assertTrue(isAnchor(3));
    }
    
    // test 41
    @Test public void testCtrlDownTwiceThenSpace_extended2() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // select 0,2
        assertTrue(isSelected(0,2));
        assertTrue(isNotSelected(1,3,4));
        assertTrue(isAnchor(2));
        
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 3
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 4
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 5
        keyboard.doUpArrowPress(KeyModifier.SHIFT);     // select 2,3,4
        assertTrue(isSelected(2,3,4));
        assertTrue(isNotSelected(0,1,5));
        assertTrue(isAnchor(2));
    }
    
    // test 50
    @Test public void testCtrlDownThenShiftHome() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // select 0,2
        assertTrue(isSelected(0,2));
        assertTrue(isNotSelected(1,3,4));
        assertTrue(isAnchor(2));
        
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 3
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());    // move focus to 4
        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.SHIFT);
        assertTrue(isSelected(0,1,2));
        assertTrue(isNotSelected(3,4));
        assertTrue(debug(),isAnchor(2));
    }
    
    // test 51
    @Test public void testCtrlUpThenShiftEnd() {
        sm.clearAndSelect(5);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 4
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 3
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // select 3,5
        assertTrue(isSelected(3,5));
        assertTrue(isNotSelected(1,2,4));
        assertTrue(isAnchor(3));
        
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doKeyPress(KeyCode.END, KeyModifier.SHIFT);
        assertTrue(isSelected(3,4,5,6,7,8,9));
        assertTrue(isNotSelected(0,1,2));
        assertTrue(debug(),isAnchor(3));
    }
    
    // test 42
    @Test public void testCtrlUpTwiceThenSpace_extended2() {
        sm.clearAndSelect(5);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 4
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 3
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));  // select 3,5
        assertTrue(isSelected(3,5));
        assertTrue(isNotSelected(0,1,2,4));
        assertTrue(isAnchor(3));
        
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 2
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());    // move focus to 1
        keyboard.doUpArrowPress(KeyModifier.SHIFT);     // select 0,1,2,3
        assertTrue(isSelected(0,1,2,3));
        assertTrue(isNotSelected(4,5));
        assertTrue(isAnchor(3));
    }
    
    // test 46
    @Test public void testHomeKey_withSelectedItems() {
        sm.clearSelection();
        sm.selectRange(4, 11);
        keyboard.doKeyPress(KeyCode.HOME);
        assertTrue(isSelected(0));
        assertTrue(isNotSelected(1,2,3,4,5,6,7,8,9,10,11));
    }
    
    // test 47
    @Test public void testEndKey_withSelectedItems() {
        sm.clearSelection();
        sm.selectRange(4, 11);
        keyboard.doKeyPress(KeyCode.END);
        assertTrue(isSelected(getItemCount()));
        assertTrue(isNotSelected(1,2,3,4,5,6,7,8));
    }
    
    // test 48
    @Test public void testShiftHome() {
        sm.clearAndSelect(3);
        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.SHIFT);
        assertTrue(isSelected(0,1,2,3));
        assertTrue(isNotSelected(4,5));
        assertTrue(debug(), isAnchor(3));
    }
    
    // test 49
    @Test public void testShiftEnd() {
        sm.clearAndSelect(3);
        keyboard.doKeyPress(KeyCode.END, KeyModifier.SHIFT);
        assertTrue(isSelected(3,4,5,6,7,8,9));
        assertTrue(isNotSelected(0,1,2));
        assertTrue(isAnchor(3));
    }
    
    // test 52
    @Test public void testShiftHomeThenShiftEnd() {
        sm.clearAndSelect(5);
        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.SHIFT);
        assertTrue(isSelected(0,1,2,3,4,5));
        assertTrue(isAnchor(5));
        
        keyboard.doKeyPress(KeyCode.END, KeyModifier.SHIFT);
        assertTrue(isSelected(5,6,7,8,9));
        assertTrue(isAnchor(5));
    }
    
    // test 65
    @Test public void testShiftPageUp() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));
        assertTrue(isSelected(0,2));
        assertTrue(isAnchor(2));
        
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());
        keyboard.doKeyPress(KeyCode.PAGE_UP, KeyModifier.SHIFT);
        assertTrue(isSelected(0,1,2));
        assertTrue(isAnchor(2));
    }
    
    // test 67
    @Test public void testCtrlAToSelectAll() {
        sm.clearAndSelect(5);
        keyboard.doKeyPress(KeyCode.A, KeyModifier.getShortcutKey());
        assertTrue(isSelected(0,1,2,3,4,5,6,7,8,9));
    }
    
    
    /***************************************************************************
     * Tests for discontinuous multiple selection (RT-18952)
     **************************************************************************/  
    
    // Test 1
    @Test public void test_rt18952_1() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));
        assertTrue(isSelected(0,2));
        assertTrue(isAnchor(2));
        
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        assertTrue(debug(),isSelected(0,2,3,4));
        assertTrue(isAnchor(2));
    }
    
    // Test 2
    @Test public void test_rt18952_2() {
        sm.clearAndSelect(5);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));
        assertTrue(isSelected(3,5));
        assertTrue(isAnchor(3));
        
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        assertTrue(isSelected(1,2,3,5));
        assertTrue(isAnchor(3));
    }
    
    // Test 3
    @Test public void test_rt18952_3() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));
        assertTrue(isSelected(0,2));
        assertTrue(isAnchor(2));
        
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        assertTrue(isSelected(0,2,3,4));
        assertTrue(isAnchor(2));
        
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        assertTrue(isSelected(0,1,2,3,4));
        assertTrue(isAnchor(2));
    }
    
    // Test 4
    @Test public void test_rt18952_4() {
        sm.clearAndSelect(5);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));
        assertTrue(isSelected(3,5));
        assertTrue(isAnchor(3));
        
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        assertTrue(isSelected(1,2,3,5));
        assertTrue(isAnchor(3));
        
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        assertTrue(isSelected(1,2,3,4,5));
        assertTrue(isAnchor(3));
    }
    
    // TODO skipped some tests here (5-8)
    
    // Test 9
    @Test public void test_rt18952_9() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));
        assertTrue(isSelected(0,2));
        assertTrue(isAnchor(2));
        
        keyboard.doKeyPress(KeyCode.END, KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        assertTrue(isSelected(0,2,3,4,5,6,7,8,9));
        assertTrue(isAnchor(2));
    }
    
    // Test 10
    @Test public void test_rt18952_10() {
        sm.clearAndSelect(9);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));
        assertTrue(isSelected(7,9));
        assertTrue(isAnchor(7));
        
        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        assertTrue(isSelected(0,1,2,3,4,5,6,7,9));
        assertTrue(isAnchor(7));
    }
    
    // Test 11
    @Test public void test_rt18952_11() {
        sm.clearAndSelect(5);
        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        assertTrue(isSelected(0,1,2,3,4,5));
        assertTrue(isAnchor(5));
        
        keyboard.doKeyPress(KeyCode.END, KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        assertTrue(isSelected(0,1,2,3,4,5,6,7,8,9));
        assertTrue(isAnchor(5));
    }
    
    // Test 12
    @Test public void test_rt18952_12() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));
        assertTrue(isSelected(0,2));
        assertTrue(isAnchor(2));
        
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        assertTrue(isSelected(0,2,3,4));
        assertTrue(isAnchor(2));
        
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        keyboard.doUpArrowPress(KeyModifier.getShortcutKey(), KeyModifier.SHIFT);
        assertTrue(isSelected(0,1,2,3,4));
        assertTrue(isAnchor(2));

        keyboard.doUpArrowPress(KeyModifier.getShortcutKey());
        keyboard.doKeyPress(KeyCode.SPACE,KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null));
        assertTrue(isSelected(1,2,3,4));
        assertTrue(fm.isFocused(0));
        assertTrue(isAnchor(0));
    }
    
    
    
    /***************************************************************************
     * Tests for editing
     **************************************************************************/
    
    // test 43 (part 1)
    @Test public void testF2EntersEditModeAndEscapeCancelsEdit_part1() {
        treeView.setEditable(true);
        
        sm.clearAndSelect(0);
        assertNull(treeView.getEditingItem());
        keyboard.doKeyPress(KeyCode.F2);
        assertEquals(root, treeView.getEditingItem());
        
        keyboard.doKeyPress(KeyCode.ESCAPE);
        assertNull(treeView.getEditingItem());
    }
    
//    // test 43 (part 2)
//    @Test public void testF2EntersEditModeAndEscapeCancelsEdit_part2() {
//        treeView.setEditable(true);
//        
//        sm.clearAndSelect(0);
//        keyboard.doKeyPress(KeyCode.F2);
//        
//        
//    }
    
    
    /***************************************************************************
     * Tests for TreeView-specific functionality
     **************************************************************************/ 
    
    // Test 1 (TreeView test cases)
    @Test public void testRightArrowExpandsBranch() {
        sm.clearAndSelect(0);
        root.setExpanded(false);
        assertFalse(root.isExpanded());
        keyboard.doRightArrowPress();
        assertTrue(root.isExpanded());
    }
    
    // Test 2 (TreeView test cases)
    @Test public void testRightArrowOnExpandedBranch() {
        sm.clearAndSelect(0);
        keyboard.doRightArrowPress();
        assertTrue(isNotSelected(0));
        assertTrue(isSelected(1));
    }
    
    // Test 3 (TreeView test cases)
    @Test public void testRightArrowOnLeafNode() {
        sm.clearAndSelect(1);
        keyboard.doRightArrowPress();
        assertTrue(isNotSelected(0));
        assertTrue(isSelected(1));
        assertTrue(isNotSelected(2));
    }
    
    // Test 4 (TreeView test cases)
    @Test public void testLeftArrowCollapsesBranch() {
        sm.clearAndSelect(0);
        assertTrue(root.isExpanded());
        keyboard.doLeftArrowPress();
        assertFalse(root.isExpanded());
    }
    
    // Test 5 (TreeView test cases)
    @Test public void testLeftArrowOnLeafMovesSelectionToParent() {
        sm.clearAndSelect(2);
        assertTrue(root.isExpanded());
        keyboard.doLeftArrowPress();
        assertTrue(root.isExpanded());
        assertTrue(isSelected(0));
        assertTrue(isNotSelected(2));
    }
    
    // Test 6 (TreeView test cases)
    @Test public void testLeftArrowMultipleTimes() {
        sm.clearAndSelect(5);
        keyboard.doLeftArrowPress();
        assertTrue(child3.isExpanded());
        assertTrue(isSelected(3));
        assertTrue(isNotSelected(5));
        
        keyboard.doLeftArrowPress();
        assertFalse(child3.isExpanded());
        assertTrue(isSelected(3));
        
        keyboard.doLeftArrowPress();
        assertTrue(isSelected(0));
        assertTrue(root.isExpanded());
        
        keyboard.doLeftArrowPress();
        assertTrue(isSelected(0));
        assertFalse(root.isExpanded());
    }
    
    // Test 7 (TreeView test cases)
    @Test public void testDownArrowTwice() {
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress();
        keyboard.doDownArrowPress();
        assertTrue(isSelected(2));
        assertTrue(isNotSelected(0));
    }
    
    // Test 8 (TreeView test cases)
    @Test public void testDownArrowFourTimes() {
        // adding children to child2, but not expanding it
        child2.getChildren().addAll(new TreeItem("another child"), new TreeItem("And another!"));
        child2.setExpanded(false);
        
        child3.setExpanded(true);
        sm.clearAndSelect(0);
        keyboard.doDownArrowPress();
        keyboard.doDownArrowPress();
        keyboard.doDownArrowPress();
        keyboard.doDownArrowPress();
        assertTrue(isSelected(4));
        assertTrue(isNotSelected(0));
    }
    
    // Test 9 (TreeView test cases)
    @Test public void testUpArrow() {
        sm.clearAndSelect(1);
        keyboard.doUpArrowPress();
        assertTrue(isSelected(0));
        assertTrue(isNotSelected(1));
    }
    
    // Test 9 (TreeView test cases)
    @Test public void testUpArrowFourTimes() {
        // adding children to child2, but not expanding it
        child2.getChildren().addAll(new TreeItem("another child"), new TreeItem("And another!"));
        child2.setExpanded(false);
        
        sm.clearAndSelect(5);
        keyboard.doUpArrowPress();
        keyboard.doUpArrowPress();
        keyboard.doUpArrowPress();
        keyboard.doUpArrowPress();
        
        assertTrue(isSelected(1));
        assertTrue(isNotSelected(5));
    }
    
    // Test 20 (TreeView test cases)
    // NOTE: this used to be isSelected but changed when we removed functionality
    // for KeyCode.SLASH. Rather than remove the test I'm now testing to make
    // sure it does nothing.
    @Test public void testCtrlForwardSlashToSelectAll() {
        sm.clearAndSelect(1);
        keyboard.doKeyPress(KeyCode.SLASH, KeyModifier.getShortcutKey());
        assertTrue(isSelected(1));
        assertTrue(isNotSelected(0,2,3,4,5,6,7,8,9));
    }
    
    // Test 21 (TreeView test cases)
    // NOTE: this used to be isNotSelected but changed when we removed functionality
    // for KeyCode.BACK_SLASH. Rather than remove the test I'm now testing to make
    // sure it does nothing.
    @Test public void testCtrlBackSlashToClearSelection() {
        sm.selectAll();
        fm.focus(1);
        keyboard.doKeyPress(KeyCode.BACK_SLASH, KeyModifier.getShortcutKey());
        assertTrue(isSelected(0,1,2,3,4,5,6,7,8,9));
        assertTrue(fm.isFocused(1));
    }
    
    // Test 24 (TreeView test cases)
    @Ignore("Not yet working")
    @Test public void testExpandCollapseImpactOnSelection() {
        sm.clearAndSelect(5);
        assertTrue(child3.isExpanded());
        keyboard.doUpArrowPress(KeyModifier.SHIFT);
        keyboard.doUpArrowPress(KeyModifier.SHIFT);
        assertTrue(isSelected(3,4,5));
        
        keyboard.doLeftArrowPress();
        assertFalse(child3.isExpanded());
        assertTrue(isSelected(3));
        
        keyboard.doRightArrowPress();
        assertTrue(child3.isExpanded());
        assertTrue(isSelected(3,4,5));
    }
    
    // Test 54 (TreeView test cases)
    @Test public void testAsteriskExpandsAllBranchesFromRoot() {
        // adding children to child2, but not expanding it
        child2.getChildren().addAll(new TreeItem("another child"), new TreeItem("And another!"));
        child2.setExpanded(false);
        
        sm.clearAndSelect(0);
        assertFalse(child2.isExpanded());
        assertTrue(child3.isExpanded());
        keyboard.doKeyPress(KeyCode.MULTIPLY);
        
        assertTrue(child2.isExpanded());
        assertTrue(child3.isExpanded());
    }
    
    // Test 57 (TreeView test cases)
    @Test public void testMinusCollapsesBranch() {
        sm.clearAndSelect(3);
        assertTrue(child3.isExpanded());
        keyboard.doKeyPress(KeyCode.SUBTRACT);
        assertFalse(child3.isExpanded());
    }
    
    // Test 58 (TreeView test cases)
    @Test public void testPlusCollapsesBranch() {
        sm.clearAndSelect(3);
        child3.setExpanded(false);
        assertFalse(child3.isExpanded());
        keyboard.doKeyPress(KeyCode.ADD);
        assertTrue(child3.isExpanded());
    }
    
    
    /***************************************************************************
     * Tests for specific bug reports
     **************************************************************************/
    
    @Test public void test_rt18642() {
        sm.clearAndSelect(1);                          // select 1
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());   // shift focus to 2
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());   // shift focus to 3
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null)); // set anchor, and also select, 3
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());   // shift focus to 4
        keyboard.doDownArrowPress(KeyModifier.getShortcutKey());   // shift focus to 5
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null)); // set anchor, and also select, 5
        
        assertTrue(isSelected(1, 3, 5));
        assertTrue(isNotSelected(0, 2, 4));
        
        // anchor is at 5, so shift+UP should select rows 4 and 5 only
        keyboard.doUpArrowPress(KeyModifier.SHIFT);   
        assertTrue(isSelected(4, 5));
        assertTrue(isNotSelected(0, 1, 2, 3));
    }
    
    @Test public void test_rt14451_1() {
        sm.clearAndSelect(5);                          

        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.SHIFT); 
        assertTrue(isSelected(0,1,2,3,4,5));
        assertTrue(isNotSelected(6,7,8,9));
        
        keyboard.doKeyPress(KeyCode.END, KeyModifier.SHIFT); 
        assertTrue(isNotSelected(0,1,2,3,4));
        assertTrue(isSelected(5,6,7,8,9));
        
        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.SHIFT); 
        assertTrue(isSelected(0,1,2,3,4,5));
        assertTrue(debug(), isNotSelected(6,7,8,9));
    } 
    
    @Test public void test_rt14451_2() {
        sm.clearAndSelect(5);                          

        keyboard.doKeyPress(KeyCode.END, KeyModifier.SHIFT); 
        assertTrue(isNotSelected(0,1,2,3,4));
        assertTrue(isSelected(5,6,7,8,9));
        
        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.SHIFT); 
        assertTrue(isSelected(0,1,2,3,4,5));
        assertTrue(debug(), isNotSelected(6,7,8,9));
        
        keyboard.doKeyPress(KeyCode.END, KeyModifier.SHIFT); 
        assertTrue(isNotSelected(0,1,2,3,4));
        assertTrue(isSelected(5,6,7,8,9));
    } 
    
    @Test public void test_rt26835_1() {
        sm.clearAndSelect(5);                          
        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.getShortcutKey()); 
        assertTrue(fm.isFocused(0));
    } 
    
    @Test public void test_rt26835_2() {
        sm.clearAndSelect(5);                          
        keyboard.doKeyPress(KeyCode.END, KeyModifier.getShortcutKey()); 
        assertTrue(debug(), fm.isFocused(getItemCount()));
    } 
    
    @Test public void test_rt27175() {
        sm.clearAndSelect(5);                          
        keyboard.doKeyPress(KeyCode.HOME, KeyModifier.SHIFT, KeyModifier.getShortcutKey()); 
        assertTrue(debug(), fm.isFocused(0));
        assertTrue(isSelected(0,1,2,3,4,5));
    }
    
    @Test public void test_rt28065() {
        sm.setSelectionMode(SelectionMode.MULTIPLE);
        
        treeView.getSelectionModel().select(0);
        assertEquals(0, treeView.getSelectionModel().getSelectedIndex());
        assertEquals(root, treeView.getSelectionModel().getSelectedItem());
        assertEquals(0, treeView.getFocusModel().getFocusedIndex());
        assertEquals(root, treeView.getFocusModel().getFocusedItem());
        
        keyboard.doKeyPress(KeyCode.A, KeyModifier.getShortcutKey());
        assertEquals(0, treeView.getSelectionModel().getSelectedIndex());
        assertEquals(root, treeView.getSelectionModel().getSelectedItem());
        assertEquals(0, treeView.getFocusModel().getFocusedIndex());
        assertEquals(root, treeView.getFocusModel().getFocusedItem());
    }
    
    @Ignore @Test public void test_rt29930() {
        sm.setSelectionMode(SelectionMode.MULTIPLE);
        
        sm.clearAndSelect(0);
        
        keyboard.doDownArrowPress(KeyModifier.SHIFT); // select rows [0,1]
        keyboard.doDownArrowPress(KeyModifier.SHIFT); // select rows [0,1,2]
        assertTrue(isSelected(0,1,2));
        assertEquals(2, fm.getFocusedIndex());
        assertEquals(0, getAnchor());
        
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.getShortcutKey(),
                (Utils.isMac()  ? KeyModifier.CTRL : null)); // set new anchor point
        assertTrue(isSelected(0,1));
        assertEquals(2, fm.getFocusedIndex());
        assertEquals(2, getAnchor());
        
        keyboard.doDownArrowPress(KeyModifier.SHIFT); // select rows [2,3]
        assertTrue(isSelected(2,3));
        assertTrue(isNotSelected(0,1));
        assertEquals(3, fm.getFocusedIndex());
        assertEquals(2, getAnchor());
    }

    private int rt29849_start_count = 0;
    private int rt29849_cancel_count = 0;
    @Test public void test_rt29849() {
        treeView.setEditable(true);

        treeView.setOnEditStart(new EventHandler<TreeView.EditEvent<String>>() {
            @Override public void handle(TreeView.EditEvent<String> event) {
                rt29849_start_count++;
            }
        });
        treeView.setOnEditCancel(new EventHandler<TreeView.EditEvent<String>>() {
            @Override public void handle(TreeView.EditEvent<String> event) {
                rt29849_cancel_count++;
            }
        });

        // initially the counts should be zero
        assertEquals(0, rt29849_start_count);
        assertEquals(0, rt29849_cancel_count);

        IndexedCell cell = VirtualFlowTestUtils.getCell(treeView, 0);
        assertTrue(cell.isEditable());
        assertFalse(cell.isEditing());
        assertEquals(0, cell.getIndex());

        // do an edit, start count should be one, cancel still zero
        treeView.edit(root);
        assertTrue(cell.isEditing());
        assertEquals(1, rt29849_start_count);
        assertEquals(0, rt29849_cancel_count);

        // cancel edit, now both counts should be 1
//        keyboard.doKeyPress(KeyCode.ESCAPE);
        treeView.edit(null);
        assertFalse(cell.isEditing());
        assertEquals(1, rt29849_start_count);
        assertEquals(1, rt29849_cancel_count);
    }

    private int rt31577_count = 0;
    @Test public void test_rt31577() {
        final MultipleSelectionModel sm = treeView.getSelectionModel();
        sm.setSelectionMode(SelectionMode.SINGLE);
        sm.clearSelection();

        // the actual bug is that the selectedItem property does not fire an
        // event when the selected items list changes (due to deselection).
        // It actually does always contain the right value - it just doesn't
        // let anyone know it!
        sm.selectedItemProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable observable) {
                rt31577_count++;
            }
        });

        assertTrue(sm.getSelectedItems().isEmpty());
        assertFalse(sm.isSelected(1));
        assertEquals(0, rt31577_count);

        // select the first row
        keyboard.doKeyPress(KeyCode.KP_DOWN);
        assertEquals(1, sm.getSelectedItems().size());
        assertTrue(sm.isSelected(0));
        assertTrue(sm.getSelectedItems().contains(root));
        assertEquals(root, sm.getSelectedItem());
        assertEquals(1, rt31577_count);

        // deselect the row
        keyboard.doKeyPress(KeyCode.SPACE, KeyModifier.CTRL,
                Utils.isMac() ? KeyModifier.getShortcutKey() : null);
        assertTrue(sm.getSelectedItems().isEmpty());
        assertFalse(sm.isSelected(1));
        assertNull(sm.getSelectedItem());
        assertEquals(2, rt31577_count);
    }

    @Test public void test_rt32383_pageDown() {
        // this test requires a lot of data
        for (int i = 0; i < 100; i++) {
            root.getChildren().add(new TreeItem<String>("Row " + i));
        }

        final MultipleSelectionModel sm = treeView.getSelectionModel();
        sm.setSelectionMode(SelectionMode.SINGLE);
        sm.clearAndSelect(0);

        final TreeItem<String> initialFocusOwner = fm.getFocusedItem();

        keyboard.doKeyPress(KeyCode.PAGE_DOWN, KeyModifier.CTRL);
        Toolkit.getToolkit().firePulse();
        final TreeItem<String> newFocusOwner = fm.getFocusedItem();
        assertNotSame(initialFocusOwner, newFocusOwner);

        keyboard.doKeyPress(KeyCode.PAGE_DOWN, KeyModifier.CTRL);
        Toolkit.getToolkit().firePulse();
        final TreeItem<String> nextFocusOwner = fm.getFocusedItem();
        assertNotSame(initialFocusOwner, nextFocusOwner);
        assertNotSame(newFocusOwner, nextFocusOwner);
    }

    @Test public void test_rt32383_pageUp() {
        // this test requires a lot of data
        for (int i = 0; i < 100; i++) {
            root.getChildren().add(new TreeItem<String>("Row " + i));
        }

        final int lastIndex = 99;

        final MultipleSelectionModel sm = treeView.getSelectionModel();
        sm.setSelectionMode(SelectionMode.SINGLE);
        sm.clearAndSelect(lastIndex);

        // need to make sure we scroll down to the bottom!
        treeView.scrollTo(lastIndex);
        Toolkit.getToolkit().firePulse();

        final TreeItem<String> initialFocusOwner = fm.getFocusedItem();

        keyboard.doKeyPress(KeyCode.PAGE_UP, KeyModifier.CTRL);
        Toolkit.getToolkit().firePulse();
        final TreeItem<String> newFocusOwner = fm.getFocusedItem();
        assertNotSame(initialFocusOwner, newFocusOwner);

        keyboard.doKeyPress(KeyCode.PAGE_UP, KeyModifier.CTRL);
        Toolkit.getToolkit().firePulse();
        final TreeItem<String> nextFocusOwner = fm.getFocusedItem();
        assertNotSame(initialFocusOwner, nextFocusOwner);
        assertNotSame(newFocusOwner, nextFocusOwner);
    }
}
