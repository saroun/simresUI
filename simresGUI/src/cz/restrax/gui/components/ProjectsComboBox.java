package cz.restrax.gui.components;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import cz.restrax.sim.ProjectList;
import cz.restrax.sim.RsxProject;
import cz.saroun.classes.editors.BetterComboBox;

public class ProjectsComboBox extends BetterComboBox {
	private static final long serialVersionUID = 7262464702595092876L;
	private ProjectList projects=null;
	public ProjectsComboBox(ProjectList list) {
		super();
		setDefaultChangeListener();
		setProjects(list);
	}
	
	public void setDefaultChangeListener() {		
		ItemListener item = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getItem() instanceof RsxProject) {
					//RsxProject prj = (RsxProject) e.getItem();
					RsxProject prj=(RsxProject) getSelectedItem();
					if (e.getStateChange() == ItemEvent.SELECTED) {
						//int i=getSelectedIndex();
						projects.setAsCurrent(prj);
					}
					//System.out.printf("%s %s current=%s\n",this.getClass().getName(),prj.getDescription(),prj.isCurrent());
				}
			}			
		};
		setChangeListener(item);
	}
	
	public void setProjects(ProjectList projects) {
		this.projects=projects;
		ItemListener il=changeListener;
		removeChangeListener();		
		this.removeAllItems();
		RsxProject cp=projects.getCurrentProject();
		//System.out.printf("%s.setProjects current=%s\n",this.getClass().getName(),cp.getDescription());
		if (cp!=null) addItem(cp);
		for (int i=0;i<projects.size();i++) {
			RsxProject cp2 = projects.get(i);
			if (cp!=cp2) addItem(cp2);
		}
		setChangeListener(il);
		setSelectedIndex(-1);
		if (cp!=null) {
			setSelectedItem(cp);
		} else if (getItemCount()>0 ){
			setSelectedIndex(0);
		} else {
			setSelectedIndex(-1);
		}
	}

	public ProjectList getProjects() {
		return projects;
	}


}
