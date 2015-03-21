package northern.captain.vendingman.dialogs;

import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.EditText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import northern.captain.vendingman.R;

/**
 * Created by leo on 20.11.14.
 */
@EFragment(R.layout.enter_text_dialog)
public class EnterTextStringDialog extends DialogFragment
{
    public interface ITextCallback
    {
        public boolean textEntered(String text);
    }

    ITextCallback callback;
    ICancelCallback cancelCallback;

    public void setCancelCallback(ICancelCallback cancelCallback)
    {
        this.cancelCallback = cancelCallback;
    }

    public void setCallback(ITextCallback callback)
    {
        this.callback = callback;
    }

    @ViewById(R.id.etext_edit)
    EditText inputText;

    @AfterViews
    void initViews()
    {
        if(titleResId != 0)
        {
            getDialog().setTitle(titleResId);
        }

        if(text != null)
        {
            inputText.setText(text);
        }

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private int titleResId = 0;
    private String text;

    public void setTitle(int resId)
    {
        titleResId = resId;
    }
    public void setInputText(String text)
    {
        this.text = text;
    }

    @Click(R.id.etext_ok_but)
    void onOkClick()
    {
        String text = inputText.getText().toString().trim();
        if(callback != null && text.length() > 0)
        {
            if(callback.textEntered(text))
            {
                dismiss();
            }
        }
    }

    @Click(R.id.etext_cancel_but)
    void onCancelClick()
    {
        dismiss();
        if(cancelCallback != null)
        {
            cancelCallback.cancel();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog)
    {
        super.onCancel(dialog);
        if(cancelCallback != null)
        {
            cancelCallback.cancel();
        }
    }
}
