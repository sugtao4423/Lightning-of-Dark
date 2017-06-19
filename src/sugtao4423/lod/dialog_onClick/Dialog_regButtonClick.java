package sugtao4423.lod.dialog_onClick;

import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class Dialog_regButtonClick implements OnClickListener{

	private EditText regEdit;
	private String regText;

	public Dialog_regButtonClick(EditText regEdit, String regText){
		this.regEdit = regEdit;
		this.regText = regText;
	}

	@Override
	public void onClick(View v){
		int start = regEdit.getSelectionStart();
		int end = regEdit.getSelectionEnd();
		Editable editable = regEdit.getText();
		editable.replace(Math.min(start, end), Math.max(start, end), regText);
	}
}