package com.example.fieldsmart

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UsuarioViewModel : ViewModel() {

    private val _nombre = MutableLiveData<String>()
    val nombre: LiveData<String> get() = _nombre

    private val _avatar = MutableLiveData<Bitmap?>()
    val avatar: LiveData<Bitmap?> get() = _avatar

    fun setNombre(nombre: String) {
        _nombre.value = nombre
    }

    fun setAvatar(bitmap: Bitmap?) {
        _avatar.value = bitmap
    }
}
