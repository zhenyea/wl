package com.wl.radio

import android.app.Application
import com.billy.android.loading.Gloading
import com.wl.radio.adapter.GlobalAdapter
import com.wl.radio.util.Constants.XMLYAPPSECRET
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.multidex.MultiDexApplication
import com.wl.radio.dao.CollectRadioDao
import com.wl.radio.dao.UserDao
import com.wl.radio.database.AppDataBase
import com.wl.radio.receiver.MyPlayerReceiver
import com.wl.radio.util.Constants
import com.wl.radio.util.Constants.APPLICATION_NEXT_SHOW_ACTION
import com.wl.radio.util.Constants.APPLICATION_PRE_SHOW_ACTION
import com.wl.radio.util.Constants.CLOSE_APP_ACTION
import com.wl.radio.util.Constants.NEXT_SHOW_ACTION
import com.wl.radio.util.Constants.PRE_SHOW_ACTION
import com.wl.radio.util.Constants.RESET_RADIO_IMG_AND_TITLE_ACTION
import com.wl.radio.util.Constants.START_OR_PAUSE_ACTION
import com.wl.radio.util.Constants.TRANSRADIO
import com.wl.radio.util.LogUtils
import com.wl.radio.util.testJava.testReceiver
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio
import com.ximalaya.ting.android.opensdk.player.appnotification.XmNotificationCreater
import com.ximalaya.ting.android.opensdk.util.BaseUtil
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshFooter
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import android.R
import android.R.attr.colorPrimary
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator
import com.wl.radio.util.Constants.TRANS_SCHEDULE
import com.wl.radio.util.StringUtils
import com.ximalaya.ting.android.opensdk.model.live.schedule.Schedule


class MyApplication : MultiDexApplication() {

