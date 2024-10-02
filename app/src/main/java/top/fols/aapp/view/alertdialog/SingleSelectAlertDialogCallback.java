package top.fols.aapp.view.alertdialog;

public interface SingleSelectAlertDialogCallback {
	public Object flagString = null;
	public Object flag = null;
	public abstract void selectComplete(SingleSelectAlertDialog obj,String key, int index, boolean isSelected, boolean isNegativeButton);
	
	public static final SingleSelectAlertDialogCallback defSingleSelectAlertDialogCallback = new SingleSelectAlertDialogCallback(){
		@Override
		public void selectComplete(SingleSelectAlertDialog obj, String key, int index, boolean isSelected, boolean isNegativeButton) {
			// TODO: Implement this method
		}
	};
}
