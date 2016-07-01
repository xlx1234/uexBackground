package org.zywx.wbpalmstar.plugin.uexbackground;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import com.google.gson.reflect.TypeToken;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.plugin.uexbackground.vo.AddTimerVO;
import org.zywx.wbpalmstar.plugin.uexbackground.vo.StartVO;

import java.util.ArrayList;
import java.util.List;

public class EUExBackground extends EUExBase {

    private static final String BUNDLE_DATA = "data";


    public EUExBackground(Context context, EBrowserView eBrowserView) {
        super(context, eBrowserView);
    }

    @Override
    protected boolean clean() {
        return false;
    }


    @Override
    public void onHandleMessage(Message message) {
        if (message == null) {
            return;
        }
        Bundle bundle = message.getData();
        switch (message.what) {

            default:
                super.onHandleMessage(message);
        }
    }

    public void start(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        StartVO startVO = DataHelper.gson.fromJson(json, StartVO.class);
        String realPath = BUtility.makeRealPath(startVO.getJsPath(),mBrwView);
        String[] resourcePaths=startVO.getJsResourcePaths();
        if (resourcePaths!=null){
            for (int i = 0; i < resourcePaths.length; i++) {
                resourcePaths[i]=BUtility.makeRealPath(resourcePaths[i],mBrwView);
            }
        }
        startVO.setJsPath(realPath);
        Intent intent = new Intent(mContext, EUExBgService.class);
        intent.setFlags(EUExBgService.FLAG_START);
        intent.putExtra(EUExBgService.KEY_START_DATA, startVO);
        mContext.startService(intent);
        BDebug.i(DataHelper.gson.toJson(startVO));
    }

    public boolean addTimer(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return false;
        }
        String json = params[0];
        AddTimerVO addTimerVO = DataHelper.gson.fromJson(json, AddTimerVO.class);
        Intent intent = new Intent(mContext, EUExBgService.class);
        intent.setFlags(EUExBgService.FLAG_ADD_TIMER);
        intent.putExtra(EUExBgService.KEY_ADD_TIMER_DATA, addTimerVO);
        mContext.startService(intent);
        return true;
    }

    public void cancelTimer(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        ArrayList<String> ids = DataHelper.gson.fromJson(params[0],
                new TypeToken<List<String>>() {
                }.getType());
        Intent intent = new Intent(mContext, EUExBgService.class);
        if (ids != null && !ids.isEmpty()) {
            intent.putStringArrayListExtra(EUExBgService.KEY_REMOVE_TIMER_DATA, ids);
            intent.setFlags(EUExBgService.FLAG_REMOVE_TIMER);
        } else {
            intent.setFlags(EUExBgService.FLAG_REMOVE_ALL_TIMER);
        }
        mContext.startService(intent);
    }

    public void stop(String[] params){
        Intent intent = new Intent(mContext, EUExBgService.class);
        mContext.stopService(intent);
    }

    private void callBackPluginJs(String methodName, String jsonData) {
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}";
        onCallback(js);
    }

}
