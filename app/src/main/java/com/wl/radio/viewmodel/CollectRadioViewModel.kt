package com.wl.radio.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.wl.radio.bean.CollectRadioBean
import com.wl.radio.model.CollectRadioModel

class CollectRadioViewModel :ViewModel(),LifecycleObserver,CollectRadioModel.DataResultListener{


    var queryStatusLiveData: MutableLiveData<String>?= MutableLiveData()
    var errorMsgLiveData: MutableLiveData<String>?= MutableLiveData()
    var allCollectRadioLiveData:MutableLiveData<List<CollectRadioBean>> = MutableLiveData()
    var isRadioCollectedLiveDate:MutableLiveData<Boolean> = MutableLiveData()

    var collectRadioModel = CollectRadioModel(this)


    override fun setQueryStatus(status: String) {
        queryStatusLiveData?.value = status

    }

    override fun setErrorMsg(msg: String) {
        errorMsgLiveData?.value = msg

    }

    override fun setAllCollectRadio(collectRadios: List<CollectRadioBean>) {
        Log.d("viewmodescollect","setAllCollectRadio")
        allCollectRadioLiveData?.value = collectRadios
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestory(){
        collectRadioModel.onDestory()
    }

    fun addCollectRadio(radioId:String){
        collectRadioModel.addCollectRadio(radioId)
    }
    fun deleteCollectRadio(collectRadioBean:CollectRadioBean){
        collectRadioModel.deleteCollectRadio(collectRadioBean)

    }

    fun deleteCollectRadioById(radioId: String){
        collectRadioModel.deleteCollectBeanById(radioId)
    }

    fun queryAllCollectRadio(){
        collectRadioModel.queryAllCollectRadio()
    }

    fun queryCollectRadioById(radioId: String){
        collectRadioModel.queryCollectRadioById(radioId)

    }

    override fun getIsRadioCollected(isCollect: Boolean) {
        isRadioCollectedLiveDate.value = isCollect
    }

}