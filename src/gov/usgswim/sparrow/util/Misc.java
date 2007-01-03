package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.Double2D;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Misc {
	public Misc() throws Exception {

	}
	
	public static void main(String[] args) throws Exception {
	  moveColumn();
	}
	
	public static void moveColumn() throws FileNotFoundException,
																										IOException {
	  String path = "/datausgs/projects/sparrow/sparrow_sim_files-1-work/coef_copy.txt";
	  String out_path = "/datausgs/projects/sparrow/sparrow_sim_files-1-work/coef_out.txt";
	  Double2D data = TabDelimFileUtil.readAsDouble(new File(path), true);
		
		for (int r = 0; r < data.getRowCount(); r++)  {
			Double error = (Double) data.getValueAt(r, 13);
			
			//ripple  all values down
			for (int i=12; i>0; i--) {
				Double v = (Double) data.getValueAt(r, i);
				data.setValueAt(v, r, i+1);
			}
			
		  data.setValueAt(error, r, 1);
			
		}


		TabDelimFileUtil.write(data, new File(out_path)); 
	}
	
	
}