    var broadcastReceiver:BroadcastReceiver = object:BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action){
                APPLICATION_NEXT_SHOW_ACTION-> playNextRadio()
                APPLICATION_PRE_SHOW_ACTION-> playPreRadio()


            }

        }

    }



    override fun onCreate() {
        super.onCreate()
        Gloading.debug(BuildConfig.DEBUG)
        Gloading.initDefault(GlobalAdapter())
        userDao = AppDataBase.getInstance(this).userDao()
        collectRadioDao = AppDataBase.getInstance(this).getCollectRadioDao()
        CommonRequest.getInstanse().init(this, XMLYAPPSECRET);
        XmPlayerManager.getInstance(this).init()
        context = this
        var intentFilter = IntentFilter()
        intentFilter.addAction(APPLICATION_NEXT_SHOW_ACTION)
        intentFilter.addAction(APPLICATION_PRE_SHOW_ACTION)
        registerReceiver(broadcastReceiver,intentFilter)




        if (BaseUtil.getCurProcessName(this).contains(":player")) {
            val instanse = XmNotificationCreater.getInstanse(this)
            instanse.setNextPendingIntent(null as PendingIntent?)
            instanse.setPrePendingIntent(null as PendingIntent?)
            instanse.setStartOrPausePendingIntent(null as PendingIntent?)

            val closeAppIntent = Intent(CLOSE_APP_ACTION)
            closeAppIntent.setClass(this, MyPlayerReceiver::class.java!!)
            val closeAppBroadcast = PendingIntent.getBroadcast(this, 0, closeAppIntent, 0)
            instanse.setClosePendingIntent(closeAppBroadcast)


            val startOrPauseIntent = Intent(START_OR_PAUSE_ACTION)
            startOrPauseIntent.setClass(this, MyPlayerReceiver::class.java!!)
            val startOrPauseBroadcast = PendingIntent.getBroadcast(this, 0, startOrPauseIntent, 0)
            instanse.setStartOrPausePendingIntent(startOrPauseBroadcast)

            val nextShowIntent = Intent(NEXT_SHOW_ACTION)
            nextShowIntent.setClass(this, MyPlayerReceiver::class.java!!)
            val nextShowBroadcast = PendingIntent.getBroadcast(this, 0, nextShowIntent, 0)
            instanse.setNextPendingIntent(nextShowBroadcast)


            val preShowIntent = Intent(PRE_SHOW_ACTION)
            preShowIntent.setClass(this, MyPlayerReceiver::class.java!!)
            val preShowBroadcast = PendingIntent.getBroadcast(this, 0, preShowIntent, 0)
            instanse.setPrePendingIntent(preShowBroadcast)

        }

        SmartRefreshLayout.setDefaultRefreshHeaderCreator(object:DefaultRefreshHeaderCreator{
            override fun createRefreshHeader(
                context: Context,
                layout: RefreshLayout
            ): RefreshHeader {
                layout.setPrimaryColorsId(com.wl.radio.R.color.colorPrimary, android.R.color.white);//全局设置主题颜色
                return  ClassicsHeader(Companion.context);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
            }

        })

        SmartRefreshLayout.setDefaultRefreshFooterCreator(object:DefaultRefreshFooterCreator{
            override fun createRefreshFooter(
                context: Context,
                layout: RefreshLayout
            ): RefreshFooter {
                //指定为经典Footer，默认是 BallPulseFooter
                return  ClassicsFooter(Companion.context).setDrawableSize(20f);
            }

        })



    }

    companion object {
        var playingRadioList: ArrayList<Radio> = ArrayList()
        var historyRadioList:ArrayList<Radio> = ArrayList()
        var scheduleList: ArrayList<Schedule> = ArrayList() //回看节目
        var scheduleIndex:Int = 0//回看节目下标

        var  context:Application? = null
       lateinit var userDao: UserDao
        lateinit var collectRadioDao:CollectRadioDao
        val TAG = "MyApplication"
        fun getContext():Context{
            return context!!
        }
        fun playNextRadio(): Radio? {
            if(scheduleList.size>0&& scheduleIndex<(scheduleList.size-1)){
                scheduleIndex++
                playPositionSchedule()

                return null
            }else if(scheduleIndex==(scheduleList.size-1)&& scheduleList.size!=0){
                scheduleIndex = 0
                playPositionSchedule()

                return null
            }else if (playingRadioList.size > 0 &&getPlayingRadioIndex() < (playingRadioList.size - 1)) {
               return playPositionRadio(getPlayingRadioIndex() + 1);
            }else if(getPlayingRadioIndex()== (playingRadioList.size-1)&&playingRadioList.size!=0){
              return  playPositionRadio(0);
            }else{
                return null
            }
        }

        fun playPositionRadio(position:Int):Radio{
            XmPlayerManager.getInstance(getContext()).playActivityRadio(playingRadioList[position])
            sendUpdateImageAndTitleRadioBroadcast(playingRadioList[position])
            return playingRadioList[position]
        }

        fun playPositionSchedule(){
            XmPlayerManager.getInstance(getContext()).playSchedule(scheduleList, scheduleIndex)
            var intent = Intent(Constants.RESET_SCHEDULE_IMG_AND_TITLE_ACTION)
            context?.sendBroadcast(intent)
        }



        fun playPreRadio():Radio?{
            if(scheduleList.size>0&& scheduleIndex!=0){
                scheduleIndex =scheduleIndex-1
                playPositionSchedule()
                return null
            }else if(scheduleIndex==0&& scheduleList.size!=0){
                scheduleIndex = (scheduleList.size-1)
                playPositionSchedule()
                return null
            }else if (playingRadioList.size > 0 && getPlayingRadioIndex() != 0) {
                return playPositionRadio(getPlayingRadioIndex()-1)
            }else if(getPlayingRadioIndex()==0&&playingRadioList.size!=0){
                return playPositionRadio(playingRadioList.size-1)

            }else{
                return null
            }

        }
        fun sendUpdateImageAndTitleRadioBroadcast(radio: Radio){
            var intent = Intent(RESET_RADIO_IMG_AND_TITLE_ACTION)
            intent.putExtra(TRANSRADIO,radio)
            getContext().sendBroadcast(intent)

            radio.let { MyApplication.addHistoryRadios(it)

                var intent  = Intent()
                intent.setAction(Constants.BROADCAST_REFRESH_PLAY_RADIO_HISTORY)
                intent.putExtra(Constants.TRANS_PLAYING_RADIO,radio)
                context?.sendBroadcast(intent)

            }
        }


        fun getPlayingRadioIndex(): Int {
            for (radioIndex in playingRadioList.indices) {
                if (playingRadioList[radioIndex].dataId.equals(XmPlayerManager.getInstance(getContext()).currSound.dataId)) {
                    return radioIndex
                } else if (radioIndex == (playingRadioList.size - 1)) {
                    break;
                }
            }


            return 0
        }

        fun getPlayingSchedule():Schedule{
             return scheduleList.get(scheduleIndex)
        }


        fun refreshRadioList(newRadioList:ArrayList<Radio>){

            if(playingRadioList.size>0){
                playingRadioList.clear()
            }

            playingRadioList.addAll(newRadioList)
        }


        fun addHistoryRadios(radio:Radio){

            if(historyRadioList.size==0){
                historyRadioList.add(radio)
            }else if(historyRadioList.contains(radio)){
              historyRadioList.remove(radio)
                historyRadioList.add(radio)
            }else{
                historyRadioList.add(radio)
            }

        }

        fun getHistoryRadios():List<Radio>{
            LogUtils.d("MyApplication-history-size","raidios"+historyRadioList.size)
            if(historyRadioList.size>=5){


                return historyRadioList.reversed().subList(0,
                    5) as List<Radio>
            }else {

                return historyRadioList
            }


        }

        fun clearSchedule(){
            MyApplication.scheduleList.clear()
            MyApplication.scheduleIndex=-1
        }

    }




    fun setColorTheme(){

    }




}