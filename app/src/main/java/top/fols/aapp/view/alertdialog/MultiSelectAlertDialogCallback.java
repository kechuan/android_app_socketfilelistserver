package top.fols.aapp.view.alertdialog;

 public interface MultiSelectAlertDialogCallback {
	public Object flagString = null;
	public Object flag = null;
	public abstract void selectComplete(MultiSelectAlertDialog obj, String key[], int index[], boolean isSelected, boolean isNegativeButton);

	public static final MultiSelectAlertDialogCallback defMultiSelectAlertDialogCallback = new MultiSelectAlertDialogCallback(){
		@Override
		public void selectComplete(MultiSelectAlertDialog obj, String key[], int index[], boolean isSelected, boolean isNegativeButton) {
			// TODO: Implement this method
		}
	};
}
