package northern.captain.vendingman.dialogs;

import android.support.v4.app.DialogFragment;
import android.view.WindowManager;
import android.widget.EditText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import northern.captain.vendingman.R;
import northern.captain.vendingman.entities.Goods;
import northern.captain.vendingman.entities.GoodsFactory;
import northern.captain.vendingman.tools.Helpers;
import northern.captain.vendingman.tools.MyToast;

/**
 * Created by leo on 14.03.15.
 */
@EFragment(R.layout.goods_edit_dialog)
public class GoodsEditDialog extends DialogFragment
{
    public interface GoodsEditCallback
    {
        public void goodsEditedOk(Goods item);
        public void goodsEditCancel();
    }

    private GoodsEditCallback callback = null;

    public GoodsEditDialog setCallback(GoodsEditCallback callback)
    {
        this.callback = callback;
        return this;
    }

    @ViewById(R.id.egoods_name_edit)
    EditText nameText;

    @ViewById(R.id.egoods_desc_edit)
    EditText descText;

    @ViewById(R.id.egoods_tags_edit)
    EditText tagsText;

    private Goods item = null;
    private boolean isEditMode = false;

    public GoodsEditDialog()
    {
    }

    public GoodsEditDialog setItem(Goods item)
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
            getDialog().setTitle(R.string.goods_edit_title);

            nameText.setText(item.name);
            descText.setText(item.description);
            tagsText.setText(item.tags);
        } else
        {
            getDialog().setTitle(R.string.goods_new_title);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    @Click(R.id.egoods_ok_but)
    protected void onOkClicked()
    {
        String name = nameText.getText().toString().trim();

        if(Helpers.isNullOrEmpty(name))
        {
            MyToast.ltoast(R.string.err_zero_name);
            return;
        }

        Goods item = isEditMode ? this.item : new Goods();

        item.setName(name);
        item.setDescription(descText.getText().toString());
        item.setTags(tagsText.getText().toString().trim());


        if(isEditMode)
        {
            GoodsFactory.instance.update(item);
        }
        else
        {
            GoodsFactory.instance.insert(item);
        }

        dismiss();

        if(callback != null)
        {
            callback.goodsEditedOk(item);
        }
    }

    @Click(R.id.egoods_cancel_but)
    protected void onCancelClicked()
    {
        dismiss();
        if(callback != null)
        {
            callback.goodsEditCancel();
        }
    }
}
