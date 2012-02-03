package edu.columbia.cs.psl.mountaindew.runtime.ui;

import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import edu.columbia.cs.psl.mountaindew.runtime.ClassLister;
import edu.columbia.cs.psl.mountaindew.struct.Package;

public class MethodSelector extends JPanel {
	public MethodSelector()
	{
		init();
	}
	JTree tree;
	DefaultMutableTreeNode rootNode;
	private void init() {
		tree = new JTree();
		rootNode = new DefaultMutableTreeNode();

		ClassLister lister = new ClassLister();
		ArrayList<Package> packages = lister.listPackages();
		for(Package p : packages)
		{
			addClassesToModel(p, rootNode);
		}
		
		tree.setModel(new DefaultTreeModel(rootNode));
		add(tree);
	}
	private void addClassesToModel(Package p,DefaultMutableTreeNode parent)
	{
		DefaultMutableTreeNode n = new DefaultMutableTreeNode(p);
		parent.add(n);
		for(Package pa : p.getChildren())
		{
			addClassesToModel(pa, n);
		}
		for(Class c : p.getClasses())
		{
			DefaultMutableTreeNode na = new DefaultMutableTreeNode(c);
			n.add(na);
			for(Method m : c.getMethods())
			{
				DefaultMutableTreeNode nm = new DefaultMutableTreeNode(m);
				na.add(nm);
			}
		}
	}
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.add(new MethodSelector());
		f.setSize(800, 600);
		f.setVisible(true);
	}
}
