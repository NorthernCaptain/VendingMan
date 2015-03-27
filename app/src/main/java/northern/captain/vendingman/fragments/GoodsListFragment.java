package northern.captain.vendingman.fragments;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.TwoWayView;

import java.util.List;

import northern.captain.vendingman.AndroidContext;
import northern.captain.vendingman.BaseFragment;
import northern.captain.vendingman.FragmentFactory;
import northern.captain.vendingman.R;
import northern.captain.vendingman.dialogs.GoodsEditDialog;
import northern.captain.vendingman.entities.Goods;
import northern.captain.vendingman.entities.GoodsFactory;
import northern.captain.vendingman.gui.SwipeDismissRecyclerViewTouchListener;
import northern.captain.vendingman.tools.Helpers;

/**
 * Created by leo on 13.03.15.
 */
@EFragment(R.layout.frag_goodsview)
public class GoodsListFragment extends BaseFragment
{
    @ViewById(R.id.fab_plus_fgoods)
    FloatingActionButton plusButton;

    @ViewById(R.id.fgoods_rview)
    TwoWayView listView;

    List<Goods> items;

    TheListAdapter adapter;

    @AfterViews
    void initViews()
    {
        listView.setHasFixedSize(true);
        items = GoodsFactory.instance.getGoodsAll();
        final Drawable divider = getResources().getDrawable(R.drawable.divider1);
        listView.addItemDecoration(new DividerItemDecoration(divider));

        adapter = new TheListAdapter();
        listView.setAdapter(adapter);
        listView.setOnTouchListener(new SwipeDismissRecyclerViewTouchListener(listView, new SwipeDismissRecyclerViewTouchListener.DismissCallbacks()
        {
            @Override
            public boolean canDismiss(int position)
            {
                Goods item = items.get(position);
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
            public void onItemClick(RecyclerView recyclerView, View view, int position, long l)
            {
                onEditItemClicked(position);
            }
        });
    }

    private class TheListAdapter extends RecyclerView.Adapter<TheListAdapter.ViewHolder>
    {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int type)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.goods_list_item, parent, false);
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
            Goods item = items.get(pos);
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
            TextView descriptionText;
            TextView nameText;
            TextView tagsText;
            LinearLayout deletedLay;
            Button undoBut;

            public ViewHolder(View itemView)
            {
                super(itemView);
                nameText = (TextView) itemView.findViewById(R.id.gitem_name);
                tagsText = (TextView) itemView.findViewById(R.id.gitem_tags);
                descriptionText = (TextView) itemView.findViewById(R.id.gitem_description);
                deletedLay = (LinearLayout) itemView.findViewById(R.id.gitem_deleted_lay);
                undoBut = (Button) itemView.findViewById(R.id.gitem_undo_btn);
            }

            public void populateData(Goods item, int pos)
            {
                item.extra = this;
                if(item.state == 1)
                {
                    deletedLay.setVisibility(View.GONE);
                    nameText.setText(item.name);
                    if(Helpers.isNullOrEmpty(item.tags))
                    {
                        tagsText.setVisibility(View.GONE);
                    } else
                    {
                        tagsText.setText(item.tags);
                        tagsText.setVisibility(View.VISIBLE);
                    }
                    descriptionText.setText(item.description);
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

            void doActualDeletion(final int pos, final Goods delItem)
            {
                for(int i = 0;i<items.size();i++)
                {
                    Goods item = items.get(i);
                    if (item.state == 0 && item.id == delItem.id)
                    {
                        remove(i);
                    }
                }
            }

            void startCountDown(final int pos, final Goods item)
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

    private void remove(int idx)
    {
        Goods item = items.get(idx);
        item.state = 0;
        GoodsFactory.instance.update(item);
        items.remove(idx);
        adapter.notifyItemRemoved(idx);
    }

    private void onEditItemClicked(final int pos)
    {
        GoodsEditDialog dialog = FragmentFactory.singleton.newGoodsEditDialog();
        dialog.setItem(items.get(pos)).setCallback(new GoodsEditDialog.GoodsEditCallback()
        {
            @Override
            public void goodsEditedOk(Goods item)
            {
                adapter.notifyItemChanged(pos);
            }

            @Override
            public void goodsEditCancel()
            {

            }
        });
        dialog.show(getFragmentManager(), "goodsedit");
    }

    private void updateData()
    {
        items = GoodsFactory.instance.getGoodsAll();
    }

    @Click(R.id.fab_plus_fgoods)
    protected void onNewItemClicked()
    {
        GoodsEditDialog dialog = FragmentFactory.singleton.newGoodsEditDialog();
        dialog.setCallback(new GoodsEditDialog.GoodsEditCallback()
        {
            @Override
            public void goodsEditedOk(Goods item)
            {
                updateData();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void goodsEditCancel()
            {

            }
        });
        dialog.show(getFragmentManager(), "goodsedit");
    }
}
