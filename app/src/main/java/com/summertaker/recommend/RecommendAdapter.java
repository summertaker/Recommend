package com.summertaker.recommend;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.summertaker.recommend.common.BaseApplication;
import com.summertaker.recommend.common.Config;
import com.summertaker.recommend.data.Item;
import com.summertaker.recommend.data.Tag;

import java.util.ArrayList;

public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.ItemViewHolder> {

    private Context mContext;
    private int mPosition;
    private String mFragmentId;
    private ArrayList<Item> mItems;

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView tvId;
        public TextView tvName;
        public LinearLayout loPrice;
        public LinearLayout loPriceL;
        public TextView tvPrice;
        public TextView tvPriceL;
        public TextView tvFlucIcon;
        public TextView tvFlucIconL;
        public TextView tvRof;
        public TextView tvRofL;
        public LinearLayout loReco;
        public TextView tvTprText;
        public TextView tvTpr;
        public TextView tvRorText;
        public TextView tvRor;
        public TextView tvRorH;
        public TextView tvListed;
        public TextView tvElapsed;
        public LinearLayout loBroker;
        public TextView tvBroker;
        public LinearLayout loTag;
        public TextView tvReason;
        public ImageView ivChart;

        public ItemViewHolder(View view) {
            super(view);

            tvId = view.findViewById(R.id.tvId);
            tvName = view.findViewById(R.id.tvName);
            loPrice = view.findViewById(R.id.loPrice);
            loPriceL = view.findViewById(R.id.loPriceL);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvPriceL = view.findViewById(R.id.tvPriceL);
            tvFlucIcon = view.findViewById(R.id.tvFlucIcon);
            tvFlucIconL = view.findViewById(R.id.tvFlucIconL);
            tvRof = view.findViewById(R.id.tvRof);
            tvRofL = view.findViewById(R.id.tvRofL);
            loReco = view.findViewById(R.id.loReco);
            tvTprText = view.findViewById(R.id.tvTprText);
            tvTpr = view.findViewById(R.id.tvTpr);
            tvRorText = view.findViewById(R.id.tvRorText);
            tvRor = view.findViewById(R.id.tvRor);
            tvRorH = view.findViewById(R.id.tvRorH);
            tvListed = view.findViewById(R.id.tvListed);
            tvElapsed = view.findViewById(R.id.tvElapsed);
            loBroker = view.findViewById(R.id.loBroker);
            tvBroker = view.findViewById(R.id.tvBroker);
            loTag = view.findViewById(R.id.loTag);
            tvReason = view.findViewById(R.id.tvReason);
            ivChart = view.findViewById(R.id.ivChart);

            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            //contextMenu.setHeaderTitle(R.string.tag);
            for (Tag tag : BaseApplication.getInstance().getTags()) {
                contextMenu.add((int) tag.getId(), this.getAdapterPosition(), mPosition, tag.getName());
            }
        }
    }

    public RecommendAdapter(Context context, int position, String fragmentId, ArrayList<Item> mItems) {
        this.mContext = context;
        this.mPosition = position;
        this.mFragmentId = fragmentId;
        this.mItems = mItems;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_row, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        Item item = mItems.get(position);

        // 번호
        String id = item.getId() + ".";
        holder.tvId.setText(id);

        // 종목이름
        String name = item.getName();
        if (item.getNor() > 1) { // 추천수
            name = name + " (" + item.getNor() + ")";
        }
        holder.tvName.setText(name);

        // 가격
        BaseApplication.getInstance().renderPrice(item, holder.tvPrice, holder.tvPriceL);

        // 등락률
        BaseApplication.getInstance().renderRof(item, holder.tvFlucIcon, holder.tvFlucIconL, holder.tvRof, holder.tvRofL);

        // 수익률
        if (mFragmentId.equals(Config.KEY_RECOMMEND_RETURN)) {
            if (item.getRor() > 0) {
                holder.tvRor.setTextColor(BaseApplication.COLOR_DANGER);
                holder.tvRorH.setTextColor(BaseApplication.COLOR_DANGER);
            } else if (item.getRor() < 0) {
                holder.tvRor.setTextColor(BaseApplication.COLOR_PRIMARY);
                holder.tvRorH.setTextColor(BaseApplication.COLOR_PRIMARY);
            } else {
                holder.tvRor.setTextColor(BaseApplication.COLOR_INK);
                holder.tvRorH.setTextColor(BaseApplication.COLOR_INK);
            }
            String ror = Config.DECIMAL_FORMAT.format(item.getRor()) + "%";
            holder.tvRor.setText(ror);
            holder.tvRorH.setText(ror);
        } else {
            holder.tvRorText.setVisibility(View.GONE);
            holder.tvRor.setVisibility(View.GONE);
            holder.tvRorH.setVisibility(View.GONE);
        }

        // [현재 추천] 목표가, 증권사, 포트폴리오
        if (item.isChartMode() && mFragmentId.equals(Config.KEY_RECOMMEND_CURRENT)) {
            String tpr = Config.NUMBER_FORMAT.format(item.getTpr());
            if (item.getTpr() == item.getPrice()) {
                holder.tvTpr.setTextColor(BaseApplication.COLOR_INK);
            } else if (item.getTpr() > item.getPrice()) {
                holder.tvTpr.setTextColor(BaseApplication.COLOR_DANGER);
            } else {
                holder.tvTpr.setTextColor(BaseApplication.COLOR_PRIMARY);
            }
            holder.tvTpr.setText(tpr);
            holder.tvTprText.setVisibility(View.VISIBLE);
            holder.tvTpr.setVisibility(View.VISIBLE);

            String broker = item.getBroker() + " - " + item.getPortfolio();
            holder.tvBroker.setText(broker);
            holder.loBroker.setVisibility(View.VISIBLE);
        } else {
            holder.tvTprText.setVisibility(View.GONE);
            holder.tvTpr.setVisibility(View.GONE);
            holder.loBroker.setVisibility(View.GONE);
        }

        // 추천일
        holder.tvListed.setText(item.getListed());

        // 경과일
        String elapsed = item.getElapsed() == 0 ? "(오늘)" : "(" + item.getElapsed() + "일 경과)";
        holder.tvElapsed.setText(elapsed);

        // 태그
        if (item.isListMode() || item.getTagIds() == null || item.getTagIds().isEmpty()) {
            holder.loTag.setVisibility(View.GONE);
        } else {
            holder.loTag.setVisibility(View.VISIBLE);
            BaseApplication.getInstance().renderTag(mContext, item, holder.loTag);
        }

        // 추천 사유
        if (item.isListMode() || item.getReason() == null || item.getReason().isEmpty()) {
            holder.tvReason.setVisibility(View.GONE);
        } else {
            holder.tvReason.setVisibility(View.VISIBLE);
            holder.tvReason.setText(item.getReason());
        }

        if (item.isListMode()) {
            //---------------
            // 리스트 모드
            //---------------
            holder.loPrice.setVisibility(View.GONE);
            holder.loPriceL.setVisibility(View.VISIBLE);
            holder.loReco.setVisibility(View.GONE);
            holder.ivChart.setVisibility(View.GONE);
        } else {
            //---------------
            // 차트 모드
            //---------------
            holder.loPrice.setVisibility(View.VISIBLE);
            holder.loPriceL.setVisibility(View.GONE);
            holder.loReco.setVisibility(View.VISIBLE);

            // 차트
            holder.ivChart.setVisibility(View.VISIBLE);
            String chartUrl = BaseApplication.getWeekCandleChartUrl(item.getCode());
            Glide.with(mContext).load(chartUrl).apply(new RequestOptions()).into(holder.ivChart);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).getId();
    }
}
