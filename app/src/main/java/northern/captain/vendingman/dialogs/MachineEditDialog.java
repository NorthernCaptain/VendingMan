package northern.captain.vendingman.dialogs;

import android.support.v4.app.DialogFragment;
import android.view.WindowManager;
import android.widget.EditText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import northern.captain.vendingman.R;
import northern.captain.vendingman.entities.VendingMachine;
import northern.captain.vendingman.entities.VendingMachineFactory;
import northern.captain.vendingman.tools.Helpers;
import northern.captain.vendingman.tools.MyToast;

/**
 * Created by leo on 14.03.15.
 */
@EFragment(R.layout.machine_edit_dialog)
public class MachineEditDialog extends DialogFragment
{
    public interface VendingMachineEditCallback
    {
        public void editedOk(VendingMachine item);
        public void editCancel();
    }

    private VendingMachineEditCallback callback = null;

    public MachineEditDialog setCallback(VendingMachineEditCallback callback)
    {
        this.callback = callback;
        return this;
    }

    @ViewById(R.id.emachine_name_edit)
    EditText nameText;

    @ViewById(R.id.emachine_desc_edit)
    EditText descText;

    @ViewById(R.id.emachine_tags_edit)
    EditText tagsText;

    private VendingMachine item = null;
    private boolean isEditMode = false;

    public MachineEditDialog()
    {
    }

    public MachineEditDialog setItem(VendingMachine item)
    {
        this.item = item;
        return this;
    }

    @AfterViews
    public void initViews()
    {
        isEditMode = item != null;
        if(isEditMode)
        {
            getDialog().setTitle(R.string.machine_edit_title);

            nameText.setText(item.name);
            descText.setText(item.description);
            tagsText.setText(item.tags);
        } else
        {
            getDialog().setTitle(R.string.machine_new_title);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    @Click(R.id.emachine_ok_but)
    protected void onOkClicked()
    {
        String name = nameText.getText().toString().trim();

        if(Helpers.isNullOrEmpty(name))
        {
            MyToast.ltoast(R.string.err_zero_name);
            return;
        }

        VendingMachine item = isEditMode ? this.item : new VendingMachine();

        item.setName(name);
        item.setDescription(descText.getText().toString());
        item.setTags(tagsText.getText().toString().trim());

        if(isEditMode)
        {
            VendingMachineFactory.instance.update(item);
        }
        else
        {
            VendingMachineFactory.instance.insert(item);
        }

        dismiss();

        if(callback != null)
        {
            callback.editedOk(item);
        }
    }

    @Click(R.id.emachine_cancel_but)
    protected void onCancelClicked()
    {
        dismiss();
        if(callback != null)
        {
            callback.editCancel();
        }
    }
}
