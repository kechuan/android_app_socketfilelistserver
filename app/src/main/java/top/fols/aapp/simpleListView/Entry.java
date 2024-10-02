package top.fols.aapp.simpleListView;

import android.view.View;
import android.widget.CompoundButton;

public class Entry {
	public String title;
	public boolean titleShow = true;
	
	public String subTitle;
	public boolean subTitleShow = false;

	public boolean checkBox = false;
	public boolean checkBoxShow = false;
	
	public View.OnClickListener onClick = null;
	public CompoundButton.OnCheckedChangeListener onChange = null;
}  

