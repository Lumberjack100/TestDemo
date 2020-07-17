package com.shmedo.mcloudapp.maps.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.maps.model.NetWorkQuality;
import com.shmedo.mcloudapp.maps.testspeed.GetSpeedTestHostsHandler;
import com.shmedo.mcloudapp.maps.testspeed.HttpDownloadTest;
import com.shmedo.mcloudapp.maps.testspeed.HttpUploadTest;
import com.shmedo.mcloudapp.maps.testspeed.PingTest;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class SpeedTestActivity extends BaseActivity {
    @BindView(R.id.startButton)
    Button startButton;
    @BindView(R.id.barImageView)
    ImageView barImageView;

    @BindView(R.id.pingTextView)
    TextView pingTextView;

    @BindView(R.id.downloadTextView)
    TextView downloadTextView;

    @BindView(R.id.uploadTextView)
    TextView uploadTextView;

    @BindView(R.id.chartPing)
    LinearLayout chartPing;

    @BindView(R.id.chartDownload)
    LinearLayout chartDownload;

    @BindView(R.id.chartUpload)
    LinearLayout chartUpload;

    private static int position = 0;
    static int lastPosition = 0;
    private GetSpeedTestHostsHandler getSpeedTestHostsHandler = null;
    private HashSet<String> tempBlackList;
    private final DecimalFormat dec = new DecimalFormat("#.##");


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, SpeedTestActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
        getSpeedTestHostsHandler.start();
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_speed_test;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startButton.setText("开始");
        tempBlackList = new HashSet<>();
    }

    @OnClick({R.id.back, R.id.startButton})
    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            finish();
        } else if (v.getId() == R.id.startButton) {
            startTest();
        }
    }

    private void startTest() {
        startButton.setEnabled(false);

        //Restart test icin eger baglanti koparsa
        if (getSpeedTestHostsHandler == null) {
            getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
            getSpeedTestHostsHandler.start();
        }

        new Thread(new Runnable() {
            RotateAnimation rotate;

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startButton.setText("根据ping选择最佳服务器...");
                    }
                });

                //Get egcodes.speedtest hosts
                int timeCount = 600; //1min
                while (!getSpeedTestHostsHandler.isFinished()) {
                    timeCount--;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    if (timeCount <= 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "No Connection...", Toast.LENGTH_LONG).show();
                                startButton.setEnabled(true);
                                startButton.setTextSize(16);
                                startButton.setText("重新测试");
                            }
                        });
                        getSpeedTestHostsHandler = null;
                        return;
                    }
                }

                //Find closest server
                HashMap<Integer, String> mapKey = getSpeedTestHostsHandler.getMapKey();
                HashMap<Integer, List<String>> mapValue = getSpeedTestHostsHandler.getMapValue();
                double selfLat = getSpeedTestHostsHandler.getSelfLat();
                double selfLon = getSpeedTestHostsHandler.getSelfLon();
                double tmp = 19349458;//默认赋值一个最大距离
                int findServerIndex = 0;
                for (int index : mapKey.keySet()) {
                    if (tempBlackList.contains(mapValue.get(index).get(5))) {
                        continue;
                    }

                    Location source = new Location("Source");
                    source.setLatitude(selfLat);
                    source.setLongitude(selfLon);

                    List<String> ls = mapValue.get(index);
                    Location dest = new Location("Dest");
                    dest.setLatitude(Double.parseDouble(ls.get(0)));
                    dest.setLongitude(Double.parseDouble(ls.get(1)));

                    double distance = source.distanceTo(dest);
                    if (tmp > distance) {
                        tmp = distance;
                        findServerIndex = index;
                    }
                }

                final double closestDistance = tmp;//得到最近的服务器距离
                final List<String> hostInfos = mapValue.get(findServerIndex);
                String testAddr = mapKey.get(findServerIndex).replace("http://", "https://");

                if (hostInfos == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startButton.setTextSize(12);
                            startButton.setText("获取主机位置时出现问题。 稍后再试。");
                        }
                    });
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startButton.setTextSize(13);
                        startButton.setText(String.format("主机位置: %s [距离: %s km]", hostInfos.get(2), new DecimalFormat("#.##").format(closestDistance / 1000)));
                    }
                });

                //重置值以及图形
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pingTextView.setText("0 ms");
                        chartPing.removeAllViews();
                        downloadTextView.setText("0 Mbps");
                        chartDownload.removeAllViews();
                        uploadTextView.setText("0 Mbps");
                        chartUpload.removeAllViews();
                    }
                });
                final List<Double> pingRateList = new ArrayList<>();
                final List<Double> downloadRateList = new ArrayList<>();
                final List<Double> uploadRateList = new ArrayList<>();
                Boolean pingTestStarted = false;
                Boolean pingTestFinished = false;
                Boolean downloadTestStarted = false;
                Boolean downloadTestFinished = false;
                Boolean uploadTestStarted = false;
                Boolean uploadTestFinished = false;

                //Init Test
                final PingTest pingTest = new PingTest(hostInfos.get(6).replace(":8080", ""), 3);
                final HttpDownloadTest downloadTest = new HttpDownloadTest(testAddr.replace(testAddr.split("/")[testAddr.split("/").length - 1], ""));
                final HttpUploadTest uploadTest = new HttpUploadTest(testAddr);

                //Tests
                while (true) {
                    if (!pingTestStarted) {
                        pingTest.start();
                        pingTestStarted = true;
                    }
                    if (pingTestFinished && !downloadTestStarted) {
                        downloadTest.start();
                        downloadTestStarted = true;
                    }
                    if (downloadTestFinished && !uploadTestStarted) {
                        uploadTest.start();
                        uploadTestStarted = true;
                    }

                    //Ping Test
                    if (pingTestFinished) {
                        //Failure
                        if (pingTest.getAvgRtt() == 0) {
                            System.out.println("Ping error...");
                        } else {
                            //Success
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pingTextView.setText(dec.format(pingTest.getAvgRtt()) + " ms");
                                }
                            });
                        }
                    } else {
                        pingRateList.add(pingTest.getInstantRtt());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pingTextView.setText(dec.format(pingTest.getInstantRtt()) + " ms");
                            }
                        });

                        //Update chart
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Creating an  XYSeries for Income
                                XYSeries pingSeries = new XYSeries("");
                                pingSeries.setTitle("");

                                int count = 0;
                                List<Double> tmpLs = new ArrayList<>(pingRateList);
                                for (Double val : tmpLs) {
                                    pingSeries.add(count++, val);
                                }

                                XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                                dataset.addSeries(pingSeries);

                                GraphicalView chartView = ChartFactory.getLineChartView(getBaseContext(), dataset, initSeriesRenderer());
                                chartPing.addView(chartView, 0);
                            }
                        });
                    }


                    //Download Test
                    if (pingTestFinished) {
                        if (downloadTestFinished) {
                            //Failure
                            if (downloadTest.getFinalDownloadRate() == 0) {
                                System.out.println("Download error...");
                            } else {
                                //Success
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        downloadTextView.setText(dec.format(downloadTest.getFinalDownloadRate()) + " Mbps");
                                    }
                                });
                            }
                        } else {
                            //Calc position
                            double downloadRate = downloadTest.getInstantDownloadRate();
                            downloadRateList.add(downloadRate);
                            position = getPositionByRate(downloadRate);

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                    rotate.setInterpolator(new LinearInterpolator());
                                    rotate.setDuration(100);
                                    barImageView.startAnimation(rotate);
                                    downloadTextView.setText(dec.format(downloadTest.getInstantDownloadRate()) + " Mbps");
                                }
                            });
                            lastPosition = position;

                            //Update chart
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Creating an  XYSeries for Income
                                    XYSeries downloadSeries = new XYSeries("");
                                    downloadSeries.setTitle("");

                                    List<Double> tmpLs = new ArrayList<>(downloadRateList);
                                    int count = 0;
                                    for (Double val : tmpLs) {
                                        downloadSeries.add(count++, val);
                                    }

                                    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                                    dataset.addSeries(downloadSeries);

                                    GraphicalView chartView = ChartFactory.getLineChartView(getBaseContext(), dataset, initSeriesRenderer());
                                    chartDownload.addView(chartView, 0);
                                }
                            });

                        }
                    }


                    //Upload Test
                    if (downloadTestFinished) {
                        if (uploadTestFinished) {
                            //Failure
                            if (uploadTest.getFinalUploadRate() == 0) {
                                System.out.println("Upload error...");
                            } else {
                                //Success
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        uploadTextView.setText(dec.format(uploadTest.getFinalUploadRate()) + " Mbps");
                                    }
                                });
                            }
                        } else {
                            //Calc position
                            double uploadRate = uploadTest.getInstantUploadRate();
                            uploadRateList.add(uploadRate);
                            position = getPositionByRate(uploadRate);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                    rotate.setInterpolator(new LinearInterpolator());
                                    rotate.setDuration(100);
                                    barImageView.startAnimation(rotate);
                                    uploadTextView.setText(dec.format(uploadTest.getInstantUploadRate()) + " Mbps");
                                }
                            });
                            lastPosition = position;

                            //Update chart
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Creating an  XYSeries for Income
                                    XYSeries uploadSeries = new XYSeries("");
                                    uploadSeries.setTitle("");

                                    int count = 0;
                                    List<Double> tmpLs = new ArrayList<>(uploadRateList);
                                    for (Double val : tmpLs) {
                                        if (count == 0) {
                                            val = 0.0;
                                        }
                                        uploadSeries.add(count++, val);
                                    }

                                    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                                    dataset.addSeries(uploadSeries);

                                    GraphicalView chartView = ChartFactory.getLineChartView(getBaseContext(), dataset, initSeriesRenderer());
                                    chartUpload.addView(chartView, 0);
                                }
                            });
                        }
                    }

                    //Test bitti
                    if (pingTestFinished && downloadTestFinished && uploadTest.isFinished()) {
                        break;
                    }

                    if (pingTest.isFinished()) {
                        pingTestFinished = true;
                    }
                    if (downloadTest.isFinished()) {
                        downloadTestFinished = true;
                    }
                    if (uploadTest.isFinished()) {
                        uploadTestFinished = true;
                    }

                    if (pingTestStarted && !pingTestFinished) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                        }
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startButton.setEnabled(true);
                        startButton.setTextSize(16);
                        startButton.setText("重新测试");
                        goToResultActivity();
                    }
                });
            }
        }).start();
    }


    private XYMultipleSeriesRenderer initSeriesRenderer() {
        XYSeriesRenderer xySeriesRenderer = new XYSeriesRenderer();
        XYSeriesRenderer.FillOutsideLine fillOutsideLine = new XYSeriesRenderer.FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BOUNDS_ALL);
        fillOutsideLine.setColor(Color.parseColor("#4d5a6a"));
        xySeriesRenderer.addFillOutsideLine(fillOutsideLine);
        xySeriesRenderer.setDisplayChartValues(false);
        xySeriesRenderer.setColor(Color.parseColor("#4d5a6a"));
        xySeriesRenderer.setShowLegendItem(false);
        xySeriesRenderer.setLineWidth(5);
        final XYMultipleSeriesRenderer xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
        xyMultipleSeriesRenderer.setXLabels(0);
        xyMultipleSeriesRenderer.setYLabels(0);
        xyMultipleSeriesRenderer.setZoomEnabled(false);
        xyMultipleSeriesRenderer.setXAxisColor(Color.parseColor("#647488"));
        xyMultipleSeriesRenderer.setYAxisColor(Color.parseColor("#2F3C4C"));
        xyMultipleSeriesRenderer.setPanEnabled(false, false);
        xyMultipleSeriesRenderer.setZoomButtonsVisible(false);
        xyMultipleSeriesRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        xyMultipleSeriesRenderer.addSeriesRenderer(xySeriesRenderer);

        return xyMultipleSeriesRenderer;
    }

    public int getPositionByRate(double rate) {
        if (rate <= 1) {
            return (int) (rate * 30);

        } else if (rate <= 10) {
            return (int) (rate * 6) + 30;

        } else if (rate <= 30) {
            return (int) ((rate - 10) * 3) + 90;

        } else if (rate <= 50) {
            return (int) ((rate - 30) * 1.5) + 150;

        } else if (rate <= 100) {
            return (int) ((rate - 50) * 1.2) + 180;
        }

        return 0;
    }

    private void goToResultActivity() {
        NetWorkQuality netWorkQuality = new NetWorkQuality();
        netWorkQuality.setDelay(pingTextView.getText().toString());
        netWorkQuality.setDownloadSpeed(downloadTextView.getText().toString());
        netWorkQuality.setUploadSpeed(uploadTextView.getText().toString());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SpeedTestResultActivity.startActivity(SpeedTestActivity.this, netWorkQuality);
            }
        }, 1000);
    }

}
