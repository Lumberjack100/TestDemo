package com.shmedo.mcloudapp.deviceconfig.adapter;

import android.graphics.Typeface;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.ProductInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/18 <br/>
 * 描述：    产品适配器
 */
public class ProductAdapter extends BaseQuickAdapter<ProductInfo, BaseViewHolder> {

    public ProductAdapter(@Nullable List<ProductInfo> data) {
        super(R.layout.item_product, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, ProductInfo productInfo) {
        holder.setText(R.id.tv_name, productInfo.getProductName());
        if (productInfo.getProductName().equals("全部")) {
            holder.setGone(R.id.tv_num, true);
        } else {
            holder.setGone(R.id.tv_num, true);
//            holder.setText(R.id.tv_num, "(" + productInfo.getDeviceNum() + ")");
        }
        TextView tvName = (TextView) holder.getView(R.id.tv_name);
        TextView tvNum = (TextView) holder.getView(R.id.tv_num);
        if (productInfo.isChecked()) {
            holder.setBackgroundResource(R.id.ll_item, R.drawable.bg_device_type_checked);
            holder.setTextColorRes(R.id.tv_name, R.color.blue_52B4F8);
            tvName.setTextSize(17);
            tvNum.setTextSize(17);
            tvName.setTypeface(tvName.getTypeface(), Typeface.BOLD);
        } else {
            holder.setBackgroundResource(R.id.ll_item, 0);
            holder.setTextColorRes(R.id.tv_name, R.color.text_color_666666);
            tvName.setTextSize(15);
            tvNum.setTextSize(15);
            tvName.setTypeface(tvName.getTypeface(), Typeface.NORMAL);
        }
    }
}
