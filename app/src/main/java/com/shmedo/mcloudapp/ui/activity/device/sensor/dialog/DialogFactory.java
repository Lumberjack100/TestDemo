package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.inter.MyOnClickListener;

import ch.ielse.view.SwitchView;

/**
 * 项目名：  mCloudAPP
 * 包名：    com.shmedo.mcloudapp.dialog
 * 文件名:   DialogFactory
 * 创建者:   dpc
 * 创建时间:  2019/6/14 10:34
 * 描述：    TODO
 */
public class DialogFactory {

    public <T> IDialogOpt<T> createDialog(final Context context, String type, T data, int channelNumber, final TextView mTvStay, final SwitchView mSwStay, MyOnClickListener onClickListener) {
        IDialogOpt opt = null;

        switch (type) {
            case "02":
                opt = new DialogStyle02(context, channelNumber);
                opt.getDialog().show();
                ((DialogStyle02) opt).setCancelOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        closeSwitchColorbg(context, mTvStay, mSwStay);
                    }
                });

                if (onClickListener != null) {
                    ((DialogStyle02) opt).setSureonClickListener(onClickListener);
                }
                break;

            case "03":
                opt = new DialogStyle03(context);
                opt.getDialog().show();
                opt.getDialog().show();
                ((DialogStyle03) opt).setCancelOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        closeSwitchColorbg(context, mTvStay, mSwStay);
                    }
                });

                if (onClickListener != null) {
                    ((DialogStyle03) opt).setSureonClickListener(onClickListener);
                }
                break;

            case "04":
                opt = new DialogStyle04(context);
                opt.getDialog().show();
                ((DialogStyle04) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        closeSwitchColorbg(context, mTvStay, mSwStay);
                    }
                });
                if (onClickListener != null) {
                    ((DialogStyle04) opt).setSureClickListener(onClickListener);
                }
                break;

            case "06":
                opt = new DialogStyle06(context);
                opt.getDialog().show();
                ((DialogStyle06) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        closeSwitchColorbg(context, mTvStay, mSwStay);
                    }
                });
                if (onClickListener != null) {
                    ((DialogStyle06) opt).setSureClickListener(onClickListener);
                }
                break;

            case "07":
                opt = new DialogStyle07(context);
                opt.getDialog().show();
                ((DialogStyle07) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        closeSwitchColorbg(context, mTvStay, mSwStay);
                    }
                });
                if (onClickListener != null) {
                    ((DialogStyle07) opt).setSureClickListener(onClickListener);
                }
                break;

            case "08":
                opt = new DialogStyle08(context);
                opt.getDialog().show();
                ((DialogStyle08) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        closeSwitchColorbg(context, mTvStay, mSwStay);
                    }
                });
                if (onClickListener != null) {
                    ((DialogStyle08) opt).setSureClickListener(onClickListener);
                }
                break;

            case "12":
                opt = new DialogStyle12(context);
                opt.getDialog().show();
                ((DialogStyle12) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        closeSwitchColorbg(context, mTvStay, mSwStay);
                    }
                });
                if (onClickListener != null) {
                    ((DialogStyle12) opt).setSureClickListener(onClickListener);
                }
                break;

            case "15":
                opt = new DialogStyle15(context);
                opt.getDialog().show();
                ((DialogStyle15) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        closeSwitchColorbg(context, mTvStay, mSwStay);
                    }
                });
                if (onClickListener != null) {
                    ((DialogStyle15) opt).setSureClickListener(onClickListener);
                }
                break;

            case "50":
                opt = new DialogStyle50(context);
                opt.getDialog().show();
                ((DialogStyle50) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        closeSwitchColorbg(context, mTvStay, mSwStay);
                    }
                });
                if (onClickListener != null) {
                    ((DialogStyle50) opt).setSureClickListener(onClickListener);
                }
                break;

            case "53":
            case "54":
                opt = new DialogStyle5354(context);
                opt.getDialog().show();
                ((DialogStyle5354) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        closeSwitchColorbg(context, mTvStay, mSwStay);
                    }
                });
                if (onClickListener != null) {
                    ((DialogStyle5354) opt).setSureClickListener(onClickListener);
                }
                break;

            case "51":
            case "52":
            case "55":
                opt = new DialogStyle515255(context);
                opt.getDialog().show();
                ((DialogStyle515255) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        closeSwitchColorbg(context, mTvStay, mSwStay);
                    }
                });
                if (onClickListener != null) {
                    ((DialogStyle515255) opt).setSureClickListener(onClickListener);
                }
                break;

            default:
        }

        if (null != opt) {
            opt.initData(data);
        }

        return null;
    }

    /**
     * 关闭选择按钮
     */
    private void closeSwitchColorbg(Context context, final TextView mTvStay, final SwitchView mSwStay) {
        mSwStay.setOpened(false);
        mTvStay.setText("已停用");
        mTvStay.setBackgroundColor(context.getResources().getColor(R.color.secondary_text));
    }
}
