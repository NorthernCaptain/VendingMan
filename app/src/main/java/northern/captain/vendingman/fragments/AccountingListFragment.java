package northern.captain.vendingman.fragments;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.TwoWayView;

import java.util.Date;
import java.util.List;

import northern.captain.vendingman.AndroidContext;
import northern.captain.vendingman.BaseFragment;
import northern.captain.vendingman.FragmentFactory;
import northern.captain.vendingman.R;
import northern.captain.vendingman.dialogs.AccountingDialog;
import northern.captain.vendingman.entities.Accounting;
import northern.captain.vendingman.entities.AccountingFactory;
import northern.captain.vendingman.entities.VendingMachine;
import northern.captain.vendingman.gui.SwipeDismissRecyclerViewTouchListener;
import northern.captain.vendingman.tools.Helpers;

/**
 * Created by leo on 13.03.15.
 */
@EFragment(R.layout.frag_machinemainlist)
public class AccountingListFragment extends BaseFragment
{
    @ViewById(R.id.fmachinemain_rview)
    TwoWayView listView;

    List<Accounting> items;

    TheListAdapter adapter;

    VendingMachine machine;

    public interface ChosenCallback
    {
        public void itemChosen(Accounting machine);
    }

    private ChosenCallback callback;

    public void setCallback(ChosenCallback callback)
    {
        this.callback = callback;
    }

    public void setMachine(VendingMachine machine)
    {
        this.machine = machine;
    }

    @AfterViews
    void initViews()
    {
        listView.setHasFixedSize(true);
        items = AccountingFactory.instance.getAccountingList(machine.id);
        final Drawable divider = getResources().getDrawable(R.drawable.divider1);
        listView.addItemDecoration(new DividerItemDecoration(divider));

        adapter = new TheListAdapter();
        listView.setAdapter(adapter);
        listView.setOnTouchListener(new SwipeDismissRecyclerViewTouchListener(listView, new SwipeDismissRecyclerViewTouchListener.DismissCallbacks()
        {
            @Override
            public boolean canDismiss(int position)
            {
                Accounting item = items.get(position);
                return item.state == 1;
            }

            @Override
            public void onDismiss(RecyclerView recyclerView, List<SwipeDismissRecyclerViewTouchListener.PendingDismissData> pendingDismissData)
            {
            }

            @Override
            public boolean showUndo(SwipeDismissRecyclerViewTouchListener.PendingDismissData pendingDismissData)
            {
                adapter.showUndo(pendingDismissData.position, pendingDismissData.view);
                return true;
            }
        }));

        ItemClickSupport itemClicker = ItemClickSupport.addTo(listView);
        itemClicker.setOnItemClickListener(new ItemClickSupport.OnItemClickListener()
        {
            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int pos, long l)
            {
                if(callback != null)
                {
                    callback.itemChosen(items.get(pos));
                } else
                {
                    doClick(items.get(pos), pos);
                }
            }
        });
    }

    private class TheListAdapter extends RecyclerView.Adapter<TheListAdapter.ViewHolder>
    {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int type)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.accounting_list_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder itemViewHolder, int i)
        {
            itemViewHolder.populateData(items.get(i), i);
        }

        @Override
        public int getItemCount()
        {
            return items.size();
        }

        public void showUndo(int pos, View view)
        {
            Accounting item = items.get(pos);
            item.state = 0;
            if(item.extra instanceof ViewHolder)
            {
                ViewHolder holder = (ViewHolder)item.extra;
                holder.showUndo(pos);
                holder.startCountDown(pos, item);
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView accTopText;
            TextView accCommentText;
            TextView accCoinsText;
            TextView accBanknotesText;
            TextView accOverallText;

            LinearLayout deletedLay;
            Button undoBut;

            public ViewHolder(View itemView)
            {
                super(itemView);
                accTopText = (TextView) itemView.findViewById(R.id.acclist_acc_desc);
                accCommentText = (TextView) itemView.findViewById(R.id.acclist_acc_comment);
                accCoinsText = (TextView) itemView.findViewById(R.id.acclist_acc_qty1);
                accBanknotesText = (TextView) itemView.findViewById(R.id.acclist_acc_qty2);
                accOverallText = (TextView) itemView.findViewById(R.id.acclist_acc_qty3);

                deletedLay = (LinearLayout) itemView.findViewById(R.id.acclist_deleted_lay);
                undoBut = (Button) itemView.findViewById(R.id.acclist_undo_btn);
            }

            public void populateData(Accounting item, int pos)
            {
                item.extra = this;
                if(item.state == 1)
                {
                    deletedLay.setVisibility(View.GONE);

                    accCoinsText.setText(Integer.toString(item.coinsQty));
                    accBanknotesText.setText(Integer.toString(item.moneyQty));
                    accOverallText.setText(Integer.toString(item.otherQty));
                    accCommentText.setText(item.getComments());

                    accTopText.setText(Helpers.smartDateTimeString(item.createdDate));
                } else
                {
                    showUndo(pos);
                }
            }
            public void showUndo(final int pos)
            {
                deletedLay.setVisibility(View.VISIBLE);
                undoBut.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        undoDeletion(pos);
                    }
                });
            }

            void undoDeletion(final int pos)
            {
                items.get(pos).state = 1;
                deletedLay.setVisibility(View.GONE);
                notifyItemChanged(pos);
            }

            void doActualDeletion(final int pos, final Accounting delItem)
            {
                for(int i = 0;i<items.size();i++)
                {
                    Accounting item = items.get(i);
                    if (item.state == 0 && item.id == delItem.id)
                    {
                        remove(i);
                    }
                }
            }

            void startCountDown(final int pos, final Accounting item)
            {
                AndroidContext.mainActivity.mainHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        doActualDeletion(pos, item);
                    }
                }, Helpers.DELETION_DELAY);
            }
        }
    }


    private void updateData()
    {
        items = AccountingFactory.instance.getAccountingList(machine.id);
        adapter.notifyDataSetChanged();
    }

    private void doClick(Accounting item, int pos)
    {
        final AccountingDialog dialog = FragmentFactory.singleton.newAccountingDialog();
        dialog.setTitle(R.string.edit_accounting_title);
        dialog.setMachine(machine);
        dialog.setAccounting(item);
        dialog.setCallback(new Runnable()
        {
            @Override
            public void run()
            {
                updateData();
            }
        });
        dialog.show(getFragmentManager(), "acc");
    }

    private void remove(int idx)
    {
        Accounting item = items.get(idx);
        item.state = 0;
        AccountingFactory.instance.update(item);
        items.remove(idx);
        adapter.notifyItemRemoved(idx);
    }

}
