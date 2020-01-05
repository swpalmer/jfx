/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

package textfun;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 * Class for playing with tabSize in Text and TextFlow.
 *
 * @author swpalmer@gmail.com
 */
public class TextFun extends Application {
    static Font font = Font.font("Monospaced");
    static Color colors[] = {Color.RED , Color.BLUE, Color.GREEN, Color.PURPLE };

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        VBox box = new VBox(
                textWidget("\t1\t2\t3\t4\t5\t6\t7\t8\t9\t0"),
                //new Separator(),
                //textFlowWidget("Tabs before\t^carets\t^abc\t^defgh", "\t^Text2 ", "\t^Text\t^3"),
                new Separator(),
                textFlowWidget("\tOne",
                        "\t\tTwo",
                        "\t\t\tThree",
                        "\t\t\t\tFour"),
                new Separator()
                );
        addEditableWidget(box);

        Scene scene = new Scene(box);
        stage.setTitle("TextFlow Toy");
        stage.setScene(scene);
        stage.show();
    }

    static Node makeSpacer(int width) {
        Region r = new Region();
        r.setPrefWidth(width);
        r.setMinWidth(width);
        r.setMaxWidth(width);
        return r;
    }

    static TextFlow noWrapTextFlow() {
        TextFlow tf = new TextFlow();
        tf.setPrefWidth(Region.USE_COMPUTED_SIZE);
        tf.setMinWidth(Region.USE_PREF_SIZE);
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    // TextArea bound to a Text node so users can experiment
    private void addEditableWidget(VBox parent) {
        TextArea tf = new TextArea();
        tf.setPromptText("Enter a string with tabs");
        tf.setText("\tpublic void method(int limit,\n"+
                   "\t                   String odd,\n"+
                   "\t                   String even) {\n"+
                   "\t\tfor (int x = 0; x < limit; x++) {\n"+
                   "\t\t\tif ((x & 1) == 1) {\n"+
                   "\t\t\t\tSystem.out.printf(\"%d is %s\", x, odd);\n"+
                   "\t\t\t} else {\n"+
                   "\t\t\t\tSystem.out.printf(\"%d is %s\", x, even);\n"+
                   "\t\t}\n"+
                   "\t}");
        Text t = new Text();
        t.setFont(font);
        t.textProperty().bind(tf.textProperty());
        parent.getChildren().addAll(tf,finishWidget(t.tabSizeProperty(), t));
    }

    // Single String in a Text node
    private Node textWidget(String msg) {
        Text t = new Text(msg);
        t.setFont(font);
        return finishWidget(t.tabSizeProperty(), t);
    }

    // A TextFlow with a Text node for each String
    // Colors are assigned to Text nodes from colors[]
    private Node textFlowWidget(String ... msgs) {
        int i = 0;
        TextFlow tf = noWrapTextFlow();
        for (String m : msgs) {
            Text t = new Text(m);
            t.setFont(font);
            t.setFill(colors[i++ % colors.length]);
            tf.getChildren().add(t);
        }
        return finishWidget(tf.tabSizeProperty(), tf);
    }

    private Node finishWidget(IntegerProperty prop, Node tn) {
        VBox vbox = new VBox();
        TextFlow colTens = noWrapTextFlow();
        TextFlow colOnes = noWrapTextFlow();
        prop.addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            buildRuler(colTens, colOnes, prop.get());
        });
        buildRuler(colTens, colOnes, prop.get());

        final int ctrlWidth = 200;
        Pane cntrls = controlsFor(prop);
        cntrls.setPrefWidth(ctrlWidth);
        Node pad1 = makeSpacer(ctrlWidth);
        Label tsLab = new Label(" Tab Size");
        tsLab.setMinWidth(ctrlWidth);
        tsLab.setMaxWidth(ctrlWidth);
        HBox h1 = new HBox(pad1,colTens);
        HBox h2 = new HBox(tsLab,colOnes);
        HBox h3 = new HBox(cntrls,tn);
        vbox.getChildren().addAll(h1, h2, h3);
        return vbox;
    }

    private void addColNums(List<Text> tens, List<Text> ones,
            String tensStr, String onesStr, Color colr) {
        Text ts10s = new Text(tensStr);
        Text ts1s = new Text(onesStr);
        ts10s.setFont(font);
        ts1s.setFont(font);
        ts10s.setFill(colr);
        ts1s.setFill(colr);
        tens.add(ts10s);
        ones.add(ts1s);
    }

    private void buildRuler(TextFlow tfcTens, TextFlow tfcOnes, int tabsize) {
        ArrayList<Text> tens = new ArrayList<>();
        ArrayList<Text> ones = new ArrayList<>();
        StringBuilder sb10s = new StringBuilder(100);
        StringBuilder sb1s = new StringBuilder(100);
        for (int i = 0; i < 100; i++) {
            char c1s = (char) ('0' + (i % 10));
            char c10s = i < 10 ? ' ' : (char) ('0' + (i / 10));
            if (i % tabsize == 0) {
                if (sb1s.length() > 0) {
                    addColNums(tens, ones, sb10s.toString(), sb1s.toString(), Color.LIGHTBLUE);
                    sb10s.setLength(0);
                    sb1s.setLength(0);
                }
                // Highlight tab stops
                addColNums(tens, ones, String.valueOf(c10s), String.valueOf(c1s), Color.CADETBLUE);
            } else {
                sb10s.append(c10s);
                sb1s.append(c1s);
            }
        }
        if (sb1s.length() > 0) {
            addColNums(tens, ones, sb10s.toString(), sb1s.toString(), Color.LIGHTBLUE);
        }
        tfcTens.getChildren().setAll(tens);
        tfcOnes.getChildren().setAll(ones);
    }

    private Pane controlsFor(IntegerProperty prop) {
        Slider s = new Slider(1, 20, 8);
        s.setBlockIncrement(1);
        prop.bind(s.valueProperty());
        Label l = new Label();
        l.setPrefWidth(40);
        l.textProperty().bind(Bindings.format(" %-2d: ", prop));
        HBox hb = new HBox(l,s);
        hb.setPrefWidth(Region.USE_COMPUTED_SIZE);
        hb.setMaxWidth(Region.USE_COMPUTED_SIZE);
        hb.setMinWidth(Region.USE_PREF_SIZE);
        return hb;
    }
}
