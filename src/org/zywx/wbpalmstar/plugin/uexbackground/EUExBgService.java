/*
 * Copyright (c) 2016.  The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.zywx.wbpalmstar.plugin.uexbackground;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.FileHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.plugin.uexbackground.vo.AddTimerVO;
import org.zywx.wbpalmstar.plugin.uexbackground.vo.StartVO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ylt on 16/3/8.
 */
public class EUExBgService extends Service {

    public static final String KEY_START_DATA = "start_data";
    public static final String KEY_ADD_TIMER_DATA = "add_timer";
    public static final String KEY_REMOVE_TIMER_DATA = "remove_timer";

    public static final int FLAG_START = 0;
    public static final int FLAG_ADD_TIMER = 1;
    public static final int FLAG_REMOVE_TIMER = 2;
    public static final int FLAG_REMOVE_ALL_TIMER = 3;
    public static final int FLAG_STOP = 4;

    EBrowserView mBackgroundView = null;//在后台运行的EBrowserView
    Timer mTimer = null;
    private StartVO mStartVO;

    private List<CallbackJsTimerTask> mJsTimerTasks = new ArrayList<CallbackJsTimerTask>();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return 0;
        }
        handleIntent(intent, flags);
        return START_REDELIVER_INTENT;
    }

    private void loadJsContentByPath(String path) {
        String content=null;
        if (path.startsWith("/")){
            try {
                content=FileHelper.readFile(path);
            } catch (IOException e) {
                if (BDebug.DEBUG){
                    e.printStackTrace();
                }
            }
        }else{
            content=FileHelper.loadAssetTextAsString(this
                    , getRealPath(path));
        }
        mBackgroundView.loadUrl("javascript:" +content);
    }

    /**
     * 处理路径中间带有../的路径
     *
     * @param path
     * @return
     */
    private String getRealPath(String path) {
        if (path.contains("../")) {
            String paths[] = path.split("/");
            int index = -1;
            for (int i = 0; i < paths.length; i++) {
                if (paths[i].equals("..")) {
                    index = i;
                    break;
                }
            }
            return getRealPath(path.replaceFirst(paths[index-1]+"/../",""));
        } else {
            return path;
        }
    }

    private void loadJs(){
        if (mStartVO!=null) {
            if (mStartVO.getJsResourcePaths() != null) {
                for (int i = 0; i < mStartVO.getJsResourcePaths().length; i++) {
                    loadJsContentByPath(mStartVO.getJsResourcePaths()[i]);
                }
            }
            loadJsContentByPath(mStartVO.getJsPath());
            EUExBase.callBackJs(mBackgroundView, JsConst.ON_LOAD, "");
        }
    }

    /**
     * webView不存在时，load js需要等onPageFinish之后
     */
    private void createViewAndLoadJs(){
        if (mBackgroundView!=null){
           loadJs();
        }else{
            mBackgroundView = new EBrowserView(this, 0, null){
                @Override
                protected void onPageFinished(EBrowserView view, String url) {
                    loadJs();
                }
            };
            mBackgroundView.init();
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.x = 0;
            params.y = 0;
            params.width = 0;
            params.height = 0;

            LinearLayout view = new LinearLayout(this);
            view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            mBackgroundView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            view.addView(mBackgroundView);
            windowManager.addView(view, params);
            mBackgroundView.loadUrl(BUtility.F_ASSET_PATH + "error/error.html");
        }

    }

    private void handleIntent(Intent intent, int flags) {
        switch (intent.getFlags()) {
            case FLAG_START:
                mStartVO = (StartVO) intent.getSerializableExtra(KEY_START_DATA);
                createViewAndLoadJs();
                break;
            case FLAG_ADD_TIMER:
                AddTimerVO addTimerVO = (AddTimerVO) intent.getSerializableExtra(KEY_ADD_TIMER_DATA);
                CallbackJsTimerTask timerTask = new CallbackJsTimerTask(addTimerVO);
                if (mTimer==null){
                    mTimer=new Timer(false);
                }
                mTimer.schedule(timerTask, 0, addTimerVO.getTimeInterval());
                mJsTimerTasks.add(timerTask);
                break;
            case FLAG_REMOVE_TIMER:
                List<String> ids = intent.getStringArrayListExtra(KEY_REMOVE_TIMER_DATA);
                if (ids != null) {
                    for (String id : ids) {
                        cancelTimerTaskById(id);
                    }
                }
                break;
            case FLAG_REMOVE_ALL_TIMER:
               cancelAllTimer();
                break;
            case FLAG_STOP:
                break;
            default:
                break;
        }
    }

    /**
     * 取消所有的定时任务
     */
    private void cancelAllTimer(){
        mJsTimerTasks.clear();
        if (mTimer!=null) {
            mTimer.cancel();
        }
        mTimer=null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void cancelTimerTaskById(String id) {
        int index = -1;
        for (int i = 0; i < mJsTimerTasks.size(); i++) {
            if (mJsTimerTasks.get(i).mAddTimerVO.getId().equals(id)) ;
            index = i;
            break;
        }
        if (index != -1) {
            mJsTimerTasks.get(index).cancel();
            mJsTimerTasks.remove(index);
        }

    }

    class CallbackJsTimerTask extends TimerTask {

        AddTimerVO mAddTimerVO;

        int count = 1;

        public CallbackJsTimerTask(AddTimerVO addTimerVO) {
            this.mAddTimerVO = addTimerVO;
        }

        @Override
        public void run() {
            if (mAddTimerVO.getRepeatTimes() != 0) {
                if (count >= mAddTimerVO.getRepeatTimes()) {
                    this.cancel();
                }
            }
            EUExBase.callBackJsObject(mBackgroundView, "uexBackground."+mAddTimerVO.getCallbackName(), count);
            count++;
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelAllTimer();
        BDebug.i(EUExBgService.class.getSimpleName(),"onDestroy");
    }
}