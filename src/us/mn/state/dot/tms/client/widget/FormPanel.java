/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2013  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms.client.widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * FormPanel is a panel for viewing and editing forms. It provides a simpler
 * API for using a GridBagLayout.
 *
 * @author Douglas Lau
 */
public class FormPanel extends JPanel {

	/** Color for value label text */
	static private final Color DARK_BLUE = new Color(0, 0, 128);

	/** Create a value label */
	static public JLabel createValueLabel() {
		JLabel lbl = new JLabel();
		lbl.setForeground(DARK_BLUE);
		// By default, labels are BOLD
		lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC));
		return lbl;
	}

	/** Create a value label */
	static public JLabel createValueLabel(String txt) {
		JLabel lbl = createValueLabel();
		lbl.setText(txt);
		return lbl;
	}

	/** Flag if components should be enabled */
	protected final boolean enable;

	/** Current row on the form */
	protected int row = 0;

	/** Current grid bag constraints state */
	protected GridBagConstraints bag;

	/** Create a new form panel */
	public FormPanel() {
		this(true);
	}

	/** Create a new form panel */
	public FormPanel(boolean e) {
		super(new GridBagLayout());
		enable = e;
		finishRow();
	}

	/** Dispose of the form panel */
	public void dispose() {
		removeAll();
	}

	/** Set the default border */
	public void setBorder() {
		setBorder(UI.border);
	}

	/** Set the form title */
	public void setTitle(String t) {
		setBorder(BorderFactory.createTitledBorder(t));
	}

	/** Create default grid bag constraints */
	public void finishRow() {
		bag = new GridBagConstraints();
		bag.anchor = GridBagConstraints.EAST;
		bag.insets.right = UI.hgap;
		bag.insets.bottom = UI.vgap;
		bag.gridx = GridBagConstraints.RELATIVE;
		bag.gridy = row++;
	}

	/** Set the grid width state */
	public void setWidth(int width) {
		bag.gridwidth = width;
	}

	/** Set the anchor state to WEST */
	public void setWest() {
		bag.anchor = GridBagConstraints.WEST;
		bag.fill = GridBagConstraints.BOTH;
	}

	/** Set the anchor state to CENTER */
	public void setCenter() {
		bag.anchor = GridBagConstraints.CENTER;
		bag.fill = GridBagConstraints.NONE;
		bag.gridwidth = GridBagConstraints.REMAINDER;
	}

	/** Set the fill mode */
	public void setFill() {
		bag.anchor = GridBagConstraints.CENTER;
		bag.fill = GridBagConstraints.BOTH;
		bag.gridwidth = GridBagConstraints.REMAINDER;
		bag.weightx = 1;
		bag.weighty = 1;
	}

	/** Set the anchor state to EAST */
	public void setEast() {
		bag.anchor = GridBagConstraints.EAST;
		bag.fill = GridBagConstraints.NONE;
	}

	/** Add one component with the current grid bag state */
	public void add(JComponent comp) {
		add(comp, bag);
		if(!(comp instanceof JLabel))
			comp.setEnabled(enable);
	}

	/** Add a pair of components to the panel */
	public void add(JComponent c1, JComponent c2) {
		setEast();
		add(c1);
		setWest();
		add(c2);
	}

	/** Add a table component */
	public void add(JTable table) {
		table.setEnabled(enable);
		add(new JScrollPane(table,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
	}

	/** Add a component with a label on the left side */
	public void add(String name, JComponent comp) {
		add(new JLabel(name), comp);
	}

	/** Add a component to the panel */
	public void addRow(JComponent comp) {
		setWidth(GridBagConstraints.REMAINDER);
		add(comp);
		finishRow();
	}

	/** Add a pair of components to the panel */
	public void addRow(JComponent c1, JComponent c2) {
		setEast();
		add(c1);
		setWest();
		setWidth(GridBagConstraints.REMAINDER);
		add(c2);
		finishRow();
	}

	/** Add a component with a label on the left side */
	public void addRow(String name, JComponent comp) {
		setEast();
		add(new JLabel(name));
		setWest();
		addRow(comp);
	}

	/** Add a component with a label on the left side and a button */
	public void addRow(String name, JComponent comp, JButton btn) {
		setEast();
		add(new JLabel(name));
		setWest();
		setWidth(4);
		add(comp);
		setCenter();
		add(btn);
		finishRow();
	}

	/** Add a text area component with a label on the left side */
	public void addRow(String name, JTextArea area) {
		setEast();
		add(new JLabel(name));
		addRow(area);
	}

	/** Add a text area component with a label on the left side */
	public void addRow(JTextArea area) {
		setFill();
		area.setWrapStyleWord(true);
		area.setLineWrap(true);
		area.setEnabled(enable);
		addRow(new JScrollPane(area,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
	}

	/** Add a list component with a label on the left side */
	public void addRow(String name, JList list) {
		setEast();
		add(new JLabel(name));
		addRow(list);
	}

	/** Add a list component */
	public void addRow(JList list) {
		setFill();
		list.setEnabled(enable);
		addRow(new JScrollPane(list,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
	}

	/** Add a table component with a label on the left side */
	public void addRow(String name, JTable table) {
		setEast();
		add(new JLabel(name));
		addRow(table);
	}

	/** Add a table component */
	public void addRow(JTable table) {
		setFill();
		add(table);
		finishRow();
	}
}
