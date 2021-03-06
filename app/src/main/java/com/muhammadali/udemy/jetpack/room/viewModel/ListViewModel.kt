package com.muhammadali.udemy.jetpack.room.viewModel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.muhammadali.udemy.jetpack.room.model.DogBreed
import com.muhammadali.udemy.jetpack.room.model.DogDao
import com.muhammadali.udemy.jetpack.room.model.DogDatabase
import com.muhammadali.udemy.jetpack.room.model.DogsService
import com.muhammadali.udemy.jetpack.room.utils.SharePreferencesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

/**
 * Created by Muhammad Ali on 04-May-20.
 * Email muhammad.ali9385@gmail.com
 */
class ListViewModel(application: Application) : BaseViewModel(application) {

    private val prefsHelper = SharePreferencesHelper(getApplication())
    private val refreshTime = 10 * 1000 * 1000 * 1000L

    private val dogsService = DogsService()
    private val disposable = CompositeDisposable()


    val dogs = MutableLiveData<List<DogBreed>>()
    val error = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun refresh() {
        val updateTime = prefsHelper.getTime()
        if (updateTime != null && updateTime != 0L && System.nanoTime() - updateTime < refreshTime) {
            fetchFromDatabase()
        } else {
            fetchFromRemote()
        }
    }

    fun refreshBypassCache() {
        fetchFromRemote()
    }

    private fun fetchFromDatabase() {
        loading.value = true
        val dogDao = DogDatabase(getApplication()).dogDao()
        launch {
            val dogs = dogDao.getAllDogs()
            dogsRetrieve(dogs)
            Toast.makeText(getApplication(), "Retrived from Database", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchFromRemote() {
        loading.value = true
        disposable.add(
            dogsService.getDogs()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<List<DogBreed>>() {
                    override fun onSuccess(t: List<DogBreed>) {
                        storeLocally(t)
                        Toast.makeText(getApplication(), "Retrived from Server", Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onError(e: Throwable) {
                        error.value = true
                        loading.value = false
                        e.printStackTrace()
                    }

                })
        )
    }

    private fun dogsRetrieve(t: List<DogBreed>) {
        dogs.value = t
        error.value = false
        loading.value = false
    }

    private fun storeLocally(t: List<DogBreed>) {
        launch {

            val dao: DogDao = DogDatabase(getApplication()).dogDao()
            dao.deleteAllDogs()
            val insertResult = dao.insertAll(*t.toTypedArray())
            var i = 0
            while (i < t.size) {
                t[i].uuid = insertResult[i].toInt()
                i++
            }
            dogsRetrieve(t)
        }
        prefsHelper.updateTime(System.nanoTime())
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}